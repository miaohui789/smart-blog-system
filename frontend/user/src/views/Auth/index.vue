<template>
  <div class="auth-page">
    <!-- 动态背景 -->
    <div class="bg-animation">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 左侧装饰区 -->
    <div class="auth-left">
      <div class="left-content">
        <div class="logo-wrapper">
          <span class="logo-icon">✦</span>
        </div>
        <h1 class="brand-title">My Blog</h1>
        <p class="brand-desc">记录生活，分享技术，探索世界</p>
        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Edit /></el-icon>
            </div>
            <span>自由创作</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <span>互动交流</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Star /></el-icon>
            </div>
            <span>收藏精华</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="auth-right">
      <div class="auth-card-container">
        <transition :name="transitionName" mode="out-in">
          <!-- 登录表单 -->
          <div v-if="currentView === 'login'" key="login" class="auth-card">
            <div class="card-header">
              <h2>欢迎回来</h2>
              <p>登录你的账号继续探索</p>
            </div>

            <!-- 登录方式切换 -->
            <div class="login-tabs">
              <div 
                class="tab-item" 
                :class="{ active: loginMode === 'username' }"
                @click="loginMode = 'username'"
              >
                <el-icon><User /></el-icon>
                <span>账号</span>
              </div>
              <div 
                class="tab-item" 
                :class="{ active: loginMode === 'email' }"
                @click="loginMode = 'email'"
              >
                <el-icon><Message /></el-icon>
                <span>邮箱</span>
              </div>
              <div 
                class="tab-item" 
                :class="{ active: loginMode === 'emailCode' }"
                @click="loginMode = 'emailCode'"
              >
                <el-icon><Key /></el-icon>
                <span>验证码</span>
              </div>
            </div>

            <!-- 用户名/邮箱密码登录 -->
            <el-form v-if="loginMode !== 'emailCode'" :model="loginForm" :rules="loginRules" ref="loginFormRef" class="auth-form">
              <el-form-item prop="username">
                <el-input 
                  v-model="loginForm.username" 
                  :placeholder="loginMode === 'email' ? '邮箱地址' : '用户名'" 
                  :prefix-icon="loginMode === 'email' ? Message : User" 
                  size="large" 
                  class="custom-input" 
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="loginForm.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password class="custom-input" @keyup.enter="handleLogin" />
              </el-form-item>
              <el-form-item>
                <button type="button" class="submit-btn" :disabled="loginLoading" @click="handleLogin">
                  <span v-if="!loginLoading">登录</span>
                  <span v-else class="btn-loading"><span class="dot"></span><span class="dot"></span><span class="dot"></span></span>
                </button>
              </el-form-item>
            </el-form>

            <!-- 邮箱验证码登录 -->
            <el-form v-else :model="emailCodeForm" :rules="emailCodeRules" ref="emailCodeFormRef" class="auth-form">
              <el-form-item prop="email">
                <el-input v-model="emailCodeForm.email" placeholder="邮箱地址" :prefix-icon="Message" size="large" class="custom-input" />
              </el-form-item>
              <el-form-item prop="code">
                <div class="code-group">
                  <el-input v-model="emailCodeForm.code" placeholder="验证码" :prefix-icon="Key" size="large" class="custom-input" maxlength="6" @keyup.enter="handleEmailCodeLogin" />
                  <button type="button" class="code-btn" :disabled="loginCountdown > 0" @click="handleSendLoginCode">
                    {{ loginCountdown > 0 ? `${loginCountdown}s` : '获取验证码' }}
                  </button>
                </div>
              </el-form-item>
              <el-form-item>
                <button type="button" class="submit-btn" :disabled="loginLoading" @click="handleEmailCodeLogin">
                  <span v-if="!loginLoading">登录</span>
                  <span v-else class="btn-loading"><span class="dot"></span><span class="dot"></span><span class="dot"></span></span>
                </button>
              </el-form-item>
            </el-form>

            <div class="card-footer">
              <a href="javascript:;" class="link-btn" @click="switchTo('forgot')">忘记密码？</a>
              
              <!-- 游客访问按钮 -->
              <div class="guest-access">
                <button class="guest-btn" @click="handleGuestAccess">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="currentColor" class="icon" viewBox="0 0 24 24">
                    <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                  </svg>
                  <span class="text">游客访问</span>
                </button>
              </div>
              
              <div class="footer-register">
                <span>还没有账号？</span>
                <a href="javascript:;" class="link-primary" @click="switchTo('register')">立即注册</a>
              </div>
              <div class="version-info">
                MyBlog系统于2026年1月22日17:20:04 正式1.0版上线
              </div>
            </div>
          </div>

          <!-- 注册表单 -->
          <div v-else-if="currentView === 'register'" key="register" class="auth-card">
            <div class="card-header">
              <h2>创建账号</h2>
              <p>加入我们，开启你的博客之旅</p>
            </div>
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" class="auth-form">
              <el-form-item prop="username">
                <el-input v-model="registerForm.username" placeholder="用户名" :prefix-icon="User" size="large" class="custom-input" />
              </el-form-item>
              <el-form-item prop="email">
                <el-input v-model="registerForm.email" placeholder="邮箱" :prefix-icon="Message" size="large" class="custom-input" />
              </el-form-item>
              <el-form-item prop="code">
                <div class="code-group">
                  <el-input v-model="registerForm.code" placeholder="验证码" :prefix-icon="Key" size="large" class="custom-input" maxlength="6" />
                  <button type="button" class="code-btn" :disabled="regCountdown > 0" @click="handleSendRegCode">
                    {{ regCountdown > 0 ? `${regCountdown}s` : '获取验证码' }}
                  </button>
                </div>
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password class="custom-input" />
              </el-form-item>
              <el-form-item prop="confirmPassword">
                <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" size="large" show-password class="custom-input" />
              </el-form-item>
              <el-form-item>
                <button type="button" class="submit-btn" :disabled="registerLoading" @click="handleRegister">
                  <span v-if="!registerLoading">注册</span>
                  <span v-else class="btn-loading"><span class="dot"></span><span class="dot"></span><span class="dot"></span></span>
                </button>
              </el-form-item>
            </el-form>
            <div class="card-footer">
              <div class="footer-register">
                <span>已有账号？</span>
                <a href="javascript:;" class="link-primary" @click="switchTo('login')">立即登录</a>
              </div>
            </div>
          </div>

          <!-- 忘记密码表单 -->
          <div v-else-if="currentView === 'forgot'" key="forgot" class="auth-card">
            <div class="card-header">
              <h2>{{ forgotStep === 1 ? '安全验证' : forgotStep === 2 ? '邮箱验证' : '重置密码' }}</h2>
              <p>{{ forgotStep === 1 ? '请完成滑动拼图验证' : forgotStep === 2 ? '输入您的注册邮箱' : '设置您的新密码' }}</p>
            </div>
            
            <!-- 步骤指示器 -->
            <div class="steps-indicator">
              <div class="step-dot" :class="{ active: forgotStep >= 1, done: forgotStep > 1 }"></div>
              <div class="step-line" :class="{ active: forgotStep > 1 }"></div>
              <div class="step-dot" :class="{ active: forgotStep >= 2, done: forgotStep > 2 }"></div>
              <div class="step-line" :class="{ active: forgotStep > 2 }"></div>
              <div class="step-dot" :class="{ active: forgotStep >= 3 }"></div>
            </div>

            <!-- 步骤1：滑动拼图验证 -->
            <div v-if="forgotStep === 1" class="step-content">
              <PuzzleCaptcha @success="onCaptchaSuccess" />
            </div>

            <!-- 步骤2：邮箱验证 -->
            <div v-else-if="forgotStep === 2" class="step-content">
              <el-form :model="forgotForm" :rules="forgotEmailRules" ref="forgotEmailFormRef" class="auth-form">
                <el-form-item prop="email">
                  <el-input v-model="forgotForm.email" placeholder="请输入注册邮箱" :prefix-icon="Message" size="large" class="custom-input" />
                </el-form-item>
                <el-form-item prop="code">
                  <div class="code-group">
                    <el-input v-model="forgotForm.code" placeholder="验证码" :prefix-icon="Key" size="large" class="custom-input" maxlength="6" />
                    <button type="button" class="code-btn" :disabled="forgotCountdown > 0" @click="handleSendForgotCode">
                      {{ forgotCountdown > 0 ? `${forgotCountdown}s` : '发送验证码' }}
                    </button>
                  </div>
                </el-form-item>
                <el-form-item>
                  <button type="button" class="submit-btn" @click="handleVerifyForgotCode">下一步</button>
                </el-form-item>
              </el-form>
            </div>

            <!-- 步骤3：重置密码 -->
            <div v-else-if="forgotStep === 3" class="step-content">
              <el-form :model="forgotForm" :rules="forgotPasswordRules" ref="forgotPasswordFormRef" class="auth-form">
                <el-form-item prop="password">
                  <el-input v-model="forgotForm.password" type="password" placeholder="新密码（6-20位）" :prefix-icon="Lock" size="large" show-password class="custom-input" />
                </el-form-item>
                <el-form-item prop="confirmPassword">
                  <el-input v-model="forgotForm.confirmPassword" type="password" placeholder="确认新密码" :prefix-icon="Lock" size="large" show-password class="custom-input" @keyup.enter="handleResetPassword" />
                </el-form-item>
                <el-form-item>
                  <button type="button" class="submit-btn" :disabled="resetLoading" @click="handleResetPassword">
                    <span v-if="!resetLoading">重置密码</span>
                    <span v-else class="btn-loading"><span class="dot"></span><span class="dot"></span><span class="dot"></span></span>
                  </button>
                </el-form-item>
              </el-form>
            </div>

            <div class="card-footer">
              <a href="javascript:;" class="link-btn back-link" @click="switchTo('login')">
                <el-icon><ArrowLeft /></el-icon>
                返回登录
              </a>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock, Message, Key, Edit, ChatDotRound, Star, ArrowLeft, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { register, sendEmailCode, sendResetCode, resetPassword, sendLoginCode } from '@/api/user'
