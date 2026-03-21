<template>
  <div class="approve-manage-container">
    <div class="toolbar">
      <h2 class="page-title">📝 领养申请审核中心</h2>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="applyList" v-loading="loading" style="width: 100%" stripe>

        <el-table-column type="expand">
          <template #default="props">
            <div class="expand-detail">
              <h4 class="detail-title">📋 资质评估报告</h4>
              <el-descriptions :column="2" border class="custom-desc">
                <el-descriptions-item label="领养理由" :span="2">{{ props.row.application.reason }}</el-descriptions-item>
                <el-descriptions-item label="养宠经验">{{ props.row.application.experience }}</el-descriptions-item>
                <el-descriptions-item label="居住环境">{{ props.row.application.housingCondition || '未填写' }}</el-descriptions-item>
                <el-descriptions-item label="饲养计划" :span="2">{{ props.row.application.feedingPlan || '未填写' }}</el-descriptions-item>
                <el-descriptions-item label="电子签名" :span="2" v-if="props.row.application.status === 3">
                  <span class="signature-text">{{ props.row.application.signature }}</span>
                  <span class="sign-time" style="color: #888; margin-left: 10px; font-size: 12px;">
                    (签署于: {{ formatTime(props.row.application.signTime) }})
                  </span>
                </el-descriptions-item>
              </el-descriptions>

              <div class="proof-images-section" v-if="props.row.application.proofImages">
                <h4 class="detail-title">📸 证明附件</h4>
                <div class="image-list">
                  <el-image
                      v-for="(img, index) in props.row.application.proofImages.split(',')"
                      :key="index"
                      :src="img"
                      :preview-src-list="props.row.application.proofImages.split(',')"
                      class="proof-img"
                      fit="cover"
                      hide-on-click-modal />
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="申请时间" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.application.applyTime) }}
          </template>
        </el-table-column>

        <!-- 申请人：昵称（真实姓名） -->
        <el-table-column label="申请人" width="200">
          <template #default="scope">
            <div class="user-info-cell" v-if="scope.row.user">
              <div class="user-name-row">
                <strong>{{ scope.row.user.username }}</strong>
                <span v-if="scope.row.user.realName" class="real-name-tag">{{ scope.row.user.realName }}</span>
              </div>
              <div class="phone-text">
                <el-icon><Phone /></el-icon> {{ scope.row.user.phone }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="意向猫咪" width="160">
          <template #default="scope">
            <div class="cat-info-cell" v-if="scope.row.cat">
              <el-avatar shape="square" :size="40" :src="scope.row.cat.coverImage" />
              <span class="cat-name">{{ scope.row.cat.nickname }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="领养理由摘要" min-width="200">
          <template #default="scope">
            <div class="reason-cell">{{ scope.row.application.reason }}</div>
          </template>
        </el-table-column>

        <el-table-column label="当前状态" width="140">
          <template #default="scope">
            <el-tag v-if="scope.row.application.status === 0" type="info" effect="dark">等待审核</el-tag>
            <el-tag v-else-if="scope.row.application.status === 1" type="warning" effect="dark">待签约</el-tag>
            <el-tag v-else-if="scope.row.application.status === 2" type="danger" effect="dark">已驳回</el-tag>
            <el-tag v-else-if="scope.row.application.status === 3" type="success" effect="dark">✅ 领养完成</el-tag>
            <el-tag v-else-if="scope.row.application.status === 4" type="info" effect="plain">已取消</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="130" fixed="right">
          <template #default="scope">
            <el-button
                v-if="scope.row.application.status === 0"
                type="primary" color="#ffb880" size="small"
                @click="openAuditDialog(scope.row)">
              处理申请
            </el-button>
            <el-button
                v-else
                type="info" size="small" plain
                @click="viewAuditDetail(scope.row)">
              查看回复
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="applyList.length === 0" description="暂时没有待处理的领养申请哦~" />
    </el-card>

    <!-- 审核弹窗 -->
    <el-dialog v-model="dialogVisible" title="📋 资质审核与家访结论" width="500px" class="cute-dialog" destroy-on-close>
      <div v-if="currentApply" class="audit-header">
        <p>
          正在审核
          <strong>{{ currentApply.user?.username }}</strong>
          <span v-if="currentApply.user?.realName" class="header-real-name">（{{ currentApply.user.realName }}）</span>
          对猫咪 <strong>{{ currentApply.cat?.nickname }}</strong> 的领养申请。
        </p>
      </div>

      <el-form :model="auditForm" :rules="rules" ref="auditFormRef" label-width="80px">
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="auditForm.status">
            <el-radio :value="1" style="color: #E6A23C; font-weight: bold;">✅ 资质合格，同意签约</el-radio>
            <el-radio :value="2" style="color: #F56C6C; font-weight: bold;">❌ 资质不符，遗憾驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="家访/意见" prop="reviewRemark">
          <el-input
              type="textarea" :rows="4"
              v-model="auditForm.reviewRemark"
              placeholder="请填写详细的审核意见..." />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false" round>取 消</el-button>
        <el-button
            v-if="currentApply?.application?.status === 0"
            type="primary" color="#ffb880"
            @click="submitAudit" :loading="submitLoading" round>
          确认提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Phone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const userInfo   = ref({})
const applyList  = ref([])
const loading    = ref(false)

const dialogVisible  = ref(false)
const submitLoading  = ref(false)
const auditFormRef   = ref(null)
const currentApply   = ref(null)
const auditForm      = ref({ status: 1, reviewRemark: '' })

const rules = {
  status:       [{ required: true, message: '请选择审核结果', trigger: 'change' }],
  reviewRemark: [{ required: true, message: '请填写审核意见', trigger: 'blur' }]
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) userInfo.value = JSON.parse(userStr)
  fetchApplies()
})

