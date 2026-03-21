<template>
  <div class="adopter-layout">
    <el-container>
      <el-header class="header">
        <div class="logo">🐱 Hajimi 领养平台</div>
        <el-menu mode="horizontal" :default-active="route.path" router class="top-menu">
          <el-menu-item index="/adopter/cat-list">喵星人列表</el-menu-item>
          <el-menu-item index="/adopter/my-records">我的领养记录</el-menu-item>
          <el-menu-item index="/adopter/community">猫咪社区</el-menu-item>
        </el-menu>

        <div class="header-right">
          <!-- 通知铃铛 -->
          <div class="notification-bell" @click="router.push('/adopter/notifications')">
            <el-badge :value="unreadCount > 0 ? unreadCount : ''" :hidden="unreadCount === 0" type="danger">
              <el-icon :size="22" class="bell-icon"><Bell /></el-icon>
            </el-badge>
          </div>

          <!-- 用户头像 + 下拉菜单 -->
          <el-dropdown>
            <span class="dropdown-link">
              <el-avatar
                  :size="34"
                  :src="userInfo.avatar || defaultAvatar"
                  style="margin-right: 8px; border: 2px solid #ffeedb;" />
              {{ userInfo.username || '铲屎官' }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/adopter/profile')">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="el-fade-in-linear" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowDown, Bell, User, SwitchButton } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userInfo = ref({})
const unreadCount = ref(0)
let pollTimer = null

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    userInfo.value = JSON.parse(userStr)
  } else {
    router.push('/login')
    return
  }
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 60000)

  // 监听 localStorage 变化，头像/昵称更新后同步刷新
  window.addEventListener('storage', syncUserInfo)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
  window.removeEventListener('storage', syncUserInfo)
})

const syncUserInfo = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) userInfo.value = JSON.parse(userStr)
}

const fetchUnreadCount = async () => {
  if (!userInfo.value.id) return
  try {
    const res = await axios.get(`http://localhost:8080/api/notification/unread/${userInfo.value.id}`)
    if (res.data.code === 200) unreadCount.value = res.data.data
  } catch (e) { /* 静默 */ }
}

const handleLogout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.adopter-layout { min-height: 100vh; background-color: #f7f8fa; }
.header { display: flex; align-items: center; justify-content: space-between; background-color: #fff; box-shadow: 0 2px 12px rgba(0,0,0,0.05); padding: 0 40px; position: sticky; top: 0; z-index: 10; }
.logo { font-size: 24px; font-weight: bold; color: #ff9d4d; white-space: nowrap; }
.top-menu { flex: 1; margin: 0 30px; border-bottom: none; }
.header-right { display: flex; align-items: center; gap: 20px; flex-shrink: 0; }
.notification-bell { cursor: pointer; padding: 4px; border-radius: 8px; transition: background 0.2s; }
.notification-bell:hover { background-color: #f5f5f5; }
.bell-icon { color: #666; display: block; }
.dropdown-link { cursor: pointer; color: #555; font-weight: bold; display: flex; align-items: center; outline: none; }
.main-content { padding: 0; max-width: 1200px; margin: 0 auto; width: 100%; }
</style>