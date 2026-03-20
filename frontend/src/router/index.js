import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/login/Login.vue'
import AdopterHome from '../views/adopter/AdopterHome.vue'
import AdminHome from '../views/admin/AdminHome.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: Login
    },
    {
      path: '/adopter',
      name: 'adopter',
      component: AdopterHome
    },
    {
      path: '/admin',
      name: 'admin',
      component: AdminHome
    },
    // ---- 救助员嵌套路由区域 ----
    {
      path: '/volunteer',
      name: 'volunteer',
      // 这里使用的是路由懒加载（按需加载），能提升页面首屏打开速度
      component: () => import('../views/volunteer/VolunteerLayout.vue'),
      redirect: '/volunteer/cat-manage', // 访问 /volunteer 自动跳转到猫咪管理
      children: [
        {
          path: 'cat-manage',
          name: 'CatManage',
          component: () => import('../views/volunteer/CatManage.vue')
        },
        {
          path: 'approve',
          name: 'ApproveManage',
          component: () => import('../views/volunteer/ApproveManage.vue')
        },
        {
          path: 'record',
          name: 'RecordManage',
          component: () => import('../views/volunteer/RecordManage.vue')
        }
      ]
    }
  ]
})

export default router