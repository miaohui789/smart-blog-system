<template>
  <div class="study-check-record-page">
    <div class="page-header">
      <div>
        <h2>抽查记录</h2>
        <p>查看用户抽查任务、答题明细、自评分与 AI 评分留痕。</p>
      </div>
    </div>

    <div class="filter-card">
      <el-input v-model="query.userId" placeholder="用户ID" clearable style="width: 140px" />
      <el-select v-model="query.categoryId" clearable placeholder="分类" style="width: 180px">
        <el-option v-for="item in flatCategories" :key="item.id" :label="item.label" :value="item.id" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="任务状态" style="width: 140px">
        <el-option label="待开始" :value="0" />
        <el-option label="进行中" :value="1" />
        <el-option label="已完成" :value="2" />
        <el-option label="已终止" :value="3" />
        <el-option label="已过期" :value="4" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="taskName" label="任务名称" min-width="220" />
        <el-table-column label="用户 / 分类" min-width="180">
          <template #default="{ row }">
            <div class="table-multi">
              <span>{{ row.userNickname || `用户#${row.userId}` }}</span>
              <small>{{ row.categoryName || '通用题库' }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="120">
          <template #default="{ row }">{{ row.answeredCount }}/{{ row.questionCount }}</template>
        </el-table-column>
        <el-table-column label="得分" width="150">
          <template #default="{ row }">
            <div class="table-multi">
              <span>自评 {{ row.selfScore || 0 }}</span>
              <span>AI {{ row.aiScore || 0 }}</span>
              <span>最终 {{ row.finalScore || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'success' : row.status === 1 ? 'warning' : 'info'">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="完成时间" width="180">
          <template #default="{ row }">{{ row.submitTime || row.startTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openDetail(row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchList"
          @size-change="fetchList"
        />
      </div>
    </div>

    <el-drawer v-model="drawerVisible" title="抽查任务详情" size="60%">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称">{{ detail.taskName }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ detail.categoryName || '通用题库' }}</el-descriptions-item>
          <el-descriptions-item label="题量">{{ detail.questionCount }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusText(detail.status) }}</el-descriptions-item>
          <el-descriptions-item label="自评分">{{ detail.selfScore || 0 }}</el-descriptions-item>
          <el-descriptions-item label="AI评分">{{ detail.aiScore || 0 }}</el-descriptions-item>
          <el-descriptions-item label="最终得分">{{ detail.finalScore || 0 }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ detail.submitTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="drawer-table">
          <el-table :data="detail.items || []">
            <el-table-column prop="sortOrder" label="#" width="60" />
            <el-table-column prop="questionTitleSnapshot" label="题目" min-width="240" show-overflow-tooltip />
            <el-table-column label="查看答案" width="100" align="center">
              <template #default="{ row }">{{ row.viewAnswerFlag === 1 ? '已查看' : '未查看' }}</template>
            </el-table-column>
            <el-table-column label="自评" width="90" align="center">
              <template #default="{ row }">{{ selfText(row.selfAssessmentResult) }}</template>
            </el-table-column>
            <el-table-column label="得分" width="160">
              <template #default="{ row }">
                <div class="table-multi">
                  <span>自评 {{ row.selfScore ?? '-' }}</span>
                  <span>AI {{ row.aiScore ?? '-' }}</span>
                  <span>最终 {{ row.finalScore ?? '-' }}</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getAdminStudyCategoryList, getAdminStudyCheckRecordDetail, getAdminStudyCheckRecordList } from '@/api/study'

const loading = ref(false)
const drawerVisible = ref(false)
const list = ref([])
const total = ref(0)
const categories = ref([])
const detail = ref(null)

const query = reactive({
  page: 1,
  pageSize: 10,
  userId: '',
  categoryId: null,
  status: null
})

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
  const res = await getAdminStudyCategoryList({ includeDisabled: true })
  categories.value = res.data || []
}

async function fetchList() {
  loading.value = true
  try {
    const params = {
      ...query,
      userId: query.userId ? Number(query.userId) : null
    }
    const res = await getAdminStudyCheckRecordList(params)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchList()
}

function handleReset() {
  query.page = 1
  query.userId = ''
  query.categoryId = null
  query.status = null
  fetchList()
}

async function openDetail(id) {
  const res = await getAdminStudyCheckRecordDetail(id)
  detail.value = res.data
  drawerVisible.value = true
}

function statusText(value) {
  const map = { 0: '待开始', 1: '进行中', 2: '已完成', 3: '已终止', 4: '已过期' }
  return map[value] || '未知'
}

function selfText(value) {
  const map = { 1: '记得', 2: '模糊', 3: '忘记' }
  return map[value] || '-'
}

onMounted(() => {
  fetchCategories()
  fetchList()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.study-check-record-page { padding: 20px; }
.page-header {
  margin-bottom: 20px;
  h2 { color: var(--text-primary); font-size: 20px; margin-bottom: 6px; }
  p { color: var(--text-muted); font-size: 13px; }
}
.filter-card,
.table-card {
  background: var(--bg-card); border: 1px solid var(--border-color); border-radius: $radius-lg; padding: 20px;
}
.filter-card { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 20px; }
.table-multi { display: flex; flex-direction: column; gap: 4px; color: var(--text-muted); font-size: 12px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
.drawer-table { margin-top: 20px; }
</style>
