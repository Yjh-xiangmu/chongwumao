<template>
  <div class="adopter-layout">
    <el-container>
      <el-header class="header">
        <div class="logo">🐱 Hajimi 领养平台</div>
        <el-menu mode="horizontal" :default-active="activeMenu" @select="handleMenuSelect" class="top-menu">
          <el-menu-item index="1">喵星人列表</el-menu-item>
          <el-menu-item index="2">我的领养记录</el-menu-item>
        </el-menu>
        <div class="user-info">
          <el-dropdown>
            <span class="dropdown-link">
              <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" style="margin-right: 8px;" />
              {{ userInfo.username || '铲屎官' }} <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <div v-if="activeMenu === '1'">
          <div class="welcome-banner">
            <div class="banner-text">
              <h2>给流浪的它，一个温暖的家 🏡</h2>
              <p>科学喂养，适龄绝育，有病就医，不离不弃。</p>
            </div>
          </div>

          <div class="filter-bar">
            <h3 class="section-title" style="margin: 0;">✨ 寻找有缘喵</h3>
            <div class="filter-inputs">
              <el-input v-model="filterForm.breed" placeholder="搜索品种 (如: 橘猫)" clearable style="width: 200px;" prefix-icon="Search" />
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
          <el-empty v-if="filteredCats.length === 0" description="没有找到符合条件的猫咪哦~" />
        </div>

        <div v-if="activeMenu === '2'">
          <h2 class="section-title">🐾 我的领养足迹</h2>
          <el-table :data="myApplies" style="width: 100%" stripe class="custom-table" v-loading="loadingRecords">
            <el-table-column label="申请时间" width="180">
              <template #default="scope">{{ formatTime(scope.row.application.applyTime) }}</template>
            </el-table-column>
            <el-table-column label="猫咪" width="200">
              <template #default="scope">
                <div style="display: flex; align-items: center; gap: 10px;">
                  <el-avatar shape="square" :size="50" :src="scope.row.cat?.coverImage" />
                  <strong style="color: #a45a1e;">{{ scope.row.cat?.nickname }}</strong>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="救助员回复" min-width="250">
              <template #default="scope">
                <span style="color: #888;">{{ scope.row.application.reviewRemark || '等待救助员审核中...' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="进度状态" width="150">
              <template #default="scope">
                <el-tag v-if="scope.row.application.status === 0" type="info" round>等待审核</el-tag>
                <el-tag v-else-if="scope.row.application.status === 1" type="warning" round>审核通过，待签约</el-tag>
                <el-tag v-else-if="scope.row.application.status === 2" type="danger" round>遗憾驳回</el-tag>
                <el-tag v-else-if="scope.row.application.status === 3" type="success" round>签约完成(已领养)</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button v-if="scope.row.application.status === 1" type="primary" color="#ffb880" round @click="openSignDialog(scope.row)">
                  签署领养协议
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-main>
    </el-container>

    <el-drawer v-model="drawerVisible" size="55%" :with-header="false" class="cat-drawer">
      <div v-if="currentCat" class="drawer-content">
        <el-carousel trigger="click" height="350px" class="cat-carousel">
          <el-carousel-item v-for="(img, index) in getCatPhotos(currentCat)" :key="index">
            <img :src="img" class="carousel-img" />
          </el-carousel-item>
        </el-carousel>

        <div class="detail-header">
          <h2>{{ currentCat.nickname }}</h2>
          <el-tag v-if="currentCat.status === 0" type="success" effect="dark" round>待领养</el-tag>
          <el-tag v-else type="danger" effect="dark" round>已被申请/领养</el-tag>
        </div>

        <el-descriptions :column="2" border class="cat-descriptions">
          <el-descriptions-item label="品种">{{ currentCat.breed }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ currentCat.age }}</el-descriptions-item>
          <el-descriptions-item label="性格描述" :span="2">{{ currentCat.description }}</el-descriptions-item>
        </el-descriptions>

        <div class="apply-section" v-if="currentCat.status === 0">
          <h3>💌 提交领养申请 (请如实填写)</h3>
          <el-form :model="applyForm" :rules="rules" ref="applyFormRef" label-position="top">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="领养理由" prop="reason">
                  <el-input type="textarea" :rows="3" v-model="applyForm.reason" placeholder="简单说说为什么想带它回家" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="养宠经验" prop="experience">
                  <el-input type="textarea" :rows="3" v-model="applyForm.experience" placeholder="以前有养过小动物吗？" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="居住环境情况" prop="housingCondition">
                  <el-input v-model="applyForm.housingCondition" placeholder="如: 自有住房/整租/合租，已安装金刚网纱窗" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="未来饲养计划" prop="feedingPlan">
                  <el-input v-model="applyForm.feedingPlan" placeholder="打算喂什么牌子的猫粮？是否接受定期回访？" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="上传证明附件 (室内环境、纱窗特写等，最多3张)" prop="proofImages">
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

    <el-dialog v-model="signDialogVisible" title="📜 签署领养协议" width="600px" destroy-on-close class="cute-dialog">
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
          <el-input v-model="signatureName" placeholder="请在此输入您的真实姓名作为电子签名确认" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="signDialogVisible = false" round>我再考虑一下</el-button>
        <el-button type="primary" color="#ff9d4d" @click="confirmSign" round>确认签署，接它回家！</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Male, Female, Plus, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const userInfo = ref({})
const activeMenu = ref('1')

// 猫咪大厅相关
const catList = ref([])
const filterForm = ref({ breed: '', gender: null, status: null })

// 我的记录相关
const myApplies = ref([])
const loadingRecords = ref(false)

// 详情与申请
const drawerVisible = ref(false)
const currentCat = ref(null)
const applyFormRef = ref(null)
const submitLoading = ref(false)
const proofFileList = ref([])
const applyForm = ref({ reason: '', experience: '', housingCondition: '', feedingPlan: '', proofImages: '' })

// 签约相关
const signDialogVisible = ref(false)
const signTarget = ref(null)
const signatureName = ref('')

const rules = {
  reason: [{ required: true, message: '请填写领养理由', trigger: 'blur' }],
  housingCondition: [{ required: true, message: '请填写居住情况', trigger: 'blur' }],
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    userInfo.value = JSON.parse(userStr)
  } else {
    router.push('/login')
  }
  fetchCats()
})