import PuzzleCaptcha from './PuzzleCaptcha.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前视图
const currentView = ref('login')
const transitionName = ref('slide-right')

// 登录模式
const loginMode = ref('username') // 'username' | 'email' | 'emailCode'

// 根据路由初始化视图
watch(() => route.path, (path) => {
  if (path === '/login') currentView.value = 'login'
  else if (path === '/register') currentView.value = 'register'
  else if (path === '/forgot-password') currentView.value = 'forgot'
}, { immediate: true })

// 切换视图
function switchTo(view) {
  const order = { login: 0, register: 1, forgot: 2 }
  transitionName.value = order[view] > order[currentView.value] ? 'slide-left' : 'slide-right'
  currentView.value = view
  
  // 更新URL但不刷新页面
  const paths = { login: '/login', register: '/register', forgot: '/forgot-password' }
  history.replaceState(null, '', paths[view])
  
  // 重置忘记密码步骤
  if (view === 'forgot') {
    forgotStep.value = 1
  }
}

// ========== 登录 ==========
const loginFormRef = ref()
const emailCodeFormRef = ref()
const loginLoading = ref(false)
const loginCountdown = ref(0)
const loginForm = reactive({ username: '', password: '' })
const emailCodeForm = reactive({ email: '', code: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const emailCodeRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

async function handleLogin() {
  await loginFormRef.value.validate()
  loginLoading.value = true
  try {
    await userStore.login(loginForm, loginMode.value === 'email' ? 'username' : 'username')
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    // 错误已处理
  } finally {
    loginLoading.value = false
  }
}

// 游客访问
function handleGuestAccess() {
  ElMessageBox.confirm(
    '游客模式下，你可以浏览文章和内容，但无法进行评论、点赞、收藏等互动操作。是否继续？',
    '游客访问',
    {
      confirmButtonText: '继续访问',
      cancelButtonText: '取消',
      type: 'info',
    }
  ).then(() => {
    ElMessage.success('欢迎访问')
    router.push('/')
  }).catch(() => {
    // 用户取消
  })
}

async function handleEmailCodeLogin() {
  await emailCodeFormRef.value.validate()
  loginLoading.value = true
  try {
    await userStore.login(emailCodeForm, 'emailCode')
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    // 错误已处理
  } finally {
    loginLoading.value = false
  }
}

async function handleSendLoginCode() {
  if (!emailCodeForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(emailCodeForm.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }
  
  startCountdown('login')
  
  try {
    const res = await sendLoginCode(emailCodeForm.email)
    if (res.code === 200) {
      ElMessageBox.alert('验证码已发送到您的邮箱，请注意查收', '发送成功', {
        confirmButtonText: '我知道了',
        type: 'success'
      })
    }
  } catch (e) {
    console.log('发送验证码请求异常')
  }
}

// ========== 注册 ==========
const registerFormRef = ref()
const registerLoading = ref(false)
const regCountdown = ref(0)
const registerForm = reactive({ username: '', email: '', code: '', password: '', confirmPassword: '' })
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (r, v, cb) => v !== registerForm.password ? cb(new Error('两次密码不一致')) : cb(), trigger: 'blur' }
  ]
}

