<template>
  <div class="study-learn-page">
    <section class="study-hero glass-card">
      <div class="hero-left">
        <div class="hero-top">
          <div>
            <h1 class="hero-title">题库学习</h1>
            <p class="hero-desc">按分类、难度、状态筛选题目，支持收藏、笔记和掌握度管理。</p>
          </div>
          <div class="hero-actions">
            <el-button type="primary" @click="goRandomQuestion">
              <el-icon><RefreshRight /></el-icon>随机来一题
            </el-button>
            <el-button @click="goCheck">
              <el-icon><Promotion /></el-icon>开始抽查
            </el-button>
            <el-button text @click="goBack">返回</el-button>
          </div>
        </div>
        <div class="random-strategy-card">
          <div class="strategy-item strategy-item-select">
            <div class="strategy-copy">
              <span class="strategy-title">随机分类范围</span>
              <span class="strategy-desc">可单独指定随机抽题分类，未选择时沿用当前筛选分类</span>
            </div>
            <el-select
              v-model="randomOptions.categoryId"
              class="strategy-select"
              placeholder="全部分类"
              clearable
            >
              <el-option
                v-for="item in flatCategories"
                :key="item.id"
                :label="item.label"
                :value="item.id"
              />
            </el-select>
          </div>
          <div class="strategy-item">
            <div class="strategy-copy">
              <span class="strategy-title">优先未学习 / 待复习</span>
              <span class="strategy-desc">开启后会优先抽你还没学或该复习的题目</span>
            </div>
            <el-switch v-model="randomOptions.preferPendingReview" />
          </div>
          <div class="strategy-item">
            <div class="strategy-copy">
              <span class="strategy-title">优先学习次数少的</span>
              <span class="strategy-desc">开启后会优先从学习次数更少的一批题目中随机抽取</span>
            </div>
            <el-switch v-model="randomOptions.preferLowStudyCount" />
          </div>
          <div class="strategy-item">
            <div class="strategy-copy">
              <span class="strategy-title">最近 10 题不重复</span>
              <span class="strategy-desc">避免连续抽到刚看过的题，没有更多候选题时自动回退</span>
            </div>
            <el-switch v-model="randomOptions.avoidRecentRepeat" />
          </div>
        </div>
      </div>
      <div class="hero-stats">
        <div class="stat-card">
          <span class="stat-label">题库总量</span>
          <strong class="stat-value">{{ overview.totalQuestions || 0 }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-label">已学习</span>
          <strong class="stat-value">{{ overview.studiedQuestions || 0 }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-label">已掌握</span>
          <strong class="stat-value">{{ overview.masteredQuestions || 0 }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-label">平均得分</span>
          <strong class="stat-value">{{ overview.avgScore || 0 }}</strong>
        </div>
      </div>
    </section>

    <section class="panel-section glass-card">
      <div class="panel-header">
        <div>
          <h2>题库筛选</h2>
          <p>按分类、难度、学习状态快速定位当前要练习的题目。</p>
        </div>
      </div>
      <div class="filter-grid">
        <el-input
          v-model="query.keyword"
          class="filter-item filter-keyword"
          placeholder="搜索题目标题、关键字"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="query.categoryId"
          class="filter-item"
          placeholder="全部分类"
          clearable
        >
          <el-option
            v-for="item in flatCategories"
            :key="item.id"
            :label="item.label"
            :value="item.id"
          />
        </el-select>
        <el-select
          v-model="query.difficulty"
          class="filter-item"
          placeholder="全部难度"
          clearable
        >
          <el-option label="入门" :value="1" />
          <el-option label="进阶" :value="2" />
          <el-option label="较难" :value="3" />
          <el-option label="高阶" :value="4" />
          <el-option label="专家" :value="5" />
        </el-select>
        <el-select
          v-model="query.studyStatus"
          class="filter-item"
          placeholder="全部学习状态"
          clearable
        >
          <el-option label="未开始" :value="0" />
          <el-option label="学习中" :value="1" />
          <el-option label="已掌握" :value="2" />
          <el-option label="待复习" :value="3" />
        </el-select>
        <el-select
          v-model="query.isFavorite"
          class="filter-item"
          placeholder="收藏状态"
          clearable
        >
          <el-option label="仅收藏" :value="1" />
          <el-option label="未收藏" :value="0" />
        </el-select>
        <el-select
          v-model="query.studyCountSort"
          class="filter-item"
          placeholder="学习次数排序"
          clearable
        >
          <el-option label="学习次数升序" value="asc" />
          <el-option label="学习次数降序" value="desc" />
        </el-select>
        <div class="filter-item filter-actions">
          <el-button type="primary" @click="handleSearch">筛选</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>
    </section>

    <section class="panel-section glass-card">
      <div class="panel-header">
        <div>
          <h2>题目列表</h2>
          <p>共 {{ total }} 道题，支持点击查看答案、笔记和学习进度。</p>
        </div>
        <el-radio-group v-model="displayMode" size="small">
          <el-radio-button label="舒展" />
          <el-radio-button label="紧凑" />
        </el-radio-group>
      </div>

      <el-empty v-if="!loading && !questions.length" description="当前筛选下暂无题目" />

      <div v-else class="question-grid" :class="{ compact: displayMode === '紧凑' }">
        <div
          v-for="item in questions"
          :key="item.id"
          class="question-card"
          @click="goQuestionDetail(item.id)"
        >
          <div class="question-top">
            <div class="question-meta">
              <span class="category-chip">{{ item.categoryName || '未分类' }}</span>
              <span class="question-no">#{{ item.questionNo || item.id }}</span>
            </div>
            <button class="ghost-icon-btn" @click.stop="handleToggleFavorite(item)">
              <el-icon :class="{ active: item.isFavorite === 1 }"><Star /></el-icon>
            </button>
          </div>

          <h3 class="question-title">{{ item.title }}</h3>
          <p class="question-summary">点击进入查看题目内容、记录笔记并管理学习进度。</p>

          <div class="question-tags">
            <span class="tag-item">{{ difficultyText(item.difficulty) }}</span>
            <span class="tag-item">{{ statusText(item.studyStatus) }}</span>
            <span v-if="item.isWrongQuestion === 1" class="tag-item warning">错题</span>
            <span v-if="item.aiScoreEnabled === 1" class="tag-item success">AI评分</span>
          </div>

          <div class="question-footer">
            <div class="footer-stat">
              <el-icon><Reading /></el-icon>
              <span>{{ item.studyCount || 0 }} 次学习</span>
            </div>
            <div class="footer-stat">
              <el-icon><Compass /></el-icon>
              <span>掌握度 {{ item.masteryLevel || 0 }}/5</span>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination-wrapper" v-if="total > query.pageSize">
        <el-pagination
          v-model:current-page="query.page"
          :page-size="query.pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchQuestions"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Compass, Promotion, Reading, RefreshRight, Search, Star } from '@element-plus/icons-vue'
import { getRandomStudyQuestion, getStudyCategories, getStudyOverview, getStudyQuestions, toggleStudyFavorite } from '@/api/study'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const RANDOM_OPTION_STORAGE_KEY = 'study_random_options'
const RANDOM_HISTORY_LIMIT = 10

const loading = ref(false)
const questions = ref([])
const categories = ref([])
const total = ref(0)
const displayMode = ref('舒展')
const overview = ref({
  totalQuestions: 0,
  studiedQuestions: 0,
  masteredQuestions: 0,
  avgScore: 0
})
const query = reactive({
  page: 1,
  pageSize: 10,
  categoryId: null,
  keyword: '',
  difficulty: null,
  studyStatus: null,
  isFavorite: null,
  studyCountSort: null
})
const randomOptions = reactive(loadRandomOptions())

const flatCategories = computed(() => {
  const result = []
  const walk = (list = [], level = 0) => {
    list.forEach(item => {
      result.push({ id: item.id, label: `${'　'.repeat(level)}${item.categoryName}` })
      if (item.children?.length) walk(item.children, level + 1)
    })
  }
  walk(categories.value)
  return result
})

async function fetchCategories() {
  const res = await getStudyCategories()
  categories.value = res.data || []
}

async function fetchOverview() {
  if (!userStore.isLoggedIn) {
    overview.value.totalQuestions = total.value
    return
  }
  const res = await getStudyOverview()
  overview.value = { ...overview.value, ...(res.data || {}) }
}

async function fetchQuestions() {
  loading.value = true
  try {
    const params = { ...query }
    if (params.isFavorite === null) delete params.isFavorite
    if (!params.studyCountSort) delete params.studyCountSort
    const res = await getStudyQuestions(params)
    questions.value = res.data?.list || []
    total.value = res.data?.total || 0
    if (!userStore.isLoggedIn) {
      overview.value.totalQuestions = total.value
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchQuestions()
  fetchOverview()
}

function handleReset() {
  query.page = 1
  query.categoryId = null
  query.keyword = ''
  query.difficulty = null
  query.studyStatus = null
  query.isFavorite = null
  query.studyCountSort = null
  fetchQuestions()
  fetchOverview()
}

function goQuestionDetail(id) {
  router.push(`/study/learn/${id}`)
}

async function goRandomQuestion() {
  const recentQuestionIds = randomOptions.avoidRecentRepeat ? getRecentRandomQuestionIds() : []
  const randomCategoryId = randomOptions.categoryId ?? query.categoryId
  const res = await getRandomStudyQuestion({
    categoryId: randomCategoryId,
    difficulty: query.difficulty,
    preferPendingReview: randomOptions.preferPendingReview,
    preferLowStudyCount: randomOptions.preferLowStudyCount,
    excludeIds: recentQuestionIds.join(',')
  })
  if (res.data?.id) {
    updateRecentRandomQuestionIds(res.data.id)
    router.push(`/study/learn/${res.data.id}`)
  }
}

function goCheck() {
  router.push('/study/check')
}

function goBack() {
  router.push('/study')
}

async function handleToggleFavorite(item) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('登录后可收藏题目')
    router.push('/login')
    return
  }
  const nextFavorite = item.isFavorite === 1 ? 0 : 1
  await toggleStudyFavorite(item.id, nextFavorite)
  item.isFavorite = nextFavorite
}

function difficultyText(value) {
  const map = { 1: '入门', 2: '进阶', 3: '较难', 4: '高阶', 5: '专家' }
  return map[value] || '未分级'
}

function statusText(value) {
  const map = { 0: '未开始', 1: '学习中', 2: '已掌握', 3: '待复习' }
  return map[value] || '未开始'
}

function loadRandomOptions() {
  const defaults = {
    categoryId: null,
    preferPendingReview: true,
    preferLowStudyCount: false,
    avoidRecentRepeat: true
  }
  try {
    const saved = JSON.parse(localStorage.getItem(RANDOM_OPTION_STORAGE_KEY) || '{}')
    return { ...defaults, ...saved }
  } catch (error) {
    return defaults
  }
}

function getRandomHistoryStorageKey() {
  return `study_random_recent_${userStore.userInfo?.id || 'guest'}`
}

function getRecentRandomQuestionIds() {
  try {
    const saved = JSON.parse(localStorage.getItem(getRandomHistoryStorageKey()) || '[]')
    if (!Array.isArray(saved)) {
      return []
    }
    return saved
      .map(item => Number(item))
      .filter(item => Number.isInteger(item) && item > 0)
      .slice(0, RANDOM_HISTORY_LIMIT)
  } catch (error) {
    return []
  }
}

function updateRecentRandomQuestionIds(questionId) {
  if (!randomOptions.avoidRecentRepeat || !questionId) {
    return
  }
  const nextIds = [questionId, ...getRecentRandomQuestionIds().filter(id => id !== questionId)].slice(0, RANDOM_HISTORY_LIMIT)
  localStorage.setItem(getRandomHistoryStorageKey(), JSON.stringify(nextIds))
}

watch(
  () => ({ ...randomOptions }),
  value => {
    localStorage.setItem(RANDOM_OPTION_STORAGE_KEY, JSON.stringify(value))
  },
  { deep: true }
)

onMounted(async () => {
  await Promise.all([fetchCategories(), fetchQuestions()])
  await fetchOverview()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.study-learn-page { display: flex; flex-direction: column; gap: 24px; min-height: 520px; }
.study-hero, .panel-section { padding: 24px; }
.study-hero { display: grid; grid-template-columns: 1.4fr 1fr; gap: 24px; align-items: start; }
.hero-left { display: flex; flex-direction: column; gap: 16px; }
.hero-top { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.hero-title { font-size: 24px; line-height: 1.3; color: var(--text-primary); margin-bottom: 6px; }
.hero-desc { color: var(--text-secondary); font-size: 13px; line-height: 1.7; }
.hero-actions { display: flex; gap: 10px; flex-wrap: wrap; flex-shrink: 0; }
.random-strategy-card {
  margin-top: 18px;
  padding: 16px 18px;
  border: 1px solid var(--border-color);
  border-radius: 18px;
  background: rgba($primary-color, 0.04);
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.strategy-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.strategy-item-select {
  align-items: flex-start;
}
.strategy-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.strategy-select {
  width: min(320px, 100%);
  flex: 0 0 320px;
}
.strategy-title {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 600;
}
.strategy-desc,
.strategy-tip {
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.7;
}
.strategy-tip {
  padding-top: 2px;
  border-top: 1px dashed var(--border-color);
}
.hero-stats { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; align-content: start; }
.stat-card { padding: 18px; border-radius: 16px; border: 1px solid var(--border-color); background: rgba($primary-color, 0.05); }
.stat-label { display: block; color: var(--text-muted); font-size: 13px; margin-bottom: 8px; }
.stat-value { color: var(--text-primary); font-size: 24px; font-weight: 700; }
.panel-header {
  display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 20px;
  h2 { color: var(--text-primary); font-size: 20px; margin-bottom: 6px; }
  p { color: var(--text-muted); font-size: 13px; }
}
.filter-grid {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid var(--border-color);
  background: rgba($primary-color, 0.03);
  overflow: hidden;
}
.filter-item {
  flex: 1 1 160px;
  min-width: 0;
}
.filter-keyword {
  flex: 2.2 1 300px;
}
.filter-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  white-space: nowrap;
  margin-left: auto;
  flex: 0 0 auto;
}
.question-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.question-grid.compact { grid-template-columns: 1fr; }
.question-card {
  border: 1px solid var(--border-color); border-radius: 18px; padding: 20px; background: var(--bg-card); cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
  &:hover { transform: translateY(-3px); border-color: rgba($primary-color, 0.36); box-shadow: 0 16px 30px var(--shadow-color); }
}
.question-top, .question-footer, .question-meta, .question-tags { display: flex; align-items: center; }
.question-top, .question-footer { justify-content: space-between; }
.question-meta, .question-tags { gap: 8px; flex-wrap: wrap; }
.category-chip, .question-no, .tag-item { border-radius: 999px; padding: 4px 10px; font-size: 12px; }
.category-chip { background: rgba($primary-color, 0.12); color: $primary-color; }
.question-no { background: var(--bg-card-hover); color: var(--text-muted); }
.ghost-icon-btn {
  width: 34px; height: 34px; border: 1px solid var(--border-color); border-radius: 10px; display: inline-flex; align-items: center;
  justify-content: center; background: transparent; color: var(--text-muted); cursor: pointer; transition: all 0.2s ease;
  &:hover { color: $primary-color; border-color: rgba($primary-color, 0.36); }
  .active { color: #f59e0b; }
}
.question-title { color: var(--text-primary); font-size: 18px; line-height: 1.6; margin: 16px 0 10px; }
.question-summary {
  min-height: 64px; color: var(--text-secondary); font-size: 13px; line-height: 1.8; display: -webkit-box; -webkit-line-clamp: 3;
  -webkit-box-orient: vertical; overflow: hidden;
}
.question-tags { margin-top: 16px; }
.tag-item { background: var(--bg-card-hover); color: var(--text-secondary); }
.tag-item.warning { background: rgba(#f59e0b, 0.12); color: #f59e0b; }
.tag-item.success { background: rgba(#22c55e, 0.12); color: #22c55e; }
.question-footer { margin-top: 18px; padding-top: 14px; border-top: 1px solid var(--border-color); gap: 12px; flex-wrap: wrap; }
.footer-stat { display: inline-flex; align-items: center; gap: 6px; color: var(--text-muted); font-size: 13px; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 24px; }
@media (max-width: 1100px) {
  .study-hero, .question-grid { grid-template-columns: 1fr; }
  .filter-actions {
    width: 100%;
    justify-content: stretch;
    margin-left: 0;
  }
  .filter-actions .el-button { flex: 1; }
}
@media (max-width: 768px) {
  .study-hero, .panel-section { padding: 18px; }
  .hero-title { font-size: 24px; }
  .panel-header { align-items: flex-start; flex-direction: column; }
  .strategy-item {
    align-items: flex-start;
    flex-direction: column;
  }
  .strategy-select {
    width: 100%;
    flex-basis: auto;
  }
  .filter-grid {
    padding: 14px;
  }
  .filter-item,
  .filter-keyword,
  .filter-actions {
    flex-basis: 100%;
  }
}
</style>