const formatTime = (timeStr) => {
  return timeStr ? timeStr.replace('T', ' ').substring(0, 16) : ''
}

// 菜单切换
const handleMenuSelect = (index) => {
  activeMenu.value = index
  if (index === '1') fetchCats()
  if (index === '2') fetchMyRecords()
}

// 获取猫咪并实现前端过滤
const fetchCats = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/cat/list')
    if (res.data.code === 200) catList.value = res.data.data
  } catch (error) { ElMessage.error('获取列表失败') }
}

const filteredCats = computed(() => {
  return catList.value.filter(cat => {
    const matchBreed = !filterForm.value.breed || cat.breed.includes(filterForm.value.breed)
    const matchGender = !filterForm.value.gender || cat.gender === filterForm.value.gender
    const matchStatus = filterForm.value.status === null || filterForm.value.status === '' || cat.status === filterForm.value.status
    return matchBreed && matchGender && matchStatus
  })
})

const getCatPhotos = (cat) => cat.photoUrls ? cat.photoUrls.split(',') : (cat.coverImage ? [cat.coverImage] : ['https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'])

const openCatDetail = (cat) => {
  currentCat.value = cat
  applyForm.value = { reason: '', experience: '', housingCondition: '', feedingPlan: '', proofImages: '' }
  proofFileList.value = []
  drawerVisible.value = true
}

// 附件上传
const beforeUpload = (file) => {
  if (file.size / 1024 / 1024 > 5) { ElMessage.error('照片大小不能超过 5MB'); return false }
  return true
}
const handleUploadSuccess = (res, uploadFile, uploadFiles) => {
  if (res.code === 200) { uploadFile.url = res.data; proofFileList.value = uploadFiles }
}

