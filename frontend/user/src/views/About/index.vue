<template>
  <div class="about-page">
    <!-- 3D 博主介绍卡片 -->
    <div class="blogger-card-wrapper">
      <div class="parent">
        <div class="card">
          <!-- Logo 圆形装饰 -->
          <div class="logo">
            <span class="circle circle1"></span>
            <span class="circle circle2"></span>
            <span class="circle circle3"></span>
            <span class="circle circle4"></span>
            <span class="circle circle5">
              <span class="avatar-letter">B</span>
            </span>
          </div>
          
          <!-- 玻璃效果层 -->
          <div class="glass"></div>
          
          <!-- 内容区域 -->
          <div class="content">
            <span class="title">不辉放弃</span>
            <span class="subtitle">全栈开发工程师 / 技术博主</span>
            <span class="text">
              热爱编程，专注于 Web 全栈开发与系统架构设计。喜欢探索新技术，乐于分享学习心得。
              希望通过这个博客记录成长轨迹，与志同道合的朋友交流技术、共同进步。
            </span>
          </div>
          
          <!-- 底部区域 -->
          <div class="bottom">
            <div class="social-buttons-container">
              <a href="https://github.com/miaohui789" target="_blank" class="social-button" title="GitHub">
                <svg viewBox="0 0 24 24" class="svg">
                  <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
                </svg>
              </a>
              <a href="https://space.bilibili.com/1589988727" target="_blank" class="social-button" title="Bilibili">
                <svg viewBox="0 0 24 24" class="svg">
                  <path d="M17.813 4.653h.854c1.51.054 2.769.578 3.773 1.574 1.004.995 1.524 2.249 1.56 3.76v7.36c-.036 1.51-.556 2.769-1.56 3.773s-2.262 1.524-3.773 1.56H5.333c-1.51-.036-2.769-.556-3.773-1.56S.036 18.858 0 17.347v-7.36c.036-1.511.556-2.765 1.56-3.76 1.004-.996 2.262-1.52 3.773-1.574h.774l-1.174-1.12a1.234 1.234 0 0 1-.373-.906c0-.356.124-.659.373-.907l.027-.027c.267-.249.573-.373.92-.373.347 0 .653.124.92.373L9.653 4.44c.071.071.134.142.187.213h4.267a.836.836 0 0 1 .16-.213l2.853-2.747c.267-.249.573-.373.92-.373.347 0 .662.151.929.4.267.249.391.551.391.907 0 .355-.124.657-.373.906zM5.333 7.24c-.746.018-1.373.276-1.88.773-.506.498-.769 1.13-.786 1.894v7.52c.017.764.28 1.395.786 1.893.507.498 1.134.756 1.88.773h13.334c.746-.017 1.373-.275 1.88-.773.506-.498.769-1.129.786-1.893v-7.52c-.017-.765-.28-1.396-.786-1.894-.507-.497-1.134-.755-1.88-.773zM8 11.107c.373 0 .684.124.933.373.25.249.383.569.4.96v1.173c-.017.391-.15.711-.4.96-.249.25-.56.374-.933.374s-.684-.125-.933-.374c-.25-.249-.383-.569-.4-.96V12.44c0-.373.129-.689.386-.947.258-.257.574-.386.947-.386zm8 0c.373 0 .684.124.933.373.25.249.383.569.4.96v1.173c-.017.391-.15.711-.4.96-.249.25-.56.374-.933.374s-.684-.125-.933-.374c-.25-.249-.383-.569-.4-.96V12.44c.017-.391.15-.711.4-.96.249-.249.56-.373.933-.373z"/>
                </svg>
              </a>
              <a href="mailto:example@email.com" class="social-button" title="邮箱">
                <svg viewBox="0 0 24 24" class="svg">
                  <path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/>
                </svg>
              </a>
            </div>
            <div class="stats-container">
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
          </div>
        </div>
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
import { InfoFilled, Cpu } from '@element-plus/icons-vue'
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

