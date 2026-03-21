<template>
  <div style="padding: 40px 0;">

    <!-- 顶部个人信息卡 -->
    <el-card class="profile-card" shadow="never">
      <div class="profile-header">
        <!-- 头像上传 -->
        <div class="avatar-wrap">
          <el-upload
              action="http://localhost:8080/api/file/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              class="avatar-uploader">
            <el-avatar :size="90" :src="userInfo.avatar || defaultAvatar" class="avatar-img" />
            <div class="avatar-mask">
              <el-icon color="#fff"><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </el-upload>
        </div>

        <div class="profile-info">
          <div class="profile-name">
            {{ userInfo.username }}
            <span v-if="userInfo.realName" class="real-name-badge">{{ userInfo.realName }}</span>
          </div>
          <div class="profile-meta">
            <el-tag size="small" :type="roleTagType" effect="light" round>{{ roleLabel }}</el-tag>
            <span class="profile-phone">{{ userInfo.phone }}</span>
          </div>
          <div v-if="userInfo.address" class="profile-address">
            📍 {{ userInfo.address }}
          </div>
          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-num">{{ myPostList.length }}</span>
              <span class="stat-label">帖子</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">{{ favoriteList.length }}</span>
              <span class="stat-label">收藏</span>
            </div>
          </div>
        </div>

        <el-button class="edit-btn" round @click="openEditDialog">编辑资料</el-button>
      </div>
    </el-card>

    <!-- 内容 Tab -->
    <el-card class="content-card" shadow="never">
      <el-tabs v-model="activeTab">

        <!-- 我的收藏 -->
        <el-tab-pane name="favorites">
          <template #label><span>⭐ 我的收藏</span></template>
          <el-empty v-if="favoriteList.length === 0" description="还没有收藏任何猫咪哦~" :image-size="80" />
          <el-row v-else :gutter="20">
            <el-col :span="6" v-for="item in favoriteList" :key="item.favorite.id" style="margin-bottom: 20px;">
              <el-card class="fav-card" :body-style="{ padding: '0' }" shadow="hover">
                <div class="fav-img-wrap">
                  <img :src="item.cat?.coverImage || defaultAvatar" class="fav-img" />
                  <div class="fav-status-badge"
                       :class="item.cat?.status === 0 ? 'status-free' : (item.cat?.status === 1 ? 'status-taken' : 'status-done')">
                    {{ item.cat?.status === 0 ? '待领养' : (item.cat?.status === 1 ? '已预定' : '已领养') }}
                  </div>
                </div>
                <div class="fav-info">
                  <div class="fav-name">{{ item.cat?.nickname }}</div>
                  <div class="fav-tags">
                    <el-tag size="small" type="warning" effect="light" round>{{ item.cat?.breed || '田园猫' }}</el-tag>
                    <el-tag size="small" type="success" effect="light" round>{{ item.cat?.age || '未知' }}</el-tag>
                  </div>
                  <el-button size="small" type="danger" plain round
                             style="width: 100%; margin-top: 10px;"
                             @click="removeFavorite(item.cat?.id)">
                    取消收藏
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 我的帖子 -->
        <el-tab-pane name="posts">
          <template #label><span>📝 我的帖子</span></template>
          <el-empty v-if="myPostList.length === 0" description="还没有发布过帖子哦~" :image-size="80" />
          <div v-else class="my-post-list">
            <el-card v-for="item in myPostList" :key="item.post.id"
                     class="my-post-card" shadow="never"
                     @click="goToCommunity(item.post.id)">
              <div class="post-content-text">{{ item.post.content }}</div>
              <div v-if="item.post.imageUrls" class="post-thumbs">
                <img v-for="(img, idx) in item.post.imageUrls.split(',').slice(0, 3)" :key="idx"
                     :src="img" class="post-thumb" />
              </div>
              <div class="post-meta-row">
                <span class="meta-item">❤️ {{ item.post.likeCount }}</span>
                <span class="meta-item">💬 {{ item.post.commentCount }}</span>
                <span class="meta-time">{{ formatTime(item.post.createTime) }}</span>
              </div>
            </el-card>
          </div>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane name="security">
          <template #label><span>🔒 账号安全</span></template>
          <div class="security-wrap">
            <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="90px" style="max-width: 400px;">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" color="#ff9d4d" round @click="changePassword" :loading="pwdLoading">
                  确认修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

      </el-tabs>
    </el-card>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="editDialogVisible" title="✏️ 编辑个人资料" width="440px" destroy-on-close>
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="昵称" prop="username">
          <el-input v-model="editForm.username" placeholder="你的昵称" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="editForm.realName" placeholder="用于签约时身份核验" />
          <div class="form-tip">签署领养协议时会核对真实姓名，请如实填写</div>
        </el-form-item>
        <el-form-item label="居住地址">
          <el-input v-model="editForm.address" placeholder="如: 上海市浦东新区" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false" round>取消</el-button>
        <el-button type="primary" color="#ff9d4d" @click="submitEdit" :loading="editLoading" round>保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Camera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userInfo = ref({})
