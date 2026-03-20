<template>
  <div class="backend-layout">
    <el-container class="layout-container">
      <el-aside width="220px" class="aside">
        <div class="logo">🐱 救助员工作台</div>
        <el-menu default-active="1" class="side-menu" background-color="#34495e" text-color="#ecf0f1" active-text-color="#ffb880">
          <el-menu-item index="1"><el-icon><Notebook /></el-icon><span>猫咪档案管理</span></el-menu-item>
          <el-menu-item index="2"><el-icon><DocumentChecked /></el-icon><span>领养申请审核</span></el-menu-item>
          <el-menu-item index="3"><el-icon><Calendar /></el-icon><span>回访记录管理</span></el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="header-breadcrumb">猫咪档案库</div>
          <el-dropdown>
            <span class="dropdown-link">
              {{ userInfo.username || '救助员' }} <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-header>

        <el-main class="main-content">
          <div class="toolbar">
            <h2 class="page-title">喵星人常驻花名册 🐾</h2>
            <el-button type="primary" color="#ffb880" icon="Plus" round size="large" @click="openAddDialog">
              录入新主子
            </el-button>
          </div>

          <el-row :gutter="20">
            <el-col :span="6" v-for="cat in catList" :key="cat.id" style="margin-bottom: 20px;">
              <el-card class="cat-card" :body-style="{ padding: '0px' }" shadow="hover">
                <div class="cat-image-wrapper">
                  <img :src="cat.coverImage || 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'" class="cat-image" />
                  <div class="status-badge" :class="cat.status === 0 ? 'status-free' : 'status-taken'">
                    {{ cat.status === 0 ? '待领养' : '已被预定' }}
                  </div>
                </div>
                <div class="cat-info">
                  <div class="cat-header">
                    <span class="cat-name">{{ cat.nickname }}</span>
                    <el-icon :color="cat.gender === 1 ? '#409EFF' : '#F56C6C'"><component :is="cat.gender === 1 ? 'Male' : 'Female'" /></el-icon>
                  </div>
                  <div class="cat-tags">
                    <el-tag size="small" type="warning" effect="light" round>{{ cat.breed || '田园小猫' }}</el-tag>
                    <el-tag size="small" type="success" effect="light" round>{{ cat.age || '未知' }}</el-tag>
                  </div>
                  <p class="cat-desc">{{ cat.description || '这只小猫咪很神秘，还没有介绍哦~' }}</p>

                  <div class="card-actions">
                    <el-button type="primary" plain size="small" icon="EditPen" class="action-btn" @click="openRecordDialog(cat)">记录动态</el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-empty v-if="catList.length === 0" description="档案库空空如也，快去救助小可爱吧~" />
        </el-main>
      </el-container>
    </el-container>

    <el-dialog v-model="dialogVisible" title="🐾 录入新主子档案" width="600px" class="cute-dialog" destroy-on-close>
      <el-form :model="catForm" :rules="rules" ref="catFormRef" label-width="90px">
        <el-form-item label="喵咪相册" prop="fileList">
          <el-upload
              class="upload-demo"
              action="http://localhost:8080/api/file/upload"
              list-type="picture-card"
              v-model:file-list="fileList"
              :on-success="handleUploadSuccess"
              :on-remove="handleRemove"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              multiple
              :limit="5"
              :on-exceed="handleExceed">
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多支持上传 5 张照片，第一张将作为封面图哦！</div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="喵咪昵称" prop="nickname">
              <el-input v-model="catForm.nickname" placeholder="如: 橘子" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="档案/芯片" prop="earTag">
              <el-input v-model="catForm.earTag" placeholder="内部编号(选填)" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品种" prop="breed">
              <el-input v-model="catForm.breed" placeholder="如: 中华田园橘猫" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input v-model="catForm.age" placeholder="如: 3个月" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="catForm.gender">
                <el-radio :value="1">小王子</el-radio>
                <el-radio :value="2">小公主</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="健康状况" prop="healthStatus">
              <el-input v-model="catForm.healthStatus" placeholder="如: 已驱虫/已绝育" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="性格描述" prop="description">
          <el-input type="textarea" :rows="3" v-model="catForm.description" placeholder="描述一下它的性格特点，比如：超级粘人，是个踩奶怪..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" round>取 消</el-button>
          <el-button type="primary" color="#ffb880" @click="submitAddCat" :loading="submitLoading" round>确认建档</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="recordDialogVisible" :title="'✏️ 记录 ' + currentRecordCat?.nickname + ' 的日常动态'" width="500px" class="cute-dialog" destroy-on-close>
      <el-form :model="recordForm" :rules="recordRules" ref="recordFormRef" label-width="80px">
        <el-form-item label="动态类型" prop="recordType">
          <el-select v-model="recordForm.recordType" placeholder="请选择记录类型" style="width: 100%;">
            <el-option label="🏥 疫苗/驱虫" value="疫苗/驱虫" />
            <el-option label="🩺 就医记录" value="就医记录" />
            <el-option label="📸 日常卖萌" value="日常卖萌" />
            <el-option label="🏠 行为训练" value="行为训练" />
          </el-select>
        </el-form-item>

        <el-form-item label="详细内容" prop="content">
          <el-input type="textarea" :rows="4" v-model="recordForm.content" placeholder="记录一下今天发生的事情吧..." />
        </el-form-item>

        <el-form-item label="附加照片" prop="recordFileList">
          <el-upload
              class="upload-demo"
              action="http://localhost:8080/api/file/upload"
              list-type="picture-card"
              v-model:file-list="recordFileList"
              :on-success="handleRecordUploadSuccess"
              :before-upload="beforeUpload"
              multiple
              :limit="3"
              :on-exceed="handleRecordExceed">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="recordDialogVisible = false" round>取 消</el-button>
          <el-button type="primary" color="#ffb880" @click="submitRecord" :loading="recordSubmitLoading" round>发布动态</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Notebook, DocumentChecked, Calendar, Plus, Male, Female, EditPen } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const userInfo = ref({})
