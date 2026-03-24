<template>
  <div class="study-history-page">
    <section class="history-header glass-card">
      <div>
        <div class="hero-badge">复盘中心</div>
        <h1>抽查历史</h1>
        <p>查看每次抽查任务的完成度、得分和记忆情况，持续追踪自己的掌握变化。</p>
      </div>
      <div class="header-actions">
        <el-button @click="handleGoBack">返回上一页</el-button>
        <el-button @click="$router.push('/study/check')">新建抽查</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <div v-for="item in statCards" :key="item.label" class="stats-card glass-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </section>

    <section class="history-table glass-card">
      <div class="panel-title">
        <h2>任务列表</h2>
        <p>仅展示最近两天的抽查记录，超过保留期的历史会自动清理。</p>
      </div>

      <el-table :data="history" v-loading="loading">
        <el-table-column prop="taskName" label="任务名称" min-width="200" />
        <el-table-column label="分类 / 模式" min-width="180">
          <template #default="{ row }">
            <div class="table-multi">
              <span>{{ row.categoryName || '通用题库' }}</span>
              <small>{{ modeText(row.checkMode) }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="完成度" width="120">
          <template #default="{ row }">{{ row.answeredCount }}/{{ row.questionCount }}</template>
        </el-table-column>
        <el-table-column label="最终得分" width="120">
          <template #default="{ row }">{{ row.finalScore || 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'success' : row.status === 1 ? 'warning' : 'info'">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="完成时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.submitTime || row.startTime) || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="$router.push(`/study/check/${row.id}`)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper" v-if="total > query.pageSize">
        <el-pagination
          v-model:current-page="query.page"
          :page-size="query.pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchHistory"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { formatDateTime } from '@/utils/format'
import { getStudyCheckHistory, getStudyCheckStatistics } from '@/api/study'

const router = useRouter()
const loading = ref(false)
const history = ref([])
const total = ref(0)
const stats = ref({})
const query = reactive({ page: 1, pageSize: 10 })

const statCards = computed(() => [
  { label: '累计任务', value: stats.value.totalTaskCount || 0 },
  { label: '已完成任务', value: stats.value.completedTaskCount || 0 },
  { label: '已答题数', value: stats.value.answeredQuestionCount || 0 },
  { label: '平均得分', value: stats.value.avgScore || 0 },
  { label: '最高得分', value: stats.value.bestScore || 0 },
  { label: '薄弱题数', value: stats.value.wrongQuestionCount || 0 }
])

async function fetchHistory() {
  loading.value = true
  try {
    const res = await getStudyCheckHistory({ ...query })
    history.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchStatistics() {
  const res = await getStudyCheckStatistics()
  stats.value = res.data || {}
}

function modeText(value) {
  const map = { 1: '随机抽查', 2: '专项抽查', 3: '收藏抽查', 4: '薄弱题抽查', 5: '错题抽查', 6: '混合抽查' }
  return map[value] || '抽查'
}

function statusText(value) {
  const map = { 0: '待开始', 1: '进行中', 2: '已完成', 3: '已终止', 4: '已过期' }
  return map[value] || '未知状态'
}

function handleGoBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/study/check')
}

onMounted(() => {
  fetchHistory()
  fetchStatistics()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.study-history-page { display: flex; flex-direction: column; gap: 24px; }
.history-header, .history-table, .stats-card { padding: 22px; }
.history-header { display: flex; justify-content: space-between; gap: 16px; align-items: center; }
.header-actions { display: flex; gap: 12px; flex-wrap: wrap; }
.hero-badge {
  width: fit-content; padding: 6px 12px; border-radius: 999px; background: rgba($primary-color, 0.12);
  color: $primary-color; font-size: 12px; font-weight: 600; margin-bottom: 10px;
}
h1 { color: var(--text-primary); font-size: 28px; margin-bottom: 8px; }
p { color: var(--text-secondary); }
.stats-grid { display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 16px; }
.stats-card {
  span { display: block; color: var(--text-muted); font-size: 13px; margin-bottom: 8px; }
  strong { color: var(--text-primary); font-size: 24px; }
}
.panel-title {
  margin-bottom: 18px;
  h2 { color: var(--text-primary); font-size: 20px; margin-bottom: 6px; }
  p { color: var(--text-muted); font-size: 13px; }
}
.history-table :deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(var(--bg-card-rgb), 0.3);
  --el-table-row-hover-bg-color: rgba(var(--bg-card-rgb), 0.74);
  --el-table-current-row-bg-color: rgba(var(--bg-card-rgb), 0.74);
  --el-table-header-text-color: var(--text-secondary);
  --el-table-text-color: var(--text-primary);
  --el-table-border-color: var(--border-color);
  background: transparent;
  color: var(--text-primary);
}
.history-table :deep(.el-table th.el-table__cell) {
  background: rgba(var(--bg-card-rgb), 0.22);
  color: var(--text-secondary);
  border-bottom-color: var(--border-color);
}
.history-table :deep(.el-table tr) {
  background: transparent;
}
.history-table :deep(.el-table td.el-table__cell) {
  background: transparent;
  border-bottom-color: var(--border-color);
  color: var(--text-primary);
}
.history-table :deep(.el-table__body tr:hover > td.el-table__cell),
.history-table :deep(.el-table__body tr.hover-row > td.el-table__cell),
.history-table :deep(.el-table__body tr.current-row > td.el-table__cell) {
  background: rgba(var(--bg-card-rgb), 0.74) !important;
}
.history-table :deep(.el-table__fixed),
.history-table :deep(.el-table__fixed-right) {
  box-shadow: none;
}
.history-table :deep(.el-table__fixed::before),
.history-table :deep(.el-table__fixed-right::before) {
  background: var(--border-color);
}
.history-table :deep(.el-table__fixed .el-table__cell),
.history-table :deep(.el-table__fixed-right .el-table__cell) {
  background: rgba(var(--bg-card-rgb), 0.22) !important;
}
.history-table :deep(.el-table__fixed-body-wrapper tbody tr td.el-table__cell),
.history-table :deep(.el-table__fixed-right .el-table__fixed-body-wrapper tbody tr td.el-table__cell) {
  background: transparent !important;
}
.history-table :deep(.el-table__fixed-body-wrapper tbody tr:hover > td.el-table__cell),
.history-table :deep(.el-table__fixed-body-wrapper tbody tr.hover-row > td.el-table__cell),
.history-table :deep(.el-table__fixed-right .el-table__fixed-body-wrapper tbody tr:hover > td.el-table__cell),
.history-table :deep(.el-table__fixed-right .el-table__fixed-body-wrapper tbody tr.hover-row > td.el-table__cell) {
  background: rgba(var(--bg-card-rgb), 0.74) !important;
}
.table-multi {
  display: flex; flex-direction: column; gap: 4px;
  span { color: var(--text-primary); }
  small { color: var(--text-muted); }
}
.pagination-wrapper { display: flex; justify-content: center; margin-top: 24px; }
@media (max-width: 1100px) {
  .stats-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 768px) {
  .history-header { flex-direction: column; align-items: flex-start; }
  .header-actions { width: 100%; }
  .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
