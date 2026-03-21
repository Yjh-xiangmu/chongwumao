<template>
  <div>
    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <span class="table-title">领养人名册</span>
        <el-input
            v-model="searchKeyword"
            placeholder="搜索姓名 / 手机号"
            prefix-icon="Search"
            clearable
            style="width: 240px;"
            @input="handleSearch" />
      </div>

      <el-table :data="filteredList" v-loading="loading" style="width: 100%" stripe>
        <el-table-column prop="id" label="ID" width="70" />

        <el-table-column label="用户信息" width="200">
          <template #default="scope">
            <div class="user-cell">
              <el-avatar
                  :size="36"
                  :src="scope.row.avatar || defaultAvatar" />
              <div>
                <div class="user-name">{{ scope.row.username }}</div>
                <div class="user-phone">{{ scope.row.phone }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="真实姓名" width="120">
          <template #default="scope">
            {{ scope.row.realName || '—' }}
          </template>
        </el-table-column>

        <el-table-column label="身份证号" width="190">
          <template #default="scope">
            <span class="id-card-text">{{ maskIdCard(scope.row.idCard) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="居住地址" min-width="160">
          <template #default="scope">
            {{ scope.row.address || '—' }}
          </template>
        </el-table-column>

        <el-table-column label="注册时间" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="openDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="filteredList.length === 0 && !loading" description="暂无领养人数据" />
    </el-card>

    <!-- 领养人详情抽屉 -->
    <el-drawer v-model="detailVisible" title="👤 领养人详情" size="38%" destroy-on-close>
      <div v-if="currentUser" class="detail-content">

        <!-- 基本信息 -->
        <div class="detail-profile">
          <el-avatar :size="70" :src="currentUser.avatar || defaultAvatar" class="detail-avatar" />
          <div>
            <div class="detail-name">{{ currentUser.username }}</div>
            <div class="detail-phone">{{ currentUser.phone }}</div>
          </div>
        </div>

        <el-descriptions :column="1" border size="small" style="margin-top: 20px;">
          <el-descriptions-item label="真实姓名">{{ currentUser.realName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentUser.idCard || '—' }}</el-descriptions-item>
          <el-descriptions-item label="居住地址">{{ currentUser.address || '—' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatTime(currentUser.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>领养记录</el-divider>

        <!-- 该用户的领养申请记录 -->
        <div v-loading="recordLoading">
          <el-empty v-if="userRecords.length === 0" description="暂无领养记录" :image-size="60" />
          <div v-else class="record-list">
            <div v-for="item in userRecords" :key="item.application.id" class="record-item">
              <el-avatar shape="square" :size="44" :src="item.cat?.coverImage" />
              <div class="record-info">
                <div class="record-cat-name">{{ item.cat?.nickname }}</div>
                <div class="record-time">申请于 {{ formatTime(item.application.applyTime) }}</div>
              </div>
              <el-tag
                  :type="statusTagType(item.application.status)"
                  size="small" round effect="dark">
                {{ statusLabel(item.application.status) }}
              </el-tag>
            </div>
          </div>
        </div>

      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const loading = ref(false)
const adopterList = ref([])
const searchKeyword = ref('')

const detailVisible = ref(false)
const currentUser = ref(null)
const userRecords = ref([])
const recordLoading = ref(false)

const filteredList = computed(() => {
  if (!searchKeyword.value) return adopterList.value
  const kw = searchKeyword.value.toLowerCase()
  return adopterList.value.filter(u =>
      u.username?.toLowerCase().includes(kw) || u.phone?.includes(kw)
  )
})

onMounted(() => fetchAdopters())

const formatTime = (timeStr) => timeStr ? timeStr.replace('T', ' ').substring(0, 16) : '—'

const maskIdCard = (id) => {
  if (!id || id.length < 8) return id || '—'
  return id.substring(0, 4) + '**********' + id.substring(id.length - 4)
}

const statusLabel = (s) => ['待审核','待签约','已驳回','已完成','已取消'][s] || '未知'
const statusTagType = (s) => ['info','warning','danger','success','info'][s] || 'info'

const handleSearch = () => { /* 由 computed 自动过滤 */ }

const fetchAdopters = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/user/adopters')
    if (res.data.code === 200) adopterList.value = res.data.data
  } catch (e) { ElMessage.error('获取领养人列表失败') }
  finally { loading.value = false }
}

const openDetail = async (user) => {
  currentUser.value = user
  userRecords.value = []
  detailVisible.value = true
  recordLoading.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/adopt/myList/${user.id}`)
    if (res.data.code === 200) userRecords.value = res.data.data
  } catch (e) { ElMessage.error('获取记录失败') }
  finally { recordLoading.value = false }
}
</script>

<style scoped>
.table-card { border-radius: 12px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.table-title { font-size: 18px; font-weight: bold; color: #333; }

.user-cell { display: flex; align-items: center; gap: 10px; }
.user-name { font-weight: bold; color: #333; font-size: 14px; }
.user-phone { font-size: 12px; color: #999; }
.id-card-text { font-family: monospace; color: #666; font-size: 13px; }

/* 抽屉 */
.detail-content { padding: 0 10px; }
.detail-profile { display: flex; align-items: center; gap: 16px; padding: 10px 0 20px; }
.detail-avatar { border: 3px solid #ffeedb; }
.detail-name { font-size: 20px; font-weight: bold; color: #333; }
.detail-phone { font-size: 13px; color: #999; margin-top: 4px; }

.record-list { display: flex; flex-direction: column; gap: 12px; }
.record-item { display: flex; align-items: center; gap: 12px; padding: 10px; background: #f7f8fa; border-radius: 10px; }
.record-info { flex: 1; }
.record-cat-name { font-weight: bold; color: #a45a1e; font-size: 14px; }
.record-time { font-size: 12px; color: #bbb; margin-top: 2px; }
</style>