<template>
  <div class="forgot-page">
    <!-- 动态背景 -->
    <div class="bg-animation">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
    </div>

    <!-- 左侧装饰区 -->
    <div class="forgot-left">
      <div class="left-content">
        <div class="logo-wrapper">
          <span class="logo-icon">✦</span>
        </div>
        <h1 class="brand-title">My Blog</h1>
        <p class="brand-desc">找回密码，重新开始</p>
        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Lock /></el-icon>
            </div>
            <span>安全验证</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Message /></el-icon>
            </div>
            <span>邮箱确认</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Check /></el-icon>
            </div>
            <span>快速重置</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧表单 -->
    <div class="forgot-right">
      <div class="forgot-card">
        <!-- 步骤指示器 -->
        <div class="steps">
          <div class="step" :class="{ active: step >= 1, done: step > 1 }">
            <span class="step-num">1</span>
            <span class="step-text">滑动验证</span>
          </div>
          <div class="step-line" :class="{ active: step > 1 }"></div>
          <div class="step" :class="{ active: step >= 2, done: step > 2 }">
            <span class="step-num">2</span>
            <span class="step-text">邮箱验证</span>
          </div>
          <div class="step-line" :class="{ active: step > 2 }"></div>
          <div class="step" :class="{ active: step >= 3 }">
            <span class="step-num">3</span>
            <span class="step-text">重置密码</span>
          </div>
        </div>

        <!-- 步骤1：滑动验证 -->
        <div v-show="step === 1" class="step-content">
          <div class="step-header">
            <h2>安全验证</h2>
            <p>请完成滑动验证以继续</p>
          </div>
          <SliderCaptcha @success="onSliderSuccess" />
        </div>

        <!-- 步骤2：邮箱验证 -->
        <div v-show="step === 2" class="step-content">
          <div class="step-header">
            <h2>邮箱验证</h2>
            <p>输入您的注册邮箱，我们将发送验证码</p>
          </div>
          <el-form :model="form" :rules="emailRules" ref="emailFormRef" class="forgot-form">
            <el-form-item prop="email">
              <el-input 
                v-model="form.email" 
                placeholder="请输入注册邮箱" 
                :prefix-icon="Message"
                size="large"
                class="custom-input"
              />
            </el-form-item>
            <el-form-item prop="code">
              <div class="code-input-group">
                <el-input 
                  v-model="form.code" 
                  placeholder="请输入验证码" 
                  :prefix-icon="Key"
                  size="large"
                  class="custom-input"
                  maxlength="6"
                />
                <button 
                  type="button" 
                  class="send-code-btn" 
                  :disabled="codeSending || countdown > 0"
                  @click="handleSendCode"
                >
                  {{ countdown > 0 ? `${countdown}s` : (codeSending ? '发送中...' : '发送验证码') }}
                </button>
              </div>
            </el-form-item>
            <el-form-item>
              <button type="button" class="submit-btn" :disabled="!form.code" @click="handleVerifyCode">
                下一步
              </button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 步骤3：重置密码 -->
        <div v-show="step === 3" class="step-content">
          <div class="step-header">
            <h2>设置新密码</h2>
            <p>请输入您的新密码</p>
          </div>
          <el-form :model="form" :rules="passwordRules" ref="passwordFormRef" class="forgot-form">
            <el-form-item prop="password">
              <el-input 
                v-model="form.password" 
                type="password"
                placeholder="请输入新密码（6-20位）" 
                :prefix-icon="Lock"
                size="large"
                show-password
                class="custom-input"
              />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input 
                v-model="form.confirmPassword" 
                type="password"
                placeholder="请确认新密码" 
                :prefix-icon="Lock"
                size="large"
                show-password
                class="custom-input"
                @keyup.enter="handleResetPassword"
              />
            </el-form-item>
            <el-form-item>
              <button type="button" class="submit-btn" :disabled="loading" @click="handleResetPassword">
                <span v-if="!loading">重置密码</span>
                <span v-else class="btn-loading">
                  <span class="dot"></span>
                  <span class="dot"></span>
                  <span class="dot"></span>
                </span>
              </button>
            </el-form-item>
          </el-form>
        </div>

        <div class="forgot-footer">
          <router-link to="/login" class="back-link">
            <el-icon><ArrowLeft /></el-icon>
            返回登录
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Lock, Message, Key, Check, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { sendResetCode, resetPassword } from '@/api/user'
import SliderCaptcha from './SliderCaptcha.vue'

