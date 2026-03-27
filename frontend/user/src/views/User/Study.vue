<template>
  <div class="study-dashboard-page">
    <section class="hero-panel glass-card">
      <div class="hero-main">
        <div class="hero-badge">学习中心</div>
        <h1 class="hero-title">我的学习</h1>
        <p class="hero-desc">
          这里不再一次性堆满所有学习内容，而是按“总览、复习、收藏、抽查”拆成独立子模块，
          方便你更快找到当下最想做的事。
        </p>
        <div class="hero-actions">
          <el-button type="primary" @click="goStudyLearn">
            <el-icon><Reading /></el-icon>
            去刷题
          </el-button>
          <el-button @click="goStudyCheck">
            <el-icon><Promotion /></el-icon>
            开始抽查
          </el-button>
        </div>
      </div>

      <div class="hero-side">
        <div class="focus-card">
          <span class="focus-label">当前建议</span>
          <strong class="focus-title">{{ currentSuggestion.title }}</strong>
          <p class="focus-desc">{{ currentSuggestion.desc }}</p>
          <el-button text @click="handleSuggestionAction">
            {{ currentSuggestion.actionText }}
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </section>

    <Loading v-if="loading" text="正在整理你的学习模块..." />

    <template v-else>
      <section class="module-nav glass-card">
        <button
          v-for="item in moduleCards"
          :key="item.key"
          class="module-card"
          :class="{ active: activeModule === item.key }"
          @click="switchModule(item.key)"
        >
          <div class="module-card-top">
            <div class="module-icon" :class="item.iconClass">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <span class="module-count">{{ item.count }}</span>
          </div>
          <h2>{{ item.title }}</h2>
          <p>{{ item.desc }}</p>
        </button>
      </section>

      <section class="content-shell glass-card">
        <div class="content-header">
          <div>
            <h2>{{ activeModuleConfig.title }}</h2>
            <p>{{ activeModuleConfig.desc }}</p>
          </div>
          <div class="content-actions">
            <el-button
              v-for="action in activeModuleConfig.actions"
              :key="action.label"
              :type="action.type || 'default'"
              :text="action.text"
              @click="action.handler"
            >
              {{ action.label }}
            </el-button>
          </div>
        </div>

        <div v-if="activeModule === 'overview'" class="module-panel overview-panel">
          <div class="summary-grid">
            <div v-for="item in summaryCards" :key="item.label" class="summary-card">
              <span class="summary-label">{{ item.label }}</span>
              <strong class="summary-value">{{ item.value }}</strong>
              <span class="summary-hint">{{ item.hint }}</span>
            </div>
          </div>

          <div class="two-col-grid">
            <div class="sub-panel">
              <div class="sub-panel-header">
                <h3>学习进度</h3>
                <span>围绕覆盖率和掌握率快速判断节奏</span>
              </div>
              <div class="progress-list">
                <div v-for="item in progressItems" :key="item.label" class="progress-item">
                  <div class="progress-head">
                    <span>{{ item.label }}</span>
                    <span>{{ item.detail }}</span>
                  </div>
                  <el-progress :percentage="item.percent" :stroke-width="10" :show-text="false" />
                </div>
              </div>
            </div>

            <div class="sub-panel">
              <div class="sub-panel-header">
                <h3>抽查表现</h3>
                <span>把成绩和任务完成情况放在一起看</span>
              </div>
              <div class="score-grid">
                <div class="score-card">
                  <span>平均分</span>
                  <strong>{{ formatScore(statistics.avgScore) }}</strong>
                </div>
                <div class="score-card">
                  <span>最高分</span>
                  <strong>{{ formatScore(statistics.bestScore) }}</strong>
                </div>
                <div class="score-card">
                  <span>最近得分</span>
                  <strong>{{ formatScore(statistics.latestScore) }}</strong>
                </div>
              </div>
              <div class="stat-pills">
                <span class="stat-pill">完成任务 {{ safeNumber(statistics.completedTaskCount) }}</span>
                <span class="stat-pill">答题 {{ safeNumber(statistics.answeredQuestionCount) }}</span>
                <span class="stat-pill">错题 {{ safeNumber(statistics.wrongQuestionCount) }}</span>
                <span class="stat-pill">收藏 {{ safeNumber(statistics.favoriteQuestionCount) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="activeModule === 'review'" class="module-panel review-panel">
          <div class="two-col-grid">
            <div class="sub-panel">
              <div class="sub-panel-header">
                <h3>优先复习题</h3>
                <span>先处理待复习和掌握度偏低的内容</span>
              </div>

              <div v-if="reviewQuestions.length" class="item-list">
                <button
                  v-for="item in reviewQuestions"
                  :key="item.id"
                  class="item-card"
                  @click="goQuestionDetail(item.id)"
                >
                  <div class="item-card-top">
                    <span class="category-chip">{{ item.categoryName || '未分类' }}</span>
                    <span class="status-chip warning">{{ statusText(item.studyStatus) }}</span>
                  </div>
                  <h3>{{ item.title }}</h3>
                  <div class="item-meta">
                    <span>掌握度 {{ safeNumber(item.masteryLevel) }}/5</span>
                    <span>学习 {{ safeNumber(item.studyCount) }} 次</span>
                    <span v-if="item.nextReviewTime">下次复习 {{ formatDate(item.nextReviewTime, 'MM-DD HH:mm') }}</span>
                  </div>
                </button>
              </div>

              <el-empty v-else description="当前没有待复习题目">
                <el-button type="primary" @click="goStudyLearn">去学习题库</el-button>
              </el-empty>
            </div>

            <div class="sub-panel">
              <div class="sub-panel-header">
                <h3>薄弱记录</h3>
                <span>把最近状态不稳的题目集中出来复盘</span>
              </div>

              <div v-if="failedRecords.length" class="item-list compact-list">
                <button
                  v-for="item in failedRecords"
                  :key="item.taskItemId || item.questionId"
                  class="item-card compact"
                  @click="goQuestionDetail(item.questionId)"
                >
                  <div class="item-card-top">
                    <span class="category-chip muted">{{ item.categoryName || '未分类' }}</span>
                    <el-tag size="small" type="danger">{{ checkModeText(item.checkMode) }}</el-tag>
                  </div>
                  <h3>{{ item.questionTitle }}</h3>
                  <div class="item-meta">
                    <span>得分 {{ formatScore(item.finalScore) }}</span>
                    <span>及格线 {{ formatScore(item.scorePassMark) }}</span>
                    <span>{{ selfResultText(item.selfAssessmentResult) }}</span>
                    <span>{{ formatRelativeTime(item.submitTime) || formatDate(item.submitTime, 'MM-DD HH:mm') }}</span>
                  </div>
                </button>
              </div>

              <el-empty v-else description="最近没有薄弱记录，继续保持" />
            </div>
          </div>
        </div>

        <div v-else-if="activeModule === 'favorite'" class="module-panel favorite-panel">
          <div class="sub-panel">
            <div class="sub-panel-header">
              <h3>我的收藏题</h3>
              <span>把你沉淀的重点题单独维护，复习时更聚焦</span>
            </div>

            <div v-if="favoriteQuestions.length" class="item-list">
              <button
                v-for="item in favoriteQuestions"
                :key="item.id"
                class="item-card"
                @click="goQuestionDetail(item.id)"
              >
                <div class="item-card-top">
                  <span class="category-chip">{{ item.categoryName || '未分类' }}</span>
                  <span class="status-chip favorite">
                    <el-icon><Star /></el-icon>
                    已收藏
                  </span>
                </div>
                <h3>{{ item.title }}</h3>
                <div class="item-meta">
                  <span>{{ difficultyText(item.difficulty) }}</span>
                  <span>{{ statusText(item.studyStatus) }}</span>
                  <span>学习 {{ safeNumber(item.studyCount) }} 次</span>
                  <span v-if="item.lastStudyTime">最近学习 {{ formatDate(item.lastStudyTime, 'MM-DD HH:mm') }}</span>
                </div>
              </button>
            </div>

            <el-empty v-else description="你还没有收藏学习题目">
              <el-button type="primary" @click="goStudyLearn">去收藏重点题</el-button>
            </el-empty>
          </div>
        </div>

        <div v-else class="module-panel check-panel">
          <div class="two-col-grid">
            <div class="sub-panel">
              <div class="sub-panel-header">
                <h3>最近抽查</h3>
                <span>继续未完成任务，或回看最近完成的复盘记录</span>
              </div>

              <div v-if="recentCheckHistory.length" class="item-list">
                <button
                  v-for="item in recentCheckHistory"
                  :key="item.id"
                  class="item-card"
                  @click="goTaskDetail(item.id)"
                >
                  <div class="item-card-top">
                    <div class="history-title-wrap">
                      <span class="task-name">{{ item.taskName || '未命名抽查任务' }}</span>
                      <span class="task-sub">{{ checkModeText(item.checkMode) }}<span v-if="item.categoryName"> · {{ item.categoryName }}</span></span>
                    </div>
                    <el-tag size="small" :type="taskStatusTagType(item.status)">{{ taskStatusText(item.status) }}</el-tag>
                  </div>
                  <div class="item-meta">
                    <span>{{ safeNumber(item.answeredCount) }}/{{ safeNumber(item.questionCount) }} 题已答</span>
                    <span>最终分 {{ formatScore(item.finalScore) }}</span>
                    <span>{{ formatDuration(item.durationSeconds) }}</span>
                    <span>{{ formatDate(item.submitTime || item.startTime, 'MM-DD HH:mm') }}</span>
                  </div>
                </button>
              </div>

              <el-empty v-else description="还没有抽查记录">
                <el-button type="primary" @click="goStudyCheck">去开始第一次抽查</el-button>
              </el-empty>
            </div>

            <div class="sub-panel">
              <div class="sub-panel-header">
                <h3>抽查统计</h3>
                <span>用最少的指标，快速判断抽查状态是否稳定</span>
              </div>

              <div class="score-grid">
                <div class="score-card">
                  <span>累计任务</span>
                  <strong>{{ safeNumber(statistics.totalTaskCount) }}</strong>
                </div>
                <div class="score-card">
                  <span>已完成</span>
                  <strong>{{ safeNumber(statistics.completedTaskCount) }}</strong>
                </div>
                <div class="score-card">
                  <span>最近得分</span>
                  <strong>{{ formatScore(statistics.latestScore) }}</strong>
                </div>
              </div>

              <div class="stat-pills">
                <span class="stat-pill">平均分 {{ formatScore(statistics.avgScore) }}</span>
                <span class="stat-pill">最高分 {{ formatScore(statistics.bestScore) }}</span>
                <span class="stat-pill">答题 {{ safeNumber(statistics.answeredQuestionCount) }}</span>
                <span class="stat-pill">薄弱题 {{ safeNumber(statistics.wrongQuestionCount) }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowRight,
  DataAnalysis,
  Medal,
  Promotion,
  Reading,
  Star
} from '@element-plus/icons-vue'
import { getStudyDashboard } from '@/api/study'
import Loading from '@/components/Loading/index.vue'
import { formatDate, formatRelativeTime } from '@/utils/format'

const router = useRouter()
const route = useRoute()

const MODULE_KEYS = ['overview', 'review', 'favorite', 'check']

const loading = ref(false)
const activeModule = ref(resolveModuleKey(route.query.tab))
const dashboard = ref({
  overview: {},
  statistics: {},
  favoriteQuestions: [],
  reviewQuestions: [],
  failedRecords: [],
  recentCheckHistory: []
})

const overview = computed(() => dashboard.value.overview || {})
const statistics = computed(() => dashboard.value.statistics || {})
const favoriteQuestions = computed(() => dashboard.value.favoriteQuestions || [])
const reviewQuestions = computed(() => dashboard.value.reviewQuestions || [])
const failedRecords = computed(() => dashboard.value.failedRecords || [])
const recentCheckHistory = computed(() => dashboard.value.recentCheckHistory || [])

const moduleCards = computed(() => [
  {
    key: 'overview',
    title: '学习总览',
    desc: '先看全局进度和抽查表现，再决定下一步做什么。',
    count: `${safeNumber(overview.value.studiedQuestions)} / ${safeNumber(overview.value.totalQuestions)}`,
    icon: DataAnalysis,
    iconClass: 'icon-overview'
  },
  {
    key: 'review',
    title: '复习计划',
    desc: '把待复习和薄弱题集中出来，减少来回切换。',
    count: safeNumber(overview.value.reviewingQuestions),
    icon: Reading,
    iconClass: 'icon-review'
  },
  {
    key: 'favorite',
    title: '收藏题库',
    desc: '把自己关心的重点题整理成专属复习清单。',
    count: safeNumber(overview.value.favoriteQuestions),
    icon: Star,
    iconClass: 'icon-favorite'
  },
  {
    key: 'check',
    title: '抽查复盘',
    desc: '查看最近任务、成绩和完成度，直接进入复盘。',
    count: safeNumber(statistics.value.completedTaskCount),
    icon: Medal,
    iconClass: 'icon-check'
  }
])

const summaryCards = computed(() => [
  {
    label: '题库总量',
    value: safeNumber(overview.value.totalQuestions),
    hint: `已学习 ${safeNumber(overview.value.studiedQuestions)}`
  },
  {
    label: '已掌握',
    value: safeNumber(overview.value.masteredQuestions),
    hint: `待复习 ${safeNumber(overview.value.reviewingQuestions)}`
  },
  {
    label: '错题数量',
    value: safeNumber(overview.value.wrongQuestions),
    hint: `收藏 ${safeNumber(overview.value.favoriteQuestions)}`
  },
  {
    label: '抽查完成',
    value: safeNumber(overview.value.completedCheckTaskCount),
    hint: `累计 ${safeNumber(overview.value.checkTaskCount)} 次`
  }
])

const progressItems = computed(() => {
  const totalQuestions = safeNumber(overview.value.totalQuestions)
  const studiedQuestions = safeNumber(overview.value.studiedQuestions)
  const masteredQuestions = safeNumber(overview.value.masteredQuestions)
  const totalTasks = safeNumber(statistics.value.totalTaskCount)
  const completedTasks = safeNumber(statistics.value.completedTaskCount)

  return [
    {
      label: '学习覆盖率',
      percent: calcPercent(studiedQuestions, totalQuestions),
      detail: `${studiedQuestions} / ${totalQuestions}`
    },
    {
      label: '掌握转化率',
      percent: calcPercent(masteredQuestions, studiedQuestions),
      detail: `${masteredQuestions} / ${studiedQuestions || 0}`
    },
    {
      label: '抽查完成率',
      percent: calcPercent(completedTasks, totalTasks),
      detail: `${completedTasks} / ${totalTasks}`
    }
  ]
})

const activeModuleConfig = computed(() => {
  const map = {
    overview: {
      title: '学习总览',
      desc: '这里适合快速看一眼整体学习状态，决定接下来优先进入哪个模块。',
      actions: [
        { label: '去刷题', type: 'primary', handler: goStudyLearn },
        { label: '开始抽查', handler: goStudyCheck }
      ]
    },
    review: {
      title: '复习计划',
      desc: '把最该回看的题和最近暴露出的薄弱点单独收口，避免页面过载。',
      actions: [
        { label: '继续刷题', type: 'primary', handler: goStudyLearn },
        { label: '切到抽查', text: true, handler: () => switchModule('check') }
      ]
    },
    favorite: {
      title: '收藏题库',
      desc: '收藏题是用户主动沉淀出来的重点内容，单独成组更利于复习。',
      actions: [
        { label: '管理收藏题', type: 'primary', handler: goStudyLearn }
      ]
    },
    check: {
      title: '抽查复盘',
      desc: '只展示最近任务和关键成绩指标，避免把复盘信息和刷题信息混在一起。',
      actions: [
        { label: '开始抽查', type: 'primary', handler: goStudyCheck },
        { label: '查看历史', text: true, handler: goCheckHistory }
      ]
    }
  }
  return map[activeModule.value]
})

const currentSuggestion = computed(() => {
  if (safeNumber(overview.value.reviewingQuestions) > 0) {
    return {
      title: '优先处理待复习题',
      desc: `你当前有 ${safeNumber(overview.value.reviewingQuestions)} 道题待复习，先巩固旧知识更划算。`,
      actionText: '进入复习计划',
      action: () => switchModule('review')
    }
  }

  if (safeNumber(statistics.value.completedTaskCount) > 0) {
    return {
      title: '回顾最近抽查表现',
      desc: `最近得分 ${formatScore(statistics.value.latestScore)}，可以顺手看下复盘结果。`,
      actionText: '查看抽查复盘',
      action: () => switchModule('check')
    }
  }

  return {
    title: '开始第一轮学习',
    desc: '如果还没有形成学习记录，建议先从题库刷题开始，建立自己的节奏。',
    actionText: '进入题库学习',
    action: goStudyLearn
  }
})

watch(
  () => route.query.tab,
  value => {
    const nextModule = resolveModuleKey(value)
    if (nextModule !== activeModule.value) {
      activeModule.value = nextModule
    }
  }
)

watch(activeModule, value => {
  const nextQuery = { ...route.query }
  if (value === 'overview') {
    delete nextQuery.tab
  } else {
    nextQuery.tab = value
  }
  router.replace({ query: nextQuery })
})

function resolveModuleKey(value) {
  return MODULE_KEYS.includes(value) ? value : 'overview'
}

function switchModule(key) {
  activeModule.value = resolveModuleKey(key)
}

function handleSuggestionAction() {
  currentSuggestion.value.action()
}

function safeNumber(value) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num : 0
}

function calcPercent(part, total) {
  if (!total) {
    return 0
  }
  return Math.max(0, Math.min(100, Math.round((part / total) * 100)))
}

function formatScore(value) {
  if (value === null || value === undefined || value === '') {
    return '--'
  }
  const score = Number(value)
  if (!Number.isFinite(score)) {
    return '--'
  }
  return Number.isInteger(score) ? `${score}` : score.toFixed(1)
}

function formatDuration(seconds) {
  const totalSeconds = safeNumber(seconds)
  if (!totalSeconds) {
    return '耗时 --'
  }
  const minutes = Math.floor(totalSeconds / 60)
  const remainSeconds = totalSeconds % 60
  if (!minutes) {
    return `${remainSeconds} 秒`
  }
  if (minutes < 60) {
    return `${minutes} 分 ${remainSeconds} 秒`
  }
  const hours = Math.floor(minutes / 60)
  const remainMinutes = minutes % 60
  return `${hours} 小时 ${remainMinutes} 分`
}

function difficultyText(value) {
  const map = { 1: '入门', 2: '进阶', 3: '较难', 4: '高阶', 5: '专家' }
  return map[value] || '未分级'
}

function statusText(value) {
  const map = { 0: '未开始', 1: '学习中', 2: '已掌握', 3: '待复习' }
  return map[value] || '未开始'
}

function checkModeText(value) {
  const map = {
    1: '随机抽查',
    2: '专项抽查',
    3: '收藏抽查',
    4: '薄弱题抽查',
    5: '错题抽查',
    6: '混合抽查'
  }
  return map[value] || '抽查'
}

function selfResultText(value) {
  const map = {
    1: '自评记住了',
    2: '自评有些模糊',
    3: '自评忘记了'
  }
  return map[value] || '等待复盘'
}

function taskStatusText(value) {
  const map = {
    0: '待开始',
    1: '进行中',
    2: '已完成',
    3: '已终止',
    4: '已过期'
  }
  return map[value] || '未知状态'
}

function taskStatusTagType(value) {
  const map = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'danger'
  }
  return map[value] || 'info'
}