// 提交领养申请
const submitApply = async () => {
  if (!applyFormRef.value) return
  if (proofFileList.value.length > 0) {
    const urls = proofFileList.value.map(f => f.url || (f.response && f.response.data))
    applyForm.value.proofImages = urls.join(',')
  }

  await applyFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const payload = {
          userId: userInfo.value.id,
          catId: currentCat.value.id,
          ...applyForm.value
        }
        const res = await axios.post('http://localhost:8080/api/adopt/apply', payload)
        if (res.data.code === 200) {
          ElMessage.success('申请提交成功！请去"我的领养记录"查看进度。')
          drawerVisible.value = false
          fetchCats()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (error) { ElMessage.error('网络请求失败') }
      finally { submitLoading.value = false }
    }
  })
}

// 获取我的记录
const fetchMyRecords = async () => {
  loadingRecords.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/adopt/myList/${userInfo.value.id}`)
    if (res.data.code === 200) myApplies.value = res.data.data
  } catch (error) { ElMessage.error('获取记录失败') }
  finally { loadingRecords.value = false }
}

// 签约流程
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
      fetchMyRecords() // 刷新记录表
    } else { ElMessage.error(res.data.message) }
  } catch (error) { ElMessage.error('网络错误') }
}

const handleLogout = () => { localStorage.removeItem('user'); router.push('/login') }
</script>

<style scoped>
.adopter-layout { min-height: 100vh; background-color: #f7f8fa; }
.header { display: flex; align-items: center; justify-content: space-between; background-color: #fff; box-shadow: 0 2px 12px rgba(0,0,0,0.05); padding: 0 40px; position: sticky; top: 0; z-index: 10; }
.logo { font-size: 24px; font-weight: bold; color: #ff9d4d; }
.top-menu { flex: 1; margin: 0 40px; border-bottom: none; }
.dropdown-link { cursor: pointer; color: #555; font-weight: bold; display: flex; align-items: center; outline: none; }
.main-content { padding: 40px auto; max-width: 1200px; margin: 0 auto; width: 100%; }

.welcome-banner { margin-top: 20px; height: 160px; background: linear-gradient(135deg, #ffeedb, #ffb880); border-radius: 24px; display: flex; align-items: center; padding: 0 60px; box-shadow: 0 10px 30px rgba(255, 184, 128, 0.2); }
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
.status-free { background-color: rgba(103, 194, 58, 0.9); }
.status-taken { background-color: rgba(230, 162, 60, 0.9); }
.status-done { background-color: rgba(245, 108, 108, 0.9); }

.cat-info { padding: 20px; }
.cat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.cat-name { font-size: 20px; font-weight: bold; color: #333; }
.cat-tags { display: flex; gap: 8px; }

.drawer-content { padding: 20px 40px; height: 100%; overflow-y: auto; }
.cat-carousel { border-radius: 16px; overflow: hidden; margin-bottom: 30px; box-shadow: 0 10px 20px rgba(0,0,0,0.1); }
.carousel-img { width: 100%; height: 100%; object-fit: cover; }
.detail-header { display: flex; align-items: center; gap: 15px; margin-bottom: 20px; }
.detail-header h2 { margin: 0; font-size: 28px; color: #333; }
.cat-descriptions { margin-bottom: 30px; }

.apply-section { background-color: #fff9e6; padding: 30px; border-radius: 16px; border: 1px solid #ffeedb; }
.apply-section h3 { margin-top: 0; color: #a45a1e; margin-bottom: 20px; }
.submit-btn { width: 100%; margin-top: 20px; font-size: 16px; padding: 24px 0; font-weight: bold; }
.taken-notice { margin-top: 40px; padding: 40px; background-color: #fdf6ec; border-radius: 16px; }

.custom-table { border-radius: 16px; overflow: hidden; box-shadow: 0 4px 16px rgba(0,0,0,0.05); }

/* 协议文本框样式 */
.agreement-box { background-color: #f7f8fa; padding: 20px; border-radius: 8px; font-size: 14px; line-height: 1.8; color: #555; max-height: 300px; overflow-y: auto; }
.agreement-box ol { padding-left: 20px; }
</style>