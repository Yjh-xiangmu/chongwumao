<template>
  <div>
    <el-row :gutter="20">
      <!-- 左：基本信息设置 -->
      <el-col :span="14">
        <el-card shadow="never" class="settings-card">
          <template #header>
            <span class="card-title">🏠 平台基本信息</span>
          </template>

          <el-form :model="siteForm" :rules="siteRules" ref="siteFormRef" label-width="100px">
            <el-form-item label="平台名称" prop="siteName">
              <el-input v-model="siteForm.siteName" placeholder="如: Hajimi 领养平台" />
            </el-form-item>
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="siteForm.contactPhone" placeholder="对外展示的联系电话" />
            </el-form-item>
            <el-form-item label="联系邮箱">
              <el-input v-model="siteForm.contactEmail" placeholder="如: contact@hajimi.com" />
            </el-form-item>
            <el-form-item label="平台地址">
              <el-input v-model="siteForm.address" placeholder="平台所在城市/地址" />
            </el-form-item>
            <el-form-item label="平台简介">
              <el-input
                  type="textarea" :rows="3"
                  v-model="siteForm.description"
                  placeholder="一句话介绍平台..." />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" color="#ff9d4d" round @click="saveSiteSettings" :loading="saving">
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右：运营数据概览 + 快速操作 -->
      <el-col :span="10">
        <el-card shadow="never" class="settings-card" style="margin-bottom: 16px;">
          <template #header>
            <span class="card-title">⚡ 快速操作</span>
          </template>
          <div class="quick-actions">
            <el-button class="quick-btn" @click="$router.push('/admin/volunteer-manage')">
              <span class="quick-icon">👥</span>管理救助员
            </el-button>
            <el-button class="quick-btn" @click="$router.push('/admin/adopter-manage')">
              <span class="quick-icon">👤</span>查看领养人
            </el-button>
            <el-button class="quick-btn" @click="$router.push('/admin/dashboard')">
              <span class="quick-icon">📊</span>查看数据大盘
            </el-button>
          </div>
        </el-card>

        <el-card shadow="never" class="settings-card">
          <template #header>
            <span class="card-title">🔧 系统信息</span>
          </template>
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="前端框架">Vue 3 + Element Plus</el-descriptions-item>
            <el-descriptions-item label="后端框架">Spring Boot 3.2</el-descriptions-item>
            <el-descriptions-item label="数据库">MySQL 8.0</el-descriptions-item>
            <el-descriptions-item label="当前时间">{{ currentTime }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

const saving = ref(false)
const currentTime = ref('')
let timer = null

// 平台设置存在 localStorage 里（演示用，实际项目可做成后端接口）
const siteFormRef = ref(null)
const siteForm = ref({
  siteName:     'Hajimi 领养平台',
  contactPhone: '',
  contactEmail: '',
  address:      '',
  description:  '',
})
const siteRules = {
  siteName: [{ required: true, message: '平台名称不能为空', trigger: 'blur' }],
}

onMounted(() => {
  // 读取已保存的配置
  const saved = localStorage.getItem('siteSettings')
  if (saved) siteForm.value = JSON.parse(saved)

  // 实时时间
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => { if (timer) clearInterval(timer) })

const updateTime = () => {
  currentTime.value = new Date().toLocaleString('zh-CN')
}

const saveSiteSettings = async () => {
  if (!siteFormRef.value) return
  await siteFormRef.value.validate((valid) => {
    if (valid) {
      saving.value = true
      // 模拟保存延迟
      setTimeout(() => {
        localStorage.setItem('siteSettings', JSON.stringify(siteForm.value))
        ElMessage.success('设置已保存')
        saving.value = false
      }, 600)
    }
  })
}
</script>

<style scoped>
.settings-card { border-radius: 14px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.card-title { font-size: 15px; font-weight: bold; color: #444; }

.quick-actions { display: flex; flex-direction: column; gap: 10px; }
.quick-btn {
  width: 100%; justify-content: flex-start; height: 46px;
  border-radius: 10px; font-size: 14px; color: #555;
  border: 1px solid #f0f0f0; background: #f7f8fa;
  display: flex; align-items: center; gap: 10px;
}
.quick-btn:hover { background: #fff9e6; border-color: #ffb880; color: #a45a1e; }
.quick-icon { font-size: 18px; }
</style>