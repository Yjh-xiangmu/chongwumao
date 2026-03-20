<template>
  <div class="cat-manage-container">
    <div class="toolbar">
      <h2 class="page-title">喵星人常驻花名册 🐾</h2>
      <el-button type="primary" color="#ffb880" icon="Plus" round size="large" @click="openAddDialog">
        录入新主子
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="6" v-for="cat in catList" :key="cat.id" style="margin-bottom: 20px;">
        <el-card class="cat-card" :body-style="{ padding: '0px' }" shadow="hover" @click="openCatDetail(cat)">
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
              <el-button type="primary" plain size="small" icon="EditPen" class="action-btn" @click.stop="openRecordDialog(cat)">记录动态</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="catList.length === 0" description="档案库空空如也，快去救助小可爱吧~" />

    <el-drawer v-model="detailDrawerVisible" size="45%" :with-header="false" class="cat-detail-drawer" destroy-on-close>
      <div v-if="currentCatDetail" class="drawer-header">
        <el-avatar :size="60" :src="currentCatDetail.coverImage" />
        <div class="header-info">
          <h2>{{ currentCatDetail.nickname }} 的专属档案</h2>
          <span class="header-sub">耳标号: {{ currentCatDetail.earTag || '暂无' }}</span>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="✏️ 基础档案编辑" name="info">
          <div class="tab-content">
            <el-form :model="editForm" :rules="rules" ref="editFormRef" label-width="90px" label-position="left">
              <el-form-item label="喵咪相册" prop="fileList">
                <el-upload
                    class="upload-demo" action="http://localhost:8080/api/file/upload" list-type="picture-card"
                    v-model:file-list="editFileList" :on-success="handleEditUploadSuccess" :before-upload="beforeUpload"
                    multiple :limit="5" :on-exceed="handleExceed">
                  <el-icon><Plus /></el-icon>
                </el-upload>
              </el-form-item>

              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="喵咪昵称" prop="nickname"><el-input v-model="editForm.nickname" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="档案/芯片" prop="earTag"><el-input v-model="editForm.earTag" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="品种" prop="breed"><el-input v-model="editForm.breed" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="年龄" prop="age"><el-input v-model="editForm.age" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="性别" prop="gender">
                    <el-radio-group v-model="editForm.gender"><el-radio :value="1">小王子</el-radio><el-radio :value="2">小公主</el-radio></el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="12"><el-form-item label="健康状况" prop="healthStatus"><el-input v-model="editForm.healthStatus" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="领养状态" prop="status">
                    <el-select v-model="editForm.status" style="width: 100%;">
                      <el-option label="待领养" :value="0" />
                      <el-option label="已被申请" :value="1" />
                      <el-option label="已领养" :value="2" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="性格描述" prop="description">
                <el-input type="textarea" :rows="3" v-model="editForm.description" />
              </el-form-item>
              <el-button type="primary" color="#ffb880" @click="submitEditCat" :loading="editSubmitLoading" class="full-btn" round>
                保存档案修改
              </el-button>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="📸 成长动态履历" name="timeline">
          <div class="tab-content timeline-container">
            <el-empty v-if="timelineData.length === 0" description="还没有记录过动态哦，快去记录吧~" />
            <el-timeline v-else>
              <el-timeline-item
                  v-for="(item, index) in timelineData"
                  :key="index"
                  :timestamp="formatTime(item.createTime)"
                  placement="top"
                  :type="getTimelineType(item.recordType)">
                <el-card shadow="hover" class="timeline-card">
                  <h4 class="timeline-title">{{ item.recordType }}</h4>
                  <p class="timeline-content">{{ item.content }}</p>
                  <div v-if="item.mediaUrls" class="timeline-images">
                    <el-image
                        v-for="(img, idx) in item.mediaUrls.split(',')"
                        :key="idx"
                        :src="img"
                        :preview-src-list="item.mediaUrls.split(',')"
                        fit="cover"
                        class="timeline-img"
                        hide-on-click-modal />
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>

    <el-dialog v-model="dialogVisible" title="🐾 录入新主子档案" width="600px" class="cute-dialog" destroy-on-close>
      <el-form :model="catForm" :rules="rules" ref="catFormRef" label-width="90px">
        <el-form-item label="喵咪相册" prop="fileList">
          <el-upload
              class="upload-demo" action="http://localhost:8080/api/file/upload" list-type="picture-card"
              v-model:file-list="fileList" :on-success="handleUploadSuccess" :before-upload="beforeUpload"
              multiple :limit="5" :on-exceed="handleExceed">
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多支持上传 5 张照片，第一张将作为封面图哦！</div>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="喵咪昵称" prop="nickname"><el-input v-model="catForm.nickname" placeholder="如: 橘子" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="档案/芯片" prop="earTag"><el-input v-model="catForm.earTag" placeholder="内部编号(选填)" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="品种" prop="breed"><el-input v-model="catForm.breed" placeholder="如: 中华田园橘猫" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="年龄" prop="age"><el-input v-model="catForm.age" placeholder="如: 3个月" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="catForm.gender"><el-radio :value="1">小王子</el-radio><el-radio :value="2">小公主</el-radio></el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="健康状况" prop="healthStatus"><el-input v-model="catForm.healthStatus" placeholder="如: 已驱虫/已绝育" /></el-form-item></el-col>
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
              class="upload-demo" action="http://localhost:8080/api/file/upload" list-type="picture-card"
              v-model:file-list="recordFileList" :on-success="handleRecordUploadSuccess" :before-upload="beforeUpload"
              multiple :limit="3" :on-exceed="handleRecordExceed">
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
import { Plus, Male, Female, EditPen } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const userInfo = ref({})
const catList = ref([])

