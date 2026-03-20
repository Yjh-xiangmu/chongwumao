<template>
  <div class="backend-layout">
    <el-container class="layout-container">
      <el-aside width="220px" class="aside">
        <div class="logo">🐱 救助员工作台</div>
        <el-menu :default-active="route.path" router class="side-menu" background-color="#34495e" text-color="#ecf0f1" active-text-color="#ffb880">
          <el-menu-item index="/volunteer/cat-manage">
            <el-icon><Notebook /></el-icon><span>猫咪档案管理</span>
          </el-menu-item>
          <el-menu-item index="/volunteer/approve">
            <el-icon><DocumentChecked /></el-icon><span>领养申请审核</span>
          </el-menu-item>
          <el-menu-item index="/volunteer/record">
            <el-icon><Calendar /></el-icon><span>回访记录管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="header-breadcrumb">日常工作台</div>
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
          <router-view v-slot="{ Component }">
            <transition name="el-fade-in-linear" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowDown, Notebook, DocumentChecked, Calendar } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userInfo = ref({})

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) userInfo.value = JSON.parse(userStr)
})

const handleLogout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.backend-layout, .layout-container { height: 100vh; }
.aside { background-color: #34495e; display: flex; flex-direction: column; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 18px; font-weight: bold; background-color: #2c3e50; }
.side-menu { flex: 1; border-right: none; }
.header { display: flex; justify-content: space-between; align-items: center; background-color: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.1); z-index: 1; }
.header-breadcrumb { font-weight: bold; color: #666; font-size: 16px; }
.dropdown-link { cursor: pointer; display: flex; align-items: center; color: #333; font-weight: bold; outline: none;}
.main-content { background-color: #f7f8fa; padding: 30px; overflow-y: auto; }
</style>