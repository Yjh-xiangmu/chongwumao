<template>
  <div class="login-container">
    <div class="bg-shape shape1"></div>
    <div class="bg-shape shape2"></div>

    <div class="login-box">
      <!-- Logo 区 -->
      <div class="login-logo">
        <span class="logo-emoji">🐱</span>
        <span class="logo-text">{{ siteName }}</span>
      </div>

      <!-- 角色切换 Tab -->
      <div class="role-tabs">
        <div
            v-for="role in roles" :key="role.key"
            class="role-tab"
            :class="{ active: activeRole === role.key }"
            @click="switchRole(role.key)">
          <span class="role-tab-icon">{{ role.icon }}</span>
          <span class="role-tab-label">{{ role.label }}</span>
        </div>
      </div>

      <!-- 登录/注册表单区 -->
      <div class="form-area">

        <!-- 登录表单（三个角色共用，字段一样） -->
        <transition name="fade" mode="out-in">
          <div v-if="!isRegister" :key="activeRole + '_login'">
            <p class="form-hint">{{ currentRole.hint }}</p>
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
              <el-form-item prop="phone">
                <el-input
                    v-model="loginForm.phone"
                    placeholder="手机号"
                    prefix-icon="Phone"
                    size="large"
                    @focus="isTypingPassword = false" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="密码"
                    prefix-icon="Lock"
                    size="large"
                    show-password
                    @focus="isTypingPassword = true"
                    @blur="isTypingPassword = false"
                    @keyup.enter="handleLogin" />
              </el-form-item>
              <el-button
                  type="primary"
                  class="submit-btn"
                  :color="currentRole.color"
                  size="large"
                  round
                  :loading="loading"
                  @click="handleLogin">
                登录
              </el-button>
              <div v-if="activeRole === 'adopter'" class="switch-text">
                还没有账号？<span @click="isRegister = true">立即注册</span>
              </div>
              <div v-else class="switch-text gray">
                {{ currentRole.tip }}
              </div>
            </el-form>
          </div>

          <!-- 注册表单（仅领养人可注册） -->
          <div v-else key="register">
            <p class="form-hint">创建你的领养人账号</p>
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef">
              <el-form-item prop="username">
                <el-input v-model="registerForm.username" placeholder="昵称" prefix-icon="User" size="large" />
              </el-form-item>
              <el-form-item prop="phone">
                <el-input v-model="registerForm.phone" placeholder="手机号（用于登录）" prefix-icon="Phone" size="large" />
              </el-form-item>
              <el-form-item prop="idCard">
                <el-input v-model="registerForm.idCard" placeholder="身份证号" prefix-icon="Postcard" size="large" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="设置密码" prefix-icon="Lock" size="large" show-password />
              </el-form-item>
              <el-button type="warning" class="submit-btn" size="large" round :loading="loading" @click="handleRegister">
                注册
              </el-button>
              <div class="switch-text">
                已有账号？<span @click="isRegister = false">返回登录</span>
              </div>
            </el-form>
          </div>
        </transition>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

// 读取系统设置里的平台名称
const saved = localStorage.getItem('siteSettings')
const siteName = saved ? (JSON.parse(saved).siteName || 'Hajimi 领养平台') : 'Hajimi 领养平台'

const roles = [
  {
    key:   'adopter',
    label: '领养人',
    icon:  '🏠',
    hint:  '欢迎回来，铲屎官！',
    color: '#ff9d4d',
    tip:   '',
    allowRegister: true,
  },
  {
    key:   'volunteer',
    label: '救助员',
    icon:  '🙋',
    hint:  '救助员工作台入口',
    color: '#67b346',
    tip:   '账号由管理员分配，初始密码 123456',
    allowRegister: false,
  },
  {
    key:   'admin',
    label: '管理员',
    icon:  '👑',
    hint:  '系统管理员入口',
    color: '#2c3e50',
    tip:   '请使用管理员专属账号登录',
    allowRegister: false,
  },
]

const activeRole   = ref('adopter')
const isRegister   = ref(false)
const isTypingPassword = ref(false)
const loading      = ref(false)

const currentRole = computed(() => roles.find(r => r.key === activeRole.value))

