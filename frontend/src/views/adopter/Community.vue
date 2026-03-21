<template>
  <div style="padding: 40px 0;">

    <!-- 发帖区 -->
    <el-card class="post-editor-card" shadow="never">
      <div class="editor-inner">
        <!-- 当前用户头像 -->
        <el-avatar :size="40" :src="userInfo.avatar || defaultAvatar" class="self-avatar" />
        <div class="editor-right">
          <el-input
              type="textarea"
              v-model="newPostContent"
              :rows="3"
              placeholder="分享你和喵咪的故事..."
              resize="none"
              class="post-input" />
          <div class="editor-actions">
            <el-upload
                action="http://localhost:8080/api/file/upload"
                list-type="picture-card"
                v-model:file-list="postImageList"
                :on-success="handlePostImageSuccess"
                :before-upload="beforeUpload"
                :limit="6"
                class="post-upload">
              <el-icon><Picture /></el-icon>
            </el-upload>
            <el-button type="primary" color="#ff9d4d" round :loading="postLoading" @click="submitPost">发布</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 帖子列表 -->
    <div class="post-list">
      <el-card
          v-for="item in postList" :key="item.post.id"
          class="post-card" shadow="never"
          @click="openPostDetail(item)">
        <div class="post-header">
          <el-avatar :size="38" :src="item.user?.avatar || defaultAvatar" />
          <div class="post-meta">
            <span class="post-author">{{ item.user?.username || '匿名用户' }}</span>
            <span class="post-time">{{ formatTime(item.post.createTime) }}</span>
          </div>
        </div>

        <div class="post-content">{{ item.post.content }}</div>

        <div v-if="item.post.imageUrls" class="post-images" @click.stop>
          <el-image
              v-for="(img, idx) in item.post.imageUrls.split(',')" :key="idx"
              :src="img"
              :preview-src-list="item.post.imageUrls.split(',')"
              fit="cover" class="post-img" hide-on-click-modal />
        </div>

        <div class="post-footer" @click.stop>
          <span class="action-btn" @click="togglePostLike(item)">
            <el-icon :color="item.liked ? '#ff4d4f' : '#999'">
              <component :is="item.liked ? 'StarFilled' : 'Star'" />
            </el-icon>
            <span :style="{ color: item.liked ? '#ff4d4f' : '#999' }">{{ item.post.likeCount }}</span>
          </span>
          <span class="action-btn" @click="openPostDetail(item)">
            <el-icon color="#999"><ChatDotRound /></el-icon>
            <span style="color: #999;">{{ item.post.commentCount }}</span>
          </span>
        </div>
      </el-card>

      <el-empty v-if="postList.length === 0" description="还没有帖子，来发第一条吧~" />
    </div>

    <!-- 帖子详情抽屉（含评论） -->
    <el-drawer v-model="detailVisible" size="50%" :with-header="false" destroy-on-close>
      <div v-if="currentPost" class="detail-wrap">

        <!-- 帖子正文 -->
        <div class="detail-post">
          <div class="post-header">
            <el-avatar :size="40" :src="currentPost.user?.avatar || defaultAvatar" />
            <div class="post-meta">
              <span class="post-author">{{ currentPost.user?.username }}</span>
              <span class="post-time">{{ formatTime(currentPost.post.createTime) }}</span>
            </div>
          </div>
          <div class="post-content">{{ currentPost.post.content }}</div>
          <div v-if="currentPost.post.imageUrls" class="post-images">
            <el-image
                v-for="(img, idx) in currentPost.post.imageUrls.split(',')" :key="idx"
                :src="img" :preview-src-list="currentPost.post.imageUrls.split(',')"
                fit="cover" class="post-img" hide-on-click-modal />
          </div>
          <div class="post-footer">
            <span class="action-btn" @click="togglePostLike(currentPost)">
              <el-icon :color="currentPost.liked ? '#ff4d4f' : '#999'">
                <component :is="currentPost.liked ? 'StarFilled' : 'Star'" />
              </el-icon>
              <span :style="{ color: currentPost.liked ? '#ff4d4f' : '#999' }">{{ currentPost.post.likeCount }}</span>
            </span>
          </div>
        </div>

        <el-divider>评论 {{ currentPost.post.commentCount }}</el-divider>

        <!-- 评论区 -->
        <div class="comment-section">
          <!-- 评论输入框 -->
          <div class="comment-input-wrap">
            <div class="comment-input-row">
              <!-- 当前用户头像 -->
              <el-avatar :size="32" :src="userInfo.avatar || defaultAvatar" />
              <el-input
                  v-model="commentContent"
                  :placeholder="replyTarget ? `回复 @${replyTarget.username}：` : '说点什么吧...'"
                  @keyup.enter="submitComment"
                  class="comment-input">
                <template #suffix>
                  <el-button type="primary" color="#ff9d4d" size="small" @click="submitComment" :loading="commentLoading">发送</el-button>
                </template>
              </el-input>
            </div>
            <el-tag v-if="replyTarget" closable @close="cancelReply" style="margin-top: 6px; margin-left: 42px;" type="warning">
              回复 @{{ replyTarget.username }}
            </el-tag>
          </div>

          <!-- 评论列表 -->
          <div class="comment-list">
            <div v-for="rootItem in commentList" :key="rootItem.comment.id" class="comment-root">
              <!-- 一级评论 -->
              <div class="comment-item">
                <el-avatar :size="34" :src="rootItem.user?.avatar || defaultAvatar" />
                <div class="comment-body">
                  <span class="comment-author">{{ rootItem.user?.username }}</span>
                  <div class="comment-content">{{ rootItem.comment.content }}</div>
                  <div class="comment-actions">
                    <span class="comment-time">{{ formatTime(rootItem.comment.createTime) }}</span>
                    <span class="action-text" @click="toggleCommentLike(rootItem)">
                      <el-icon :size="13" :color="rootItem.liked ? '#ff4d4f' : '#bbb'">
                        <component :is="rootItem.liked ? 'StarFilled' : 'Star'" />
                      </el-icon>
                      {{ rootItem.comment.likeCount }}
                    </span>
                    <span class="action-text" @click="setReply(rootItem.comment, rootItem.user)">回复</span>
                  </div>

                  <!-- 子评论 -->
                  <div v-if="rootItem.children?.length > 0" class="children-wrap">
                    <div
                        v-for="childItem in getVisibleChildren(rootItem)"
                        :key="childItem.comment.id"
                        class="comment-item child-item">
                      <el-avatar :size="28" :src="childItem.user?.avatar || defaultAvatar" />
                      <div class="comment-body">
                        <span class="comment-author">{{ childItem.user?.username }}</span>
                        <span v-if="childItem.replyToUser" class="reply-to">
                          回复 <span class="reply-name">@{{ childItem.replyToUser.username }}</span>
                        </span>
                        <div class="comment-content">{{ childItem.comment.content }}</div>
                        <div class="comment-actions">
                          <span class="comment-time">{{ formatTime(childItem.comment.createTime) }}</span>
                          <span class="action-text" @click="toggleCommentLike(childItem)">
                            <el-icon :size="13" :color="childItem.liked ? '#ff4d4f' : '#bbb'">
                              <component :is="childItem.liked ? 'StarFilled' : 'Star'" />
                            </el-icon>
                            {{ childItem.comment.likeCount }}
                          </span>
                          <span class="action-text" @click="setReply(childItem.comment, childItem.user)">回复</span>
                        </div>
                      </div>
                    </div>

                    <!-- 展开/收起 -->
                    <div
                        v-if="rootItem.children.length > 3"
                        class="expand-btn"
                        @click="toggleExpand(rootItem.comment.id)">
                      {{ expandedRoots.has(rootItem.comment.id)
                        ? '收起'
                        : `展开剩余 ${rootItem.children.length - 3} 条回复` }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <el-empty v-if="commentList.length === 0" description="还没有评论，来抢沙发吧~" :image-size="60" />
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Picture, ChatDotRound, StarFilled, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userInfo = ref({})
const newPostContent = ref('')
const postImageList = ref([])
const postLoading = ref(false)
const postList = ref([])

const detailVisible = ref(false)
const currentPost = ref(null)
const commentList = ref([])
const commentContent = ref('')
const commentLoading = ref(false)
const replyTarget = ref(null)
const expandedRoots = ref(new Set())

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) userInfo.value = JSON.parse(userStr)
  fetchPosts()

  // 监听个人中心更新头像后同步
  window.addEventListener('storage', () => {
    const userStr = localStorage.getItem('user')
    if (userStr) userInfo.value = JSON.parse(userStr)
  })
})

