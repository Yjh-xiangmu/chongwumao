import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/login/Login.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'login', component: Login },

    // ---- 领养人 ----
    {
      path: '/adopter',
      component: () => import('../views/adopter/AdopterLayout.vue'),
      redirect: '/adopter/cat-list',
      children: [
        { path: 'cat-list',      name: 'CatList',       component: () => import('../views/adopter/CatList.vue') },
        { path: 'my-records',    name: 'MyRecords',     component: () => import('../views/adopter/MyRecords.vue') },
        { path: 'community',     name: 'Community',     component: () => import('../views/adopter/Community.vue') },
        { path: 'notifications', name: 'Notifications', component: () => import('../views/adopter/Notifications.vue') },
        { path: 'profile',       name: 'Profile',       component: () => import('../views/adopter/Profile.vue') },
      ]
    },

    // ---- 管理员 ----
    {
      path: '/admin',
      component: () => import('../views/admin/AdminLayout.vue'),
      redirect: '/admin/dashboard',
      children: [
        { path: 'dashboard',        name: 'Dashboard',       component: () => import('../views/admin/Dashboard.vue') },
        { path: 'adopter-manage',   name: 'AdopterManage',   component: () => import('../views/admin/AdopterManage.vue') },
        { path: 'volunteer-manage', name: 'VolunteerManage', component: () => import('../views/admin/VolunteerManage.vue') },
        { path: 'settings',         name: 'SystemSettings',  component: () => import('../views/admin/SystemSettings.vue') },
      ]
    },

    // ---- 救助员 ----
    {
      path: '/volunteer',
      component: () => import('../views/volunteer/VolunteerLayout.vue'),
      redirect: '/volunteer/cat-manage',
      children: [
        { path: 'cat-manage', name: 'CatManage',     component: () => import('../views/volunteer/CatManage.vue') },
        { path: 'approve',    name: 'ApproveManage', component: () => import('../views/volunteer/ApproveManage.vue') },
        { path: 'record',     name: 'RecordManage',  component: () => import('../views/volunteer/RecordManage.vue') },
      ]
    }
  ]
})

export default router