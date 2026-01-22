<template>
  <div class="version-history-page">
    <div class="page-header">
      <div class="header-container" :style="containerBgStyle">
        <div class="header-icon">
          <el-icon><Histogram /></el-icon>
        </div>
        <h1 class="page-title">版本历史</h1>
        <p class="page-subtitle">记录每一次进步的足迹</p>
      </div>
    </div>

    <div class="content-container" :style="containerBgStyle">
      <div v-loading="loading" class="timeline-wrapper">
        <div v-if="versionList.length === 0" class="empty-state">
          <el-icon class="empty-icon"><Document /></el-icon>
          <p>暂无版本记录</p>
        </div>

        <div v-else class="timeline">
          <div
            v-for="version in versionList"
            :key="version.id"
            class="timeline-item"
          >
            <div 
              class="version-card"
              :class="{ 'is-expanded': expandedVersions.includes(version.id) }"
              @mouseenter="handleMouseEnter(version.id)"
              @mouseleave="handleMouseLeave"
            >
              <div class="version-header" @click="toggleVersion(version.id)">
                <div class="version-meta">
                  <span class="version-number">
                    <el-icon><PriceTag /></el-icon>
                    v{{ version.versionNumber }}
                  </span>
                  <div v-if="version.isMajor" class="major-button" data-tooltip="重大版本">
                    <div class="button-wrapper">
                      <div class="text">MAJOR</div>
                      <span class="icon">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                          <path d="M3.612 15.443c-.386.198-.824-.149-.746-.592l.83-4.73L.173 6.765c-.329-.314-.158-.888.283-.95l4.898-.696L7.538.792c.197-.39.73-.39.927 0l2.184 4.327 4.898.696c.441.062.612.636.282.95l-3.522 3.356.83 4.73c.078.443-.36.79-.746.592L8 13.187l-4.389 2.256z"/>
                        </svg>
                      </span>
                    </div>
                  </div>
                </div>
                <div class="header-right">
                  <span class="version-date">
                    <el-icon><Calendar /></el-icon>
                    {{ formatDate(version.releaseDate) }}
                  </span>
                  <el-icon class="collapse-icon">
                    <ArrowDown v-if="!expandedVersions.includes(version.id)" />
                    <ArrowUp v-else />
                  </el-icon>
                </div>
              </div>

              <h2 class="version-name">{{ version.versionName }}</h2>

              <transition name="expand">
                <div v-show="expandedVersions.includes(version.id)" class="version-content">
                  <p v-if="version.description" class="version-description">
                    {{ version.description }}
                  </p>

                  <div class="version-details">
                    <div v-if="parseJson(version.features).length > 0" class="detail-section feature-section">
                      <div class="section-header">
                        <div class="section-icon">
                          <el-icon><Plus /></el-icon>
                        </div>
                        <span class="section-label">新增功能</span>
                        <span class="section-count">{{ parseJson(version.features).length }}</span>
                      </div>
                      <div class="section-content">
                        <ul class="feature-list">
                          <li v-for="(feature, idx) in parseJson(version.features)" :key="idx">
                            <span class="list-dot"></span>
                            <span class="list-text">{{ feature }}</span>
                          </li>
                        </ul>
                      </div>
                    </div>

                    <div v-if="parseJson(version.improvements).length > 0" class="detail-section improvement-section">
                      <div class="section-header">
                        <div class="section-icon">
                          <el-icon><Promotion /></el-icon>
                        </div>
                        <span class="section-label">优化改进</span>
                        <span class="section-count">{{ parseJson(version.improvements).length }}</span>
                      </div>
                      <div class="section-content">
                        <ul class="feature-list">
                          <li v-for="(improvement, idx) in parseJson(version.improvements)" :key="idx">
                            <span class="list-dot"></span>
                            <span class="list-text">{{ improvement }}</span>
                          </li>
                        </ul>
                      </div>
                    </div>

                    <div v-if="parseJson(version.bugFixes).length > 0" class="detail-section bugfix-section">
                      <div class="section-header">
                        <div class="section-icon">
                          <el-icon><CircleCheck /></el-icon>
                        </div>
                        <span class="section-label">问题修复</span>
                        <span class="section-count">{{ parseJson(version.bugFixes).length }}</span>
                      </div>
                      <div class="section-content">
                        <ul class="feature-list">
                          <li v-for="(bugFix, idx) in parseJson(version.bugFixes)" :key="idx">
                            <span class="list-dot"></span>
                            <span class="list-text">{{ bugFix }}</span>
                          </li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Document, Histogram, Calendar, Star, PriceTag, Plus, Promotion, CircleCheck, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { getSystemVersionList } from '@/api/systemVersion'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const loading = ref(false)
