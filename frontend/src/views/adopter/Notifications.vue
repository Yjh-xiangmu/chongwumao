<template>
  <div style="padding: 40px 0;">
    <div class="page-header">
      <h2 class="section-title">🔔 消息通知</h2>
      <div class="header-actions">
        <el-button
            v-if="activeTab === 'system' && systemList.length > 0"
            size="small" plain round
            @click="markAllRead(0)">
          全部已读
        </el-button>
        <el-button
            v-if="activeTab === 'community' && communityList.length > 0"
            size="small" plain round
            @click="markAllRead(1)">
          全部已读
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="notification-tabs">
      <!-- 系统通知 -->
      <el-tab-pane name="system">
        <template #label>
          <span>系统通知</span>
          <el-badge
              v-if="systemUnread > 0"
              :value="systemUnread"
              type="danger"
              style="margin-left: 6px;" />
        </template>

        <el-empty v-if="systemList.length === 0" description="暂无系统通知" :image-size="80" />
        <div v-else class="notification-list">
          <div
              v-for="item in systemList" :key="item.id"
              class="notification-item"
              :class="{ unread: item.isRead === 0 }"
              @click="handleRead(item)">
            <div class="notif-icon">{{ getIcon(item.subType) }}</div>
            <div class="notif-body">
              <div class="notif-title">{{ item.title }}</div>
              <div class="notif-content">{{ item.content }}</div>
              <div class="notif-time">{{ formatTime(item.createTime) }}</div>
            </div>
            <div class="unread-dot" v-if="item.isRead === 0"></div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 社区通知 -->
      <el-tab-pane name="community">
        <template #label>
          <span>社区通知</span>
          <el-badge
              v-if="communityUnread > 0"
              :value="communityUnread"
              type="danger"
              style="margin-left: 6px;" />
        </template>

        <el-empty v-if="communityList.length === 0" description="暂无社区通知" :image-size="80" />
        <div v-else class="notification-list">
          <div
              v-for="item in communityList" :key="item.id"
              class="notification-item"
              :class="{ unread: item.isRead === 0 }"
              @click="handleRead(item)">
            <div class="notif-icon">{{ getIcon(item.subType) }}</div>
            <div class="notif-body">
              <div class="notif-title">{{ item.title }}</div>
              <div class="notif-content">{{ item.content }}</div>
              <div class="notif-time">{{ formatTime(item.createTime) }}</div>
            </div>
            <div class="unread-dot" v-if="item.isRead === 0"></div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const userInfo = ref({})
const activeTab = ref('system')
const systemList = ref([])
const communityList = ref([])

const systemUnread = computed(() => systemList.value.filter(n => n.isRead === 0).length)
const communityUnread = computed(() => communityList.value.filter(n => n.isRead === 0).length)

// 子类型对应图标
const iconMap = {
  APPLY_SUBMIT:    '📋',
  APPLY_APPROVED:  '✅',
  APPLY_REJECTED:  '❌',
  SIGN_SUCCESS:    '🎉',
  COMMENT_LIKE:    '❤️',
  COMMENT_REPLY:   '💬',
  POST_COMMENT:    '📝',
}
const getIcon = (subType) => iconMap[subType] || '🔔'

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) userInfo.value = JSON.parse(userStr)
  fetchNotifications()
})

const formatTime = (timeStr) => timeStr ? timeStr.replace('T', ' ').substring(0, 16) : ''

const fetchNotifications = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/api/notification/list/${userInfo.value.id}`)
    if (res.data.code === 200) {
      systemList.value = res.data.data.systemList
      communityList.value = res.data.data.communityList
    }
  } catch (error) { ElMessage.error('获取通知失败') }
}

// 点击单条：标记已读
const handleRead = async (item) => {
  if (item.isRead === 1) return
  try {
    await axios.post(`http://localhost:8080/api/notification/read/${item.id}`)
    item.isRead = 1
  } catch (e) { /* 静默 */ }
}

// 全部已读
const markAllRead = async (type) => {
  try {
    await axios.post(`http://localhost:8080/api/notification/readAll/${userInfo.value.id}?type=${type}`)
    if (type === 0) systemList.value.forEach(n => n.isRead = 1)
    else communityList.value.forEach(n => n.isRead = 1)
    ElMessage.success('已全部标记为已读')
  } catch (e) { ElMessage.error('操作失败') }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.section-title { color: #555; margin: 0; }
.notification-tabs { background: #fff; border-radius: 16px; padding: 0 20px 20px; box-shadow: 0 4px 16px rgba(0,0,0,0.05); }

.notification-list { display: flex; flex-direction: column; gap: 2px; }

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
}
.notification-item:hover { background-color: #f7f8fa; }
.notification-item.unread { background-color: #fff9f0; }

.notif-icon { font-size: 28px; flex-shrink: 0; width: 40px; text-align: center; }
.notif-body { flex: 1; }
.notif-title { font-weight: bold; color: #333; font-size: 15px; margin-bottom: 4px; }
.notif-content { color: #666; font-size: 13px; line-height: 1.6; }
.notif-time { color: #bbb; font-size: 12px; margin-top: 6px; }

.unread-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background-color: #ff4d4f;
  flex-shrink: 0;
  margin-top: 6px;
}
</style>