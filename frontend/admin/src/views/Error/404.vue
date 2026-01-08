<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">404</h1>
      <p class="error-message">页面不存在</p>
      <p class="error-desc">抱歉，您访问的页面不存在或已被移除</p>
      <el-button type="primary" @click="$router.push('/dashboard')">
        <el-icon><HomeFilled /></el-icon>
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { HomeFilled } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-dark);
  transition: background-color 0.3s;
}

.error-content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 16px;
}

.error-message {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  transition: color 0.3s;
}

.error-desc {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 32px;
  transition: color 0.3s;
}
</style>