const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''

const fetchApplies = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/adopt/list')
    if (res.data.code === 200) applyList.value = res.data.data
  } catch (e) { ElMessage.error('获取申请列表失败') }
  finally { loading.value = false }
}

const openAuditDialog = (row) => {
  currentApply.value = row
  auditForm.value = { status: 1, reviewRemark: '' }
  dialogVisible.value = true
}

const viewAuditDetail = (row) => {
  currentApply.value = row
  auditForm.value = {
    status: row.application.status === 3 ? 1 : row.application.status,
    reviewRemark: row.application.reviewRemark || '暂无详细意见'
  }
  dialogVisible.value = true
}

const submitAudit = async () => {
  if (!auditFormRef.value) return
  await auditFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const res = await axios.post('http://localhost:8080/api/adopt/audit', {
        id:           currentApply.value.application.id,
        catId:        currentApply.value.cat.id,
        status:       auditForm.value.status,
        reviewRemark: auditForm.value.reviewRemark,
        reviewerId:   userInfo.value.id
      })
      if (res.data.code === 200) {
        ElMessage.success('审核处理成功！')
        dialogVisible.value = false
        fetchApplies()
      } else { ElMessage.error(res.data.message) }
    } catch (e) { ElMessage.error('网络请求失败') }
    finally { submitLoading.value = false }
  })
}
</script>

<style scoped>
.approve-manage-container { padding-bottom: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { color: #555; font-size: 24px; margin: 0; }
.table-card { border-radius: 16px; border: none; box-shadow: 0 4px 16px rgba(0,0,0,0.05); }

.expand-detail { padding: 20px 40px; background-color: #fafbfc; border-radius: 8px; margin: 10px 20px; border: 1px dashed #e4e7ed; }
.detail-title { color: #a45a1e; margin-top: 0; margin-bottom: 15px; font-size: 16px; }
.custom-desc { margin-bottom: 20px; }
.signature-text { font-family: "楷体", "KaiTi", serif; font-size: 20px; font-weight: bold; color: #333; }
.proof-images-section { margin-top: 25px; }
.image-list { display: flex; gap: 15px; flex-wrap: wrap; }
.proof-img { width: 120px; height: 120px; border-radius: 8px; border: 1px solid #dcdfe6; cursor: pointer; transition: transform 0.3s; }
.proof-img:hover { transform: scale(1.05); }

/* 申请人列 */
.user-info-cell { display: flex; flex-direction: column; gap: 3px; }
.user-name-row { display: flex; align-items: center; gap: 6px; }
.real-name-tag { font-size: 11px; color: #aaa; background: #f5f5f5; padding: 1px 7px; border-radius: 10px; }
.phone-text { font-size: 12px; color: #888; display: flex; align-items: center; gap: 4px; }

.cat-info-cell { display: flex; align-items: center; gap: 10px; }
.cat-name { font-weight: bold; color: #a45a1e; }
.reason-cell { font-size: 13px; color: #666; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-height: 1.5; }

.audit-header { background-color: #fdf6ec; padding: 15px; border-radius: 8px; margin-bottom: 20px; color: #e6a23c; font-size: 14px; }
.audit-header p { margin: 0; }
.header-real-name { color: #c47633; font-size: 13px; }

:deep(.cute-dialog) { border-radius: 16px; overflow: hidden; }
:deep(.el-dialog__header) { background-color: #fff9e6; padding: 20px; margin-right: 0; }
:deep(.el-dialog__title) { color: #a45a1e; font-weight: bold; }
</style>