// ---- 原有的猫咪档案相关变量 ----
const dialogVisible = ref(false)
const submitLoading = ref(false)
const catFormRef = ref(null)
const fileList = ref([])
const catForm = ref({ nickname: '', earTag: '', breed: '', age: '', gender: 1, healthStatus: '', description: '', coverImage: '', photoUrls: '' })
const rules = { nickname: [{ required: true, message: '必须给小猫起个名字呀', trigger: 'blur' }] }

// ---- 履历记录相关变量 ----
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

// ---- 新增：详情与编辑相关变量 ----
const detailDrawerVisible = ref(false)
const activeTab = ref('info')
const currentCatDetail = ref(null)
const editFormRef = ref(null)
const editSubmitLoading = ref(false)
const editForm = ref({})
const editFileList = ref([])
const timelineData = ref([])

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) userInfo.value = JSON.parse(userStr)
  fetchCats()
})

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16) // 截取到分钟
}

// 根据类型返回时间轴圆点颜色
const getTimelineType = (type) => {
  if (type.includes('疫苗') || type.includes('就医')) return 'danger'
  if (type.includes('日常')) return 'success'
  if (type.includes('训练')) return 'warning'
  return 'primary'
}

const fetchCats = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/cat/list')
    if (res.data.code === 200) catList.value = res.data.data
  } catch (error) { ElMessage.error('获取猫咪列表失败') }
}

// ---- 上传校验 ----
const beforeUpload = (file) => {
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) { ElMessage.error('照片大小不能超过 5MB！'); return false }
  return true
}

// ---- 新增：打开猫咪详情抽屉 ----
const openCatDetail = async (cat) => {
  currentCatDetail.value = cat
  activeTab.value = 'info' // 默认打开基本信息 Tab

  // 1. 初始化编辑表单数据 (深拷贝，防止直接修改表格数据)
  editForm.value = JSON.parse(JSON.stringify(cat))

  // 2. 初始化编辑页面的照片墙
  editFileList.value = []
  if (cat.photoUrls) {
    const urls = cat.photoUrls.split(',')
    editFileList.value = urls.map((url, index) => ({ name: `照片${index+1}`, url: url }))
  } else if (cat.coverImage) {
    editFileList.value = [{ name: '封面', url: cat.coverImage }]
  }

  // 3. 获取这只猫咪的时间轴履历
  await fetchCatTimeline(cat.id)

  detailDrawerVisible.value = true
}

// 获取猫咪时间轴数据
const fetchCatTimeline = async (catId) => {
  try {
    const res = await axios.get(`http://localhost:8080/api/record/list/${catId}`)
    if (res.data.code === 200) {
      timelineData.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取履历记录失败')
  }
}

// 编辑页面图片上传成功处理
const handleEditUploadSuccess = (res, uploadFile, uploadFiles) => {
  if (res.code === 200) { uploadFile.url = res.data; editFileList.value = uploadFiles }
  else { ElMessage.error(res.message) }
}

// 提交编辑修改
const submitEditCat = async () => {
  if (!editFormRef.value) return

  if (editFileList.value.length === 0) {
    ElMessage.warning('照片墙不能全空哦！')
    return
  }

  // 提取最新的图片 URL
  const urls = editFileList.value.map(f => f.url || (f.response && f.response.data))
  editForm.value.coverImage = urls[0]
  editForm.value.photoUrls = urls.join(',')

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editSubmitLoading.value = true
      try {
        const res = await axios.put('http://localhost:8080/api/cat/update', editForm.value)
        if (res.data.code === 200) {
          ElMessage.success('档案修改成功！')
          detailDrawerVisible.value = false
          fetchCats() // 刷新外面的列表
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (error) {
        ElMessage.error('网络请求失败')
      } finally {
        editSubmitLoading.value = false
      }
    }
  })
}

