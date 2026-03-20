<template>
  <div class="adopter-layout">
    <el-container>
      <el-header class="header">
        <div class="logo">🐱 Hajimi 领养平台</div>
        <el-menu mode="horizontal" :ellipsis="false" default-active="1" class="top-menu">
          <el-menu-item index="1">喵星人列表</el-menu-item>
          <el-menu-item index="2">我的领养记录</el-menu-item>
          <el-menu-item index="3">救助社区</el-menu-item>
        </el-menu>
        <div class="user-info">
          <el-dropdown>
            <span class="dropdown-link">
              <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" style="margin-right: 8px;" />
              {{ userInfo.username || '铲屎官' }} <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <div class="welcome-banner">
          <div class="banner-text">
            <h2>给流浪的它，一个温暖的家 🏡</h2>
            <p>领养代替购买，让每一份爱都有归属。在这里，许多可爱的小生命正在等待与你相遇。</p>
          </div>
        </div>

        <h3 class="section-title">✨ 等待新家的毛孩子们</h3>
        <el-row :gutter="24">
          <el-col :span="6" v-for="cat in catList" :key="cat.id" style="margin-bottom: 30px;">
            <el-card class="cat-card" :body-style="{ padding: '0px' }" shadow="hover" @click="openCatDetail(cat)">
              <div class="cat-image-wrapper">
                <img :src="cat.coverImage || 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'" class="cat-image" />
                <div class="status-badge" :class="cat.status === 0 ? 'status-free' : 'status-taken'">
                  {{ cat.status === 0 ? '寻找铲屎官中' : '已被预定' }}
                </div>
              </div>
              <div class="cat-info">
                <div class="cat-header">
                  <span class="cat-name">{{ cat.nickname }}</span>
                  <el-icon :color="cat.gender === 1 ? '#409EFF' : '#F56C6C'"><component :is="cat.gender === 1 ? 'Male' : 'Female'" /></el-icon>
                </div>
                <div class="cat-tags">
                  <el-tag size="small" type="warning" effect="light" round>{{ cat.breed || '田园猫' }}</el-tag>
                  <el-tag size="small" type="success" effect="light" round>{{ cat.age || '未知' }}</el-tag>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="catList.length === 0" description="暂时没有猫咪待领养哦~" />
      </el-main>
    </el-container>

    <el-drawer v-model="drawerVisible" size="50%" :with-header="false" class="cat-drawer">
      <div v-if="currentCat" class="drawer-content">
        <el-carousel trigger="click" height="400px" class="cat-carousel" border-radius="16px">
          <el-carousel-item v-for="(img, index) in getCatPhotos(currentCat)" :key="index">
            <img :src="img" class="carousel-img" />
          </el-carousel-item>
        </el-carousel>

        <div class="detail-header">
          <h2>{{ currentCat.nickname }}</h2>
          <el-tag v-if="currentCat.status === 0" type="success" effect="dark" round>待领养</el-tag>
          <el-tag v-else type="danger" effect="dark" round>已被申请</el-tag>
        </div>

        <el-descriptions :column="2" border class="cat-descriptions">
          <el-descriptions-item label="品种">{{ currentCat.breed }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ currentCat.age }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentCat.gender === 1 ? '公猫' : '母猫' }}</el-descriptions-item>
          <el-descriptions-item label="健康状况">{{ currentCat.healthStatus || '健康' }}</el-descriptions-item>
          <el-descriptions-item label="性格描述" :span="2">{{ currentCat.description }}</el-descriptions-item>
        </el-descriptions>

        <el-divider border-style="dashed" />

        <div class="apply-section" v-if="currentCat.status === 0">
          <h3>💌 提交领养申请</h3>
          <el-form :model="applyForm" :rules="rules" ref="applyFormRef" label-position="top">
            <el-form-item label="为什么想领养它？ (领养理由)" prop="reason">
              <el-input type="textarea" :rows="3" v-model="applyForm.reason" placeholder="请真诚地表达您的领养意愿，这有助于救助员更快通过审核哦~" />
            </el-form-item>
            <el-form-item label="您的养宠经验及未来规划" prop="experience">
              <el-input type="textarea" :rows="3" v-model="applyForm.experience" placeholder="比如：以前养过猫/狗，家里已经封窗，能接受科学喂养、定期回访等" />
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Male, Female } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const userInfo = ref({})
const catList = ref([])

// 详情抽屉相关
const drawerVisible = ref(false)
const currentCat = ref(null)

// 申请表单相关
const applyFormRef = ref(null)
const submitLoading = ref(false)
const applyForm = ref({ reason: '', experience: '' })

const rules = {
  reason: [{ required: true, message: '请填写领养理由哦', trigger: 'blur' }],
  experience: [{ required: true, message: '请简单描述一下养宠经验', trigger: 'blur' }]
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    userInfo.value = JSON.parse(userStr)
  } else {
    // 没登录踢回登录页
    router.push('/login')
  }
  fetchCats()
})