/* 3D 博主卡片容器 */
.blogger-card-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 3D 卡片 - 横向布局 */
.parent {
  width: 100%;
  max-width: 600px;
  height: 280px;
  perspective: 1200px;
}

.card {
  width: 100%;
  height: 100%;
  border-radius: 30px;
  background: var(--card-bg);
  transition: all 0.6s ease-in-out;
  transform-style: preserve-3d;
  box-shadow:
    rgba(30, 30, 60, 0) 40px 50px 25px -40px,
    rgba(30, 30, 60, 0.2) 0px 25px 25px -5px;
  position: relative;
}

/* 玻璃效果层 */
.glass {
  transform-style: preserve-3d;
  position: absolute;
  inset: 8px;
  border-radius: 35px;
  border-top-left-radius: 100%;
  background: var(--glass-bg);
  transform: translate3d(0px, 0px, 30px);
  border-right: 1px solid var(--glass-border);
  border-bottom: 1px solid var(--glass-border);
  transition: all 0.6s ease-in-out;
}

/* 内容区域 */
.content {
  position: absolute;
  top: 0;
  left: 180px;
  right: 0;
  padding: 28px 28px 0 20px;
  transform: translate3d(0, 0, 31px);
}

.content .title {
  display: block;
  color: var(--card-title);
  font-weight: 900;
  font-size: 24px;
}

.content .subtitle {
  display: block;
  color: var(--card-subtitle);
  font-size: 13px;
  font-weight: 500;
  margin-top: 4px;
}

.content .text {
  display: block;
  color: var(--card-text);
  font-size: 13px;
  margin-top: 12px;
  line-height: 1.7;
}

/* 底部区域 */
.bottom {
  padding: 12px 20px;
  transform-style: preserve-3d;
  position: absolute;
  bottom: 20px;
  left: 180px;
  right: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transform: translate3d(0, 0, 31px);
}

/* 社交按钮 */
.social-buttons-container {
  display: flex;
  gap: 12px;
  transform-style: preserve-3d;
}

.social-button {
  width: 34px;
  height: 34px;
  padding: 7px;
  background: var(--social-btn-bg);
  border-radius: 50%;
  border: none;
  display: grid;
  place-content: center;
  box-shadow: rgba(30, 30, 60, 0.4) 0px 8px 6px -5px;
  cursor: pointer;
  text-decoration: none;
}

.social-button:first-child {
  transition: transform 0.3s ease-in-out 0.4s, box-shadow 0.3s ease-in-out 0.4s, background 0.2s;
}

.social-button:nth-child(2) {
  transition: transform 0.3s ease-in-out 0.6s, box-shadow 0.3s ease-in-out 0.6s, background 0.2s;
}

.social-button:nth-child(3) {
  transition: transform 0.3s ease-in-out 0.8s, box-shadow 0.3s ease-in-out 0.8s, background 0.2s;
}

.social-button .svg {
  width: 18px;
  height: 18px;
  fill: var(--social-icon);
}

.social-button:hover {
  background: var(--social-btn-hover);
}

.social-button:hover .svg {
  fill: #ffffff;
}

/* 统计数据 */
.stats-container {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--card-title);
}

.stat-label {
  font-size: 12px;
  color: var(--card-text);
}

/* Logo 圆形装饰 */
.logo {
  position: absolute;
  left: 0;
  top: 0;
  transform-style: preserve-3d;
}

.logo .circle {
  display: block;
  position: absolute;
  aspect-ratio: 1;
  border-radius: 50%;
  top: 0;
  left: 0;
  box-shadow: rgba(100, 100, 111, 0.2) 10px 10px 20px 0px;
  background: var(--circle-bg);
  transition: all 0.6s ease-in-out;
}

.logo .circle1 {
  width: 200px;
  transform: translate3d(0, 0, 25px);
  top: 40px;
  left: -10px;
}

.logo .circle2 {
  width: 160px;
  transform: translate3d(0, 0, 45px);
  top: 50px;
  left: 0px;
  transition-delay: 0.2s;
}

.logo .circle3 {
  width: 120px;
  transform: translate3d(0, 0, 65px);
  top: 60px;
  left: 10px;
  transition-delay: 0.4s;
}

