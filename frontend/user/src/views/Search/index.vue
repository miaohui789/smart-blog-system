<template>
  <div class="search-page">
    <div class="search-header glass-card">
      <h1 class="search-title">全站搜索</h1>
      <p class="search-subtitle">支持搜索文章、面试题和用户</p>
      <div class="search-input-wrapper">
        <el-input
          v-model="keyword"
          placeholder="输入关键词搜索文章、面试题、用户..."
          size="large"
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleClear"
        />
        <el-button type="primary" size="large" @click="handleSearch">
          搜索
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-wrapper">
      <Loading />
    </div>

    <div v-else-if="searched" class="search-result">
      <p class="result-summary">
        共找到 <span class="highlight">{{ total }}</span> 条结果
        <span v-if="engine" class="engine-badge">{{ engineLabel }}</span>
      </p>

      <section v-if="articleTotal" class="result-section glass-card">
        <div class="section-header">
          <h2>相关文章</h2>
          <span>{{ articleTotal }} 篇</span>
        </div>
        <div class="result-list">
          <router-link
            v-for="article in articles"
            :key="article.id"
            :to="`/article/${article.id}`"
            class="result-card"
          >
            <div class="result-card-top">
              <span class="result-type">文章</span>
              <span class="result-meta">{{ article.categoryName || '未分类' }}</span>
            </div>
            <h3 v-html="article.titleHighlight || highlightKeyword(article.title)"></h3>
            <p v-html="article.summaryHighlight || highlightKeyword(article.summary || '')"></p>
            <div v-if="article.tags?.length" class="tag-list">
              <span
                v-for="tag in article.tags"
                :key="tag.id"
                class="tag-item"
              >
                {{ tag.name }}
              </span>
            </div>
          </router-link>
        </div>
      </section>

      <section v-if="studyTotal" class="result-section glass-card">
        <div class="section-header">
          <h2>面试题结果</h2>
          <span>{{ studyTotal }} 道</span>
        </div>
        <div class="result-list">
          <router-link
            v-for="item in studyQuestions"
            :key="item.id"
            :to="`/study/learn/${item.id}`"
            class="result-card"
          >
            <div class="result-card-top">
              <span class="result-type study">面试题</span>
              <span class="result-meta">{{ item.categoryName || '未分类' }}</span>
            </div>
            <h3 v-html="item.titleHighlight || highlightKeyword(item.title)"></h3>
            <p v-html="item.answerSummaryHighlight || highlightKeyword(item.answerSummary || '')"></p>
            <div class="tag-list">
              <span v-if="item.questionCode" class="tag-item">{{ item.questionCode }}</span>
              <span v-if="item.difficulty" class="tag-item">难度 {{ item.difficulty }}</span>
              <span class="tag-item">学习 {{ item.studyCount || 0 }} 次</span>
            </div>
          </router-link>
        </div>
      </section>

      <section v-if="userResults.length" class="result-section glass-card">
        <div class="section-header">
          <h2>相关用户</h2>
          <span>{{ userResults.length }} 位</span>
        </div>
        <div class="user-list">
          <router-link
            v-for="user in userResults"
            :key="user.id"
            :to="`/user/${user.id}`"
            class="user-card"
          >
            <img v-if="user.avatar" :src="user.avatar" :alt="user.nickname || user.username" class="user-avatar" />
            <div v-else class="user-avatar placeholder">
              {{ (user.nickname || user.username || 'U').charAt(0).toUpperCase() }}
            </div>
            <div class="user-info">
              <h3>{{ user.nickname || user.username }}</h3>
              <p>{{ user.intro || '这个人很懒，什么都没写~' }}</p>
            </div>
          </router-link>
        </div>
      </section>

      <el-empty v-if="!total" description="没有找到相关内容">
        <template #image>
          <el-icon :size="60" color="#71717a"><Search /></el-icon>
        </template>
      </el-empty>

      <el-pagination
        v-if="needPagination"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="Math.max(articleTotal, studyTotal)"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <div v-else class="search-tips glass-card">
      <el-icon :size="48" color="#71717a"><Search /></el-icon>
      <p>输入关键词开始搜索</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { searchAll } from '@/api/search'
import Loading from '@/components/Loading/index.vue'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const articles = ref([])
const studyQuestions = ref([])
const userResults = ref([])
const articleTotal = ref(0)
const studyTotal = ref(0)
const searched = ref(false)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const engine = ref('')

const total = computed(() => articleTotal.value + studyTotal.value + userResults.value.length)
const needPagination = computed(() => articleTotal.value > pageSize.value || studyTotal.value > pageSize.value)
const engineLabel = computed(() => engine.value === 'elasticsearch' ? 'ES 搜索' : '数据库兜底')