const loginFormRef    = ref(null)
const registerFormRef = ref(null)
const loginForm    = reactive({ phone: '', password: '' })
const registerForm = reactive({ username: '', phone: '', idCard: '', password: '' })

const switchRole = (key) => {
  activeRole.value = key
  isRegister.value = false
  loginForm.phone    = ''
  loginForm.password = ''
}

const validateIdCard = (rule, value, callback) => {
  const reg = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/
  if (!value) callback(new Error('请输入身份证号'))
  else if (!reg.test(value)) callback(new Error('身份证号格式不正确'))
  else callback()
}

const loginRules = {
  phone:    [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}
const registerRules = {
  username: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone:    [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  idCard:   [{ required: true, validator: validateIdCard, trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const roleMap = { adopter: 1, volunteer: 2, admin: 3 }
const routeMap = { 1: '/adopter', 2: '/volunteer', 3: '/admin' }

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await axios.post('http://localhost:8080/api/auth/login', loginForm)
      if (res.data.code === 200) {
        const user = res.data.data
        // 校验角色是否匹配选择的入口
        if (user.role !== roleMap[activeRole.value]) {
          ElMessage.error(`请使用正确的入口登录，当前账号角色不匹配`)
          return
        }
        localStorage.setItem('user', JSON.stringify(user))
        ElMessage.success('登录成功！')
        router.push(routeMap[user.role])
      } else {
        ElMessage.error(res.data.message || '登录失败')
      }
    } catch (e) {
      ElMessage.error('网络请求失败，请检查后端服务')
    } finally {
      loading.value = false
    }
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await axios.post('http://localhost:8080/api/auth/register', registerForm)
      if (res.data.code === 200) {
        ElMessage.success('注册成功，请登录！')
        isRegister.value = false
      } else {
        ElMessage.error(res.data.message || '注册失败')
      }
    } catch (e) {
      ElMessage.error('网络请求失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fff9e6;
  overflow: hidden;
  position: relative;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  z-index: 0;
  animation: float 6s ease-in-out infinite;
}
.shape1 { width: 420px; height: 420px; background: #ffb880; top: -120px; left: -120px; }
.shape2 { width: 320px; height: 320px; background: #80c7ff; bottom: -60px; right: -60px; animation-delay: -3s; }
@keyframes float { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-20px); } }

.login-box {
  width: 420px;
  background: rgba(255,255,255,0.88);
  backdrop-filter: blur(12px);
  border-radius: 24px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.08);
  padding: 36px 36px 40px;
  position: relative;
  z-index: 1;
}

/* Logo */
.login-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 28px;
}
.logo-emoji { font-size: 36px; }
.logo-text { font-size: 22px; font-weight: bold; color: #a45a1e; }

/* 角色 Tab */
.role-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 28px;
  background: #f5f5f5;
  border-radius: 14px;
  padding: 5px;
}
.role-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 10px 6px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s;
  user-select: none;
}
.role-tab:hover { background: #fff; }
.role-tab.active {
  background: #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}
.role-tab-icon { font-size: 22px; }
.role-tab-label { font-size: 13px; font-weight: bold; color: #555; }
.role-tab.active .role-tab-label { color: #a45a1e; }

/* 表单区 */
.form-area { min-height: 220px; }
.form-hint { text-align: center; color: #888; font-size: 14px; margin: 0 0 20px 0; }

.submit-btn { width: 100%; margin-top: 8px; font-size: 15px; height: 46px; }

.switch-text { text-align: center; margin-top: 16px; font-size: 13px; color: #aaa; }
.switch-text span { color: #ff9d4d; cursor: pointer; font-weight: bold; }
.switch-text span:hover { text-decoration: underline; }
.switch-text.gray { color: #bbb; }

/* 表单输入框圆角 */
:deep(.el-input__wrapper) {
  border-radius: 20px;
  background-color: #f7f8fa;
  box-shadow: none !important;
  border: 1px solid transparent;
  transition: border-color 0.2s;
}
:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  border-color: #ffb880;
  background-color: #fff;
}

/* 过渡动画 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s, transform 0.2s; }
.fade-enter-from { opacity: 0; transform: translateY(8px); }
.fade-leave-to   { opacity: 0; transform: translateY(-8px); }
</style>