const router = useRouter()
const step = ref(1)
const loading = ref(false)
const codeSending = ref(false)
const countdown = ref(0)
const emailFormRef = ref()
const passwordFormRef = ref()

const form = ref({
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const passwordRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== form.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 滑动验证成功
function onSliderSuccess() {
  step.value = 2
}

// 发送验证码
async function handleSendCode() {
  if (!form.value.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  // 简单的邮箱格式验证
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.value.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }
  
  codeSending.value = true
  try {
    const res = await sendResetCode(form.value.email)
    if (res.code === 200) {
      ElMessage.success('验证码已发送，请查收邮件')
      startCountdown()
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '发送失败')
  } finally {
    codeSending.value = false
  }
}

// 开始倒计时
function startCountdown() {
  countdown.value = 90
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// 验证邮箱验证码
async function handleVerifyCode() {
  await emailFormRef.value.validate()
  step.value = 3
}

// 重置密码
async function handleResetPassword() {
  await passwordFormRef.value.validate()
  loading.value = true
  try {
    const res = await resetPassword({
      email: form.value.email,
      code: form.value.code,
      password: form.value.password
    })
    if (res.code === 200) {
      ElMessage.success('密码重置成功，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message || '重置失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '重置失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.forgot-page {
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
  background: #f59e0b;
  top: -200px;
  left: -100px;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: $primary-color;
  bottom: -200px;
  right: -100px;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, 30px); }
}

.forgot-left {
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
    border-color: #f59e0b;
    background: rgba(#f59e0b, 0.05);
  }
}

.feature-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f59e0b;
  border-radius: 10px;
  
  .el-icon {
    font-size: 18px;
    color: white;
  }
}

.forgot-right {
  width: 520px;
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

.forgot-card {
  width: 100%;
  max-width: 420px;
  padding: 40px 36px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
}

/* 步骤指示器 */
.steps {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.step-num {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--bg-input);
  border: 2px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-muted);
  transition: all 0.3s;
}

.step-text {
  font-size: 12px;
  color: var(--text-muted);
  transition: all 0.3s;
}

.step.active .step-num {
  background: $primary-color;
  border-color: $primary-color;
  color: white;
}

.step.active .step-text {
  color: $primary-color;
}

.step.done .step-num {
  background: #22c55e;
  border-color: #22c55e;
  color: white;
}

.step-line {
  width: 40px;
  height: 2px;
  background: var(--border-color);
  margin: 0 12px;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.step-line.active {
  background: #22c55e;
}

/* 步骤内容 */
.step-content {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.step-header {
  text-align: center;
  margin-bottom: 28px;
  
  h2 {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 8px;
  }
  
  p {
    color: var(--text-muted);
    font-size: 14px;
  }
}

.forgot-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  
  :deep(.el-form-item__error) {
    padding-top: 4px;
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

.code-input-group {
  display: flex;
  gap: 12px;
  
  .custom-input {
    flex: 1;
  }
}

.send-code-btn {
  padding: 0 20px;
  height: 48px;
  border: 1px solid $primary-color;
  border-radius: 12px;
  background: transparent;
  color: $primary-color;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  
  &:hover:not(:disabled) {
    background: rgba($primary-color, 0.1);
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.submit-btn {
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

.forgot-footer {
  text-align: center;
  margin-top: 24px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--text-muted);
  font-size: 14px;
  transition: color 0.3s;
  
  &:hover {
    color: $primary-color;
  }
}
</style>
