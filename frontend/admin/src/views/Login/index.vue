<template>
  <div class="login-page">
    <!-- 左侧装饰区 -->
    <div class="login-left">
      <div class="left-content">
        <span class="logo-icon">✦</span>
        <h1 class="brand-title">博客管理系统</h1>
        <p class="brand-desc">高效管理，轻松运营</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据统计</span>
          </div>
          <div class="feature-item">
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </div>
          <div class="feature-item">
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-right">
      <div class="login-card">
        <div class="login-header">
          <h2 class="login-title">管理员登录</h2>
          <p class="login-subtitle">请输入您的账号密码</p>
        </div>
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="用户名" 
              :prefix-icon="User" 
              size="large"
              autocomplete="off"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="密码" 
              :prefix-icon="Lock" 
              size="large" 
              show-password
              autocomplete="new-password"
              @keyup.enter="handleLogin" 
            />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              :loading="loading" 
              @click="handleLogin" 
              class="login-btn"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 主题切换 -->
      <div class="theme-toggle" @click="toggleTheme">
        <el-icon v-if="themeStore.isDark"><Sunny /></el-icon>
        <el-icon v-else><Moon /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Sunny, Moon, DataAnalysis, Document, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()
const formRef = ref()
const loading = ref(false)

const form = ref({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function toggleTheme(event) {
  themeStore.toggleTheme(event)
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form.value)
    await userStore.fetchUserInfo()
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
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
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.1) 0%, rgba(236, 72, 153, 0.1) 100%);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(168, 85, 247, 0.08) 0%, transparent 50%);
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
  font-size: 36px;
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
  position: relative;
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
  background: $primary-gradient;
  border: none;
  
  &:hover {
    opacity: 0.9;
  }
}

.theme-toggle {
  position: absolute;
  top: 24px;
  right: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-darker);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  
  .el-icon {
    font-size: 20px;
    color: var(--text-muted);
  }
  
  &:hover {
    background: var(--bg-card-hover);
    border-color: var(--border-light);
  }
}
</style>