function goStudyLearn() {
  router.push('/study/learn')
}

function goStudyCheck() {
  router.push('/study/check')
}

function goCheckHistory() {
  router.push('/study/check/history')
}

function goQuestionDetail(id) {
  if (!id) {
    return
  }
  router.push(`/study/learn/${id}`)
}

function goTaskDetail(taskId) {
  if (!taskId) {
    goCheckHistory()
    return
  }
  router.push(`/study/check/${taskId}`)
}

async function fetchDashboard() {
  loading.value = true
  try {
    const res = await getStudyDashboard()
    dashboard.value = {
      overview: res.data?.overview || {},
      statistics: res.data?.statistics || {},
      favoriteQuestions: res.data?.favoriteQuestions || [],
      reviewQuestions: res.data?.reviewQuestions || [],
      failedRecords: res.data?.failedRecords || [],
      recentCheckHistory: res.data?.recentCheckHistory || []
    }
  } catch (error) {
    console.error('获取学习中心数据失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchDashboard)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.study-dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 560px;
}

.hero-panel,
.module-nav,
.content-shell {
  padding: 24px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: 22px;
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(280px, 0.75fr);
  gap: 20px;
  align-items: stretch;
}

.hero-main {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-badge {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba($primary-color, 0.12);
  color: $primary-color;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 14px;
}

.hero-title {
  margin: 0 0 10px;
  font-size: 30px;
  line-height: 1.2;
  color: var(--text-primary);
}

.hero-desc {
  margin: 0;
  max-width: 640px;
  line-height: 1.9;
  font-size: 14px;
  color: var(--text-secondary);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 20px;
}

.focus-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 10px;
  padding: 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.focus-label {
  font-size: 12px;
  color: var(--text-muted);
}

.focus-title {
  font-size: 22px;
  line-height: 1.4;
  color: var(--text-primary);
}

.focus-desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.8;
  color: var(--text-secondary);
}

.module-nav {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.module-card {
  padding: 18px;
  border-radius: 18px;
  border: 1px solid var(--border-color);
  background: rgba(255, 255, 255, 0.02);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease, background-color 0.2s ease;
}

.module-card:hover,
.module-card.active {
  transform: translateY(-2px);
  border-color: rgba($primary-color, 0.28);
  background: rgba($primary-color, 0.07);
}

.module-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.module-icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.icon-overview {
  background: rgba(#3b82f6, 0.15);
  color: #3b82f6;
}

.icon-review {
  background: rgba(#f59e0b, 0.15);
  color: #f59e0b;
}

.icon-favorite {
  background: rgba(#facc15, 0.15);
  color: #facc15;
}

.icon-check {
  background: rgba(#22c55e, 0.15);
  color: #22c55e;
}

.module-count {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.module-card h2 {
  margin: 0 0 8px;
  font-size: 17px;
  color: var(--text-primary);
}

.module-card p {
  margin: 0;
  font-size: 13px;
  line-height: 1.8;
  color: var(--text-secondary);
}

.content-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.content-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.content-header h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: var(--text-primary);
}

.content-header p {
  margin: 0;
  font-size: 13px;
  line-height: 1.8;
  color: var(--text-secondary);
}

.content-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.module-panel {
  min-height: 320px;
}

.summary-grid,
.two-col-grid,
.score-grid {
  display: grid;
  gap: 16px;
}

.summary-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.two-col-grid {
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  margin-top: 16px;
}

.summary-card,
.sub-panel,
.score-card,
.item-card {
  border: 1px solid var(--border-color);
  background: rgba(255, 255, 255, 0.02);
  border-radius: 18px;
}

.summary-card {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-label,
.summary-hint {
  font-size: 12px;
  color: var(--text-muted);
}

.summary-value {
  font-size: 30px;
  line-height: 1;
  color: var(--text-primary);
}

.sub-panel {
  padding: 20px;
}

.sub-panel-header {
  margin-bottom: 16px;
}

.sub-panel-header h3 {
  margin: 0 0 8px;
  font-size: 19px;
  color: var(--text-primary);
}

.sub-panel-header span {
  font-size: 12px;
  line-height: 1.8;
  color: var(--text-secondary);
}

.progress-list,
.item-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.progress-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.progress-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
  color: var(--text-secondary);
}

.progress-head span:first-child {
  color: var(--text-primary);
}

.score-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.score-card {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.score-card span {
  font-size: 12px;
  color: var(--text-muted);
}

.score-card strong {
  font-size: 24px;
  color: var(--text-primary);
}

.stat-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.stat-pill {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 12px;
  color: var(--text-secondary);
  background: rgba(255, 255, 255, 0.05);
}

.item-card {
  width: 100%;
  padding: 16px 18px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, background-color 0.2s ease;
}

.item-card:hover {
  transform: translateY(-2px);
  border-color: rgba($primary-color, 0.28);
  background: rgba($primary-color, 0.06);
}

.item-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.item-card h3 {
  margin: 12px 0 10px;
  font-size: 16px;
  line-height: 1.6;
  color: var(--text-primary);
}

.item-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  font-size: 12px;
  line-height: 1.8;
  color: var(--text-muted);
}

.category-chip,
.status-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1;
}

.category-chip {
  background: rgba($primary-color, 0.12);
  color: $primary-color;
}

.category-chip.muted {
  background: rgba(255, 255, 255, 0.06);
  color: var(--text-secondary);
}

.status-chip.warning {
  background: rgba(#f59e0b, 0.14);
  color: #f59e0b;
}

.status-chip.favorite {
  background: rgba(#facc15, 0.14);
  color: #facc15;
}

.history-title-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.task-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.task-sub {
  font-size: 12px;
  color: var(--text-secondary);
}

:deep(.el-progress-bar__outer) {
  background: rgba(255, 255, 255, 0.06);
}

:deep(.el-progress-bar__inner) {
  background: $primary-color;
}

@media (max-width: 1200px) {
  .hero-panel,
  .two-col-grid {
    grid-template-columns: 1fr;
  }

  .module-nav,
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .study-dashboard-page {
    gap: 16px;
  }

  .hero-panel,
  .module-nav,
  .content-shell,
  .sub-panel {
    padding: 18px;
  }

  .hero-title {
    font-size: 24px;
  }

  .module-nav,
  .summary-grid,
  .score-grid {
    grid-template-columns: 1fr;
  }

  .content-header,
  .item-card-top,
  .progress-head {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .content-header {
    gap: 12px;
  }

  .hero-actions,
  .content-actions {
    width: 100%;
    flex-wrap: nowrap;
  }

  .hero-actions :deep(.el-button),
  .content-actions :deep(.el-button) {
    flex: 1;
    margin: 0 !important;
    justify-content: center;
  }
  
  .content-actions :deep(.el-button) {
    height: 36px;
  }
}
</style>
