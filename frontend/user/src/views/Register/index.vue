<template>
  <div class="register-page">
    <div class="register-card glass-card">
      <div class="register-header">
        <span class="logo-icon">✦</span>
        <h1 class="register-title">创建账号</h1>
        <p class="register-subtitle">加入我们，开启你的博客之旅</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱" :prefix-icon="Message" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleRegister" class="register-btn">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
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
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    await register({
      username: form.username,
      email: form.email,
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
  transition: background-color 0.3s;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      radial-gradient(ellipse 80% 50% at 20% -20%, rgba(74, 158, 255, 0.12), transparent),
      radial-gradient(ellipse 60% 40% at 80% 100%, rgba(99, 102, 241, 0.1), transparent);
    pointer-events: none;
  }
}

.register-card {
  width: 420px;
  padding: $spacing-2xl;
  position: relative;
  z-index: 1;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: background-color 0.3s, border-color 0.3s;
}

.register-header {
  text-align: center;
  margin-bottom: $spacing-xl;
}

.logo-icon {
  font-size: 40px;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  display: block;
  margin-bottom: $spacing-md;
}

.register-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: $spacing-sm;
  transition: color 0.3s;
}

.register-subtitle {
  color: var(--text-muted);
  font-size: 14px;
  transition: color 0.3s;
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
}

.register-footer {
  text-align: center;
  color: var(--text-muted);
  font-size: 14px;
  margin-top: $spacing-lg;
  transition: color 0.3s;

  a {
    color: $primary-light;
    margin-left: 4px;
    font-weight: 500;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