async function handleSendRegCode() {
  // 先检查邮箱是否填写
  if (!registerForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  // 验证邮箱格式
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(registerForm.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }
  
  // 立即开始倒计时，提升用户体验
  startCountdown('reg')
  
  try {
    const res = await sendEmailCode(registerForm.email)
    if (res.code === 200) {
      ElMessageBox.alert('验证码已发送到您的邮箱，请注意查收', '发送成功', {
        confirmButtonText: '我知道了',
        type: 'success'
      })
    }
  } catch (e) {
    // 静默处理错误，不影响用户操作
    console.log('发送验证码请求异常')
  }
}

async function handleRegister() {
  await registerFormRef.value.validate()
  registerLoading.value = true
  try {
    await register({
      username: registerForm.username,
      email: registerForm.email,
      code: registerForm.code,
      password: registerForm.password
    })
    ElMessage.success('注册成功，请登录')
    switchTo('login')
  } catch (e) {
    console.error(e)
  } finally {
    registerLoading.value = false
  }
}

// ========== 忘记密码 ==========
const forgotStep = ref(1)
const forgotEmailFormRef = ref()
const forgotPasswordFormRef = ref()
const resetLoading = ref(false)
const forgotCountdown = ref(0)
const forgotForm = reactive({ email: '', code: '', password: '', confirmPassword: '' })

const forgotEmailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const forgotPasswordRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (r, v, cb) => v !== forgotForm.password ? cb(new Error('两次密码不一致')) : cb(), trigger: 'blur' }
  ]
}

