<template>
  <div style="padding: 40px 0;">
    <h2 class="section-title">🐾 我的领养足迹</h2>
    <el-table :data="myApplies" style="width: 100%" stripe class="custom-table" v-loading="loadingRecords">
      <el-table-column label="申请时间" width="160">
        <template #default="scope">{{ formatTime(scope.row.application.applyTime) }}</template>
      </el-table-column>

      <el-table-column label="猫咪" width="180">
        <template #default="scope">
          <div style="display: flex; align-items: center; gap: 10px;">
            <el-avatar shape="square" :size="45" :src="scope.row.cat?.coverImage" />
            <strong style="color: #a45a1e;">{{ scope.row.cat?.nickname }}</strong>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="救助员回复" min-width="200">
        <template #default="scope">
          <span style="color: #888;">{{ scope.row.application.reviewRemark || '等待救助员审核中...' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="进度状态" width="140">
        <template #default="scope">
          <el-tag v-if="scope.row.application.status === 0" type="info" round>等待审核</el-tag>
          <el-tag v-else-if="scope.row.application.status === 1" type="warning" round>待签约</el-tag>
          <el-tag v-else-if="scope.row.application.status === 2" type="danger" round>遗憾驳回</el-tag>
          <el-tag v-else-if="scope.row.application.status === 3" type="success" round>已领养</el-tag>
          <el-tag v-else-if="scope.row.application.status === 4" type="info" round effect="plain">已取消</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <div style="display: flex; gap: 8px; flex-wrap: wrap;">
            <el-popconfirm
                v-if="scope.row.application.status === 0"
                title="确定要取消这次申请吗？"
                @confirm="cancelApply(scope.row.application.id)">
              <template #reference>
                <el-button type="danger" plain size="small" round>取消申请</el-button>
              </template>
            </el-popconfirm>

            <el-button
                v-if="scope.row.application.status === 1"
                type="primary" color="#ffb880" size="small" round
                @click="openSignDialog(scope.row)">
              签署协议
            </el-button>

            <template v-if="scope.row.application.status === 3">
              <el-button type="info" plain size="small" round @click="openDetailDrawer(scope.row)">查看详情</el-button>
              <el-button type="success" plain size="small" round @click="openLifeRecordDialog(scope.row)">记录生活</el-button>
            </template>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 已领养详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="🐱 领养详情" size="45%" destroy-on-close>
      <div v-if="currentDetail" class="detail-drawer-content">
        <div class="detail-section">
          <h3 class="section-label">猫咪档案</h3>
          <div class="cat-profile">
            <el-image :src="currentDetail.cat?.coverImage" class="cat-profile-img" fit="cover" />
            <div class="cat-profile-info">
              <div class="cat-profile-name">{{ currentDetail.cat?.nickname }}</div>
              <el-descriptions :column="1" size="small">
                <el-descriptions-item label="品种">{{ currentDetail.cat?.breed }}</el-descriptions-item>
                <el-descriptions-item label="年龄">{{ currentDetail.cat?.age }}</el-descriptions-item>
                <el-descriptions-item label="健康状况">{{ currentDetail.cat?.healthStatus }}</el-descriptions-item>
                <el-descriptions-item label="性格描述">{{ currentDetail.cat?.description }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </div>

        <el-divider />

        <div class="detail-section">
          <h3 class="section-label">申请信息</h3>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="申请时间">{{ formatTime(currentDetail.application.applyTime) }}</el-descriptions-item>
            <el-descriptions-item label="签约时间">{{ formatTime(currentDetail.application.signTime) }}</el-descriptions-item>
            <el-descriptions-item label="领养理由">{{ currentDetail.application.reason }}</el-descriptions-item>
            <el-descriptions-item label="养宠经验">{{ currentDetail.application.experience }}</el-descriptions-item>
            <el-descriptions-item label="居住环境">{{ currentDetail.application.housingCondition }}</el-descriptions-item>
            <el-descriptions-item label="饲养计划">{{ currentDetail.application.feedingPlan }}</el-descriptions-item>
            <el-descriptions-item label="电子签名">
              <span class="signature-text">{{ currentDetail.application.signature }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider />

        <div class="detail-section">
          <h3 class="section-label">生活记录</h3>
          <el-empty v-if="followUpList.length === 0" description="还没有提交过生活记录哦~" :image-size="80" />
          <el-timeline v-else>
            <el-timeline-item
                v-for="item in followUpList" :key="item.id"
                :timestamp="formatTime(item.createTime)" placement="top">
              <el-card shadow="hover" class="timeline-card">
                <div class="timeline-tag">
                  <el-tag v-if="item.submitType === 0" type="success" size="small">本人提交</el-tag>
                  <el-tag v-else type="warning" size="small">救助员代录</el-tag>
                </div>
                <p class="timeline-health">{{ item.healthStatus }}</p>
                <p v-if="item.remark" class="timeline-remark">{{ item.remark }}</p>
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
      </div>
    </el-drawer>

    <!-- 记录生活弹窗 -->
    <el-dialog v-model="lifeRecordVisible" title="📸 记录猫咪近况" width="500px" destroy-on-close>
      <el-form :model="lifeForm" :rules="lifeRules" ref="lifeFormRef" label-width="90px">
        <el-form-item label="健康状况" prop="healthStatus">
          <el-input
              type="textarea" :rows="3" v-model="lifeForm.healthStatus"
              placeholder="描述一下最近的健康状况，比如：精神好，食欲佳，已完成本月驱虫..." />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="lifeForm.remark" placeholder="其他想说的..." />
        </el-form-item>
        <el-form-item label="近期照片">
          <el-upload
              action="http://localhost:8080/api/file/upload"
              list-type="picture-card"
              v-model:file-list="lifePhotoList"
              :on-success="handleLifeUploadSuccess"
              :before-upload="beforeUpload"
              :limit="5">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="lifeRecordVisible = false" round>取消</el-button>
        <el-button type="primary" color="#ff9d4d" @click="submitLifeRecord" :loading="lifeSubmitLoading" round>提交记录</el-button>
      </template>
    </el-dialog>

    <!-- 签约弹窗 -->
    <el-dialog v-model="signDialogVisible" title="📜 签署领养协议" width="600px" destroy-on-close>
      <div class="agreement-box">
        <h3 style="text-align: center;">宠物领养协议书</h3>
        <p>甲方（救助方）：Hajimi 领养平台</p>
        <p>乙方（领养方）：<strong>{{ userInfo.username }}</strong></p>
        <p>为了保障猫咪 <strong>{{ signTarget?.cat?.nickname }}</strong> 的福利，乙方承诺做到以下几点：</p>
        <ol>
          <li>科学喂养，提供充足干净的饮食。</li>
          <li>有病就医，按时接种疫苗及驱虫。</li>
          <li>不散养，家中必须安装防护纱窗。</li>
          <li>适龄绝育，绝不遗弃、转卖或虐待。</li>
          <li>配合甲方进行定期的线上或线下回访。</li>
        </ol>
        <p style="color: #F56C6C; font-weight: bold;">* 如违反上述协议，甲方有权无条件收回猫咪。</p>
      </div>
      <el-form style="margin-top: 20px;">
        <el-form-item label="电子签名">
          <el-input v-model="signatureName" placeholder="请输入您的真实姓名作为电子签名确认" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="signDialogVisible = false" round>再考虑一下</el-button>
        <el-button type="primary" color="#ff9d4d" @click="confirmSign" round>确认签署，接它回家！</el-button>
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
const myApplies = ref([])
const loadingRecords = ref(false)

const detailDrawerVisible = ref(false)
const currentDetail = ref(null)
const followUpList = ref([])

const lifeRecordVisible = ref(false)
const lifeSubmitLoading = ref(false)
const lifeFormRef = ref(null)
const lifePhotoList = ref([])
const currentLifeTarget = ref(null)
const lifeForm = ref({ healthStatus: '', remark: '', photoUrls: '' })
const lifeRules = {
  healthStatus: [{ required: true, message: '请描述一下近期健康状况', trigger: 'blur' }]
}

const signDialogVisible = ref(false)
const signTarget = ref(null)
const signatureName = ref('')

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) userInfo.value = JSON.parse(userStr)
  fetchMyRecords()
})

const formatTime = (timeStr) => timeStr ? timeStr.replace('T', ' ').substring(0, 16) : ''

const fetchMyRecords = async () => {
  loadingRecords.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/adopt/myList/${userInfo.value.id}`)
    if (res.data.code === 200) myApplies.value = res.data.data
  } catch (error) { ElMessage.error('获取记录失败') }
  finally { loadingRecords.value = false }
}

const cancelApply = async (id) => {
  try {
    const res = await axios.post(`http://localhost:8080/api/adopt/cancel/${id}`)
    if (res.data.code === 200) {
      ElMessage.success('申请已取消')
      fetchMyRecords()
    } else { ElMessage.error(res.data.message) }
  } catch (error) { ElMessage.error('操作失败') }
}

