<template>
  <div class="study-question-page">
    <div class="page-header">
      <div>
        <h2>学习题库</h2>
        <p>支持企业级题库维护、来源追踪、AI评分配置和 Markdown 批量导入。</p>
      </div>
      <div class="header-actions">
        <el-button @click="openImportDialog">
          <el-icon><Upload /></el-icon>
          导入题库
        </el-button>
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          新增题目
        </el-button>
      </div>
    </div>

    <div class="filter-card">
      <el-input v-model="query.keyword" placeholder="搜索题目标题 / 编码 / 关键字" clearable style="width: 240px" />
      <el-select v-model="query.categoryId" clearable placeholder="分类" style="width: 180px">
        <el-option v-for="item in flatCategories" :key="item.id" :label="item.label" :value="item.id" />
      </el-select>
      <el-select v-model="query.difficulty" clearable placeholder="难度" style="width: 120px">
        <el-option label="入门" :value="1" />
        <el-option label="进阶" :value="2" />
        <el-option label="较难" :value="3" />
        <el-option label="高阶" :value="4" />
        <el-option label="专家" :value="5" />
      </el-select>
      <el-select v-model="query.reviewStatus" clearable placeholder="审核状态" style="width: 140px">
        <el-option label="草稿" :value="0" />
        <el-option label="待审核" :value="1" />
        <el-option label="已发布" :value="2" />
        <el-option label="已下线" :value="3" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="启停状态" style="width: 120px">
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="title" label="题目标题" min-width="260" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="150" />
        <el-table-column prop="questionCode" label="编码" min-width="140" show-overflow-tooltip />
        <el-table-column label="难度" width="90" align="center">
          <template #default="{ row }">{{ difficultyText(row.difficulty) }}</template>
        </el-table-column>
        <el-table-column label="评分" width="120" align="center">
          <template #default="{ row }">{{ row.scorePassMark }}/{{ row.scoreFullMark }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="reviewTagType(row.reviewStatus)">{{ reviewText(row.reviewStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="统计" width="170">
          <template #default="{ row }">
            <div class="table-multi">
              <span>浏览 {{ row.viewCount || 0 }}</span>
              <span>学习 {{ row.studyCount || 0 }}</span>
              <span>抽查 {{ row.checkCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEditDialog(row.id)">编辑</el-button>
            <el-button text type="danger" @click="handleDelete(row.id)">删除</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增题目' : '编辑题目'"
      width="min(1100px, calc(100vw - 32px))"
      top="4vh"
      class="study-question-dialog"
    >
      <el-form :model="form" label-position="top" class="question-form">
        <div class="form-grid form-grid--meta">
          <el-form-item label="所属分类" class="span-2">
            <el-select v-model="form.categoryId" placeholder="请选择分类">
              <el-option v-for="item in flatCategories" :key="item.id" :label="item.label" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="题号">
            <el-input-number v-model="form.questionNo" :min="1" :max="99999" controls-position="right" />
          </el-form-item>
          <el-form-item label="题目编码">
            <el-input v-model="form.questionCode" placeholder="可选，建议保持唯一" />
          </el-form-item>
          <el-form-item label="题目类型">
            <el-input-number v-model="form.questionType" :min="1" :max="10" controls-position="right" />
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="form.difficulty">
              <el-option label="入门" :value="1" />
              <el-option label="进阶" :value="2" />
              <el-option label="较难" :value="3" />
              <el-option label="高阶" :value="4" />
              <el-option label="专家" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item label="预估分钟">
            <el-input-number v-model="form.estimatedMinutes" :min="1" :max="120" controls-position="right" />
          </el-form-item>
          <el-form-item label="满分">
            <el-input-number v-model="form.scoreFullMark" :min="0" :max="100" controls-position="right" />
          </el-form-item>
          <el-form-item label="及格分">
            <el-input-number v-model="form.scorePassMark" :min="0" :max="100" controls-position="right" />
          </el-form-item>
          <el-form-item label="审核状态">
            <el-select v-model="form.reviewStatus">
              <el-option label="草稿" :value="0" />
              <el-option label="待审核" :value="1" />
              <el-option label="已发布" :value="2" />
              <el-option label="已下线" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="启停状态" class="span-2">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <el-form-item label="题目标题">
          <el-input v-model="form.title" maxlength="300" />
        </el-form-item>
        <el-form-item label="题目内容">
          <el-input v-model="form.questionStem" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item label="标准答案">
          <el-input v-model="form.standardAnswer" type="textarea" :rows="10" />
        </el-form-item>
        <el-form-item label="答案摘要">
          <el-input v-model="form.answerSummary" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="form.keywords" placeholder="多个关键字可用逗号分隔" />
        </el-form-item>
        <el-form-item label="评分规则">
          <el-input v-model="form.scoreRubricJson" type="textarea" :rows="4" placeholder="可填写 JSON 评分维度" />
        </el-form-item>
        <div class="form-grid form-grid--extra">
          <el-form-item label="AI评分">
            <el-switch v-model="aiScoreSwitch" />
          </el-form-item>
          <el-form-item label="允许自评">
            <el-switch v-model="selfAssessmentSwitch" />
          </el-form-item>
          <el-form-item label="Prompt版本" class="span-2">
            <el-input v-model="form.aiScorePromptVersion" />
          </el-form-item>
          <el-form-item label="来源文件" class="span-2">
            <el-input v-model="form.sourceFileName" />
          </el-form-item>
          <el-form-item label="来源章节" class="span-2">
            <el-input v-model="form.sourceSection" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="批量导入 Markdown 题库" width="620px">
      <el-form :model="importForm" label-width="110px">
        <el-form-item label="题库目录">
          <el-input v-model="importForm.sourceDir" placeholder="默认使用项目根目录下的 面试题大全md" />
        </el-form-item>
        <el-form-item label="仅导入文件">
          <el-input v-model="importForm.onlyFile" placeholder="例如 Java八股文200道-Java基础篇.md" />
        </el-form-item>
        <el-form-item label="数量限制">
          <el-input-number v-model="importForm.limit" :min="1" :max="10000" />
        </el-form-item>
        <el-form-item label="建表预处理">
          <el-switch v-model="importForm.bootstrap" />
        </el-form-item>
        <el-form-item label="仅演练">
          <el-switch v-model="importForm.dryRun" />
        </el-form-item>
      </el-form>
      <div class="import-output" v-if="importOutput">
        <pre>{{ importOutput }}</pre>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="importing" @click="handleImport">执行导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import {
  createAdminStudyQuestion,
  deleteAdminStudyQuestion,
  getAdminStudyCategoryList,
  getAdminStudyQuestionDetail,
  getAdminStudyQuestionList,
  importAdminStudyQuestion,
  updateAdminStudyQuestion
} from '@/api/study'

const DEFAULT_SCORE_RUBRIC_JSON = JSON.stringify(
  {
    levels: {
      good: { min: 75, label: '良好' },
      pass: { min: 60, label: '及格' },
      weak: { min: 0, label: '待提升' },
      excellent: { min: 90, label: '优秀' }
    },
    dimensions: [
      { code: 'accuracy', name: '准确性', score: 40 },
      { code: 'completeness', name: '完整性', score: 30 },
      { code: 'clarity', name: '表达清晰度', score: 20 },
      { code: 'practicality', name: '实战性', score: 10 }
    ]
  },
  null,
  2
)

const loading = ref(false)
const submitting = ref(false)
const importing = ref(false)
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const list = ref([])
const total = ref(0)
const categories = ref([])
const importOutput = ref('')

const query = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  categoryId: null,
  difficulty: null,
  reviewStatus: null,
  status: null
})

