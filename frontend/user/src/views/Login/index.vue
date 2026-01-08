<template>
  <div class="login-page">
    <!-- 左侧装饰区 -->
    <div class="login-left">
      <div class="left-content">
        <span class="logo-icon">✦</span>
        <h1 class="brand-title">My Blog</h1>
        <p class="brand-desc">记录生活，分享技术，探索世界</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Edit /></el-icon>
            <span>自由创作</span>
          </div>
          <div class="feature-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>互动交流</span>
          </div>
          <div class="feature-item">
            <el-icon><Star /></el-icon>
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
        </div>
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" @click="handleLogin" class="login-btn">
              登录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Edit, ChatDotRound, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form.value)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.login-page {
  min-height: 100vh;
  display: flex;
  background: var(--bg-dark);
  transition: background-color 0.3s;
}

.login-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(59, 130, 246, 0.08) 0%, transparent 50%);
    animation: pulse 15s ease-in-out infinite;
  }
  
  @media (max-width: 900px) {
    display: none;
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

.left-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 40px;
}

.logo-icon {
  font-size: 64px;
  color: #ef4444;
  display: block;
  margin-bottom: 24px;
}

.brand-title {
  font-size: 42px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
  transition: color 0.3s;
}

.brand-desc {
  font-size: 16px;
  color: var(--text-muted);
  margin-bottom: 48px;
  transition: color 0.3s;
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
  gap: 12px;
  padding: 12px 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  color: var(--text-secondary);
  font-size: 15px;
  transition: all 0.3s;
  
  .el-icon {
    font-size: 20px;
    color: $primary-color;
  }
  
  &:hover {
    transform: translateX(8px);
    border-color: $primary-color;
  }
}

.login-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--bg-card);
  border-left: 1px solid var(--border-color);
  transition: background-color 0.3s, border-color 0.3s;
  
  @media (max-width: 900px) {
    width: 100%;
    border-left: none;
  }
}

.login-card {
  width: 100%;
  max-width: 360px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  transition: color 0.3s;
}

.login-subtitle {
  color: var(--text-muted);
  font-size: 14px;
  transition: color 0.3s;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
}

.login-footer {
  text-align: center;
  color: var(--text-muted);
  font-size: 14px;
  margin-top: 24px;
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
