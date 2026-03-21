<template>
  <div class="backend-layout">
    <el-container class="layout-container">
      <el-aside width="220px" class="aside">
        <div class="logo">🐱 Hajimi 管理后台</div>
        <el-menu
            :default-active="route.path"
            router
            class="side-menu"
            background-color="#2c3e50"
            text-color="#ecf0f1"
            active-text-color="#ff9d4d">
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataLine /></el-icon><span>数据大盘</span>
          </el-menu-item>
          <el-menu-item index="/admin/adopter-manage">
            <el-icon><User /></el-icon><span>领养人管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/volunteer-manage">
            <el-icon><Avatar /></el-icon><span>救助员管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/settings">
            <el-icon><Setting /></el-icon><span>系统设置</span>
          </el-menu-item>
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
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowDown, DataLine, User, Avatar, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const titles = {
  '/admin/dashboard': '数据大盘',
  '/admin/adopter-manage': '领养人管理',
  '/admin/volunteer-manage': '救助员管理',
  '/admin/settings': '系统设置',
}
const currentTitle = computed(() => titles[route.path] || '管理后台')

const handleLogout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.backend-layout, .layout-container { height: 100vh; }
.aside { background-color: #2c3e50; display: flex; flex-direction: column; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 18px; font-weight: bold; background-color: #1a252f; }
.side-menu { flex: 1; border-right: none; }
.header { display: flex; justify-content: space-between; align-items: center; background-color: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.1); z-index: 1; }
.header-breadcrumb { font-weight: bold; color: #666; font-size: 16px; }
.dropdown-link { cursor: pointer; display: flex; align-items: center; color: #333; outline: none; }
.main-content { background-color: #f0f2f5; padding: 20px; overflow-y: auto; }
</style>