const activeTab = ref('favorites')
const favoriteList = ref([])
const myPostList = ref([])

const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)
const editForm = ref({ username: '', realName: '', address: '' })
const editRules = {
  username: [{ required: true, message: '昵称不能为空', trigger: 'blur' }]
}

const pwdFormRef = ref(null)
const pwdLoading = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.value.newPassword) callback(new Error('两次密码不一致'))
  else callback()
}
const pwdRules = {
  oldPassword:     [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword:     [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }]
}

const roleLabel = computed(() => ({ 1: '领养人', 2: '救助员', 3: '管理员' }[userInfo.value.role] || '用户'))
const roleTagType = computed(() => ({ 1: 'success', 2: 'warning', 3: 'danger' }[userInfo.value.role] || 'info'))

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) userInfo.value = JSON.parse(userStr)
  fetchFavorites()
  fetchMyPosts()
})

const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''

// ---- 头像上传 ----
const beforeAvatarUpload = (file) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片文件'); return false }
  if (file.size / 1024 / 1024 > 5) { ElMessage.error('图片不能超过 5MB'); return false }
  return true
}
const handleAvatarSuccess = async (res) => {
  if (res.code !== 200) { ElMessage.error('上传失败'); return }
  try {
    const r = await axios.put('http://localhost:8080/api/user/update', {
      id: userInfo.value.id,
      username: userInfo.value.username,
      realName: userInfo.value.realName,
      address: userInfo.value.address,
      avatar: res.data
    })
    if (r.data.code === 200) {
      userInfo.value = r.data.data
      localStorage.setItem('user', JSON.stringify(userInfo.value))
      ElMessage.success('头像更新成功 🎉')
    }
  } catch (e) { ElMessage.error('头像保存失败') }
}

// ---- 编辑资料 ----
const openEditDialog = () => {
  editForm.value = {
    username: userInfo.value.username,
    realName: userInfo.value.realName || '',
    address:  userInfo.value.address  || ''
  }
  editDialogVisible.value = true
}
const submitEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    editLoading.value = true
    try {
      const res = await axios.put('http://localhost:8080/api/user/update', {
        id:       userInfo.value.id,
        username: editForm.value.username,
        realName: editForm.value.realName,
        address:  editForm.value.address,
        avatar:   userInfo.value.avatar
      })
      if (res.data.code === 200) {
        userInfo.value = res.data.data
        localStorage.setItem('user', JSON.stringify(userInfo.value))
        ElMessage.success('资料已更新')
        editDialogVisible.value = false
      } else { ElMessage.error(res.data.message) }
    } catch (e) { ElMessage.error('更新失败') }
    finally { editLoading.value = false }
  })
}

// ---- 修改密码 ----
const changePassword = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    pwdLoading.value = true
    try {
      const res = await axios.post('http://localhost:8080/api/user/changePassword', {
        userId:      userInfo.value.id,
        oldPassword: pwdForm.value.oldPassword,
        newPassword: pwdForm.value.newPassword
      })
      if (res.data.code === 200) {
        ElMessage.success('密码修改成功，请重新登录')
        localStorage.removeItem('user')
        router.push('/login')
      } else { ElMessage.error(res.data.message) }
    } catch (e) { ElMessage.error('修改失败') }
    finally { pwdLoading.value = false }
  })
}