const catList = ref([])

// ---- 原有的猫咪档案相关变量 ----
const dialogVisible = ref(false)
const submitLoading = ref(false)
const catFormRef = ref(null)
const fileList = ref([])
const catForm = ref({ nickname: '', earTag: '', breed: '', age: '', gender: 1, healthStatus: '', description: '', coverImage: '', photoUrls: '' })
const rules = { nickname: [{ required: true, message: '必须给小猫起个名字呀', trigger: 'blur' }] }

// ---- 新增的履历记录相关变量 ----
const recordDialogVisible = ref(false)
const recordSubmitLoading = ref(false)
const recordFormRef = ref(null)
const currentRecordCat = ref(null)
const recordFileList = ref([])
const recordForm = ref({ recordType: '日常卖萌', content: '', mediaUrls: '' })
const recordRules = {
  recordType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '写点什么吧~', trigger: 'blur' }]
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) userInfo.value = JSON.parse(userStr)
  fetchCats()
})

const fetchCats = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/cat/list')
    if (res.data.code === 200) catList.value = res.data.data
  } catch (error) {
    ElMessage.error('获取猫咪列表失败')
  }
}

// ---- 原有的猫咪建档方法 ----
const openAddDialog = () => {
  catForm.value = { nickname: '', earTag: '', breed: '', age: '', gender: 1, healthStatus: '', description: '', coverImage: '', photoUrls: '' }
  fileList.value = []
  dialogVisible.value = true
}

const handleExceed = () => ElMessage.warning('最多只能上传 5 张照片哦！')
const beforeUpload = (file) => {
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) { ElMessage.error('照片大小不能超过 5MB！'); return false }
  return true
}

const handleUploadSuccess = (res, uploadFile, uploadFiles) => {
  if (res.code === 200) { uploadFile.url = res.data; fileList.value = uploadFiles }
  else { ElMessage.error(res.message) }
}
const handleUploadError = (err) => { ElMessage.error('图片上传失败') }
const handleRemove = (uploadFile, uploadFiles) => { fileList.value = uploadFiles }