const fetchCats = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/cat/list')
    if (res.data.code === 200) {
      catList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取猫咪列表失败')
  }
}

// 解析多图相册
const getCatPhotos = (cat) => {
  if (cat.photoUrls) {
    return cat.photoUrls.split(',')
  } else if (cat.coverImage) {
    return [cat.coverImage]
  }
  return ['https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png']
}

// 打开猫咪详情
const openCatDetail = (cat) => {
  currentCat.value = cat
  applyForm.value = { reason: '', experience: '' } // 重置表单
  drawerVisible.value = true
}

// 提交领养申请
const submitApply = async () => {
  if (!applyFormRef.value) return
  await applyFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const payload = {
          userId: userInfo.value.id,
          catId: currentCat.value.id,
          reason: applyForm.value.reason,
          experience: applyForm.value.experience
        }
        const res = await axios.post('http://localhost:8080/api/adopt/apply', payload)
        if (res.data.code === 200) {
          ElMessage.success('申请提交成功！请耐心等待救助员审核哦~')
          drawerVisible.value = false
          fetchCats() // 刷新列表，猫咪状态会变成已被申请
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (error) {
        ElMessage.error('网络请求失败')
      } finally {
        submitLoading.value = false
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
.adopter-layout { min-height: 100vh; background-color: #f7f8fa; }
.header { display: flex; align-items: center; justify-content: space-between; background-color: #fff; box-shadow: 0 2px 12px rgba(0,0,0,0.05); padding: 0 40px; position: sticky; top: 0; z-index: 10; }
.logo { font-size: 24px; font-weight: bold; color: #ff9d4d; }
.top-menu { flex: 1; margin: 0 40px; border-bottom: none; }
.dropdown-link { cursor: pointer; color: #555; font-weight: bold; display: flex; align-items: center; outline: none; }
.main-content { padding: 40px auto; max-width: 1200px; margin: 0 auto; width: 100%; }

/* 横幅样式 */
.welcome-banner { margin-top: 20px; height: 200px; background: linear-gradient(135deg, #ffeedb, #ffb880); border-radius: 24px; display: flex; align-items: center; padding: 0 60px; position: relative; overflow: hidden; box-shadow: 0 10px 30px rgba(255, 184, 128, 0.2); }
.banner-text { position: relative; z-index: 1; }
.banner-text h2 { margin: 0 0 10px 0; font-size: 32px; color: #a45a1e; }
.banner-text p { margin: 0; font-size: 16px; color: #c47633; max-width: 600px; line-height: 1.6; }

.section-title { margin: 40px 0 20px 0; color: #333; font-size: 22px; }

/* 猫咪卡片 */
.cat-card { border-radius: 16px; border: none; overflow: hidden; transition: all 0.3s; cursor: pointer; }
.cat-card:hover { transform: translateY(-8px); box-shadow: 0 15px 30px rgba(0,0,0,0.1) !important; }
.cat-image-wrapper { position: relative; height: 240px; width: 100%; overflow: hidden; }
.cat-image { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
.cat-card:hover .cat-image { transform: scale(1.08); }
.status-badge { position: absolute; top: 12px; right: 12px; padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: bold; color: white; backdrop-filter: blur(8px); box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
.status-free { background-color: rgba(103, 194, 58, 0.9); }
.status-taken { background-color: rgba(245, 108, 108, 0.9); }
.cat-info { padding: 20px; }
.cat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.cat-name { font-size: 20px; font-weight: bold; color: #333; }
.cat-tags { display: flex; gap: 8px; }

/* 详情抽屉样式 */
.drawer-content { padding: 20px 40px; height: 100%; overflow-y: auto; }
.cat-carousel { border-radius: 16px; overflow: hidden; margin-bottom: 30px; box-shadow: 0 10px 20px rgba(0,0,0,0.1); }
.carousel-img { width: 100%; height: 100%; object-fit: cover; }
.detail-header { display: flex; align-items: center; gap: 15px; margin-bottom: 20px; }
.detail-header h2 { margin: 0; font-size: 28px; color: #333; }
.cat-descriptions { margin-bottom: 30px; }

.apply-section { background-color: #fff9e6; padding: 30px; border-radius: 16px; border: 1px solid #ffeedb; }
.apply-section h3 { margin-top: 0; color: #a45a1e; margin-bottom: 20px; }
.submit-btn { width: 100%; margin-top: 10px; font-size: 16px; padding: 24px 0; font-weight: bold; }
.taken-notice { margin-top: 40px; padding: 40px; background-color: #fdf6ec; border-radius: 16px; }

/* 美化全局滚动条 */
::-webkit-scrollbar { width: 8px; }
::-webkit-scrollbar-thumb { background: #e0e0e0; border-radius: 4px; }
::-webkit-scrollbar-thumb:hover { background: #ffb880; }
</style>