function highlightKeyword(text) {
  if (!text || !keyword.value.trim()) return text
  const kw = keyword.value.trim()
  const regex = new RegExp(`(${kw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return text.replace(regex, '<mark class="search-highlight">$1</mark>')
}

async function handleSearch() {
  if (!keyword.value.trim()) return

  loading.value = true
  searched.value = true

  try {
    const res = await searchAll({
      keyword: keyword.value,
      page: currentPage.value,
      pageSize: pageSize.value,
      recordHot: currentPage.value === 1
    })
    articles.value = res.data?.articles?.list || []
    studyQuestions.value = res.data?.studyQuestions?.list || []
    userResults.value = res.data?.users || []
    articleTotal.value = res.data?.articles?.total || 0
    studyTotal.value = res.data?.studyQuestions?.total || 0
    engine.value = res.data?.engine || ''
    router.replace({ query: { keyword: keyword.value, page: currentPage.value } })
  } catch (e) {
    console.error(e)
    articles.value = []
    studyQuestions.value = []
    userResults.value = []
    articleTotal.value = 0
    studyTotal.value = 0
    engine.value = ''
  } finally {
    loading.value = false
  }
}

function handleClear() {
  articles.value = []
  studyQuestions.value = []
  userResults.value = []
  articleTotal.value = 0
  studyTotal.value = 0
  engine.value = ''
  searched.value = false
  currentPage.value = 1
  router.replace({ query: {} })
}

function handlePageChange(page) {
  currentPage.value = page
  handleSearch()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  if (route.query.keyword) {
    keyword.value = route.query.keyword
    currentPage.value = Number(route.query.page || 1)
    handleSearch()
  }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.search-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-xl;
  --search-hit-color: #b45309;
  --search-hit-bg: linear-gradient(180deg, rgba(255, 236, 179, 0.96), rgba(253, 230, 138, 0.9));
  --search-hit-border: rgba(245, 158, 11, 0.5);
  --search-hit-shadow: rgba(245, 158, 11, 0.22);
  --search-hit-text-shadow: none;
}

:global(:root[data-theme='dark']) .search-page {
  --search-hit-color: #111827;
  --search-hit-bg: linear-gradient(180deg, rgba(253, 224, 71, 0.98), rgba(250, 204, 21, 0.92));
  --search-hit-border: rgba(252, 211, 77, 0.62);
  --search-hit-shadow: rgba(250, 204, 21, 0.3);
  --search-hit-text-shadow: 0 1px 1px rgba(255, 255, 255, 0.18);
}

.search-header,
.result-section,
.search-tips {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: background-color 0.3s, border-color 0.3s;
}

.search-header {
  padding: $spacing-xl;
  text-align: center;
}

.search-title {
  font-size: 30px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: $spacing-sm;
}

.search-subtitle {
  color: var(--text-secondary);
  margin-bottom: $spacing-lg;
}

.search-input-wrapper {
  display: flex;
  gap: $spacing-md;
  max-width: 760px;
  margin: 0 auto;

  .el-input {
    flex: 1;
  }
}

.loading-wrapper {
  padding: $spacing-2xl;
  text-align: center;
}

.result-summary {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  align-self: flex-start;
  padding: 10px 14px;
  border-radius: 14px;
  background: rgba(var(--bg-card-rgb), 0.92);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  font-size: 14px;
  box-shadow: 0 8px 20px var(--shadow-color);

  .highlight {
    color: $primary-color;
    font-weight: 600;
  }
}

.engine-badge {
  display: inline-flex;
  align-items: center;
  margin-left: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba($primary-color, 0.1);
  color: $primary-color;
  font-size: 12px;
}

.search-result {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.result-section {
  padding: 18px 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;

  h2 {
    margin: 0;
    font-size: 18px;
    color: var(--text-primary);
  }

  span {
    color: var(--text-muted);
    font-size: 13px;
  }
}

.result-list,
.user-list {
  display: grid;
  gap: 10px;
}

.result-card,
.user-card {
  display: block;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid rgba(127, 144, 168, 0.18);
  background: rgba(var(--bg-card-rgb), 0.8);
  text-decoration: none;
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-1px);
    border-color: rgba($primary-color, 0.28);
    box-shadow: 0 10px 24px var(--shadow-color);
  }
}

.result-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.result-type {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba($primary-color, 0.12);
  color: $primary-color;
  font-size: 12px;
  font-weight: 600;

  &.study {
    background: rgba(#f59e0b, 0.12);
    color: #f59e0b;
  }
}

.result-meta {
  font-size: 12px;
  color: var(--text-muted);
}

.result-card h3,
.user-info h3 {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.5;
}

.result-card p,
.user-info p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 14px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.tag-item {
  padding: 3px 8px;
  border-radius: 999px;
  background: var(--bg-card-hover);
  color: var(--text-secondary);
  font-size: 11px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;

  &.placeholder {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background: rgba($primary-color, 0.14);
    color: $primary-color;
    font-weight: 700;
  }
}

.search-tips {
  padding: $spacing-2xl;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;

  p {
    color: var(--text-muted);
    font-size: 15px;
  }
}

:deep(.search-highlight),
:deep(.search-hit) {
  display: inline;
  padding: 0 0.35em;
  margin: 0 0.04em;
  border-radius: 0.45em;
  border: 1px solid var(--search-hit-border);
  background: var(--search-hit-bg);
  box-shadow: 0 8px 16px -12px var(--search-hit-shadow);
  color: var(--search-hit-color);
  -webkit-text-fill-color: var(--search-hit-color);
  font-style: normal;
  font-weight: 800;
  text-decoration: none;
  text-shadow: var(--search-hit-text-shadow);
  box-decoration-break: clone;
  -webkit-box-decoration-break: clone;
}

:deep(.search-highlight) {
  background-color: transparent;
}

:deep(.el-pagination) {
  justify-content: center;
}

@media (max-width: 768px) {
  .search-input-wrapper {
    flex-direction: column;
    
    .el-button {
      width: 100%;
    }
  }

  .search-title {
    font-size: 24px;
  }

   .result-summary {
    width: 100%;
    justify-content: space-between;
  }

  .result-card h3,
  .user-info h3 {
    font-size: 17px;
  }
}
</style>