const submitAddCat = async () => {
  if (!catFormRef.value) return
  if (fileList.value.length === 0) { ElMessage.warning('请至少上传一张猫咪照片哦！'); return }
  const urls = fileList.value.map(f => f.url || (f.response && f.response.data))
  catForm.value.coverImage = urls[0]
  catForm.value.photoUrls = urls.join(',')
  await catFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const payload = { ...catForm.value, creatorId: userInfo.value.id }
        const res = await axios.post('http://localhost:8080/api/cat/add', payload)
        if (res.data.code === 200) {
          ElMessage.success('录入成功！')
          dialogVisible.value = false
          fetchCats()
        }
      } catch (error) { ElMessage.error('录入失败') }
      finally { submitLoading.value = false }
    }
  })
}

// ---- 新增的写履历方法 ----
const openRecordDialog = (cat) => {
  currentRecordCat.value = cat
  recordForm.value = { recordType: '日常卖萌', content: '', mediaUrls: '' }
  recordFileList.value = []
  recordDialogVisible.value = true
}

const handleRecordExceed = () => ElMessage.warning('动态最多上传 3 张照片哦！')

const handleRecordUploadSuccess = (res, uploadFile, uploadFiles) => {
  if (res.code === 200) { uploadFile.url = res.data; recordFileList.value = uploadFiles }
  else { ElMessage.error(res.message) }
}

const submitRecord = async () => {
  if (!recordFormRef.value) return

  // 提取履历照片的 URL
  if (recordFileList.value.length > 0) {
    const urls = recordFileList.value.map(f => f.url || (f.response && f.response.data))
    recordForm.value.mediaUrls = urls.join(',')
  }

  await recordFormRef.value.validate(async (valid) => {
    if (valid) {
      recordSubmitLoading.value = true
      try {
        const payload = {
          ...recordForm.value,
          catId: currentRecordCat.value.id,
          creatorId: userInfo.value.id
        }
        const res = await axios.post('http://localhost:8080/api/record/add', payload)
        if (res.data.code === 200) {
          ElMessage.success('动态发布成功！')
          recordDialogVisible.value = false
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (error) {
        ElMessage.error('网络请求失败')
      } finally {
        recordSubmitLoading.value = false
      }
    }
  })
}

const handleLogout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
/* 原有样式保持不变 */
.backend-layout, .layout-container { height: 100vh; }
.aside { background-color: #34495e; display: flex; flex-direction: column; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 18px; font-weight: bold; background-color: #2c3e50; }
.side-menu { flex: 1; border-right: none; }
.header { display: flex; justify-content: space-between; align-items: center; background-color: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.1); z-index: 1; }
.header-breadcrumb { font-weight: bold; color: #666; font-size: 16px; }
.dropdown-link { cursor: pointer; display: flex; align-items: center; color: #333; font-weight: bold; }
.main-content { background-color: #f7f8fa; padding: 30px; }

.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.page-title { color: #555; font-size: 24px; margin: 0; }

.cat-card { border-radius: 16px; border: none; overflow: hidden; transition: transform 0.3s; position: relative; }
.cat-card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.1) !important; }
.cat-image-wrapper { position: relative; height: 200px; width: 100%; overflow: hidden; }
.cat-image { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
.cat-card:hover .cat-image { transform: scale(1.05); }
.status-badge { position: absolute; top: 10px; right: 10px; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; color: white; backdrop-filter: blur(4px); }
.status-free { background-color: rgba(103, 194, 58, 0.85); }
.status-taken { background-color: rgba(245, 108, 108, 0.85); }
.cat-info { padding: 16px; }
.cat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.cat-name { font-size: 18px; font-weight: bold; color: #333; }
.cat-tags { margin-bottom: 12px; display: flex; gap: 8px; }
.cat-desc { font-size: 13px; color: #888; margin: 0; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

/* 新增操作按钮样式 */
.card-actions { margin-top: 15px; border-top: 1px dashed #ebeef5; padding-top: 12px; text-align: right; }
.action-btn { width: 100%; border-radius: 8px; }

.upload-tip { font-size: 12px; color: #999; margin-top: 5px; }
:deep(.cute-dialog) { border-radius: 16px; overflow: hidden; }
:deep(.el-dialog__header) { background-color: #fff9e6; padding: 20px; margin-right: 0; }
:deep(.el-dialog__title) { color: #a45a1e; font-weight: bold; }
</style>