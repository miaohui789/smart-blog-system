<template>
  <div class="about-page">
    <div class="about-card glass-card">
      <div class="about-avatar">
        <el-avatar :size="120" src="">
          <span class="avatar-text">B</span>
        </el-avatar>
      </div>
      <h1 class="about-name text-gradient">博主名称</h1>
      <p class="about-intro">一个热爱技术的开发者，专注于 Web 开发与系统架构</p>
      <div class="about-stats">
        <div class="stat-item">
          <span class="stat-value">{{ stats.articleCount }}</span>
          <span class="stat-label">文章</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ stats.categoryCount }}</span>
          <span class="stat-label">分类</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ stats.tagCount }}</span>
          <span class="stat-label">标签</span>
        </div>
      </div>
      <div class="about-social">
        <a href="#" class="social-link" title="GitHub">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
          </svg>
        </a>
        <a href="#" class="social-link" title="邮箱">
          <el-icon><Message /></el-icon>
        </a>
        <a href="#" class="social-link" title="网站">
          <el-icon><Link /></el-icon>
        </a>
      </div>
    </div>

    <div class="about-content glass-card">
      <h2>
        <el-icon><InfoFilled /></el-icon>
        关于本站
      </h2>
      <p>这是一个使用现代技术栈构建的个人博客系统，旨在分享技术文章、记录学习心得。</p>
      
      <h2>
        <el-icon><Cpu /></el-icon>
        技术栈
      </h2>
      <div class="tech-stack">
        <div class="tech-group">
          <h3>前端</h3>
          <div class="tech-tags">
            <span class="tech-tag">Vue 3</span>
            <span class="tech-tag">Pinia</span>
            <span class="tech-tag">Vue Router</span>
            <span class="tech-tag">Element Plus</span>
            <span class="tech-tag">Vite</span>
            <span class="tech-tag">SCSS</span>
          </div>
        </div>
        <div class="tech-group">
          <h3>后端</h3>
          <div class="tech-tags">
            <span class="tech-tag">Spring Boot</span>
            <span class="tech-tag">MyBatis Plus</span>
            <span class="tech-tag">MySQL</span>
            <span class="tech-tag">Redis</span>
            <span class="tech-tag">MinIO</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Link, Message, InfoFilled, Cpu } from '@element-plus/icons-vue'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'

const stats = ref({
  articleCount: 0,
  categoryCount: 0,
  tagCount: 0
})

onMounted(async () => {
  try {
    const [categoryRes, tagRes] = await Promise.all([
      getCategoryList(),
      getTagList()
    ])
    stats.value.categoryCount = categoryRes.data?.length || 0
    stats.value.tagCount = tagRes.data?.length || 0
  } catch (e) {
    console.error(e)
  }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.about-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-xl;
  max-width: 800px;
}

.about-card {
  padding: $spacing-2xl;
  text-align: center;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: background-color 0.3s, border-color 0.3s;
}

.about-avatar {
  margin-bottom: $spacing-lg;

  :deep(.el-avatar) {
    background: $primary-gradient;
    font-size: 48px;
    font-weight: 700;
  }
}

.avatar-text {
  font-size: 48px;
  font-weight: 700;
}

.about-name {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: $spacing-sm;
}

.about-intro {
  color: var(--text-secondary);
  margin-bottom: $spacing-xl;
  font-size: 15px;
  line-height: 1.6;
  transition: color 0.3s;
}

.about-stats {
  display: flex;
  justify-content: center;
  gap: $spacing-2xl;
  margin-bottom: $spacing-xl;
  padding: $spacing-lg 0;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
  transition: border-color 0.3s;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-label {
  font-size: 13px;
  color: var(--text-muted);
  transition: color 0.3s;
}

.about-social {
  display: flex;
  justify-content: center;
  gap: $spacing-md;
}

.social-link {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-card-hover);
  border: 1px solid var(--border-color);
  border-radius: 50%;
  color: var(--text-secondary);
  transition: all 0.3s;

  &:hover {
    background: $primary-gradient;
    border-color: transparent;
    color: white;
    transform: translateY(-2px);
  }
}

.about-content {
  padding: $spacing-xl;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: background-color 0.3s, border-color 0.3s;

  h2 {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-size: 20px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: $spacing-md;
    margin-top: $spacing-xl;
    transition: color 0.3s;

    &:first-child {
      margin-top: 0;
    }

    .el-icon {
      color: $primary-color;
    }
  }

  p {
    color: var(--text-secondary);
    line-height: 1.8;
    transition: color 0.3s;
  }
}

.tech-stack {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.tech-group {
  h3 {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-muted);
    margin-bottom: $spacing-sm;
    transition: color 0.3s;
  }
}

.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.tech-tag {
  background: rgba($primary-color, 0.1);
  color: $primary-light;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}
</style>
