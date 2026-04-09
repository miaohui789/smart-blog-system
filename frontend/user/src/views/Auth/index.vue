<template>
  <div class="auth-page">
    <!-- 左侧艺术区 - 简约高端设计 -->
    <div class="auth-left">
      <!-- 极简背景排版水印 -->
      <div class="bg-watermark">VISION</div>
      
      <!-- 极简十字准星 -->
      <div class="crosshairs">
        <span class="cross" style="top: 15%; left: 20%;">+</span>
        <span class="cross" style="top: 25%; right: 30%;">+</span>
        <span class="cross" style="bottom: 35%; left: 40%;">+</span>
        <span class="cross" style="bottom: 20%; right: 15%;">+</span>
      </div>

      <!-- 装饰坐标/系统版本号 -->
      <div class="decor-coords">
        SYS.VER // 1.0.01<br>
        LAT. 40° 42' 46" N<br>
        DESIGN SYSTEM
      </div>

      <div class="brand">
        <span class="dot"></span>
        <span class="brand-text">MyBlog.</span>
      </div>
      <div class="slogan-container">
        <div class="slogan-line"></div>
        <h1 class="slogan-title">Stay Hungry<br>Stay Foolish.</h1>
        <p class="slogan-desc">Discover the power of simplified thought.<br>Share your ideas with the world in a minimalist space.</p>
      </div>
      <div class="footer-text">
        <span>&copy; 2026 MyBlog System</span>
        <span>Design is intelligence made visible.</span>
      </div>
      <!-- 极简几何装饰 (无渐变) -->
      <div class="decor-circle"></div>
      <div class="decor-grid"></div>
    </div>

    <!-- 右侧表单区 -->
    <div class="auth-right">
      <div class="auth-card-container">
        <transition :name="transitionName" mode="out-in">
          <!-- 登录表单 -->
          <div v-if="currentView === 'login'" key="login" class="auth-card">
            <div class="form-header">
              <h2 class="form-title">欢 迎 登 录</h2>
              <p class="form-subtitle">WELCOME BACK</p>
            </div>

            <!-- 登录方式切换 -->
            <div class="login-tabs">
              <span class="tab-item" :class="{ active: loginMode === 'username' }" @click="loginMode = 'username'">账号</span>
              <span class="tab-divider">|</span>
              <span class="tab-item" :class="{ active: loginMode === 'email' }" @click="loginMode = 'email'">邮箱</span>
              <span class="tab-divider">|</span>
              <span class="tab-item" :class="{ active: loginMode === 'emailCode' }" @click="loginMode = 'emailCode'">验证码</span>
            </div>

            <!-- 用户名/邮箱密码登录 -->
            <el-form v-if="loginMode !== 'emailCode'" :model="loginForm" :rules="loginRules" ref="loginFormRef" class="auth-form">
              <el-form-item prop="username">
                <div class="user-box" :class="{ filled: !!loginForm.username }">
                  <input v-model="loginForm.username" type="text" autocomplete="off" />
                  <label>{{ loginMode === 'email' ? '邮箱地址' : '用户名' }}</label>
                </div>
              </el-form-item>
              <el-form-item prop="password">
                <div class="user-box" :class="{ filled: !!loginForm.password }">
                  <input v-model="loginForm.password" :type="showLoginPwd ? 'text' : 'password'" autocomplete="off" @keyup.enter="handleLogin" />
                  <label>密码</label>
                  <span class="pwd-toggle" @click="showLoginPwd = !showLoginPwd">{{ showLoginPwd ? '隐藏' : '显示' }}</span>
                </div>
              </el-form-item>
              <el-form-item class="btn-item">
                <button type="button" class="glow-btn" :disabled="loginLoading" @click="handleLogin">
                  <em></em><em></em><em></em><em></em>
                  <span v-if="!loginLoading">登 录</span>
                  <span v-else class="btn-loading"><i class="dot"></i><i class="dot"></i><i class="dot"></i></span>
                </button>
              </el-form-item>
            </el-form>

            <!-- 邮箱验证码登录 -->
            <el-form v-else :model="emailCodeForm" :rules="emailCodeRules" ref="emailCodeFormRef" class="auth-form">
              <el-form-item prop="email">
                <div class="user-box" :class="{ filled: !!emailCodeForm.email }">
                  <input v-model="emailCodeForm.email" type="email" autocomplete="off" />
                  <label>邮箱地址</label>
                </div>
              </el-form-item>
              <el-form-item prop="code">
                <div class="user-box code-box" :class="{ filled: !!emailCodeForm.code }">
                  <input v-model="emailCodeForm.code" type="text" autocomplete="off" maxlength="6" @keyup.enter="handleEmailCodeLogin" />
                  <label>验证码</label>
                  <button type="button" class="send-code-btn" :disabled="loginCountdown > 0" @click="handleSendLoginCode">
                    {{ loginCountdown > 0 ? `${loginCountdown}s` : '获取验证码' }}
                  </button>
                </div>
              </el-form-item>
              <el-form-item class="btn-item">
                <button type="button" class="glow-btn" :disabled="loginLoading" @click="handleEmailCodeLogin">
                  <em></em><em></em><em></em><em></em>
                  <span v-if="!loginLoading">登 录</span>
                  <span v-else class="btn-loading"><i class="dot"></i><i class="dot"></i><i class="dot"></i></span>
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
            <div class="form-header">
              <h2 class="form-title">创 建 账 号</h2>
              <p class="form-subtitle">CREATE ACCOUNT</p>
            </div>
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" class="auth-form">
              <el-form-item prop="username">
                <div class="user-box" :class="{ filled: !!registerForm.username }">
                  <input v-model="registerForm.username" type="text" autocomplete="off" />
                  <label>用户名</label>
                </div>
              </el-form-item>
              <el-form-item prop="email">
                <div class="user-box" :class="{ filled: !!registerForm.email }">
                  <input v-model="registerForm.email" type="email" autocomplete="off" />
                  <label>邮箱</label>
                </div>
              </el-form-item>
              <el-form-item prop="code">
                <div class="user-box code-box" :class="{ filled: !!registerForm.code }">
                  <input v-model="registerForm.code" type="text" autocomplete="off" maxlength="6" />
                  <label>邮箱验证码</label>
                  <button type="button" class="send-code-btn" :disabled="regCountdown > 0" @click="handleSendRegCode">
                    {{ regCountdown > 0 ? `${regCountdown}s` : '获取验证码' }}
                  </button>
                </div>
              </el-form-item>
              <el-form-item prop="password">
                <div class="user-box" :class="{ filled: !!registerForm.password }">
                  <input v-model="registerForm.password" :type="showRegPwd ? 'text' : 'password'" autocomplete="off" />
                  <label>密码</label>
                  <span class="pwd-toggle" @click="showRegPwd = !showRegPwd">{{ showRegPwd ? '隐藏' : '显示' }}</span>
                </div>
              </el-form-item>
              <el-form-item prop="confirmPassword">
                <div class="user-box" :class="{ filled: !!registerForm.confirmPassword }">
                  <input v-model="registerForm.confirmPassword" :type="showRegConfirmPwd ? 'text' : 'password'" autocomplete="off" />
                  <label>确认密码</label>
                  <span class="pwd-toggle" @click="showRegConfirmPwd = !showRegConfirmPwd">{{ showRegConfirmPwd ? '隐藏' : '显示' }}</span>
                </div>
              </el-form-item>
              <el-form-item class="btn-item">
                <button type="button" class="glow-btn" :disabled="registerLoading" @click="handleRegister">
                  <em></em><em></em><em></em><em></em>
                  <span v-if="!registerLoading">注 册</span>
                  <span v-else class="btn-loading"><i class="dot"></i><i class="dot"></i><i class="dot"></i></span>
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
            <div class="form-header">
              <h2 class="form-title">
                {{ forgotStep === 1 ? '安 全 验 证' : forgotStep === 2 ? '邮 箱 验 证' : '重 置 密 码' }}
              </h2>
              <p class="form-subtitle">PASSWORD RECOVERY</p>
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
                  <div class="user-box" :class="{ filled: !!forgotForm.email }">
                    <input v-model="forgotForm.email" type="email" autocomplete="off" />
                    <label>注册邮箱</label>
                  </div>
                </el-form-item>
                <el-form-item prop="code">
                  <div class="user-box code-box" :class="{ filled: !!forgotForm.code }">
                    <input v-model="forgotForm.code" type="text" autocomplete="off" maxlength="6" />
                    <label>验证码</label>
                    <button type="button" class="send-code-btn" :disabled="forgotCountdown > 0" @click="handleSendForgotCode">
                      {{ forgotCountdown > 0 ? `${forgotCountdown}s` : '发送验证码' }}
                    </button>
                  </div>
                </el-form-item>
                <el-form-item class="btn-item">
                  <button type="button" class="glow-btn" @click="handleVerifyForgotCode">
                    <em></em><em></em><em></em><em></em>
                    <span>下 一 步</span>
                  </button>
                </el-form-item>
              </el-form>
            </div>

            <!-- 步骤3：重置密码 -->
            <div v-else-if="forgotStep === 3" class="step-content">
              <el-form :model="forgotForm" :rules="forgotPasswordRules" ref="forgotPasswordFormRef" class="auth-form">
                <el-form-item prop="password">
                  <div class="user-box" :class="{ filled: !!forgotForm.password }">
                    <input v-model="forgotForm.password" :type="showForgotPwd ? 'text' : 'password'" autocomplete="off" />
                    <label>新密码（6-20位）</label>
                    <span class="pwd-toggle" @click="showForgotPwd = !showForgotPwd">{{ showForgotPwd ? '隐藏' : '显示' }}</span>
                  </div>
                </el-form-item>
                <el-form-item prop="confirmPassword">
                  <div class="user-box" :class="{ filled: !!forgotForm.confirmPassword }">
                    <input v-model="forgotForm.confirmPassword" :type="showForgotConfirmPwd ? 'text' : 'password'" autocomplete="off" @keyup.enter="handleResetPassword" />
                    <label>确认新密码</label>
                    <span class="pwd-toggle" @click="showForgotConfirmPwd = !showForgotConfirmPwd">{{ showForgotConfirmPwd ? '隐藏' : '显示' }}</span>
                  </div>
                </el-form-item>
                <el-form-item class="btn-item">
                  <button type="button" class="glow-btn" :disabled="resetLoading" @click="handleResetPassword">
                    <em></em><em></em><em></em><em></em>
                    <span v-if="!resetLoading">重 置 密 码</span>
                    <span v-else class="btn-loading"><i class="dot"></i><i class="dot"></i><i class="dot"></i></span>
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