function onCaptchaSuccess() {
  setTimeout(() => {
    forgotStep.value = 2
  }, 500)
}

async function handleSendForgotCode() {
  // 先检查邮箱是否填写
  if (!forgotForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  // 验证邮箱格式
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(forgotForm.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }
  
  // 立即开始倒计时
  startCountdown('forgot')
  
  try {
    const res = await sendResetCode(forgotForm.email)
    if (res.code === 200) {
      ElMessageBox.alert('验证码已发送到您的邮箱，请注意查收', '发送成功', {
        confirmButtonText: '我知道了',
        type: 'success'
      })
    }
  } catch (e) {
    // 静默处理错误
    console.log('发送验证码请求异常')
  }
}

async function handleVerifyForgotCode() {
  await forgotEmailFormRef.value.validate()
  forgotStep.value = 3
}

async function handleResetPassword() {
  await forgotPasswordFormRef.value.validate()
  resetLoading.value = true
  try {
    const res = await resetPassword({
      email: forgotForm.email,
      code: forgotForm.code,
      password: forgotForm.password
    })
    if (res.code === 200) {
      ElMessage.success('密码重置成功')
      switchTo('login')
    } else {
      ElMessage.error(res.message || '重置失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '重置失败')
  } finally {
    resetLoading.value = false
  }
}

// 倒计时
function startCountdown(type) {
  if (type === 'login') {
    loginCountdown.value = 60
    const timer = setInterval(() => {
      loginCountdown.value--
      if (loginCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  } else if (type === 'reg') {
    regCountdown.value = 90
    const timer = setInterval(() => {
      regCountdown.value--
      if (regCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  } else {
    forgotCountdown.value = 90
    const timer = setInterval(() => {
      forgotCountdown.value--
      if (forgotCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.auth-page {
  min-height: 100vh;
  display: flex;
  background: var(--bg-dark);
  position: relative;
  overflow: hidden;
}

.bg-animation {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.2;
  animation: float 20s ease-in-out infinite;
  will-change: transform, opacity;
}

.orb-1 {
  width: 700px;
  height: 700px;
  background: linear-gradient(135deg, $primary-color 0%, #ec4899 50%, $primary-color 100%);
  top: -250px;
  left: -150px;
  animation: float1 20s ease-in-out infinite, pulse1 6s ease-in-out infinite;
}

.orb-2 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #22c55e 0%, #10b981 50%, #22c55e 100%);
  bottom: -250px;
  right: -150px;
  animation: float2 25s ease-in-out infinite, pulse2 7s ease-in-out infinite;
}

.orb-3 {
  width: 550px;
  height: 550px;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 50%, #3b82f6 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: float3 30s ease-in-out infinite, pulse3 8s ease-in-out infinite;
}

@keyframes float1 {
  0%, 100% { 
    transform: translate(0, 0) rotate(0deg) scale(1);
  }
  25% { 
    transform: translate(80px, -50px) rotate(90deg) scale(1.15);
  }
  50% { 
    transform: translate(50px, 60px) rotate(180deg) scale(0.9);
  }
  75% { 
    transform: translate(-60px, 30px) rotate(270deg) scale(1.1);
  }
}

@keyframes float2 {
  0%, 100% { 
    transform: translate(0, 0) rotate(0deg) scale(1);
  }
  33% { 
    transform: translate(-80px, 60px) rotate(120deg) scale(1.12);
  }
  66% { 
    transform: translate(60px, -70px) rotate(240deg) scale(0.88);
  }
}

@keyframes pulse1 {
  0%, 100% { 
    opacity: 0.2;
    filter: blur(80px) brightness(1);
  }
  50% { 
    opacity: 0.35;
    filter: blur(100px) brightness(1.3);
  }
}

@keyframes pulse2 {
  0%, 100% { 
    opacity: 0.2;
    filter: blur(80px) brightness(1);
  }
  50% { 
    opacity: 0.32;
    filter: blur(95px) brightness(1.25);
  }
}

@keyframes float3 {
  0%, 100% { 
    transform: translate(-50%, -50%) rotate(0deg) scale(1);
  }
  33% { 
    transform: translate(-30%, -70%) rotate(120deg) scale(1.18);
  }
  66% { 
    transform: translate(-70%, -30%) rotate(240deg) scale(0.82);
  }
}

@keyframes pulse3 {
  0%, 100% { 
    opacity: 0.18;
    filter: blur(80px) brightness(1);
  }
  50% { 
    opacity: 0.28;
    filter: blur(90px) brightness(1.22);
  }
}

.auth-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  
  @media (max-width: 900px) {
    display: none;
  }
}

.left-content {
  text-align: center;
  padding: 40px;
}

.logo-wrapper { margin-bottom: 24px; }
.logo-icon { font-size: 64px; color: $primary-color; display: block; }
.brand-title { font-size: 42px; font-weight: 800; color: var(--text-primary); margin-bottom: 12px; }
.brand-desc { font-size: 16px; color: var(--text-muted); margin-bottom: 48px; }

.features { display: flex; flex-direction: column; gap: 16px; }

.feature-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 14px 28px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  color: var(--text-secondary);
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateX(8px);
    border-color: $primary-color;
    background: rgba($primary-color, 0.05);
  }
}

.feature-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $primary-color;
  border-radius: 10px;
  .el-icon { font-size: 18px; color: white; }
}

.auth-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  z-index: 1;
  
  @media (max-width: 900px) {
    width: 100%;
  }
}

.auth-card-container {
  width: 100%;
  max-width: 400px;
  perspective: 1000px;
}

.auth-card {
  padding: 36px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
  margin-bottom: 28px;
  
  h2 { font-size: 26px; font-weight: 700; color: var(--text-primary); margin-bottom: 8px; }
  p { color: var(--text-muted); font-size: 14px; }
}

/* 登录方式切换 */
.login-tabs {
  display: flex;
  gap: 6px;
  margin-bottom: 20px;
  padding: 4px;
  background: var(--bg-input);
  border-radius: 10px;
  border: 1px solid var(--border-color);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 10px;
  border-radius: 7px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.3s ease;
  
  .el-icon {
    font-size: 15px;
  }
  
  &:hover {
    color: var(--text-secondary);
    background: rgba($primary-color, 0.05);
  }
  
  &.active {
    color: white;
    background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
    box-shadow: 0 3px 10px rgba($primary-color, 0.3);
  }
}

/* 步骤指示器 */
.steps-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  gap: 0;
}

.step-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--border-color);
  transition: all 0.3s;
  
  &.active { background: $primary-color; transform: scale(1.2); }
  &.done { background: #22c55e; }
}

.step-line {
  width: 50px;
  height: 2px;
  background: var(--border-color);
  transition: all 0.3s;
  
  &.active { background: #22c55e; }
}

.step-content {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.auth-form {
  :deep(.el-form-item) { margin-bottom: 18px; }
  :deep(.el-form-item__error) { padding-top: 4px; }
}

:deep(.custom-input) {
  .el-input__wrapper {
    padding: 0 16px;
    height: 46px;
    background: var(--bg-input);
    border: 1px solid var(--border-color);
    border-radius: 12px;
    box-shadow: none;
    transition: all 0.3s ease;
    
    &:hover { border-color: var(--border-light); }
    &.is-focus { border-color: $primary-color; box-shadow: 0 0 0 3px rgba($primary-color, 0.1); }
  }
  
  .el-input__inner { height: 100%; color: var(--text-primary); &::placeholder { color: var(--text-disabled); } }
  .el-input__prefix { color: var(--text-muted); }
}

.code-group {
  display: flex;
  gap: 10px;
  width: 100%;
  
  .custom-input { flex: 1; }
}

.code-btn {
  flex-shrink: 0;
  width: 100px;
  height: 46px;
  border: 1px solid $primary-color;
  border-radius: 12px;
  background: transparent;
  color: $primary-color;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover:not(:disabled) { background: rgba($primary-color, 0.1); }
  &:disabled { opacity: 0.5; cursor: not-allowed; border-color: var(--border-color); color: var(--text-muted); }
}

.submit-btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 12px;
  background: $primary-color;
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover:not(:disabled) { background: $primary-dark; transform: translateY(-2px); box-shadow: 0 8px 20px rgba($primary-color, 0.3); }
  &:active:not(:disabled) { transform: translateY(0); }
  &:disabled { opacity: 0.7; cursor: not-allowed; }
}

.btn-loading { display: flex; align-items: center; justify-content: center; gap: 6px; }

.dot {
  width: 7px; height: 7px; background: white; border-radius: 50%;
  animation: bounce 1.4s ease-in-out infinite;
  &:nth-child(1) { animation-delay: 0s; }
  &:nth-child(2) { animation-delay: 0.15s; }
  &:nth-child(3) { animation-delay: 0.3s; }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.card-footer {
  text-align: center;
  margin-top: 20px;
}

.link-btn {
  color: var(--text-muted);
  font-size: 13px;
  transition: color 0.3s;
  &:hover { color: $primary-color; }
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

// 游客访问按钮
.guest-access {
  margin: 16px 0;
}

.guest-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  border: 2px solid #3b82f6;
  border-radius: 9999px;
  background-color: #3b82f6;
  padding: 0.5rem 1.25rem;
  text-align: center;
  color: #fff;
  outline: 0;
  transition: all 0.2s ease;
  text-decoration: none;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  
  &:hover {
    background-color: #2563eb;
    border-color: #2563eb;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
  }
  
  &:active {
    transform: translateY(0);
  }
  
  .icon {
    height: 1.1rem;
    width: 1.1rem;
    transition: transform 0.2s ease;
  }
  
  &:hover .icon {
    transform: scale(1.1);
  }
  
  .text {
    line-height: 1;
  }
}

.footer-register {
  margin-top: 12px;
  color: var(--text-muted);
  font-size: 14px;
}

.link-primary {
  color: $primary-color;
  font-weight: 600;
  margin-left: 4px;
  &:hover { text-decoration: underline; }
}

.version-info {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
  color: var(--text-disabled);
  font-size: 11px;
  line-height: 1.6;
  opacity: 0.7;
}

/* 过渡动画 */
.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-left-enter-from { opacity: 0; transform: translateX(30px); }
.slide-left-leave-to { opacity: 0; transform: translateX(-30px); }
.slide-right-enter-from { opacity: 0; transform: translateX(-30px); }
.slide-right-leave-to { opacity: 0; transform: translateX(30px); }
</style>
