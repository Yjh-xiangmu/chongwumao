<template>
  <div class="record-manage-container">
    <div class="toolbar">
      <h2 class="page-title">📋 领养回访管理</h2>
      <el-tag type="info" effect="plain">每隔 15 天需回访一次，领养人自主提交则无需额外记录</el-tag>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="adoptedList" v-loading="loading" style="width: 100%" stripe>

        <el-table-column label="猫咪" width="180">
          <template #default="scope">
            <div class="cat-cell">
              <el-avatar shape="square" :size="45" :src="scope.row.cat?.coverImage" />
              <strong class="cat-name">{{ scope.row.cat?.nickname }}</strong>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="领养人" width="180">
          <template #default="scope">
            <div v-if="scope.row.user" class="user-cell">
              <span class="user-name-text">{{ scope.row.user.username }}<span v-if="scope.row.user.realName" class="real-name-text">（{{ scope.row.user.realName }}）</span></span>
              <span class="phone-text">{{ scope.row.user.phone }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="签约时间" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.application.signTime) }}
          </template>
        </el-table-column>

        <el-table-column label="最近回访" width="160">
          <template #default="scope">
            <span v-if="scope.row.lastRecord">{{ formatTime(scope.row.lastRecord.createTime) }}</span>
            <span v-else style="color: #bbb;">从未记录</span>
          </template>
        </el-table-column>

        <el-table-column label="最近提交方式" width="130">
          <template #default="scope">
            <template v-if="scope.row.lastRecord">
              <el-tag v-if="scope.row.lastRecord.submitType === 0" type="success" size="small">本人提交</el-tag>
              <el-tag v-else type="warning" size="small">救助员代录</el-tag>
            </template>
            <span v-else style="color: #bbb;">—</span>
          </template>
        </el-table-column>

        <el-table-column label="回访状态" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.needFollowUp" type="danger" effect="dark">⚠️ 需回访</el-tag>
            <el-tag v-else type="success" effect="light">✅ 正常</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <div style="display: flex; gap: 8px;">
              <el-button size="small" type="info" plain round @click="openHistoryDrawer(scope.row)">查看记录</el-button>
              <el-button
                  v-if="scope.row.needFollowUp"
                  size="small" type="warning" round
                  @click="openVolunteerRecordDialog(scope.row)">
                电话代录
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="adoptedList.length === 0 && !loading" description="暂无已领养记录" />
    </el-card>

    <!-- 历史记录抽屉 -->
    <el-drawer v-model="historyDrawerVisible" title="📂 回访历史记录" size="40%" destroy-on-close>
      <div v-if="currentRow" class="drawer-header-info">
        <el-avatar shape="square" :size="50" :src="currentRow.cat?.coverImage" />
        <div>
          <div class="drawer-cat-name">{{ currentRow.cat?.nickname }}</div>
          <div class="drawer-user-name">领养人：{{ currentRow.user?.username }}{{ currentRow.user?.realName ? `（${currentRow.user.realName}）` : "" }}</div>
        </div>
      </div>

      <div class="drawer-body">
        <el-empty v-if="historyList.length === 0" description="暂无回访记录" :image-size="80" />
        <el-timeline v-else>
          <el-timeline-item
              v-for="item in historyList" :key="item.id"
              :timestamp="formatTime(item.createTime)" placement="top"
              :type="item.submitType === 0 ? 'success' : 'warning'">
            <el-card shadow="hover" class="timeline-card">
              <div class="timeline-header">
                <el-tag v-if="item.submitType === 0" type="success" size="small">本人提交</el-tag>
                <el-tag v-else type="warning" size="small">救助员代录</el-tag>
              </div>
              <p class="timeline-health">{{ item.healthStatus }}</p>
              <p v-if="item.remark" class="timeline-remark">备注：{{ item.remark }}</p>
              <div v-if="item.photoUrls" class="timeline-photos">
                <el-image
                    v-for="(img, idx) in item.photoUrls.split(',')" :key="idx"
                    :src="img" :preview-src-list="item.photoUrls.split(',')"
                    fit="cover" class="timeline-photo" hide-on-click-modal />
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>

    <!-- 救助员电话代录弹窗 -->
    <el-dialog v-model="volunteerRecordVisible" title="📞 电话回访 — 代录记录" width="500px" destroy-on-close>
      <el-alert
          type="warning"
          title="此功能用于领养人未主动提交时，救助员电话沟通后代为录入。"
          show-icon :closable="false"
          style="margin-bottom: 20px;" />

      <div v-if="currentRow" class="record-target-info">
        <span>正在为 <strong>{{ currentRow.user?.username }}</strong> 的猫咪 <strong>{{ currentRow.cat?.nickname }}</strong> 代录回访记录</span>
      </div>

      <el-form :model="volunteerForm" :rules="volunteerRules" ref="volunteerFormRef" label-width="90px" style="margin-top: 15px;">
        <el-form-item label="健康状况" prop="healthStatus">
          <el-input
              type="textarea" :rows="3" v-model="volunteerForm.healthStatus"
              placeholder="根据电话沟通内容，记录猫咪健康状况..." />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="volunteerForm.remark" placeholder="其他补充信息..." />
        </el-form-item>
        <el-form-item label="附件照片">
          <el-upload
              action="http://localhost:8080/api/file/upload"
              list-type="picture-card"
              v-model:file-list="volunteerPhotoList"
              :on-success="handleVolunteerUploadSuccess"
              :before-upload="beforeUpload"
              :limit="3">
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div style="font-size: 12px; color: #999; margin-top: 5px;">如有领养人微信发来的照片，可在此上传</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="volunteerRecordVisible = false" round>取消</el-button>
        <el-button type="warning" @click="submitVolunteerRecord" :loading="volunteerSubmitLoading" round>
          确认代录（非领养人主动提交）
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const userInfo = ref({})
const adoptedList = ref([])
const loading = ref(false)

