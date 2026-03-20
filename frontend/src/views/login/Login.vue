<template>
  <div class="login-container">
    <div class="bg-shape shape1"></div>
    <div class="bg-shape shape2"></div>

    <div class="login-box" :class="{ 'is-register': isRegister }">
      <div class="cat-animation-area">
        <div class="cat-placeholder" :class="{ 'hide-eyes': isTypingPassword }">
          <span class="cat-face">🐱</span>
        </div>
      </div>

      <div class="form-wrapper login-form">
        <h2>欢迎回来，铲屎官</h2>
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
          <el-form-item prop="phone">
            <el-input
                v-model="loginForm.phone"
                placeholder="请输入登录手机号"
                prefix-icon="Phone"
                @focus="isTypingPassword = false" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
                @focus="isTypingPassword = true"
                @blur="isTypingPassword = false" />
          </el-form-item>
          <el-button type="primary" class="submit-btn" round :loading="loading" @click="handleLogin(loginFormRef)">登 录</el-button>
          <div class="switch-text">
            还没有账号？ <span @click="switchMode(true)">喵星人注册</span>
          </div>
        </el-form>
      </div>

      <div class="form-wrapper register-form">
        <h2>加入领养大家庭</h2>
        <el-form :model="registerForm" :rules="rules" ref="registerFormRef">
          <el-form-item prop="username">
            <el-input v-model="registerForm.username" placeholder="设置你的专属昵称" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="registerForm.phone" placeholder="请输入真实的手机号(用于登录)" prefix-icon="Phone" />
          </el-form-item>
          <el-form-item prop="idCard">
            <el-input v-model="registerForm.idCard" placeholder="请输入真实的身份证号" prefix-icon="Postcard" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="设置登录密码" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-button type="warning" class="submit-btn" round :loading="loading" @click="handleRegister(registerFormRef)">注 册</el-button>
          <div class="switch-text">
            已有账号？ <span @click="switchMode(false)">去登录</span>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock, Phone, Postcard } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const isRegister = ref(false)
const isTypingPassword = ref(false)
const loading = ref(false)

const loginFormRef = ref(null)
const registerFormRef = ref(null)

// 登录只传 phone 和 password
const loginForm = reactive({ phone: '', password: '' })
const registerForm = reactive({ username: '', phone: '', idCard: '', password: '' })

const validateIdCard = (rule, value, callback) => {
  const reg = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/
  if (value === '') {
    callback(new Error('请输入身份证号'))
  } else if (!reg.test(value)) {
    callback(new Error('身份证号格式不正确'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '昵称不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  idCard: [{ required: true, validator: validateIdCard, trigger: 'blur' }]
}

const switchMode = (mode) => {
  isRegister.value = mode
  if (loginFormRef.value) loginFormRef.value.resetFields()
  if (registerFormRef.value) registerFormRef.value.resetFields()
}

const handleLogin = async (formEl) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await axios.post('http://localhost:8080/api/auth/login', loginForm)
        if (res.data.code === 200) {
          ElMessage.success('登录成功，欢迎回来喵！')

          // 获取后端返回的用户信息
          const user = res.data.data
          // 将用户信息存入 localStorage
          localStorage.setItem('user', JSON.stringify(user))

          // 根据角色动态跳转
          if (user.role === 1) {
            router.push('/adopter') // 领养人
          } else if (user.role === 2) {
            router.push('/volunteer') // 救助员
          } else if (user.role === 3) {
            router.push('/admin') // 管理员
          }
        } else {
          ElMessage.error(res.data.message || '登录失败')
        }
      } catch (error) {
        ElMessage.error('网络请求失败，请检查后端服务')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleRegister = async (formEl) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await axios.post('http://localhost:8080/api/auth/register', registerForm)
        if (res.data.code === 200) {
          ElMessage.success('注册成功，快去登录吧！')
          switchMode(false)
        } else {
          ElMessage.error(res.data.message || '注册失败')
        }
      } catch (error) {
        ElMessage.error('网络请求失败，请检查后端服务')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* 样式保持不变 */
.login-container { height: 100vh; display: flex; justify-content: center; align-items: center; background-color: #fff9e6; overflow: hidden; position: relative; }
.bg-shape { position: absolute; border-radius: 50%; filter: blur(60px); z-index: 0; animation: float 6s ease-in-out infinite; }
.shape1 { width: 400px; height: 400px; background: #ffb880; top: -100px; left: -100px; }
.shape2 { width: 300px; height: 300px; background: #80c7ff; bottom: -50px; right: -50px; animation-delay: -3s; }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-20px); } }
.login-box { width: 400px; min-height: 480px; background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(10px); border-radius: 20px; box-shadow: 0 15px 35px rgba(0,0,0,0.05); position: relative; z-index: 1; padding: 40px 30px; overflow: hidden; transition: all 0.5s ease; }
.cat-animation-area { text-align: center; margin-bottom: 20px; height: 60px; }
.cat-placeholder { font-size: 50px; transition: transform 0.3s; }
.cat-placeholder.hide-eyes { transform: scale(0.9) translateY(10px); opacity: 0.7; }
.form-wrapper { position: absolute; top: 100px; left: 30px; right: 30px; transition: all 0.5s ease; }
.login-form { transform: translateX(0); opacity: 1; pointer-events: auto; }
.register-form { transform: translateX(100%); opacity: 0; pointer-events: none; }
.is-register .login-form { transform: translateX(-100%); opacity: 0; pointer-events: none; }
.is-register .register-form { transform: translateX(0); opacity: 1; pointer-events: auto; }
h2 { text-align: center; color: #555; margin-bottom: 30px; font-weight: 600; }
.submit-btn { width: 100%; margin-top: 15px; font-size: 16px; height: 45px; }
.switch-text { text-align: center; margin-top: 20px; font-size: 14px; color: #888; }
.switch-text span { color: #ff9d4d; cursor: pointer; font-weight: bold; }
.switch-text span:hover { text-decoration: underline; }
:deep(.el-input__wrapper) { border-radius: 20px; background-color: #f7f8fa; box-shadow: none !important; border: 1px solid transparent; transition: border-color 0.3s; }
:deep(.el-input__wrapper:hover), :deep(.el-input__wrapper.is-focus) { border-color: #ffb880; background-color: #fff; }
</style>