// ---- 收藏 ----
const fetchFavorites = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/api/favorite/list/${userInfo.value.id}`)
    if (res.data.code === 200) favoriteList.value = res.data.data
  } catch (e) { ElMessage.error('获取收藏失败') }
}
const removeFavorite = async (catId) => {
  try {
    const res = await axios.post('http://localhost:8080/api/favorite/toggle', null, {
      params: { userId: userInfo.value.id, catId }
    })
    if (res.data.code === 200) { ElMessage.success('已取消收藏'); fetchFavorites() }
  } catch (e) { ElMessage.error('操作失败') }
}

// ---- 我的帖子 ----
const fetchMyPosts = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/api/community/post/myList/${userInfo.value.id}`)
    if (res.data.code === 200) myPostList.value = res.data.data
  } catch (e) { ElMessage.error('获取帖子失败') }
}
const goToCommunity = (postId) => {
  router.push({ name: 'Community', query: { postId } })
}
</script>

<style scoped>
.profile-card { border-radius: 20px; border: none; box-shadow: 0 4px 20px rgba(0,0,0,0.06); margin-bottom: 20px; }
.profile-header { display: flex; align-items: center; gap: 28px; }
.avatar-wrap { position: relative; flex-shrink: 0; }
.avatar-uploader { display: block; }
.avatar-img { display: block; border: 3px solid #ffeedb; }
.avatar-mask { position: absolute; inset: 0; border-radius: 50%; background: rgba(0,0,0,0.45); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px; color: #fff; font-size: 12px; opacity: 0; transition: opacity 0.2s; cursor: pointer; }
.avatar-wrap:hover .avatar-mask { opacity: 1; }

.profile-info { flex: 1; }
.profile-name { font-size: 22px; font-weight: bold; color: #333; margin-bottom: 8px; display: flex; align-items: center; gap: 10px; }
.real-name-badge { font-size: 13px; font-weight: normal; color: #888; background: #f5f5f5; padding: 2px 10px; border-radius: 20px; }
.profile-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.profile-phone { font-size: 13px; color: #999; }
.profile-address { font-size: 13px; color: #aaa; margin-bottom: 12px; }
.profile-stats { display: flex; gap: 30px; }
.stat-item { display: flex; flex-direction: column; align-items: center; }
.stat-num { font-size: 20px; font-weight: bold; color: #ff9d4d; }
.stat-label { font-size: 12px; color: #999; }
.edit-btn { align-self: flex-start; }

.content-card { border-radius: 20px; border: none; box-shadow: 0 4px 20px rgba(0,0,0,0.06); }

.fav-card { border-radius: 14px; border: none; overflow: hidden; transition: transform 0.2s; }
.fav-card:hover { transform: translateY(-4px); }
.fav-img-wrap { position: relative; height: 160px; overflow: hidden; }
.fav-img { width: 100%; height: 100%; object-fit: cover; }
.fav-status-badge { position: absolute; top: 8px; right: 8px; padding: 3px 10px; border-radius: 12px; font-size: 11px; font-weight: bold; color: #fff; backdrop-filter: blur(4px); }
.status-free { background: rgba(103,194,58,0.9); }
.status-taken { background: rgba(230,162,60,0.9); }
.status-done { background: rgba(245,108,108,0.9); }
.fav-info { padding: 14px; }
.fav-name { font-size: 16px; font-weight: bold; color: #333; margin-bottom: 8px; }
.fav-tags { display: flex; gap: 6px; }

.my-post-list { display: flex; flex-direction: column; gap: 12px; }
.my-post-card { border-radius: 14px; border: 1px solid #f0f0f0; cursor: pointer; transition: box-shadow 0.2s; }
.my-post-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
.post-content-text { font-size: 15px; color: #444; line-height: 1.7; margin-bottom: 10px; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.post-thumbs { display: flex; gap: 8px; margin-bottom: 10px; }
.post-thumb { width: 80px; height: 80px; border-radius: 8px; object-fit: cover; }
.post-meta-row { display: flex; align-items: center; gap: 16px; padding-top: 8px; border-top: 1px solid #f5f5f5; }
.meta-item { font-size: 13px; color: #999; }
.meta-time { font-size: 12px; color: #ccc; margin-left: auto; }

.security-wrap { padding: 20px 0; }
.form-tip { font-size: 12px; color: #bbb; margin-top: 4px; }
</style>