const versionList = ref([])
const expandedVersions = ref([])
const hoveredVersion = ref(null)

// 计算容器背景样式
const containerBgStyle = computed(() => {
  const isDark = themeStore.isDark
  
  if (themeStore.hasCustomBackground) {
    // 特殊背景主题：透明
    if (isDark) {
      return {
        background: 'rgba(39, 39, 42, 0.6)',
        backdropFilter: 'blur(20px)',
        WebkitBackdropFilter: 'blur(20px)'
      }
    } else {
      return {
        background: 'rgba(255, 255, 255, 0.6)',
        backdropFilter: 'blur(20px)',
        WebkitBackdropFilter: 'blur(20px)'
      }
    }
  } else {
    // 默认背景主题：非透明
    if (isDark) {
      return {
        background: '#27272a',
        backdropFilter: 'none',
        WebkitBackdropFilter: 'none'
      }
    } else {
      return {
        background: '#ffffff',
        backdropFilter: 'none',
        WebkitBackdropFilter: 'none'
      }
    }
  }
})

onMounted(() => {
  fetchVersionList()
})

async function fetchVersionList() {
  loading.value = true
  try {
    const res = await getSystemVersionList()
    versionList.value = res.data
    // 默认全部收起，方便用户浏览
    expandedVersions.value = []
  } finally {
    loading.value = false
  }
}

function toggleVersion(versionId) {
  const index = expandedVersions.value.indexOf(versionId)
  if (index > -1) {
    expandedVersions.value.splice(index, 1)
  } else {
    expandedVersions.value.push(versionId)
  }
}

function handleMouseEnter(versionId) {
  hoveredVersion.value = versionId
}

function handleMouseLeave() {
  hoveredVersion.value = null
}