const formatTime = (timeStr) => timeStr ? timeStr.replace('T', ' ').substring(0, 16) : ''

const beforeUpload = (file) => {
  if (file.size / 1024 / 1024 > 5) { ElMessage.error('图片不能超过 5MB'); return false }
  return true
}

// ===================== 发帖 =====================
const handlePostImageSuccess = (res, uploadFile, uploadFiles) => {
  if (res.code === 200) { uploadFile.url = res.data; postImageList.value = uploadFiles }
}

const submitPost = async () => {
  if (!newPostContent.value.trim()) { ElMessage.warning('说点什么吧~'); return }
  postLoading.value = true
  const imageUrls = postImageList.value.map(f => f.url || f.response?.data).filter(Boolean).join(',')
  try {
    const res = await axios.post('http://localhost:8080/api/community/post/add', {
      userId: userInfo.value.id,
      content: newPostContent.value,
      imageUrls: imageUrls || null
    })
    if (res.data.code === 200) {
      ElMessage.success('发布成功！')
      newPostContent.value = ''
      postImageList.value = []
      fetchPosts()
    }
  } catch (e) { ElMessage.error('发布失败') }
  finally { postLoading.value = false }
}

// ===================== 帖子列表 =====================
const fetchPosts = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/community/post/list', {
      params: { currentUserId: userInfo.value.id || 0 }
    })
    if (res.data.code === 200) postList.value = res.data.data
  } catch (e) { ElMessage.error('获取帖子失败') }
}