// 密码显示控制
const showLoginPwd = ref(false)
const showRegPwd = ref(false)
const showRegConfirmPwd = ref(false)
const showForgotPwd = ref(false)
const showForgotConfirmPwd = ref(false)

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
  if (loginLoading.value) return

  await loginFormRef.value.validate()
  loginLoading.value = true
  try {
    await userStore.login(loginForm, loginMode.value === 'email' ? 'username' : 'username')
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    if (e?.message) {
      ElMessage.error(e.message)
    }
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
    userStore.resetState()
    ElMessage.success('欢迎访问')
    router.push('/')
  }).catch(() => {
  })
}

async function handleEmailCodeLogin() {
  if (loginLoading.value) return

  await emailCodeFormRef.value.validate()
  loginLoading.value = true
  try {
    await userStore.login(emailCodeForm, 'emailCode')
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    if (e?.message) {
      ElMessage.error(e.message)
    }
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
  background: #0a0a0c;
  position: relative;
  overflow: hidden;
  align-items: stretch;
}

/* ===== 左侧艺术区 ===== */
.auth-left {
  flex: 1;
  background-color: transparent;
  padding: 5% 6%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
  z-index: 2;

  .brand {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 1.5rem;
    font-weight: 700;
    color: #fff;
    letter-spacing: -0.5px;
    z-index: 10;
    
    .dot {
      width: 12px;
      height: 12px;
      background-color: #fff;
    }
  }

  .slogan-container {
    position: relative;
    z-index: 10;
    margin-top: -10vh;
    padding-left: 20px;
    
    .slogan-line {
      position: absolute;
      left: 0;
      top: 10px;
      bottom: 20px;
      width: 2px;
      background-color: #fff;
    }

    .slogan-title {
      font-size: 5rem;
      font-weight: 600;
      line-height: 1.1;
      margin: 0 0 2rem;
      color: #fff;
      letter-spacing: -2px;
      font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
    }

    .slogan-desc {
      font-size: 1.25rem;
      color: rgba(255, 255, 255, 0.5);
      line-height: 1.6;
      font-weight: 300;
      max-width: 480px;
      letter-spacing: 0.5px;
    }
  }

  .footer-text {
    z-index: 10;
    display: flex;
    justify-content: space-between;
    font-size: 0.8rem;
    color: rgba(255, 255, 255, 0.3);
    text-transform: uppercase;
    letter-spacing: 1.5px;
  }

  /* 极简几何装饰 */
  .bg-watermark {
    position: absolute;
    top: 45%;
    left: 45%;
    transform: translate(-50%, -50%);
    font-size: 16vw;
    font-weight: 900;
    color: transparent;
    -webkit-text-stroke: 1px rgba(255, 255, 255, 0.02);
    white-space: nowrap;
    z-index: 0;
    pointer-events: none;
    font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
    letter-spacing: 0.05em;
    user-select: none;
  }

  .crosshairs .cross {
    position: absolute;
    color: rgba(255, 255, 255, 0.15);
    font-size: 16px;
    font-weight: 300;
    z-index: 1;
    pointer-events: none;
    user-select: none;
  }

  .decor-coords {
    position: absolute;
    right: 8%;
    top: 8%;
    font-size: 0.65rem;
    color: rgba(255, 255, 255, 0.15);
    letter-spacing: 2px;
    text-align: right;
    line-height: 1.8;
    z-index: 10;
    font-family: monospace;
    user-select: none;
  }

  .decor-circle {
    position: absolute;
    right: -15vw;
    top: 10vh;
    width: 45vw;
    height: 45vw;
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: 50%;
    z-index: 1;
    pointer-events: none;
  }

  .decor-grid {
    position: absolute;
    left: 0;
    bottom: 0;
    width: 60%;
    height: 30%;
    background-image: 
      linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
    background-size: 40px 40px;
    z-index: 0;
    mask-image: linear-gradient(to top right, black, transparent);
    -webkit-mask-image: linear-gradient(to top right, black, transparent);
  }

  @media (max-width: 1024px) {
    display: none; /* 小屏幕隐藏左侧 */
  }
}

/* ===== 右侧表单区 ===== */
.auth-right {
  width: 480px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background-color: transparent;
  position: relative;
  z-index: 5;
  
  @media (max-width: 1024px) {
    width: 100%;
    flex: 1;
    padding: 20px;
  }
}

.auth-card-container {
  width: 100%;
  max-width: 400px;
}

// ===== 暗黑卡片盒子 =====
.auth-card {
  padding: 40px 36px;
  background: rgba(255, 255, 255, 0.02);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.4), 0 0 0 1px rgba(255, 255, 255, 0.04);
  border-radius: 16px;
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  
  @media (max-width: 480px) {
    padding: 30px 20px;
  }
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-title {
  margin: 0 0 8px;
  color: #fff;
  font-size: 1.5rem;
  font-weight: 600;
  letter-spacing: 4px;
}

.form-subtitle {
  margin: 0;
  color: rgba(255, 255, 255, 0.3);
  font-size: 0.75rem;
  letter-spacing: 2px;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  text-transform: uppercase;
}

// ===== 登录方式切换 =====
.login-tabs {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.tab-divider {
  color: rgba(255, 255, 255, 0.25);
  font-size: 13px;
  user-select: none;
}

.tab-item {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 4px;
  transition: all 0.2s;
  user-select: none;

  &:hover { color: rgba(255, 255, 255, 0.8); }
  &.active { color: #fff; font-weight: 600; }
}

// ===== 浮动标签输入框 =====
.auth-form {
  :deep(.el-form-item) {
    margin-bottom: 0;
    .el-form-item__content { line-height: normal; flex-wrap: nowrap; }
    .el-form-item__error { color: #ff8080; font-size: 11px; padding-top: 4px; padding-bottom: 6px; }
  }
}

.btn-item {
  :deep(.el-form-item) { margin-top: 8px; }
}

.user-box {
  position: relative;
  width: 100%;
  padding-top: 22px;
  margin-bottom: 20px;

  input {
    width: 100%;
    padding: 10px 0;
    font-size: 15px;
    color: #fff;
    border: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.35);
    outline: none;
    background: transparent;
    transition: border-color 0.3s;

    &:focus { border-bottom-color: rgba(255, 255, 255, 0.9); }

    &:-webkit-autofill,
    &:-webkit-autofill:hover,
    &:-webkit-autofill:focus {
      -webkit-box-shadow: 0 0 0 1000px rgba(0,0,0,0) inset;
      -webkit-text-fill-color: #fff;
      caret-color: #fff;
    }
  }

  label {
    position: absolute;
    top: 32px;
    left: 0;
    font-size: 15px;
    color: rgba(255, 255, 255, 0.55);
    pointer-events: none;
    transition: all 0.3s ease;
  }

  input:focus ~ label,
  &.filled label {
    top: 4px;
    font-size: 11px;
    color: rgba(255, 255, 255, 0.8);
    letter-spacing: 1px;
  }
}

.pwd-toggle {
  position: absolute;
  right: 0;
  bottom: 10px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  user-select: none;
  transition: color 0.2s;
  &:hover { color: rgba(255, 255, 255, 0.9); }
}

.code-box {
  padding-right: 78px;

  .send-code-btn {
    position: absolute;
    right: 0;
    bottom: 8px;
    background: transparent;
    border: 1px solid rgba(255, 255, 255, 0.35);
    border-radius: 4px;
    color: rgba(255, 255, 255, 0.7);
    font-size: 12px;
    padding: 4px 8px;
    cursor: pointer;
    transition: all 0.2s;
    white-space: nowrap;

    &:hover:not(:disabled) { border-color: rgba(255, 255, 255, 0.9); color: #fff; }
    &:disabled { opacity: 0.4; cursor: not-allowed; }
  }
}

// ===== 流光边框按钮 =====
.glow-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 14px 20px;
  margin-top: 10px;
  font-weight: 700;
  color: #fff;
  font-size: 14px;
  letter-spacing: 5px;
  overflow: hidden;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: background 0.4s, color 0.4s, border-radius 0.4s;
  border-radius: 6px; /* 默认加个圆角好看些 */

  em {
    position: absolute;
    display: block;
  }
  em:nth-child(1) {
    top: 0; left: -100%; width: 100%; height: 2px;
    background: linear-gradient(90deg, transparent, #fff);
    animation: glowAnim1 1.5s linear infinite;
  }
  em:nth-child(2) {
    top: -100%; right: 0; width: 2px; height: 100%;
    background: linear-gradient(180deg, transparent, #fff);
    animation: glowAnim2 1.5s linear infinite;
    animation-delay: 0.375s;
  }
  em:nth-child(3) {
    bottom: 0; right: -100%; width: 100%; height: 2px;
    background: linear-gradient(270deg, transparent, #fff);
    animation: glowAnim3 1.5s linear infinite;
    animation-delay: 0.75s;
  }
  em:nth-child(4) {
    bottom: -100%; left: 0; width: 2px; height: 100%;
    background: linear-gradient(360deg, transparent, #fff);
    animation: glowAnim4 1.5s linear infinite;
    animation-delay: 1.125s;
  }

  > span { position: relative; z-index: 1; }

  &:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.88);
    color: #1a1a2e;
    border-radius: 8px;
    em { opacity: 0; }
  }
  &:disabled {
    opacity: 0.55;
    cursor: not-allowed;
    em { animation-play-state: paused; }
  }
}

@keyframes glowAnim1 { 0% { left: -100%; } 50%,100% { left: 100%; } }
@keyframes glowAnim2 { 0% { top: -100%; } 50%,100% { top: 100%; } }
@keyframes glowAnim3 { 0% { right: -100%; } 50%,100% { right: 100%; } }
@keyframes glowAnim4 { 0% { bottom: -100%; } 50%,100% { bottom: 100%; } }

.btn-loading {
  display: inline-flex;
  align-items: center;
  gap: 5px;

  i.dot {
    display: block;
    width: 7px; height: 7px;
    background: #fff;
    border-radius: 50%;
    animation: dotBounce 1.4s ease-in-out infinite;
    font-style: normal;
    &:nth-child(1) { animation-delay: 0s; }
    &:nth-child(2) { animation-delay: 0.15s; }
    &:nth-child(3) { animation-delay: 0.3s; }
  }
}

@keyframes dotBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.card-footer {
  text-align: center;
  margin-top: 20px;
}

.link-btn {
  color: rgba(255, 255, 255, 0.4);
  font-size: 13px;
  transition: color 0.3s;
  &:hover { color: #fff; }
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
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 6px;
  background-color: transparent;
  padding: 8px 24px;
  color: #fff;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 400;
  transition: all 0.3s ease;
  
  &:hover {
    background-color: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.8);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(255, 255, 255, 0.1);
  }
  &:active { transform: translateY(0); }
  .icon { height: 1.1rem; width: 1.1rem; }
  .text { line-height: 1; }
}

.footer-register {
  margin-top: 12px;
  color: rgba(255, 255, 255, 0.45);
  font-size: 14px;
}

.link-primary {
  color: rgba(255, 255, 255, 0.85);
  font-weight: 600;
  margin-left: 4px;
  &:hover { color: #fff; text-decoration: underline; }
}

.version-info {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.25);
  font-size: 11px;
  line-height: 1.6;
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

// ===== 忘记密码步骤指示器 =====
.steps-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 28px;
}

.step-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s;
  flex-shrink: 0;

  &.active { background: rgba(255, 255, 255, 0.6); border-color: rgba(255, 255, 255, 0.8); transform: scale(1.25); }
  &.done { background: #22c55e; border-color: #22c55e; transform: scale(1); }
}

.step-line {
  width: 44px;
  height: 1px;
  background: rgba(255, 255, 255, 0.15);
  transition: background 0.4s;
  flex-shrink: 0;

  &.active { background: rgba(34, 197, 94, 0.7); }
}

.step-content {
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

// ===== 亮色主题适配 =====
:root[data-theme="light"] {
  .auth-page {
    background: #f4f4f5;
  }

  .auth-left {
    background-color: transparent;
    border-right-color: rgba(0, 0, 0, 0.05);

    .brand {
      color: #18181b;
      .dot { background-color: #18181b; }
    }

    .slogan-line { background-color: #18181b; }
    .slogan-title { color: #18181b; }
    .slogan-desc { color: rgba(0, 0, 0, 0.5); }
    .footer-text { color: rgba(0, 0, 0, 0.3); }

    .bg-watermark { -webkit-text-stroke-color: rgba(0, 0, 0, 0.03); }
    .crosshairs .cross { color: rgba(0, 0, 0, 0.2); }
    .decor-coords { color: rgba(0, 0, 0, 0.3); }

    .decor-circle { border-color: rgba(0, 0, 0, 0.08); }
    .decor-grid {
      background-image: 
        linear-gradient(rgba(0,0,0,0.04) 1px, transparent 1px),
        linear-gradient(90deg, rgba(0,0,0,0.04) 1px, transparent 1px);
    }
  }

  .auth-right {
    background-color: transparent;
  }

  .auth-card {
    background: rgba(255, 255, 255, 0.6);
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.08), 0 0 0 1px rgba(0, 0, 0, 0.04);
  }

  .form-title { color: #18181b; }
  .form-subtitle { color: rgba(0, 0, 0, 0.4); }

  .login-tabs { border-bottom-color: rgba(0, 0, 0, 0.1); }
  .tab-divider { color: rgba(0, 0, 0, 0.22); }
  .tab-item {
    color: rgba(0, 0, 0, 0.42);
    &:hover { color: rgba(0, 0, 0, 0.75); }
    &.active { color: #18181b; }
  }

  .user-box {
    input {
      color: #18181b;
      border-bottom-color: rgba(0, 0, 0, 0.22);
      &:focus { border-bottom-color: rgba(0, 0, 0, 0.75); }
      &:-webkit-autofill,
      &:-webkit-autofill:hover,
      &:-webkit-autofill:focus {
        -webkit-box-shadow: 0 0 0 1000px rgba(255, 255, 255, 0) inset;
        -webkit-text-fill-color: #18181b;
        caret-color: #18181b;
      }
    }
    label { color: rgba(0, 0, 0, 0.42); }
    input:focus ~ label,
    &.filled label { color: rgba(0, 0, 0, 0.65); }
  }

  .pwd-toggle {
    color: rgba(0, 0, 0, 0.32);
    &:hover { color: rgba(0, 0, 0, 0.8); }
  }

  .send-code-btn {
    border-color: rgba(0, 0, 0, 0.22);
    color: rgba(0, 0, 0, 0.6);
    &:hover:not(:disabled) { border-color: rgba(0, 0, 0, 0.75); color: #18181b; }
  }

  .glow-btn {
    color: #18181b;
    em:nth-child(1) { background: linear-gradient(90deg, transparent, #18181b); }
    em:nth-child(2) { background: linear-gradient(180deg, transparent, #18181b); }
    em:nth-child(3) { background: linear-gradient(270deg, transparent, #18181b); }
    em:nth-child(4) { background: linear-gradient(360deg, transparent, #18181b); }
    &:hover:not(:disabled) { background: rgba(24, 24, 27, 0.88); color: #fff; border-radius: 6px; }
  }

  .link-btn {
    color: rgba(0, 0, 0, 0.38);
    &:hover { color: #18181b; }
  }

  .guest-btn {
    border-color: rgba(0, 0, 0, 0.4);
    background-color: transparent;
    color: #18181b;
    &:hover {
      background-color: rgba(0, 0, 0, 0.05);
      border-color: #18181b;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }
  }

  .footer-register { color: rgba(0, 0, 0, 0.42); }
  .link-primary {
    color: #18181b;
    font-weight: 700;
    &:hover { color: #3f3f46; }
  }

  .version-info {
    border-top-color: rgba(0, 0, 0, 0.08);
    color: rgba(0, 0, 0, 0.22);
  }

  .step-dot {
    background: rgba(0, 0, 0, 0.12);
    border-color: rgba(0, 0, 0, 0.2);
    &.active { background: rgba(0, 0, 0, 0.45); border-color: rgba(0, 0, 0, 0.65); }
    &.done { background: #16a34a; border-color: #16a34a; }
  }
  .step-line {
    background: rgba(0, 0, 0, 0.1);
    &.active { background: rgba(22, 163, 74, 0.6); }
  }
}
</style>
