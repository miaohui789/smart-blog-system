<template>
  <div class="study-index-page">
    <section class="study-hero glass-card">
      <div class="hero-left">
        <div class="hero-badge">学习模块</div>
        <h1 class="hero-title">面试题学习中心</h1>
        <p class="hero-desc">围绕题库、学习进度、抽查复盘建立完整闭环。选择下方模块开始学习。</p>
      </div>
      <div class="hero-stats" v-if="userStore.isLoggedIn">
        <div class="stat-card" v-for="item in statCards" :key="item.label">
          <span class="stat-label">{{ item.label }}</span>
          <strong class="stat-value">{{ item.value }}</strong>
        </div>
      </div>
    </section>

    <div class="module-grid">
      <div class="module-card glass-card" @click="$router.push('/study/learn')">
        <div class="module-icon learn-icon">
          <el-icon><Reading /></el-icon>
        </div>
        <div class="module-body">
          <h2>题库学习</h2>
          <p>浏览全部题目，按分类、难度、学习状态筛选，支持收藏和笔记。</p>
          <ul class="module-features">
            <li>分类 / 难度 / 状态筛选</li>
            <li>随机抽题</li>
            <li>收藏 &amp; 笔记</li>
            <li>掌握度管理</li>
          </ul>
        </div>
        <div class="module-action">
          <el-button type="primary">进入学习 <el-icon><ArrowRight /></el-icon></el-button>
        </div>
      </div>

      <div class="module-card glass-card" @click="goCheck">
        <div class="module-icon check-icon">
          <el-icon><Promotion /></el-icon>
        </div>
        <div class="module-body">
          <h2>抽查测试</h2>
          <p>创建抽查任务，支持随机、专项、错题、收藏等多种模式，结合 AI 评分复盘。</p>
          <ul class="module-features">
            <li>6 种抽查模式</li>
            <li>AI 评分 &amp; 自评</li>
            <li>查看标准答案</li>
            <li>任务历史记录</li>
          </ul>
        </div>
        <div class="module-action">
          <el-button :type="userStore.isLoggedIn ? 'primary' : 'default'">
            {{ userStore.isLoggedIn ? '开始抽查' : '登录后使用' }}
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="module-card glass-card" @click="goHistory">
        <div class="module-icon history-icon">
          <el-icon><DataAnalysis /></el-icon>
        </div>
        <div class="module-body">
          <h2>抽查历史</h2>
          <p>查看每次抽查任务的完成度、得分和记忆情况，持续追踪掌握变化。</p>
          <ul class="module-features">
            <li>任务完成度统计</li>
            <li>得分趋势</li>
            <li>错题回顾</li>
            <li>复盘详情</li>
          </ul>
        </div>
        <div class="module-action">
          <el-button :type="userStore.isLoggedIn ? 'primary' : 'default'">
            {{ userStore.isLoggedIn ? '查看历史' : '登录后使用' }}
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, DataAnalysis, Promotion, Reading } from '@element-plus/icons-vue'
import { getStudyOverview } from '@/api/study'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const overview = ref({})

const statCards = computed(() => [
  { label: '题库总量', value: overview.value.totalQuestions || 0 },
  { label: '已学习', value: overview.value.studiedQuestions || 0 },
  { label: '已掌握', value: overview.value.masteredQuestions || 0 },
  { label: '平均得分', value: overview.value.avgScore || 0 }
])

function goCheck() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  router.push('/study/check')
}

function goHistory() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  router.push('/study/check/history')
}

onMounted(async () => {
  if (userStore.isLoggedIn) {
    const res = await getStudyOverview()
    overview.value = res.data || {}
  }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.study-index-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.study-hero {
  padding: 28px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.hero-badge {
  width: fit-content;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba($primary-color, 0.12);
  color: $primary-color;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 14px;
}

.hero-title {
  font-size: 28px;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.hero-desc {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.8;
  max-width: 500px;
}

.hero-stats {
  display: flex;
  gap: 14px;
  flex-shrink: 0;
}

.stat-card {
  padding: 16px 20px;
  border-radius: 14px;
  border: 1px solid var(--border-color);
  background: rgba($primary-color, 0.05);
  min-width: 90px;
  text-align: center;
}

.stat-label {
  display: block;
  color: var(--text-muted);
  font-size: 12px;
  margin-bottom: 6px;
}

.stat-value {
  color: var(--text-primary);
  font-size: 22px;
  font-weight: 700;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.module-card {
  padding: 28px 24px;
  border-radius: 20px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 18px;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;

  &:hover {
    transform: translateY(-4px);
    border-color: rgba($primary-color, 0.3);
    box-shadow: 0 16px 32px var(--shadow-color);
  }
}

.module-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;

  &.learn-icon {
    background: rgba(#3b82f6, 0.15);
    color: #3b82f6;
  }

  &.check-icon {
    background: rgba(#8b5cf6, 0.15);
    color: #8b5cf6;
  }

  &.history-icon {
    background: rgba(#22c55e, 0.15);
    color: #22c55e;
  }
}

.module-body {
  flex: 1;

  h2 {
    color: var(--text-primary);
    font-size: 20px;
    margin-bottom: 10px;
  }

  p {
    color: var(--text-secondary);
    font-size: 13px;
    line-height: 1.8;
    margin-bottom: 14px;
  }
}

.module-features {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;

  li {
    color: var(--text-muted);
    font-size: 13px;
    padding-left: 14px;
    position: relative;

    &::before {
      content: '·';
      position: absolute;
      left: 0;
      color: $primary-color;
    }
  }
}

.module-action {
  .el-button {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 1100px) {
  .module-grid {
    grid-template-columns: 1fr;
  }

  .study-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-stats {
    width: 100%;
    justify-content: space-between;
  }
}

@media (max-width: 768px) {
  .study-hero {
    padding: 20px;
  }

  .hero-stats {
    flex-wrap: wrap;
    gap: 10px;
  }

  .stat-card {
    flex: 1 1 calc(50% - 5px);
  }
}
</style>