const form = reactive({
  categoryId: null,
  questionNo: 1,
  questionCode: '',
  questionType: 1,
  title: '',
  questionStem: '',
  standardAnswer: '',
  answerSummary: '',
  keywords: '',
  difficulty: 2,
  estimatedMinutes: 5,
  scoreFullMark: 100,
  scorePassMark: 60,
  aiScoreEnabled: 1,
  selfAssessmentEnabled: 1,
  aiScorePromptVersion: 'v1',
  scoreRubricJson: DEFAULT_SCORE_RUBRIC_JSON,
  reviewStatus: 2,
  status: 1,
  sourceFileName: '',
  sourceSection: ''
})

const importForm = reactive({
  sourceDir: '',
  onlyFile: '',
  limit: 50,
  bootstrap: false,
  dryRun: false
})

const aiScoreSwitch = computed({
  get: () => form.aiScoreEnabled === 1,
  set: value => { form.aiScoreEnabled = value ? 1 : 0 }
})
const selfAssessmentSwitch = computed({
  get: () => form.selfAssessmentEnabled === 1,
  set: value => { form.selfAssessmentEnabled = value ? 1 : 0 }
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

function resetForm() {
  Object.assign(form, {
    categoryId: null,
    questionNo: 1,
    questionCode: '',
    questionType: 1,
    title: '',
    questionStem: '',
    standardAnswer: '',
    answerSummary: '',
    keywords: '',
    difficulty: 2,
    estimatedMinutes: 5,
    scoreFullMark: 100,
    scorePassMark: 60,
    aiScoreEnabled: 1,
    selfAssessmentEnabled: 1,
    aiScorePromptVersion: 'v1',
    scoreRubricJson: DEFAULT_SCORE_RUBRIC_JSON,
    reviewStatus: 2,
    status: 1,
    sourceFileName: '',
    sourceSection: ''
  })
}

async function fetchCategories() {
  const res = await getAdminStudyCategoryList({ includeDisabled: true })
  categories.value = res.data || []
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getAdminStudyQuestionList({ ...query })
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
  query.keyword = ''
  query.categoryId = null
  query.difficulty = null
  query.reviewStatus = null
  query.status = null
  fetchList()
}

function openCreateDialog() {
  dialogMode.value = 'create'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

async function openEditDialog(id) {
  dialogMode.value = 'edit'
  editingId.value = id
  resetForm()
  const res = await getAdminStudyQuestionDetail(id)
  Object.assign(form, res.data?.question || {})
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.categoryId) {
    ElMessage.warning('请选择分类')
    return
  }
  if (!form.title.trim()) {
    ElMessage.warning('请输入题目标题')
    return
  }
  if (!form.standardAnswer.trim()) {
    ElMessage.warning('请输入标准答案')
    return
  }
  submitting.value = true
  try {
    const payload = { ...form }
    if (dialogMode.value === 'create') {
      await createAdminStudyQuestion(payload)
      ElMessage.success('题目已创建')
    } else {
      await updateAdminStudyQuestion(editingId.value, payload)
      ElMessage.success('题目已更新')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除该题目吗？删除后不可恢复。', '提示', { type: 'warning' })
  await deleteAdminStudyQuestion(id)
  ElMessage.success('题目已删除')
  fetchList()
}

function openImportDialog() {
  importDialogVisible.value = true
  importOutput.value = ''
}

async function handleImport() {
  importing.value = true
  try {
    const payload = {
      bootstrap: importForm.bootstrap,
      dryRun: importForm.dryRun
    }
    if (importForm.sourceDir.trim()) payload.sourceDir = importForm.sourceDir.trim()
    if (importForm.onlyFile.trim()) payload.onlyFile = importForm.onlyFile.trim()
    if (importForm.limit) payload.limit = importForm.limit
    const res = await importAdminStudyQuestion(payload)
    importOutput.value = res.data?.output || '执行完成'
    ElMessage.success(payload.dryRun ? '演练完成' : '导入任务已执行')
    fetchList()
  } finally {
    importing.value = false
  }
}

function difficultyText(value) {
  const map = { 1: '入门', 2: '进阶', 3: '较难', 4: '高阶', 5: '专家' }
  return map[value] || '未分级'
}

function reviewText(value) {
  const map = { 0: '草稿', 1: '待审核', 2: '已发布', 3: '已下线' }
  return map[value] || '未知'
}

function reviewTagType(value) {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[value] || 'info'
}

onMounted(() => {
  fetchCategories()
  fetchList()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.study-question-page { padding: 20px; }
.page-header {
  display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 20px;
  h2 { color: var(--text-primary); font-size: 20px; margin-bottom: 6px; }
  p { color: var(--text-muted); font-size: 13px; }
}
.header-actions { display: flex; gap: 12px; }
.filter-card,
.table-card {
  background: var(--bg-card); border: 1px solid var(--border-color); border-radius: $radius-lg; padding: 20px;
}
.filter-card { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 20px; }
.table-multi { display: flex; flex-direction: column; gap: 4px; color: var(--text-muted); font-size: 12px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
.question-form {
  :deep(.el-form-item) { margin-bottom: 18px; }
  :deep(.el-form-item__label) {
    padding-bottom: 8px;
    color: var(--text-secondary);
    font-weight: 600;
    line-height: 1.2;
  }
  :deep(.el-select),
  :deep(.el-input),
  :deep(.el-input-number) { width: 100%; }
  :deep(.el-radio-group) {
    display: flex;
    flex-wrap: wrap;
    gap: 18px;
    min-height: 40px;
    align-items: center;
  }
}

.form-grid { display: grid; gap: 16px; }
.form-grid--meta { grid-template-columns: repeat(4, minmax(0, 1fr)); }
.form-grid--extra { grid-template-columns: repeat(4, minmax(0, 1fr)); }
.span-2 { grid-column: span 2; }

:deep(.study-question-dialog .el-dialog) {
  max-width: calc(100vw - 32px);
  border-radius: 18px;
}

:deep(.study-question-dialog .el-dialog__body) {
  max-height: calc(100vh - 180px);
  overflow-y: auto;
  padding-top: 20px;
}

:deep(.study-question-dialog .el-dialog__header) {
  padding-bottom: 10px;
}

:deep(.study-question-dialog .el-dialog__footer) {
  padding-top: 8px;
}

.import-output {
  margin-top: 12px; padding: 14px; border-radius: 12px; background: var(--bg-darker); border: 1px solid var(--border-color);
  pre { white-space: pre-wrap; word-break: break-word; color: var(--text-secondary); font-size: 12px; line-height: 1.7; }
}
@media (max-width: 1200px) {
  .form-grid--meta,
  .form-grid--extra { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 768px) {
  .page-header { flex-direction: column; align-items: flex-start; }
  .header-actions { width: 100%; flex-wrap: wrap; }
  .form-grid--meta,
  .form-grid--extra { grid-template-columns: 1fr; }
  .span-2 { grid-column: span 1; }
  :deep(.study-question-dialog .el-dialog__body) { max-height: calc(100vh - 140px); }
}
</style>