const openDetailDrawer = async (row) => {
  currentDetail.value = row
  followUpList.value = []
  detailDrawerVisible.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/followup/list/${row.application.id}`)
    if (res.data.code === 200) followUpList.value = res.data.data
  } catch (error) { ElMessage.error('获取生活记录失败') }
}

const openLifeRecordDialog = (row) => {
  currentLifeTarget.value = row
  lifeForm.value = { healthStatus: '', remark: '', photoUrls: '' }
  lifePhotoList.value = []
  lifeRecordVisible.value = true
}

const beforeUpload = (file) => {
  if (file.size / 1024 / 1024 > 5) { ElMessage.error('照片大小不能超过 5MB'); return false }
  return true
}
const handleLifeUploadSuccess = (res, uploadFile, uploadFiles) => {
  if (res.code === 200) { uploadFile.url = res.data; lifePhotoList.value = uploadFiles }
}

const submitLifeRecord = async () => {
  if (!lifeFormRef.value) return
  await lifeFormRef.value.validate(async (valid) => {
    if (valid) {
      lifeSubmitLoading.value = true
      if (lifePhotoList.value.length > 0) {
        const urls = lifePhotoList.value.map(f => f.url || (f.response && f.response.data))
        lifeForm.value.photoUrls = urls.join(',')
      }
      try {
        const payload = {
          ...lifeForm.value,
          applicationId: currentLifeTarget.value.application.id,
          catId: currentLifeTarget.value.cat.id,
          userId: userInfo.value.id,
          submitType: 0
        }
        const res = await axios.post('http://localhost:8080/api/followup/submit', payload)
        if (res.data.code === 200) {
          ElMessage.success('记录提交成功！')
          lifeRecordVisible.value = false
        } else { ElMessage.error(res.data.message) }
      } catch (error) { ElMessage.error('提交失败') }
      finally { lifeSubmitLoading.value = false }
    }
  })
}

const openSignDialog = (row) => {
  signTarget.value = row
  signatureName.value = ''
  signDialogVisible.value = true
}

const confirmSign = async () => {
  if (!signatureName.value) { ElMessage.warning('请输入您的姓名作为电子签名'); return }
  if (signatureName.value !== userInfo.value.username && signatureName.value !== userInfo.value.realName) {
    ElMessage.warning('电子签名需与您的注册昵称或真实姓名一致')
    return
  }
  try {
    const res = await axios.post('http://localhost:8080/api/adopt/sign', {
      id: signTarget.value.application.id,
      signature: signatureName.value
    })
    if (res.data.code === 200) {
      ElMessage.success('🎉 协议签署成功！恭喜您！')
      signDialogVisible.value = false
      fetchMyRecords()
    } else { ElMessage.error(res.data.message) }
  } catch (error) { ElMessage.error('网络错误') }
}
</script>

<style scoped>
.section-title { color: #555; margin-bottom: 20px; }
.custom-table { border-radius: 16px; overflow: hidden; box-shadow: 0 4px 16px rgba(0,0,0,0.05); }
.detail-drawer-content { padding: 10px 20px; }
.detail-section { margin-bottom: 10px; }
.section-label { font-size: 16px; color: #a45a1e; margin: 0 0 15px 0; }
.cat-profile { display: flex; gap: 20px; align-items: flex-start; }
.cat-profile-img { width: 120px; height: 120px; border-radius: 12px; flex-shrink: 0; }
.cat-profile-name { font-size: 20px; font-weight: bold; color: #333; margin-bottom: 10px; }
.cat-profile-info { flex: 1; }
.signature-text { font-family: "楷体", "KaiTi", serif; font-size: 18px; font-weight: bold; color: #333; }
.timeline-card { border-radius: 10px; }
.timeline-tag { margin-bottom: 8px; }
.timeline-health { margin: 0 0 6px 0; color: #444; font-size: 14px; line-height: 1.6; }
.timeline-remark { margin: 0 0 8px 0; color: #888; font-size: 13px; }
.timeline-photos { display: flex; gap: 8px; flex-wrap: wrap; }
.timeline-photo { width: 80px; height: 80px; border-radius: 8px; cursor: pointer; }
.agreement-box { background-color: #f7f8fa; padding: 20px; border-radius: 8px; font-size: 14px; line-height: 1.8; color: #555; max-height: 300px; overflow-y: auto; }
.agreement-box ol { padding-left: 20px; }
</style>