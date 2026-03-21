<template>
  <div class="pet-manage-container">
    <div class="toolbar">
      <h2 class="page-title">🐱 宠物档案总览</h2>
      <div class="toolbar-right">
        <el-input v-model="searchKeyword" placeholder="搜索猫咪名称/品种" clearable prefix-icon="Search" style="width: 220px;" />
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 130px; margin-left: 10px;">
          <el-option label="待领养" :value="0" />
          <el-option label="已被申请" :value="1" />
          <el-option label="已领养" :value="2" />
        </el-select>
      </div>
    </div>

    <el-table :data="filteredList" v-loading="loading" style="width: 100%" stripe>

      <el-table-column label="猫咪" width="200">
        <template #default="scope">
          <div class="cat-cell">
            <el-avatar shape="square" :size="48" :src="scope.row.coverImage" />
            <div>
              <div class="cat-name">{{ scope.row.nickname }}</div>
              <div class="cat-breed">{{ scope.row.breed || '田园猫' }} · {{ scope.row.age || '未知' }}</div>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="性别" width="80">
        <template #default="scope">
          <el-icon v-if="scope.row.gender === 1" color="#409EFF" :size="18"><Male /></el-icon>
          <el-icon v-else-if="scope.row.gender === 2" color="#F56C6C" :size="18"><Female /></el-icon>
          <span v-else style="color:#bbb;">—</span>
        </template>
      </el-table-column>

      <el-table-column label="健康状况" min-width="140">
        <template #default="scope">
          <span style="color: #666; font-size: 13px;">{{ scope.row.healthStatus || '—' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="领养状态" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="success" effect="light" round>待领养</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="warning" effect="light" round>已被申请</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="danger" effect="light" round>已领养</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="建档时间" width="150">
        <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
      </el-table-column>

      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="openDetail(scope.row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="filteredList.length === 0 && !loading" description="暂无猫咪数据" />

    <!-- 猫咪详情抽屉 -->
    <el-drawer v-model="detailVisible" size="50%" :with-header="false" destroy-on-close>
      <div v-if="currentCat" class="drawer-wrap">

        <!-- 头部基本信息 -->
        <div class="drawer-header">
          <el-carousel v-if="catPhotos.length > 0" trigger="click" height="220px" class="cat-carousel">
            <el-carousel-item v-for="(img, i) in catPhotos" :key="i">
              <img :src="img" class="carousel-img" />
            </el-carousel-item>
          </el-carousel>
          <div class="cat-basic-info">
            <div class="drawer-cat-name">
              {{ currentCat.nickname }}
              <el-tag
                  :type="currentCat.status === 0 ? 'success' : currentCat.status === 1 ? 'warning' : 'danger'"
                  effect="dark" round size="small" style="margin-left: 8px;">
                {{ ['待领养','已被申请','已领养'][currentCat.status] }}
              </el-tag>
            </div>
            <el-descriptions :column="2" size="small" style="margin-top: 12px;">
              <el-descriptions-item label="品种">{{ currentCat.breed || '—' }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ currentCat.age || '—' }}</el-descriptions-item>
              <el-descriptions-item label="健康状况">{{ currentCat.healthStatus || '—' }}</el-descriptions-item>
              <el-descriptions-item label="建档时间">{{ formatTime(currentCat.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="性格描述" :span="2">{{ currentCat.description || '—' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <el-tabs v-model="drawerTab" style="margin-top: 16px;">

          <!-- 领养人 & 回访记录 -->
          <el-tab-pane name="followup">
            <template #label>🏠 领养信息</template>
            <div v-if="adoptionInfo">
              <!-- 领养人信息 -->
              <div class="adopter-info-card">
                <el-avatar :size="44" :src="adoptionInfo.user?.avatar || defaultAvatar" />
                <div>
                  <div class="adopter-name">
                    {{ adoptionInfo.user?.username }}
                    <span v-if="adoptionInfo.user?.realName" class="real-name-tag">{{ adoptionInfo.user.realName }}</span>
                  </div>
                  <div class="adopter-phone">{{ adoptionInfo.user?.phone }}</div>
                </div>
                <el-tag type="success" effect="dark" size="small" style="margin-left: auto;">已领养</el-tag>
              </div>

              <el-descriptions :column="1" border size="small" style="margin: 12px 0;">
                <el-descriptions-item label="签约时间">{{ formatTime(adoptionInfo.application?.signTime) }}</el-descriptions-item>
                <el-descriptions-item label="领养理由">{{ adoptionInfo.application?.reason }}</el-descriptions-item>
                <el-descriptions-item label="居住环境">{{ adoptionInfo.application?.housingCondition || '—' }}</el-descriptions-item>
                <el-descriptions-item label="饲养计划">{{ adoptionInfo.application?.feedingPlan || '—' }}</el-descriptions-item>
              </el-descriptions>

              <!-- 回访记录 -->
              <div class="section-title">回访记录</div>
              <el-empty v-if="followUpList.length === 0" description="暂无回访记录" :image-size="60" />
              <el-timeline v-else>
                <el-timeline-item
                    v-for="item in followUpList" :key="item.id"
                    :timestamp="formatTime(item.createTime)" placement="top"
                    :type="item.submitType === 0 ? 'success' : 'warning'">
                  <el-card shadow="never" class="timeline-card">
                    <el-tag :type="item.submitType === 0 ? 'success' : 'warning'" size="small">
                      {{ item.submitType === 0 ? '本人提交' : '救助员代录' }}
                    </el-tag>
                    <p class="tl-content">{{ item.healthStatus }}</p>
                    <p v-if="item.remark" class="tl-remark">{{ item.remark }}</p>
                    <div v-if="item.photoUrls" class="tl-photos">
                      <el-image v-for="(img, i) in item.photoUrls.split(',')" :key="i"
                                :src="img" :preview-src-list="item.photoUrls.split(',')"
                                fit="cover" class="tl-photo" hide-on-click-modal />
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
            <el-empty v-else description="该猫咪尚未被领养" :image-size="80" />
          </el-tab-pane>

          <!-- 成长动态履历 -->
          <el-tab-pane name="growth">
            <template #label>📸 成长动态</template>
            <el-empty v-if="growthList.length === 0" description="暂无成长动态记录" :image-size="80" />
            <el-timeline v-else>
              <el-timeline-item
                  v-for="item in growthList" :key="item.id"
                  :timestamp="formatTime(item.createTime)" placement="top"
                  :type="getGrowthType(item.recordType)">
                <el-card shadow="never" class="timeline-card">
                  <div class="growth-type-tag">
                    <el-tag size="small" :type="getGrowthType(item.recordType)">{{ item.recordType }}</el-tag>
                  </div>
                  <p class="tl-content">{{ item.content }}</p>
                  <div v-if="item.mediaUrls" class="tl-photos">
                    <el-image v-for="(img, i) in item.mediaUrls.split(',')" :key="i"
                              :src="img" :preview-src-list="item.mediaUrls.split(',')"
                              fit="cover" class="tl-photo" hide-on-click-modal />
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>

        </el-tabs>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Male, Female } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const loading       = ref(false)
const catList       = ref([])
const searchKeyword = ref('')
const filterStatus  = ref(null)

const detailVisible = ref(false)
const currentCat    = ref(null)
const drawerTab     = ref('followup')
const catPhotos     = ref([])
const adoptionInfo  = ref(null)   // 已领养信息（包含 application + user）
const followUpList  = ref([])
const growthList    = ref([])

const filteredList = computed(() => catList.value.filter(cat => {
  const matchKw     = !searchKeyword.value || cat.nickname?.includes(searchKeyword.value) || cat.breed?.includes(searchKeyword.value)
  const matchStatus = filterStatus.value === null || filterStatus.value === '' || cat.status === filterStatus.value
  return matchKw && matchStatus
}))

onMounted(() => fetchCats())

const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : '—'
const getGrowthType = (type) => {
  if (!type) return 'primary'
  if (type.includes('疫苗') || type.includes('就医')) return 'danger'
  if (type.includes('日常')) return 'success'
  if (type.includes('训练')) return 'warning'
  return 'primary'
}

const fetchCats = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/cat/list')
    if (res.data.code === 200) catList.value = res.data.data
  } catch (e) { ElMessage.error('获取猫咪列表失败') }
  finally { loading.value = false }
}

const openDetail = async (cat) => {
  currentCat.value = cat
  drawerTab.value  = 'followup'
  adoptionInfo.value  = null
  followUpList.value  = []
  growthList.value    = []

  // 解析相册
  catPhotos.value = cat.photoUrls
      ? cat.photoUrls.split(',')
      : (cat.coverImage ? [cat.coverImage] : [])

  detailVisible.value = true

  // 并行拉取数据
  await Promise.all([
    fetchAdoptionInfo(cat.id),
    fetchGrowthRecords(cat.id),
  ])
}

// 查找该猫咪的已完成领养申请（status=3）
const fetchAdoptionInfo = async (catId) => {
  try {
    // 复用救助员的回访列表接口，找该猫咪的记录
    const res = await axios.get('http://localhost:8080/api/followup/adoptedList')
    if (res.data.code === 200) {
      const found = res.data.data.find(item => item.cat?.id === catId)
      if (found) {
        adoptionInfo.value = found
        // 再拉回访记录
        const r2 = await axios.get(`http://localhost:8080/api/followup/list/${found.application.id}`)
        if (r2.data.code === 200) followUpList.value = r2.data.data
      }
    }
  } catch (e) { /* 静默 */ }
}

const fetchGrowthRecords = async (catId) => {
  try {
    const res = await axios.get(`http://localhost:8080/api/record/list/${catId}`)
    if (res.data.code === 200) growthList.value = res.data.data
  } catch (e) { /* 静默 */ }
}
</script>

<style scoped>
.pet-manage-container { padding-bottom: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.toolbar-right { display: flex; align-items: center; }
.page-title { color: #555; font-size: 24px; margin: 0; }

.cat-cell { display: flex; align-items: center; gap: 12px; }
.cat-name { font-weight: bold; color: #333; font-size: 14px; }
.cat-breed { font-size: 12px; color: #999; margin-top: 2px; }

/* 抽屉 */
.drawer-wrap { padding: 20px; height: 100%; overflow-y: auto; }
.cat-carousel { border-radius: 12px; overflow: hidden; margin-bottom: 16px; }
.carousel-img { width: 100%; height: 100%; object-fit: cover; }
.cat-basic-info { }
.drawer-cat-name { font-size: 22px; font-weight: bold; color: #333; display: flex; align-items: center; }
.section-title { font-size: 15px; font-weight: bold; color: #a45a1e; margin: 16px 0 10px; }

/* 领养人信息卡 */
.adopter-info-card { display: flex; align-items: center; gap: 12px; background: #f7f8fa; border-radius: 12px; padding: 14px; margin-bottom: 4px; }
.adopter-name { font-weight: bold; color: #333; font-size: 15px; display: flex; align-items: center; gap: 6px; }
.real-name-tag { font-size: 11px; color: #aaa; background: #eee; padding: 1px 8px; border-radius: 10px; font-weight: normal; }
.adopter-phone { font-size: 12px; color: #999; margin-top: 3px; }

/* 时间线 */
.timeline-card { border-radius: 10px; border: none; background: #fafafa; }
.tl-content { margin: 6px 0; color: #444; font-size: 14px; line-height: 1.6; }
.tl-remark { margin: 0 0 8px; color: #888; font-size: 13px; }
.tl-photos { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
.tl-photo { width: 80px; height: 80px; border-radius: 8px; cursor: pointer; }
.growth-type-tag { margin-bottom: 8px; }
</style>