.logo .circle4 {
  width: 80px;
  transform: translate3d(0, 0, 85px);
  top: 75px;
  left: 25px;
  transition-delay: 0.6s;
}

.logo .circle5 {
  width: 50px;
  transform: translate3d(0, 0, 105px);
  top: 90px;
  left: 40px;
  display: grid;
  place-content: center;
  transition-delay: 0.8s;
  background: var(--primary-color);
}

.avatar-letter {
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
}

/* 悬停效果 */
.parent:hover .card {
  transform: rotate3d(1, -1, 0, 20deg);
  box-shadow:
    rgba(30, 30, 60, 0.3) 30px 50px 25px -40px,
    rgba(30, 30, 60, 0.15) 0px 25px 30px 0px;
}

.parent:hover .card .bottom .social-button {
  transform: translate3d(0, 0, 60px);
  box-shadow: rgba(30, 30, 60, 0.25) 5px 20px 10px 0px;
}

.parent:hover .card .logo .circle2 {
  transform: translate3d(0, 0, 65px);
}

.parent:hover .card .logo .circle3 {
  transform: translate3d(0, 0, 85px);
}

.parent:hover .card .logo .circle4 {
  transform: translate3d(0, 0, 105px);
}

.parent:hover .card .logo .circle5 {
  transform: translate3d(0, 0, 125px);
}

/* 主题变量 - 暗色 */
.card {
  --card-bg: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
  --glass-bg: linear-gradient(0deg, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0.4) 100%);
  --glass-border: rgba(255, 255, 255, 0.4);
  --card-title: #f0f9ff;
  --card-subtitle: rgba(186, 230, 253, 0.9);
  --card-text: rgba(224, 242, 254, 0.85);
  --circle-bg: rgba(96, 165, 250, 0.35);
  --social-btn-bg: rgba(255, 255, 255, 0.92);
  --social-btn-hover: #1e40af;
  --social-icon: #1e40af;
}

/* 亮色主题 */
:root[data-theme="light"] .card {
  --card-bg: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  --glass-bg: linear-gradient(0deg, rgba(255, 255, 255, 0.25) 0%, rgba(255, 255, 255, 0.6) 100%);
  --glass-border: rgba(255, 255, 255, 0.5);
  --card-title: #1e3a5f;
  --card-subtitle: rgba(30, 58, 95, 0.8);
  --card-text: rgba(30, 58, 95, 0.75);
  --circle-bg: rgba(59, 130, 246, 0.3);
  --social-btn-bg: rgba(255, 255, 255, 0.92);
  --social-btn-hover: #1e3a5f;
  --social-icon: #1e3a5f;
}

/* 关于本站内容 */
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

/* 响应式 */
@media (max-width: 600px) {
  .parent {
    height: 380px;
  }
  
  .logo .circle1 {
    width: 160px;
    top: 15px;
    left: 50%;
    transform: translateX(-50%) translate3d(0, 0, 25px);
  }
  
  .logo .circle2 {
    width: 130px;
    top: 25px;
    left: 50%;
    transform: translateX(-50%) translate3d(0, 0, 45px);
  }
  
  .logo .circle3 {
    width: 100px;
    top: 35px;
    left: 50%;
    transform: translateX(-50%) translate3d(0, 0, 65px);
  }
  
  .logo .circle4 {
    width: 70px;
    top: 50px;
    left: 50%;
    transform: translateX(-50%) translate3d(0, 0, 85px);
  }
  
  .logo .circle5 {
    width: 45px;
    top: 65px;
    left: 50%;
    transform: translateX(-50%) translate3d(0, 0, 105px);
  }
  
  .content {
    left: 20px;
    top: 140px;
    padding: 0 20px;
  }
  
  .content .title {
    font-size: 20px;
  }
  
  .content .text {
    font-size: 12px;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .bottom {
    left: 20px;
    flex-direction: column;
    gap: 15px;
  }
  
  .stats-container {
    order: -1;
  }
}
</style>
