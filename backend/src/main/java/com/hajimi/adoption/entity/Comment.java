package com.hajimi.adoption.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long userId;
    // 父评论ID，null = 一级评论
    private Long parentId;
    // 根评论ID（方便一次拉出整棵子树）
    private Long rootId;
    // 被回复的目标用户ID
    private Long replyToUserId;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;
}