// 历史记录抽屉
const historyDrawerVisible = ref(false)
const currentRow = ref(null)
const historyList = ref([])

// 救助员代录弹窗
const volunteerRecordVisible = ref(false)
const volunteerSubmitLoading = ref(false)
const volunteerFormRef = ref(null)
const volunteerPhotoList = ref([])
const volunteerForm = ref({ healthStatus: '', remark: '', photoUrls: '' })
const volunteerRules = {
  healthStatus: [{ required: true, message: '请填写健康状况', trigger: 'blur' }]
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) userInfo.value = JSON.parse(userStr)
  fetchAdoptedList()
})

const formatTime = (timeStr) => timeStr ? timeStr.replace('T', ' ').substring(0, 16) : ''

const fetchAdoptedList = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/followup/adoptedList')
    if (res.data.code === 200) adoptedList.value = res.data.data
  } catch (error) { ElMessage.error('获取回访列表失败') }
  finally { loading.value = false }
}

// 查看历史回访记录
const openHistoryDrawer = async (row) => {
  currentRow.value = row
  historyList.value = []
  historyDrawerVisible.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/followup/list/${row.application.id}`)
    if (res.data.code === 200) historyList.value = res.data.data
  } catch (error) { ElMessage.error('获取记录失败') }
}

// 打开电话代录弹窗
const openVolunteerRecordDialog = (row) => {
  currentRow.value = row
  volunteerForm.value = { healthStatus: '', remark: '', photoUrls: '' }
  volunteerPhotoList.value = []
  volunteerRecordVisible.value = true
}

const beforeUpload = (file) => {
  if (file.size / 1024 / 1024 > 5) { ElMessage.error('照片大小不能超过 5MB'); return false }
  return true
}
const handleVolunteerUploadSuccess = (res, uploadFile, uploadFiles) => {
  if (res.code === 200) { uploadFile.url = res.data; volunteerPhotoList.value = uploadFiles }
}

// 提交代录记录
const submitVolunteerRecord = async () => {
  if (!volunteerFormRef.value) return
  await volunteerFormRef.value.validate(async (valid) => {
    if (valid) {
      volunteerSubmitLoading.value = true
      if (volunteerPhotoList.value.length > 0) {
        const urls = volunteerPhotoList.value.map(f => f.url || (f.response && f.response.data))
        volunteerForm.value.photoUrls = urls.join(',')
      }
      try {
        const payload = {
          ...volunteerForm.value,
          applicationId: currentRow.value.application.id,
          catId: currentRow.value.cat.id,
          userId: currentRow.value.user.id,
          submitType: 1,               // 救助员代录
          volunteerId: userInfo.value.id
        }
        const res = await axios.post('http://localhost:8080/api/followup/submit', payload)
        if (res.data.code === 200) {
          ElMessage.success('代录成功！')
          volunteerRecordVisible.value = false
          fetchAdoptedList() // 刷新列表更新回访状态
        } else { ElMessage.error(res.data.message) }
      } catch (error) { ElMessage.error('提交失败') }
      finally { volunteerSubmitLoading.value = false }
    }
  })
}
</script>

<style scoped>
.record-manage-container { padding-bottom: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { color: #555; font-size: 24px; margin: 0; }
.table-card { border-radius: 16px; border: none; box-shadow: 0 4px 16px rgba(0,0,0,0.05); }

.cat-cell { display: flex; align-items: center; gap: 10px; }
.cat-name { font-weight: bold; color: #a45a1e; }
.user-cell { display: flex; flex-direction: column; }
.user-name-text { font-size: 14px; color: #333; }
.real-name-text { font-size: 12px; color: #aaa; }
.phone-text { font-size: 12px; color: #999; margin-top: 2px; }

/* 抽屉 */
.drawer-header-info { display: flex; align-items: center; gap: 15px; padding: 0 20px 20px; border-bottom: 1px solid #f0f0f0; margin-bottom: 20px; }
.drawer-cat-name { font-size: 18px; font-weight: bold; color: #333; }
.drawer-user-name { font-size: 13px; color: #888; margin-top: 4px; }
.drawer-body { padding: 0 20px; }

/* 时间线 */
.timeline-card { border-radius: 10px; }
.timeline-header { margin-bottom: 8px; }
.timeline-health { margin: 0 0 6px 0; color: #444; font-size: 14px; line-height: 1.6; }
.timeline-remark { margin: 0 0 8px 0; color: #888; font-size: 13px; }
.timeline-photos { display: flex; gap: 8px; flex-wrap: wrap; }
.timeline-photo { width: 80px; height: 80px; border-radius: 8px; cursor: pointer; }

.record-target-info { background-color: #fdf6ec; padding: 12px 16px; border-radius: 8px; color: #e6a23c; font-size: 14px; }
</style>