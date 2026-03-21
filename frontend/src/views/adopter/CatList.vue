<template>
  <div style="padding: 40px 0;">
    <div class="welcome-banner">
      <div class="banner-text">
        <h2>给流浪的它，一个温暖的家 🏡</h2>
        <p>科学喂养，适龄绝育，有病就医，不离不弃。</p>
      </div>
    </div>

    <div class="filter-bar">
      <h3 style="margin: 0;">✨ 寻找有缘喵</h3>
      <div class="filter-inputs">
        <el-input v-model="filterForm.breed" placeholder="搜索品种" clearable style="width: 200px;" prefix-icon="Search" />
        <el-select v-model="filterForm.gender" placeholder="性别" clearable style="width: 120px; margin-left: 10px;">
          <el-option label="小王子" :value="1" />
          <el-option label="小公主" :value="2" />
        </el-select>
        <el-select v-model="filterForm.status" placeholder="状态" clearable style="width: 120px; margin-left: 10px;">
          <el-option label="待领养" :value="0" />
          <el-option label="已被预定" :value="1" />
        </el-select>
      </div>
    </div>

    <el-row :gutter="24">
      <el-col :span="6" v-for="cat in filteredCats" :key="cat.id" style="margin-bottom: 30px;">
        <el-card class="cat-card" :body-style="{ padding: '0px' }" shadow="hover" @click="openCatDetail(cat)">
          <div class="cat-image-wrapper">
            <img :src="cat.coverImage || 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'" class="cat-image" />
            <div class="status-badge" :class="cat.status === 0 ? 'status-free' : (cat.status === 1 ? 'status-taken' : 'status-done')">
              {{ cat.status === 0 ? '寻找铲屎官中' : (cat.status === 1 ? '已被预定' : '已领养') }}
            </div>
          </div>
          <div class="cat-info">
            <div class="cat-header">
              <span class="cat-name">{{ cat.nickname }}</span>
              <el-icon :color="cat.gender === 1 ? '#409EFF' : '#F56C6C'">
                <component :is="cat.gender === 1 ? 'Male' : 'Female'" />
              </el-icon>
            </div>
            <div class="cat-tags">
              <el-tag size="small" type="warning" effect="light" round>{{ cat.breed || '田园猫' }}</el-tag>
              <el-tag size="small" type="success" effect="light" round>{{ cat.age || '未知' }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="filteredCats.length === 0" description="没有找到符合条件的猫咪哦~" />

    <!-- 猫咪详情抽屉 -->
    <el-drawer v-model="drawerVisible" size="55%" :with-header="false">
      <div v-if="currentCat" class="drawer-content">
        <el-carousel trigger="click" height="350px" class="cat-carousel">
          <el-carousel-item v-for="(img, index) in getCatPhotos(currentCat)" :key="index">
            <img :src="img" class="carousel-img" />
          </el-carousel-item>
        </el-carousel>

        <div class="detail-header">
          <h2>{{ currentCat.nickname }}</h2>
          <div style="display: flex; align-items: center; gap: 10px;">
            <el-tag v-if="currentCat.status === 0" type="success" effect="dark" round>待领养</el-tag>
            <el-tag v-else type="danger" effect="dark" round>已被申请/领养</el-tag>
            <!-- 收藏按钮 -->
            <el-button
                :type="isFavored ? 'warning' : 'default'"
                :icon="isFavored ? 'StarFilled' : 'Star'"
                circle
                @click.stop="toggleFavorite"
                :title="isFavored ? '取消收藏' : '收藏这只猫'" />
          </div>
        </div>

        <el-descriptions :column="2" border class="cat-descriptions">
          <el-descriptions-item label="品种">{{ currentCat.breed }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ currentCat.age }}</el-descriptions-item>
          <el-descriptions-item label="健康状况">{{ currentCat.healthStatus }}</el-descriptions-item>
          <el-descriptions-item label="性格描述" :span="2">{{ currentCat.description }}</el-descriptions-item>
        </el-descriptions>

        <div class="apply-section" v-if="currentCat.status === 0">
          <h3>💌 提交领养申请</h3>
          <el-form :model="applyForm" :rules="rules" ref="applyFormRef" label-position="top">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="领养理由" prop="reason">
                  <el-input type="textarea" :rows="3" v-model="applyForm.reason" placeholder="简单说说为什么想带它回家" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="养宠经验">
                  <el-input type="textarea" :rows="3" v-model="applyForm.experience" placeholder="以前有养过小动物吗？" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="居住环境" prop="housingCondition">
                  <el-input v-model="applyForm.housingCondition" placeholder="如: 自有住房，已安装金刚网纱窗" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="饲养计划">
                  <el-input v-model="applyForm.feedingPlan" placeholder="打算喂什么猫粮？是否接受回访？" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="上传证明附件（最多3张）">
              <el-upload
                  action="http://localhost:8080/api/file/upload"
                  list-type="picture-card"
                  v-model:file-list="proofFileList"
                  :on-success="handleUploadSuccess"
                  :before-upload="beforeUpload"
                  :limit="3">
                <el-icon><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-button type="primary" color="#ff9d4d" class="submit-btn" size="large" @click="submitApply" :loading="submitLoading" round>
              我准备好了，申请带它回家！
            </el-button>
          </el-form>
        </div>

        <div v-else class="taken-notice">
          <el-result icon="info" title="晚了一步" sub-title="这只小可爱已经被别人看中啦，去看看其他喵星人吧~" />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Male, Female, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const userInfo = ref({})
const catList = ref([])
const filterForm = ref({ breed: '', gender: null, status: null })

