<template>
  <div class="backend-layout">
    <el-container class="layout-container">
      <el-aside width="220px" class="aside">
        <div class="logo">🐱 Hajimi 管理后台</div>
        <el-menu
            :default-active="activeIndex"
            class="side-menu"
            background-color="#2c3e50"
            text-color="#ecf0f1"
            active-text-color="#ff9d4d"
            @select="handleMenuSelect">
          <el-menu-item index="1"><el-icon><DataLine /></el-icon><span>数据大盘</span></el-menu-item>
          <el-menu-item index="2"><el-icon><User /></el-icon><span>领养人管理</span></el-menu-item>
          <el-menu-item index="3"><el-icon><Avatar /></el-icon><span>救助员管理</span></el-menu-item>
          <el-menu-item index="4"><el-icon><Setting /></el-icon><span>系统设置</span></el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="header-breadcrumb">{{ currentTitle }}</div>
          <el-dropdown>
            <span class="dropdown-link">
              超级管理员 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-header>

        <el-main class="main-content">
          <el-card v-if="activeIndex === '1'" shadow="hover" class="welcome-card">
            <h3>欢迎来到系统管理后台，今天也是充满爱心的一天！</h3>
          </el-card>

          <el-card v-if="activeIndex === '3'" shadow="never" class="table-card">
            <div class="toolbar">
              <span class="table-title">救助团队名单</span>
              <el-button type="primary" color="#ff9d4d" icon="Plus" @click="openAddDialog">添加救助员</el-button>
            </div>

            <el-table :data="volunteerList" v-loading="loading" style="width: 100%" stripe>
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="姓名/昵称" width="150" />
              <el-table-column prop="phone" label="手机号" width="150" />
              <el-table-column prop="idCard" label="身份证号" />
              <el-table-column prop="createTime" label="加入时间" width="180">
                <template #default="scope">
                  {{ formatTime(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-popconfirm title="确定要移除该救助员吗？" @confirm="handleDelete(scope.row.id)">
                    <template #reference>
                      <el-button type="danger" link>移除</el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
          </el-card>

        </el-main>
      </el-container>
    </el-container>

    <el-dialog v-model="dialogVisible" title="新增救助员" width="400px" destroy-on-close>
      <el-form :model="addForm" :rules="rules" ref="addFormRef" label-width="80px">
        <el-form-item label="姓名" prop="username">
          <el-input v-model="addForm.username" placeholder="请输入救助员姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addForm.phone" placeholder="请输入真实手机号" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="addForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-alert title="初始登录密码默认为: 123456" type="info" show-icon :closable="false" style="margin-top: 10px;" />
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" color="#ff9d4d" @click="submitAdd" :loading="submitLoading">确定添加</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, DataLine, User, Avatar, Setting, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const activeIndex = ref('3') // 默认直接展开救助员管理方便测试
const loading = ref(false)
const volunteerList = ref([])

const dialogVisible = ref(false)
const submitLoading = ref(false)
const addFormRef = ref(null)
const addForm = ref({ username: '', phone: '', idCard: '' })

// 菜单标题映射
const titles = { '1': '数据大盘', '2': '领养人管理', '3': '救助员管理', '4': '系统设置' }
const currentTitle = computed(() => titles[activeIndex.value])

// 表单校验
const validateIdCard = (rule, value, callback) => {
  const reg = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/
  if (value === '') callback(new Error('请输入身份证号'))
  else if (!reg.test(value)) callback(new Error('身份证号格式不正确'))
  else callback()
}
const rules = {
  username: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  idCard: [{ required: true, validator: validateIdCard, trigger: 'blur' }]
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ')
}

// 切换菜单
const handleMenuSelect = (index) => {
  activeIndex.value = index
  if (index === '3') fetchVolunteers()
}

// 获取救助员列表
const fetchVolunteers = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/user/volunteers')
    if (res.data.code === 200) {
      volunteerList.value = res.data.data
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('获取列表失败，请检查网络')
  } finally {
    loading.value = false
  }
}

// 打开添加弹窗
const openAddDialog = () => {
  addForm.value = { username: '', phone: '', idCard: '' }
  dialogVisible.value = true
}

// 提交添加请求
const submitAdd = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = await axios.post('http://localhost:8080/api/user/addVolunteer', addForm.value)
        if (res.data.code === 200) {
          ElMessage.success('添加成功！')
          dialogVisible.value = false
          fetchVolunteers() // 刷新列表
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (error) {
        ElMessage.error('添加失败，请检查网络')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 移除救助员
const handleDelete = async (id) => {
  try {
    const res = await axios.delete(`http://localhost:8080/api/user/delete/${id}`)
    if (res.data.code === 200) {
      ElMessage.success('移除成功')
      fetchVolunteers() // 刷新列表
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleLogout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}

// 页面加载时自动获取一次数据
onMounted(() => {
  fetchVolunteers()
})
</script>

<style scoped>
.backend-layout, .layout-container { height: 100vh; }
.aside { background-color: #2c3e50; display: flex; flex-direction: column; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 18px; font-weight: bold; background-color: #1a252f; }
.side-menu { flex: 1; border-right: none; }
.header { display: flex; justify-content: space-between; align-items: center; background-color: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.1); z-index: 1; }
.header-breadcrumb { font-weight: bold; color: #666; font-size: 16px; }
.dropdown-link { cursor: pointer; display: flex; align-items: center; color: #333; }
.main-content { background-color: #f0f2f5; padding: 20px; }

/* 卡片和表格美化 */
.welcome-card { border-radius: 12px; text-align: center; padding: 40px; color: #ff9d4d; }
.table-card { border-radius: 12px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.table-title { font-size: 18px; font-weight: bold; color: #333; }
</style>