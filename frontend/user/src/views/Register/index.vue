<template>
  <div class="register-page">
    <div class="bg-animation">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
    </div>

    <div class="register-card">
      <div class="register-header">
        <span class="logo-icon">✦</span>
        <h1 class="register-title">创建账号</h1>
        <p class="register-subtitle">加入我们，开启你的博客之旅</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" class="register-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" class="custom-input" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱" :prefix-icon="Message" size="large" class="custom-input" />
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-input-wrapper">
            <el-input v-model="form.code" placeholder="邮箱验证码" :prefix-icon="Key" size="large" class="custom-input code-input" maxlength="6" />
            <button type="button" class="send-code-btn" :disabled="countdown > 0 || sendingCode" @click="handleSendCode">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </button>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password class="custom-input" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" size="large" show-password class="custom-input" />
        </el-form-item>
        <el-form-item>
          <button type="button" class="register-btn" :disabled="loading" @click="handleRegister">
            <span v-if="!loading">注册</span>
            <span v-else class="btn-loading">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </span>
          </button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="login-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { register, sendEmailCode } from '@/api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)

const form = reactive({
  username: '',
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

// 发送验证码
async function handleSendCode() {
  // 先验证邮箱
  try {
    await formRef.value.validateField('email')
  } catch {
    return
  }
  
  // 立即开始倒计时，不等待接口返回
  countdown.value = 90
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
  
  sendingCode.value = true
  try {
    await sendEmailCode(form.email)
    ElMessage.success('验证码已发送，请查收邮箱')
  } catch (e) {
    // 不显示错误弹框，静默处理
    console.error(e)
  } finally {
    sendingCode.value = false
  }
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    await register({
      username: form.username,
      email: form.email,
      code: form.code,
      password: form.password
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-dark);
  position: relative;
  overflow: hidden;
  padding: 40px 20px;
}

.bg-animation {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.12;
  animation: float 20s ease-in-out infinite;
}

.orb-1 {
  width: 600px;
  height: 600px;
  background: $primary-color;
  top: -200px;
  right: -100px;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: #22c55e;
  bottom: -200px;
  left: -100px;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, 30px); }
}

.register-card {
  width: 100%;
  max-width: 420px;
  padding: 40px 36px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  position: relative;
  z-index: 1;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  font-size: 40px;
  color: $primary-color;
  display: block;
  margin-bottom: 16px;
}

.register-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.register-subtitle {
  color: var(--text-muted);
  font-size: 14px;
}

.register-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
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

.code-input-wrapper {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-input {
  flex: 1;
}

.send-code-btn {
  flex-shrink: 0;
  width: 110px;
  height: 48px;
  border: 1px solid $primary-color;
  border-radius: 12px;
  background: transparent;
  color: $primary-color;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover:not(:disabled) {
    background: rgba($primary-color, 0.1);
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    border-color: var(--border-color);
    color: var(--text-muted);
  }
}

.register-btn {
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

.register-footer {
  text-align: center;
  color: var(--text-muted);
  font-size: 14px;
  margin-top: 24px;
}

.login-link {
  color: $primary-color;
  margin-left: 4px;
  font-weight: 600;
  
  &:hover {
    text-decoration: underline;
  }
}
</style>