// ===================== 帖子点赞 =====================
const togglePostLike = async (item) => {
  try {
    const res = await axios.post(`http://localhost:8080/api/community/post/like/${item.post.id}`, null, {
      params: { userId: userInfo.value.id }
    })
    if (res.data.code === 200) {
      item.liked = res.data.data.liked
      item.post.likeCount = res.data.data.likeCount
    }
  } catch (e) { ElMessage.error('操作失败') }
}

// ===================== 帖子详情 + 评论 =====================
const openPostDetail = async (item) => {
  currentPost.value = item
  commentList.value = []
  expandedRoots.value = new Set()
  replyTarget.value = null
  commentContent.value = ''
  detailVisible.value = true
  await fetchComments(item.post.id)
}

const fetchComments = async (postId) => {
  try {
    const res = await axios.get(`http://localhost:8080/api/community/comment/list/${postId}`, {
      params: { currentUserId: userInfo.value.id || 0 }
    })
    if (res.data.code === 200) commentList.value = res.data.data
  } catch (e) { ElMessage.error('获取评论失败') }
}

// ===================== 发评论 =====================
const setReply = (comment, user) => {
  replyTarget.value = { comment, username: user?.username }
  commentContent.value = ''
}
const cancelReply = () => { replyTarget.value = null; commentContent.value = '' }

const submitComment = async () => {
  if (!commentContent.value.trim()) return
  commentLoading.value = true
  try {
    const payload = {
      postId: currentPost.value.post.id,
      userId: userInfo.value.id,
      content: commentContent.value,
      parentId: replyTarget.value?.comment?.id || null,
      replyToUserId: replyTarget.value?.comment?.userId || null,
    }
    const res = await axios.post('http://localhost:8080/api/community/comment/add', payload)
    if (res.data.code === 200) {
      commentContent.value = ''
      replyTarget.value = null
      currentPost.value.post.commentCount++
      await fetchComments(currentPost.value.post.id)
    }
  } catch (e) { ElMessage.error('评论失败') }
  finally { commentLoading.value = false }
}

