<template>
  <div class="login-page">
    <!-- 动态背景 -->
    <div class="bg-animation">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
    </div>

    <!-- 左侧装饰区 -->
    <div class="login-left">
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

    <!-- 右侧登录表单 -->
    <div class="login-right">
      <div class="login-card">
        <div class="login-header">
          <h2 class="login-title">欢迎回来</h2>
          <p class="login-subtitle">登录你的账号继续探索</p>
          <!-- 调试信息 -->
          <div style="color: red; font-size: 12px; margin-top: 10px;">
            调试: loginMode = {{ loginMode }}
          </div>
        </div>

        <!-- 登录方式切换 -->
        <div class="login-tabs" style="border: 2px solid red;">
          <div 
            class="tab-item" 
            :class="{ active: loginMode === 'username' }"
            @click="switchLoginMode('username')"
            style="border: 1px solid blue;"
          >
            <el-icon><User /></el-icon>
            <span>账号登录</span>
          </div>
          <div 
            class="tab-item" 
            :class="{ active: loginMode === 'email' }"
            @click="switchLoginMode('email')"
            style="border: 1px solid blue;"
          >
            <el-icon><Message /></el-icon>
            <span>邮箱登录</span>
          </div>
          <div 
            class="tab-item" 
            :class="{ active: loginMode === 'emailCode' }"
            @click="switchLoginMode('emailCode')"
            style="border: 1px solid blue;"
          >
            <el-icon><ChatLineRound /></el-icon>
            <span>验证码登录</span>
          </div>
        </div>

        <!-- 用户名密码登录 -->
        <el-form 
          v-if="loginMode === 'username'" 
          :model="usernameForm" 
          :rules="usernameRules" 
          ref="usernameFormRef" 
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="usernameForm.username" 
              placeholder="用户名" 
              :prefix-icon="User"
              size="large"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="usernameForm.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              class="custom-input"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <button type="button" class="login-btn" :disabled="loading" @click="handleLogin">
              <span v-if="!loading">登录</span>
              <span v-else class="btn-loading">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </span>
            </button>
          </el-form-item>
        </el-form>

        <!-- 邮箱密码登录 -->
        <el-form 
          v-if="loginMode === 'email'" 
          :model="emailForm" 
          :rules="emailRules" 
          ref="emailFormRef" 
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="emailForm.username" 
              placeholder="邮箱地址" 
              :prefix-icon="Message"
              size="large"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="emailForm.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              class="custom-input"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <button type="button" class="login-btn" :disabled="loading" @click="handleLogin">
              <span v-if="!loading">登录</span>
              <span v-else class="btn-loading">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </span>
            </button>
          </el-form-item>
        </el-form>

        <!-- 邮箱验证码登录 -->
        <el-form 
          v-if="loginMode === 'emailCode'" 
          :model="emailCodeForm" 
          :rules="emailCodeRules" 
          ref="emailCodeFormRef" 
          class="login-form"
        >
          <el-form-item prop="email">
            <el-input 
              v-model="emailCodeForm.email" 
              placeholder="邮箱地址" 
              :prefix-icon="Message"
              size="large"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item prop="code">
            <div class="code-input-wrapper">
              <el-input
                v-model="emailCodeForm.code"
                placeholder="验证码"
                :prefix-icon="Key"
                size="large"
                class="custom-input code-input"
                @keyup.enter="handleLogin"
              />
              <button 
                type="button" 
                class="send-code-btn" 
                :disabled="countdown > 0 || sendingCode"
                @click="handleSendCode"
              >
                <span v-if="countdown > 0">{{ countdown }}s</span>
                <span v-else-if="sendingCode">发送中...</span>
                <span v-else>获取验证码</span>
              </button>
            </div>
          </el-form-item>
          <el-form-item>
            <button type="button" class="login-btn" :disabled="loading" @click="handleLogin">
              <span v-if="!loading">登录</span>
              <span v-else class="btn-loading">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </span>
            </button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <div class="footer-links">
            <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
          </div>
          <div class="footer-register">
            <span>还没有账号？</span>
            <router-link to="/register" class="register-link">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Edit, ChatDotRound, Star, Message, ChatLineRound, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { sendLoginCode } from '@/api/user'

console.log('=== Login Component Loaded ===')

const router = useRouter()
const userStore = useUserStore()
const usernameFormRef = ref()
const emailFormRef = ref()
const emailCodeFormRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const loginMode = ref('username') // 'username' | 'email' | 'emailCode'

console.log('loginMode初始值:', loginMode.value)

onMounted(() => {
  console.log('=== Login Component Mounted ===')
  console.log('当前loginMode:', loginMode.value)
  console.log('User图标:', User)
  console.log('Message图标:', Message)
  console.log('ChatLineRound图标:', ChatLineRound)
})

// 用户名密码登录表单
const usernameForm = ref({
  username: '',
  password: ''
})

const usernameRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 邮箱密码登录表单
const emailForm = ref({
  username: '',
  password: ''
})