function parseJson(jsonStr) {
  if (!jsonStr) return []
  try {
    return JSON.parse(jsonStr)
  } catch {
    return []
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}.${month}.${day}`
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.version-history-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 60px 20px 100px;
  min-height: 100vh;
}

// 开发者介绍卡片
.developer-card-container {
  margin-bottom: 40px;
}

.developer-card {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 24px;
  background: rgba(var(--bg-card-rgb), 0.6);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 12px;
  padding: 30px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 
      0 12px 40px rgba(0, 0, 0, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.05);
  }
}

.developer-avatar {
  flex-shrink: 0;
}

.avatar-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color) 0%, #ec4899 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  color: white;
  box-shadow: 0 4px 16px rgba(168, 85, 247, 0.3);
}

.developer-info {
  flex: 1;
  min-width: 0;
}

.developer-name {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 6px 0;
}

.developer-role {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0 0 12px 0;
}

.developer-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 16px 0;
}

.developer-links {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.link-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-size: 13px;
  color: var(--text-secondary);
  background: var(--bg-dark);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  text-decoration: none;
  transition: all 0.2s ease;
  
  .el-icon {
    font-size: 14px;
  }
  
  &:hover {
    color: var(--primary-color);
    border-color: var(--primary-color);
    background: rgba(var(--primary-color-rgb, 168, 85, 247), 0.1);
  }
}

.page-header {
  margin-bottom: 40px;
}

.header-container {
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
  border-radius: 12px;
  padding: 40px 50px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.header-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  
  .el-icon {
    font-size: 48px;
    color: var(--primary-color);
  }
}

.page-title {
  font-size: 32px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 12px;
  letter-spacing: 0.5px;
}

.page-subtitle {
  font-size: 15px;
  color: var(--text-muted);
  font-weight: 400;
}

.content-container {
  max-width: 800px;
  margin: 0 auto;
  border-radius: 12px;
  padding: 60px 70px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.timeline-wrapper {
  min-height: 400px;
}

.empty-state {
  text-align: center;
  padding: 100px 20px;
  color: var(--text-muted);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.3;
}

.timeline {
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 2px;
    background: var(--border-color);
  }
}

.timeline-item {
  position: relative;
  padding-left: 40px;
  margin-bottom: 60px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &::before {
    content: '';
    position: absolute;
    left: -4px;
    top: 6px;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: var(--bg-card);
    border: 2px solid var(--primary-color);
    transition: all 0.2s ease;
  }
  
  &:hover::before {
    transform: scale(1.3);
    background: var(--primary-color);
  }
}

.version-card {
  position: relative;
  padding: 20px;
  margin: -20px;
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
  
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 8px;
    background: rgba(var(--bg-card-rgb), 0.3);
    opacity: 0;
    transition: opacity 0.3s ease;
    pointer-events: none;
  }
  
  &:hover {
    transform: translateX(6px);
    
    &::before {
      opacity: 1;
    }
    
    .collapse-icon {
      color: var(--primary-color);
    }
  }
  
  &.is-expanded {
    .collapse-icon {
      transform: rotate(0deg);
    }
  }
}

.version-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 20px;
  cursor: pointer;
  user-select: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-icon {
  font-size: 18px;
  color: var(--text-muted);
  transition: all 0.3s ease;
}

.version-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.version-number {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-color);
  font-family: 'Courier New', monospace;
  
  .el-icon {
    font-size: 16px;
  }
}

.major-button {
  --width: 90px;
  --height: 28px;
  --tooltip-height: 28px;
  --tooltip-width: 80px;
  --gap-between-tooltip-to-button: 12px;
  --button-color: linear-gradient(135deg, #a855f7 0%, #ec4899 100%);
  --tooltip-color: #fff;
  width: var(--width);
  height: var(--height);
  background: var(--button-color);
  position: relative;
  text-align: center;
  border-radius: 0.45em;
  font-family: "Arial", sans-serif;
  transition: all 0.3s;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(168, 85, 247, 0.3);
  
  &::before {
    position: absolute;
    content: attr(data-tooltip);
    width: var(--tooltip-width);
    height: var(--tooltip-height);
    background: linear-gradient(135deg, #a855f7 0%, #ec4899 100%);
    font-size: 0.75rem;
    color: #fff;
    border-radius: 0.25em;
    line-height: var(--tooltip-height);
    bottom: calc(var(--height) + var(--gap-between-tooltip-to-button) + 10px);
    left: calc(50% - var(--tooltip-width) / 2);
    opacity: 0;
    visibility: hidden;
    transition: all 0.5s;
    z-index: 10;
    box-shadow: 0 4px 12px rgba(168, 85, 247, 0.4);
  }
  
  &::after {
    position: absolute;
    content: "";
    width: 0;
    height: 0;
    border: 8px solid transparent;
    border-top-color: #a855f7;
    left: calc(50% - 8px);
    bottom: calc(100% + var(--gap-between-tooltip-to-button) - 10px);
    opacity: 0;
    visibility: hidden;
    transition: all 0.5s;
    z-index: 10;
  }
  
  .button-wrapper {
    overflow: hidden;
    position: absolute;
    width: 100%;
    height: 100%;
    left: 0;
    top: 0;
    color: #fff;
  }
  
  .text {
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    position: absolute;
    width: 100%;
    height: 100%;
    left: 0;
    top: 0;
    color: #fff;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.5px;
    transition: top 0.5s;
  }
  
  .icon {
    overflow: hidden;
    position: absolute;
    width: 100%;
    height: 100%;
    left: 0;
    color: #fff;
    top: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: top 0.5s;
    
    svg {
      width: 16px;
      height: 16px;
    }
  }
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(168, 85, 247, 0.5);
    
    .text {
      top: -100%;
    }
    
    .icon {
      top: 0;
    }
    
    &::before,
    &::after {
      opacity: 1;
      visibility: visible;
    }
    
    &::after {
      bottom: calc(var(--height) + var(--gap-between-tooltip-to-button) - 16px);
    }
    
    &::before {
      bottom: calc(var(--height) + var(--gap-between-tooltip-to-button));
    }
  }
}

.version-date {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-disabled);
  
  .el-icon {
    font-size: 14px;
  }
}

.version-name {
  font-size: 22px;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0 0 16px 0;
  cursor: pointer;
  user-select: none;
}

.version-content {
  overflow: hidden;
}

// 折叠展开动画
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  max-height: 2000px;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
  transform: translateY(-10px);
}

.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  max-height: 2000px;
  transform: translateY(0);
}

.version-description {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.8;
  margin-bottom: 24px;
  padding-left: 12px;
  border-left: 2px solid var(--border-color);
}

.version-details {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-section {
  background: var(--bg-dark);
  border-radius: 8px;
  padding: 16px;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  
  &:hover {
    border-color: var(--primary-color);
    box-shadow: 0 2px 8px rgba(168, 85, 247, 0.1);
  }
  
  .section-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
    padding-bottom: 10px;
    border-bottom: 1px solid var(--border-color);
  }
  
  .section-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    
    .el-icon {
      font-size: 18px;
    }
  }
  
  .section-label {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
    flex: 1;
  }
  
  .section-count {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 22px;
    height: 22px;
    padding: 0 6px;
    font-size: 12px;
    font-weight: 600;
    color: var(--text-muted);
    background: var(--bg-card);
    border-radius: 11px;
    border: 1px solid var(--border-color);
  }
  
  .section-content {
    padding: 0;
  }
}

// 不同类型的图标颜色
.feature-section .section-icon .el-icon {
  color: #10b981;
}

.improvement-section .section-icon .el-icon {
  color: #3b82f6;
}

.bugfix-section .section-icon .el-icon {
  color: #f59e0b;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
  
  li {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 8px 0;
    color: var(--text-secondary);
    font-size: 14px;
    line-height: 1.6;
    transition: all 0.2s ease;
    
    &:hover {
      color: var(--text-primary);
      
      .list-dot {
        background: var(--primary-color);
        transform: scale(1.3);
      }
    }
  }
}

.list-dot {
  flex-shrink: 0;
  width: 5px;
  height: 5px;
  margin-top: 7px;
  border-radius: 50%;
  background: var(--text-disabled);
  transition: all 0.2s ease;
}

.list-text {
  flex: 1;
}

// 暗色主题字体颜色统一
:root:not([data-theme="light"]) {
  .page-title,
  .version-name,
  .section-label,
  .developer-name {
    color: #dfdfd6;
  }
  
  .page-subtitle,
  .version-description,
  .list-text,
  .developer-desc,
  .link-btn,
  .empty-state {
    color: #dfdfd6;
  }
  
  .version-date,
  .collapse-icon,
  .section-count,
  .developer-role {
    color: #dfdfd6;
  }
  
  .feature-list li {
    color: #dfdfd6;
    
    &:hover {
      color: #dfdfd6;
    }
  }
}

@media (max-width: 768px) {
  .version-history-page {
    padding: 40px 16px 80px;
  }
  
  .developer-card-container {
    margin-bottom: 30px;
  }
  
  .developer-card {
    flex-direction: column;
    text-align: center;
    padding: 24px;
  }
  
  .avatar-circle {
    width: 70px;
    height: 70px;
    font-size: 28px;
  }
  
  .developer-name {
    font-size: 20px;
  }
  
  .developer-desc {
    font-size: 13px;
  }
  
  .developer-links {
    justify-content: center;
  }
  
  .page-header {
    margin-bottom: 30px;
  }
  
  .header-container {
    padding: 30px 24px;
  }
  
  .header-icon {
    margin-bottom: 16px;
    
    .el-icon {
      font-size: 40px;
    }
  }
  
  .page-title {
    font-size: 26px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .content-container {
    padding: 40px 24px;
  }
  
  .timeline-item {
    padding-left: 32px;
    margin-bottom: 50px;
  }
  
  .version-name {
    font-size: 20px;
  }
  
  .version-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