// ===================== 评论点赞 =====================
const toggleCommentLike = async (item) => {
  try {
    const res = await axios.post(`http://localhost:8080/api/community/comment/like/${item.comment.id}`, null, {
      params: { userId: userInfo.value.id }
    })
    if (res.data.code === 200) {
      item.liked = res.data.data.liked
      item.comment.likeCount = res.data.data.likeCount
    }
  } catch (e) { ElMessage.error('操作失败') }
}

// ===================== 子评论展开/收起 =====================
const toggleExpand = (rootId) => {
  if (expandedRoots.value.has(rootId)) expandedRoots.value.delete(rootId)
  else expandedRoots.value.add(rootId)
  expandedRoots.value = new Set(expandedRoots.value)
}

const getVisibleChildren = (rootItem) => {
  if (expandedRoots.value.has(rootItem.comment.id)) return rootItem.children
  return rootItem.children.slice(0, 3)
}
</script>

<style scoped>
.post-editor-card { border-radius: 16px; margin-bottom: 20px; border: none; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.editor-inner { display: flex; gap: 14px; }
.self-avatar { flex-shrink: 0; border: 2px solid #ffeedb; }
.editor-right { flex: 1; }
.post-input :deep(.el-textarea__inner) { border: none; background: #f7f8fa; border-radius: 12px; padding: 12px; font-size: 15px; }
.editor-actions { display: flex; justify-content: space-between; align-items: flex-start; margin-top: 10px; }
.post-upload :deep(.el-upload--picture-card) { width: 60px; height: 60px; border-radius: 8px; }
.post-upload :deep(.el-upload-list--picture-card .el-upload-list__item) { width: 60px; height: 60px; border-radius: 8px; }

.post-list { display: flex; flex-direction: column; gap: 12px; }
.post-card { border-radius: 16px; border: none; box-shadow: 0 2px 12px rgba(0,0,0,0.04); cursor: pointer; transition: box-shadow 0.2s; }
.post-card:hover { box-shadow: 0 6px 20px rgba(0,0,0,0.1); }
.post-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.post-meta { display: flex; flex-direction: column; }
.post-author { font-weight: bold; color: #333; font-size: 14px; }
.post-time { font-size: 12px; color: #bbb; }
.post-content { font-size: 15px; color: #444; line-height: 1.7; margin-bottom: 12px; }
.post-images { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.post-img { width: 100px; height: 100px; border-radius: 10px; cursor: pointer; object-fit: cover; }
.post-footer { display: flex; gap: 20px; padding-top: 10px; border-top: 1px solid #f5f5f5; }
.action-btn { display: flex; align-items: center; gap: 5px; cursor: pointer; font-size: 14px; color: #999; user-select: none; }

.detail-wrap { padding: 20px; height: 100%; display: flex; flex-direction: column; overflow-y: auto; }
.detail-post { flex-shrink: 0; }

.comment-section { flex: 1; }
.comment-input-wrap { margin-bottom: 20px; }
.comment-input-row { display: flex; align-items: center; gap: 10px; }
.comment-input { flex: 1; }
.comment-input :deep(.el-input__wrapper) { border-radius: 20px; }

.comment-list { display: flex; flex-direction: column; gap: 16px; }
.comment-item { display: flex; gap: 10px; }
.child-item { margin-top: 10px; }
.comment-body { flex: 1; }
.comment-author { font-weight: bold; font-size: 13px; color: #333; margin-right: 6px; }
.reply-to { font-size: 13px; color: #999; }
.reply-name { color: #ff9d4d; }
.comment-content { font-size: 14px; color: #555; line-height: 1.6; margin: 4px 0 6px; }
.comment-actions { display: flex; align-items: center; gap: 14px; }
.comment-time { font-size: 12px; color: #bbb; }
.action-text { font-size: 12px; color: #bbb; cursor: pointer; display: flex; align-items: center; gap: 3px; }
.action-text:hover { color: #ff9d4d; }

.children-wrap { background: #f7f8fa; border-radius: 12px; padding: 12px; margin-top: 10px; display: flex; flex-direction: column; gap: 10px; }
.expand-btn { font-size: 13px; color: #ff9d4d; cursor: pointer; padding: 4px 0; }
.expand-btn:hover { text-decoration: underline; }
</style>