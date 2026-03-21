package com.hajimi.adoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hajimi.adoption.common.Result;
import com.hajimi.adoption.entity.*;
import com.hajimi.adoption.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired private PostService postService;
    @Autowired private CommentService commentService;
    @Autowired private PostLikeService postLikeService;
    @Autowired private CommentLikeService commentLikeService;
    @Autowired private SysUserService sysUserService;
    @Autowired private NotificationService notificationService;

    // ===================== 帖子相关 =====================

    // 发帖
    @PostMapping("/post/add")
    public Result<String> addPost(@RequestBody Post post) {
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        postService.save(post);
        return Result.success(null, "发布成功！");
    }

    // 帖子列表（带发帖人信息 + 当前用户是否已点赞）
    @GetMapping("/post/list")
    public Result<List<Map<String, Object>>> listPosts(
            @RequestParam(defaultValue = "0") Long currentUserId) {

        List<Post> posts = postService.list(
                new QueryWrapper<Post>().orderByDesc("create_time"));

        Set<Long> likedPostIds = getLikedPostIds(currentUserId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Post post : posts) {
            result.add(buildPostMap(post, likedPostIds));
        }
        return Result.success(result, "获取成功");
    }

    // 我的帖子列表（个人中心用）
    @GetMapping("/post/myList/{userId}")
    public Result<List<Map<String, Object>>> myPosts(@PathVariable Long userId) {
        List<Post> posts = postService.list(
                new QueryWrapper<Post>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time"));

        Set<Long> likedPostIds = getLikedPostIds(userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Post post : posts) {
            result.add(buildPostMap(post, likedPostIds));
        }
        return Result.success(result, "获取成功");
    }

    // 帖子点赞 / 取消点赞（toggle）
    @PostMapping("/post/like/{postId}")
    public Result<Map<String, Object>> togglePostLike(
            @PathVariable Long postId,
            @RequestParam Long userId) {

        QueryWrapper<PostLike> qw = new QueryWrapper<PostLike>()
                .eq("post_id", postId).eq("user_id", userId);
        PostLike exist = postLikeService.getOne(qw);

        boolean liked;
        if (exist != null) {
            postLikeService.remove(qw);
            postService.update(new UpdateWrapper<Post>()
                    .eq("id", postId).setSql("like_count = like_count - 1"));
            liked = false;
        } else {
            PostLike pl = new PostLike();
            pl.setPostId(postId);
            pl.setUserId(userId);
            pl.setCreateTime(LocalDateTime.now());
            postLikeService.save(pl);
            postService.update(new UpdateWrapper<Post>()
                    .eq("id", postId).setSql("like_count = like_count + 1"));
            liked = true;

            Post post = postService.getById(postId);
            if (post != null && !post.getUserId().equals(userId)) {
                SysUser liker = sysUserService.getById(userId);
                String likerName = liker != null ? liker.getUsername() : "有人";
                notificationService.send(
                        post.getUserId(), 1, "COMMENT_LIKE",
                        "你的帖子被点赞了 ❤️",
                        likerName + " 赞了你的帖子",
                        postId);
            }
        }

        Post updated = postService.getById(postId);
        Map<String, Object> data = new HashMap<>();
        data.put("liked", liked);
        data.put("likeCount", updated != null ? updated.getLikeCount() : 0);
        return Result.success(data, liked ? "点赞成功" : "已取消点赞");
    }

    // ===================== 评论相关 =====================

    // 发评论
    @PostMapping("/comment/add")
    public Result<Map<String, Object>> addComment(@RequestBody Comment comment) {
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());

        if (comment.getParentId() != null) {
            Comment parent = commentService.getById(comment.getParentId());
            if (parent != null) {
                comment.setRootId(parent.getRootId() != null ? parent.getRootId() : parent.getId());
            }
        }

        commentService.save(comment);

        postService.update(new UpdateWrapper<Post>()
                .eq("id", comment.getPostId()).setSql("comment_count = comment_count + 1"));

        if (comment.getReplyToUserId() != null && !comment.getReplyToUserId().equals(comment.getUserId())) {
            SysUser replier = sysUserService.getById(comment.getUserId());
            String name = replier != null ? replier.getUsername() : "有人";
            notificationService.send(
                    comment.getReplyToUserId(), 1, "COMMENT_REPLY",
                    "有人回复了你 💬",
                    name + " 回复了你：" + comment.getContent(),
                    comment.getPostId());
        }

        if (comment.getParentId() == null) {
            Post post = postService.getById(comment.getPostId());
            if (post != null && !post.getUserId().equals(comment.getUserId())) {
                SysUser commenter = sysUserService.getById(comment.getUserId());
                String name = commenter != null ? commenter.getUsername() : "有人";
                notificationService.send(
                        post.getUserId(), 1, "POST_COMMENT",
                        "有人评论了你的帖子 📝",
                        name + " 评论道：" + comment.getContent(),
                        comment.getPostId());
            }
        }

        SysUser user = sysUserService.getById(comment.getUserId());
        if (user != null) user.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("comment", comment);
        data.put("user", user);
        return Result.success(data, "评论成功");
    }

    // 获取帖子评论列表（结构化）
    @GetMapping("/comment/list/{postId}")
    public Result<List<Map<String, Object>>> listComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") Long currentUserId) {

        List<Comment> roots = commentService.list(
                new QueryWrapper<Comment>()
                        .eq("post_id", postId)
                        .isNull("parent_id")
                        .orderByAsc("create_time"));

        Set<Long> likedCommentIds = new HashSet<>();
        if (currentUserId > 0) {
            commentLikeService.list(new QueryWrapper<CommentLike>().eq("user_id", currentUserId))
                    .forEach(l -> likedCommentIds.add(l.getCommentId()));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment root : roots) {
            Map<String, Object> rootMap = buildCommentMap(root, likedCommentIds);

            List<Comment> children = commentService.list(
                    new QueryWrapper<Comment>()
                            .eq("root_id", root.getId())
                            .orderByAsc("create_time"));

            List<Map<String, Object>> childrenMaps = new ArrayList<>();
            for (Comment child : children) {
                Map<String, Object> childMap = buildCommentMap(child, likedCommentIds);
                if (child.getReplyToUserId() != null) {
                    SysUser replyTo = sysUserService.getById(child.getReplyToUserId());
                    if (replyTo != null) replyTo.setPassword(null);
                    childMap.put("replyToUser", replyTo);
                }
                childrenMaps.add(childMap);
            }
            rootMap.put("children", childrenMaps);
            result.add(rootMap);
        }

        return Result.success(result, "获取成功");
    }

    // 评论点赞 / 取消点赞
    @PostMapping("/comment/like/{commentId}")
    public Result<Map<String, Object>> toggleCommentLike(
            @PathVariable Long commentId,
            @RequestParam Long userId) {

        QueryWrapper<CommentLike> qw = new QueryWrapper<CommentLike>()
                .eq("comment_id", commentId).eq("user_id", userId);
        CommentLike exist = commentLikeService.getOne(qw);

        boolean liked;
        if (exist != null) {
            commentLikeService.remove(qw);
            commentService.update(new UpdateWrapper<Comment>()
                    .eq("id", commentId).setSql("like_count = like_count - 1"));
            liked = false;
        } else {
            CommentLike cl = new CommentLike();
            cl.setCommentId(commentId);
            cl.setUserId(userId);
            cl.setCreateTime(LocalDateTime.now());
            commentLikeService.save(cl);
            commentService.update(new UpdateWrapper<Comment>()
                    .eq("id", commentId).setSql("like_count = like_count + 1"));
            liked = true;
        }

        Comment updated = commentService.getById(commentId);
        Map<String, Object> data = new HashMap<>();
        data.put("liked", liked);
        data.put("likeCount", updated != null ? updated.getLikeCount() : 0);
        return Result.success(data, liked ? "点赞成功" : "已取消");
    }

    // ===================== 工具方法 =====================

    private Set<Long> getLikedPostIds(Long userId) {
        Set<Long> ids = new HashSet<>();
        if (userId > 0) {
            postLikeService.list(new QueryWrapper<PostLike>().eq("user_id", userId))
                    .forEach(l -> ids.add(l.getPostId()));
        }
        return ids;
    }

    private Map<String, Object> buildPostMap(Post post, Set<Long> likedIds) {
        Map<String, Object> map = new HashMap<>();
        map.put("post", post);
        SysUser user = sysUserService.getById(post.getUserId());
        if (user != null) user.setPassword(null);
        map.put("user", user);
        map.put("liked", likedIds.contains(post.getId()));
        return map;
    }

    private Map<String, Object> buildCommentMap(Comment comment, Set<Long> likedIds) {
        Map<String, Object> map = new HashMap<>();
        map.put("comment", comment);
        SysUser user = sysUserService.getById(comment.getUserId());
        if (user != null) user.setPassword(null);
        map.put("user", user);
        map.put("liked", likedIds.contains(comment.getId()));
        return map;
    }
}