const emailRules = {
  username: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 邮箱验证码登录表单
const emailCodeForm = ref({
  email: '',
  code: ''
})

const emailCodeRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

function switchLoginMode(mode) {
  console.log('切换登录模式:', mode)
  loginMode.value = mode
  console.log('切换后loginMode:', loginMode.value)
}

async function handleLogin() {
  let formRef
  let formData
  let type = 'username'

  if (loginMode.value === 'username') {
    formRef = usernameFormRef.value
    formData = usernameForm.value
  } else if (loginMode.value === 'email') {
    formRef = emailFormRef.value
    formData = emailForm.value
  } else {
    formRef = emailCodeFormRef.value
    formData = emailCodeForm.value
    type = 'emailCode'
  }

  await formRef.validate()
  loading.value = true
  try {
    await userStore.login(formData, type)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    // 错误已由 request.js 拦截器统一处理并显示
  } finally {
    loading.value = false
  }
}

async function handleSendCode() {
  try {
    await emailCodeFormRef.value.validateField('email')
  } catch {
    return
  }

  sendingCode.value = true
  try {
    await sendLoginCode(emailCodeForm.value.email)
    ElMessage.success('验证码已发送，请查收邮件')
    startCountdown()
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

function startCountdown() {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.login-page {
  min-height: 100vh;
  display: flex;
  background: var(--bg-dark);
  position: relative;
  overflow: hidden;
}

.bg-animation {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.15;
  animation: float 20s ease-in-out infinite;
}

.orb-1 {
  width: 600px;
  height: 600px;
  background: $primary-color;
  top: -200px;
  left: -100px;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: #22c55e;
  bottom: -200px;
  right: -100px;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, 30px); }
}

.login-left {
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

.logo-wrapper {
  margin-bottom: 24px;
}

.logo-icon {
  font-size: 64px;
  color: $primary-color;
  display: block;
}

.brand-title {
  font-size: 42px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.brand-desc {
  font-size: 16px;
  color: var(--text-muted);
  margin-bottom: 48px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

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
  
  .el-icon {
    font-size: 18px;
    color: white;
  }
}

.login-right {
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

.login-card {
  width: 100%;
  max-width: 380px;
  padding: 40px 36px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.login-subtitle {
  color: var(--text-muted);
  font-size: 14px;
}

.login-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding: 4px;
  background: var(--bg-input);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.3s ease;
  
  .el-icon {
    font-size: 16px;
  }
  
  &:hover {
    color: var(--text-secondary);
    background: rgba($primary-color, 0.05);
  }
  
  &.active {
    color: white;
    background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
    box-shadow: 0 4px 12px rgba($primary-color, 0.3);
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  
  :deep(.el-form-item__error) {
    padding-top: 4px;
  }
}

.code-input-wrapper {
  display: flex;
  gap: 8px;
  width: 100%;
  
  .code-input {
    flex: 1;
  }
}

.send-code-btn {
  flex-shrink: 0;
  padding: 0 20px;
  height: 48px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: var(--bg-card);
  color: $primary-color;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  
  &:hover:not(:disabled) {
    border-color: $primary-color;
    background: rgba($primary-color, 0.05);
    transform: translateY(-1px);
  }
  
  &:active:not(:disabled) {
    transform: translateY(0);
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    color: var(--text-disabled);
  }
}

:deep(.custom-input) {
  .el-input__wrapper {
    padding: 0 16px;
    height: 48px;
    background: var(--bg-input);
    border: 1px solid var(--border-color);
    border-radius: 12px;
    box-shadow: none;
    transition: all 0.3s ease;
    
    &:hover {
      border-color: var(--border-light);
    }
    
    &.is-focus {
      border-color: $primary-color;
      box-shadow: 0 0 0 3px rgba($primary-color, 0.1);
    }
  }
  
  .el-input__inner {
    height: 100%;
    color: var(--text-primary);
    
    &::placeholder {
      color: var(--text-disabled);
    }
  }
  
  .el-input__prefix {
    color: var(--text-muted);
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 12px;
  background: $primary-color;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover:not(:disabled) {
    background: $primary-dark;
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba($primary-color, 0.3);
  }
  
  &:active:not(:disabled) {
    transform: translateY(0);
  }
  
  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.btn-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.dot {
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  animation: bounce 1.4s ease-in-out infinite;
  
  &:nth-child(1) { animation-delay: 0s; }
  &:nth-child(2) { animation-delay: 0.15s; }
  &:nth-child(3) { animation-delay: 0.3s; }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.login-footer {
  text-align: center;
  color: var(--text-muted);
  font-size: 14px;
  margin-top: 24px;
}

.footer-links {
  margin-bottom: 12px;
}

.forgot-link {
  color: var(--text-muted);
  font-size: 13px;
  transition: color 0.3s;
  
  &:hover {
    color: $primary-color;
  }
}

.footer-register {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.register-link {
  color: $primary-color;
  font-weight: 600;
  
  &:hover {
    text-decoration: underline;
  }
}
</style>