// ---- 原有：建档方法 ----
const openAddDialog = () => {
  catForm.value = { nickname: '', earTag: '', breed: '', age: '', gender: 1, healthStatus: '', description: '', coverImage: '', photoUrls: '' }
  fileList.value = []
  dialogVisible.value = true
}
const handleExceed = () => ElMessage.warning('最多只能上传 5 张照片哦！')
const handleUploadSuccess = (res, uploadFile, uploadFiles) => { if (res.code === 200) { uploadFile.url = res.data; fileList.value = uploadFiles } else { ElMessage.error(res.message) } }
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
        if (res.data.code === 200) { ElMessage.success('录入成功！'); dialogVisible.value = false; fetchCats() }
      } catch (error) { ElMessage.error('录入失败') } finally { submitLoading.value = false }
    }
  })
}

// ---- 原有：写履历方法 ----
const openRecordDialog = (cat) => {
  currentRecordCat.value = cat
  recordForm.value = { recordType: '日常卖萌', content: '', mediaUrls: '' }
  recordFileList.value = []
  recordDialogVisible.value = true
}
const handleRecordExceed = () => ElMessage.warning('动态最多上传 3 张照片哦！')
const handleRecordUploadSuccess = (res, uploadFile, uploadFiles) => { if (res.code === 200) { uploadFile.url = res.data; recordFileList.value = uploadFiles } else { ElMessage.error(res.message) } }

const submitRecord = async () => {
  if (!recordFormRef.value) return
  if (recordFileList.value.length > 0) {
    const urls = recordFileList.value.map(f => f.url || (f.response && f.response.data))
    recordForm.value.mediaUrls = urls.join(',')
  }
  await recordFormRef.value.validate(async (valid) => {
    if (valid) {
      recordSubmitLoading.value = true
      try {
        const payload = { ...recordForm.value, catId: currentRecordCat.value.id, creatorId: userInfo.value.id }
        const res = await axios.post('http://localhost:8080/api/record/add', payload)
        if (res.data.code === 200) { ElMessage.success('动态发布成功！'); recordDialogVisible.value = false }
        else { ElMessage.error(res.data.message) }
      } catch (error) { ElMessage.error('网络请求失败') } finally { recordSubmitLoading.value = false }
    }
  })
}
</script>

<style scoped>
.cat-manage-container { padding-bottom: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.page-title { color: #555; font-size: 24px; margin: 0; }

.cat-card { border-radius: 16px; border: none; overflow: hidden; transition: transform 0.3s; position: relative; cursor: pointer; }
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
.card-actions { margin-top: 15px; border-top: 1px dashed #ebeef5; padding-top: 12px; text-align: right; }
.action-btn { width: 100%; border-radius: 8px; }

/* 详情抽屉样式 */
.drawer-header { padding: 30px; background: linear-gradient(135deg, #fff9e6, #ffeedb); display: flex; align-items: center; gap: 20px; }
.header-info h2 { margin: 0 0 5px 0; color: #a45a1e; font-size: 24px; }
.header-sub { color: #c47633; font-size: 14px; }
.detail-tabs { padding: 0 30px; }
.tab-content { padding: 20px 0; }
.full-btn { width: 100%; padding: 24px 0; font-size: 16px; font-weight: bold; margin-top: 20px; }

/* 时间轴样式 */
.timeline-container { max-height: calc(100vh - 200px); overflow-y: auto; padding-right: 15px; }
.timeline-card { border-radius: 12px; }
.timeline-title { margin: 0 0 10px 0; font-size: 16px; color: #333; }
.timeline-content { margin: 0 0 15px 0; color: #666; font-size: 14px; line-height: 1.6; }
.timeline-images { display: flex; flex-wrap: wrap; gap: 10px; }
.timeline-img { width: 100px; height: 100px; border-radius: 8px; cursor: pointer; border: 1px solid #eee; }

.upload-tip { font-size: 12px; color: #999; margin-top: 5px; }
:deep(.cute-dialog) { border-radius: 16px; overflow: hidden; }
:deep(.el-dialog__header) { background-color: #fff9e6; padding: 20px; margin-right: 0; }
:deep(.el-dialog__title) { color: #a45a1e; font-weight: bold; }
</style>