const drawerVisible = ref(false)
const currentCat = ref(null)
const isFavored = ref(false)
const applyFormRef = ref(null)
const submitLoading = ref(false)
const proofFileList = ref([])
const applyForm = ref({ reason: '', experience: '', housingCondition: '', feedingPlan: '', proofImages: '' })

const rules = {
  reason: [{ required: true, message: '请填写领养理由', trigger: 'blur' }],
  housingCondition: [{ required: true, message: '请填写居住情况', trigger: 'blur' }],
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
  } catch (error) { ElMessage.error('获取列表失败') }
}

const filteredCats = computed(() => {
  return catList.value.filter(cat => {
    const matchBreed = !filterForm.value.breed || cat.breed?.includes(filterForm.value.breed)
    const matchGender = !filterForm.value.gender || cat.gender === filterForm.value.gender
    const matchStatus = filterForm.value.status === null || filterForm.value.status === '' || cat.status === filterForm.value.status
    return matchBreed && matchGender && matchStatus
  })
})

const getCatPhotos = (cat) => cat.photoUrls
    ? cat.photoUrls.split(',')
    : (cat.coverImage ? [cat.coverImage] : ['https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'])

const openCatDetail = async (cat) => {
  currentCat.value = cat
  isFavored.value = false
  applyForm.value = { reason: '', experience: '', housingCondition: '', feedingPlan: '', proofImages: '' }
  proofFileList.value = []
  drawerVisible.value = true
  // 检查是否已收藏
  if (userInfo.value.id) {
    try {
      const res = await axios.get('http://localhost:8080/api/favorite/check', {
        params: { userId: userInfo.value.id, catId: cat.id }
      })
      if (res.data.code === 200) isFavored.value = res.data.data
    } catch (e) { /* 静默 */ }
  }
}

// 收藏 / 取消收藏
const toggleFavorite = async () => {
  try {
    const res = await axios.post('http://localhost:8080/api/favorite/toggle', null, {
      params: { userId: userInfo.value.id, catId: currentCat.value.id }
    })
    if (res.data.code === 200) {
      isFavored.value = res.data.data.favored
      ElMessage.success(isFavored.value ? '收藏成功 ⭐' : '已取消收藏')
    }
  } catch (e) { ElMessage.error('操作失败') }
}

const beforeUpload = (file) => {
  if (file.size / 1024 / 1024 > 5) { ElMessage.error('照片大小不能超过 5MB'); return false }
  return true
}
const handleUploadSuccess = (res, uploadFile, uploadFiles) => {
  if (res.code === 200) { uploadFile.url = res.data; proofFileList.value = uploadFiles }
}

const submitApply = async () => {
  if (!applyFormRef.value) return
  if (proofFileList.value.length > 0) {
    applyForm.value.proofImages = proofFileList.value
        .map(f => f.url || (f.response && f.response.data)).join(',')
  }
  await applyFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const payload = { userId: userInfo.value.id, catId: currentCat.value.id, ...applyForm.value }
        const res = await axios.post('http://localhost:8080/api/adopt/apply', payload)
        if (res.data.code === 200) {
          ElMessage.success('申请提交成功！请去"我的领养记录"查看进度。')
          drawerVisible.value = false
          fetchCats()
        } else { ElMessage.error(res.data.message) }
      } catch (error) { ElMessage.error('网络请求失败') }
      finally { submitLoading.value = false }
    }
  })
}
</script>

<style scoped>
.welcome-banner { margin-top: 20px; height: 160px; background: linear-gradient(135deg, #ffeedb, #ffb880); border-radius: 24px; display: flex; align-items: center; padding: 0 60px; box-shadow: 0 10px 30px rgba(255,184,128,0.2); }
.banner-text h2 { margin: 0 0 10px 0; font-size: 28px; color: #a45a1e; }
.banner-text p { margin: 0; color: #c47633; }
.filter-bar { margin: 30px 0 20px 0; display: flex; justify-content: space-between; align-items: center; }
.filter-inputs { display: flex; }
.cat-card { border-radius: 16px; border: none; overflow: hidden; transition: all 0.3s; cursor: pointer; }
.cat-card:hover { transform: translateY(-8px); box-shadow: 0 15px 30px rgba(0,0,0,0.1) !important; }
.cat-image-wrapper { position: relative; height: 240px; width: 100%; overflow: hidden; }
.cat-image { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
.cat-card:hover .cat-image { transform: scale(1.08); }
.status-badge { position: absolute; top: 12px; right: 12px; padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: bold; color: white; backdrop-filter: blur(8px); }
.status-free { background-color: rgba(103,194,58,0.9); }
.status-taken { background-color: rgba(230,162,60,0.9); }
.status-done { background-color: rgba(245,108,108,0.9); }
.cat-info { padding: 20px; }
.cat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.cat-name { font-size: 20px; font-weight: bold; color: #333; }
.cat-tags { display: flex; gap: 8px; }
.drawer-content { padding: 20px 40px; height: 100%; overflow-y: auto; }
.cat-carousel { border-radius: 16px; overflow: hidden; margin-bottom: 30px; box-shadow: 0 10px 20px rgba(0,0,0,0.1); }
.carousel-img { width: 100%; height: 100%; object-fit: cover; }
.detail-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.detail-header h2 { margin: 0; font-size: 28px; color: #333; }
.cat-descriptions { margin-bottom: 30px; }
.apply-section { background-color: #fff9e6; padding: 30px; border-radius: 16px; border: 1px solid #ffeedb; }
.apply-section h3 { margin-top: 0; color: #a45a1e; }
.submit-btn { width: 100%; margin-top: 20px; font-size: 16px; padding: 24px 0; font-weight: bold; }
.taken-notice { margin-top: 40px; padding: 40px; background-color: #fdf6ec; border-radius: 16px; }
</style>