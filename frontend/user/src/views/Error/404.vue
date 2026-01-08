<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">404</h1>
      <p class="error-message">哎呀，页面走丢了</p>
      <p class="error-desc">你访问的页面不存在或已被移除</p>
      <el-button type="primary" size="large" @click="$router.push('/')">
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
      radial-gradient(ellipse 80% 50% at 20% -20%, rgba(74, 158, 255, 0.1), transparent),
      radial-gradient(ellipse 60% 40% at 80% 100%, rgba(99, 102, 241, 0.08), transparent);
    pointer-events: none;
  }
}

.error-content {
  text-align: center;
  position: relative;
  z-index: 1;
}

.error-code {
  font-size: 150px;
  font-weight: 800;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: 1;
  margin-bottom: $spacing-md;
  text-shadow: 0 0 60px rgba($primary-color, 0.3);
}

.error-message {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: $spacing-sm;
  transition: color 0.3s;
}

.error-desc {
  font-size: 15px;
  color: var(--text-muted);
  margin-bottom: $spacing-xl;
  transition: color 0.3s;
}
</style>
