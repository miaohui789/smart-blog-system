<template>
  <div class="study-check-page">
    <section class="check-hero glass-card">
      <div>
        <h1>面试题抽查</h1>
        <p>支持随机抽题、专项练习、错题优先、收藏抽查，并结合 AI 打分与自评形成复盘结果。</p>
      </div>
      <div class="hero-links">
        <el-button type="primary" @click="createTask">立即开始</el-button>
        <el-button @click="$router.push('/study/check/history')">查看历史</el-button>
        <el-button text @click="$router.push('/study')">返回</el-button>
      </div>
    </section>

    <template v-if="userStore.isLoggedIn">
      <section class="stats-grid">
        <div class="stats-card glass-card" v-for="item in statCards" :key="item.label">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </section>

      <section class="config-panel glass-card">
        <div class="panel-title">
          <h2>创建抽查任务</h2>
          <p>先选择本次抽查方式，再进入答题页。</p>
        </div>
        <el-form label-position="top">
          <div class="form-grid">
            <el-form-item label="任务名称">
              <el-input v-model="form.taskName" placeholder="例如：Java基础晚间抽查" maxlength="100" />
            </el-form-item>
            <el-form-item label="抽查模式">
              <el-select v-model="form.checkMode">
                <el-option label="随机抽查" :value="1" />
                <el-option label="专项分类抽查" :value="2" />
                <el-option label="收藏题抽查" :value="3" />
                <el-option label="薄弱题抽查" :value="4" />
                <el-option label="错题抽查" :value="5" />
                <el-option label="混合抽查" :value="6" />
              </el-select>
            </el-form-item>
            <el-form-item label="题目数量">
              <el-input-number v-model="form.questionCount" :min="1" :max="50" controls-position="right" />
            </el-form-item>
            <el-form-item label="难度范围">
              <el-select v-model="form.difficulty" clearable placeholder="全部难度">
                <el-option label="入门" :value="1" />
                <el-option label="进阶" :value="2" />
                <el-option label="较难" :value="3" />
                <el-option label="高阶" :value="4" />
                <el-option label="专家" :value="5" />
              </el-select>
            </el-form-item>
            <el-form-item label="专项分类" v-if="form.checkMode === 2">
              <el-select v-model="form.categoryId" placeholder="请选择分类">
                <el-option v-for="item in flatCategories" :key="item.id" :label="item.label" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="评分模式">
              <el-select v-model="form.scoringMode">
                <el-option label="自评优先" :value="1" />
                <el-option label="AI评分" :value="2" />
                <el-option label="自评 + AI综合" :value="3" />
              </el-select>
            </el-form-item>
          </div>

          <div class="switch-grid">
            <div class="switch-item switch-item--info">
              <div class="switch-copy">
                <span class="switch-title">评分配置</span>
                <small class="switch-note">{{ scoringModeAutoNote }}</small>
              </div>
            </div>
            <div class="switch-item">
              <span class="switch-title">支持查看标准答案</span>
              <el-switch v-model="showAnswerSwitch" />
            </div>
            <div class="switch-item">
              <span class="switch-title">仅错题</span>
              <el-switch v-model="onlyWrongSwitch" />
            </div>
            <div class="switch-item">
              <span class="switch-title">仅收藏题</span>
              <el-switch v-model="onlyFavoriteSwitch" />
            </div>
          </div>

          <div class="config-tips">
            <div class="tip-card">
              <h3>评分逻辑</h3>
              <p>{{ scoringModeSummary }}</p>
            </div>
            <div class="tip-card">
              <h3>AI 触发条件</h3>
              <p>{{ aiTriggerSummary }}</p>
            </div>
          </div>

          <div class="submit-row">
            <el-button type="primary" :loading="creating" @click="createTask">创建任务并开始答题</el-button>
          </div>
        </el-form>
      </section>

      <section class="history-panel glass-card">
        <div class="panel-title">
          <h2>最近抽查</h2>
          <p>仅保留最近两天的抽查任务，方便快速复盘并控制存储占用。</p>
        </div>
        <div class="history-list" v-if="history.length">
          <div v-for="item in history" :key="item.id" class="history-card">
            <div class="history-head">
              <div>
                <h3>{{ item.taskName }}</h3>
                <p>{{ item.categoryName || '通用题库' }} · {{ statusText(item.status) }}</p>
              </div>
              <el-button text type="primary" @click="$router.push(`/study/check/${item.id}`)">查看</el-button>
            </div>
            <div class="history-metrics">
              <span>题量 {{ item.questionCount }}</span>
              <span>完成 {{ item.answeredCount }}/{{ item.questionCount }}</span>
              <span>得分 {{ item.finalScore || 0 }}</span>
              <span>{{ formatDateTime(item.submitTime || item.startTime) }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="还没有抽查记录，创建第一场测试吧" />
      </section>
    </template>

    <section v-else class="login-panel glass-card">
      <h2>登录后开启学习闭环</h2>
      <p>抽查任务、AI评分、自评记录、历史统计都需要登录后保存。</p>
      <el-button type="primary" @click="$router.push('/login')">前往登录</el-button>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils/format'
import { createStudyCheckTask, getStudyCategories, getStudyCheckHistory, getStudyCheckStatistics } from '@/api/study'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const creating = ref(false)
const categories = ref([])
const history = ref([])
const stats = ref({})
const form = reactive({
  taskName: '今日面试题抽查',
  checkMode: 1,
  categoryId: null,
  questionCount: 10,
  difficulty: null,
  needAiScore: 1,
  allowSelfAssessment: 1,
  showStandardAnswer: 1,
  scoringMode: 3,
  onlyWrongQuestions: 0,
  onlyFavorites: 0
})

const showAnswerSwitch = computed({
  get: () => form.showStandardAnswer === 1,
  set: value => { form.showStandardAnswer = value ? 1 : 0 }
})
const onlyWrongSwitch = computed({
  get: () => form.onlyWrongQuestions === 1,
  set: value => { form.onlyWrongQuestions = value ? 1 : 0 }
})
const onlyFavoriteSwitch = computed({
  get: () => form.onlyFavorites === 1,
  set: value => { form.onlyFavorites = value ? 1 : 0 }
})
const scoringModeAutoNote = computed(() => {
  if (form.scoringMode === 1) {
    return '仅保留自评分，系统不会调用 AI，也不会展示 AI 评分相关输入。'
  }
  if (form.scoringMode === 2) {
    return '仅使用 AI 评分，系统会自动关闭自评输入，并优先抽取支持 AI 评分的题目。'
  }
  return '自评与 AI 会同时启用，提交文字作答后自动生成综合得分。'
})
const scoringModeSummary = computed(() => {
  if (form.scoringMode === 1) {
    return '最终得分取自评结果，适合快速复盘。'
  }
  if (form.scoringMode === 2) {
    return '最终得分完全由 AI 评分给出，建议填写完整作答。'
  }
  return '综合模式下，自评与 AI 同时存在时取平均分。'
})
const aiTriggerSummary = computed(() => {
  if (form.scoringMode === 1) {
    return '当前模式不会触发 AI 评分。'
  }
  if (form.scoringMode === 2) {
    return '提交文字答案后会自动触发 AI 评分；若题目不支持 AI，将不会进入本次任务。'
  }
  return '综合模式下，提交文字答案后自动触发 AI 评分，并与自评结果共同参与最终得分。'
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

const statCards = computed(() => [
  { label: '累计任务', value: stats.value.totalTaskCount || 0 },
  { label: '完成任务', value: stats.value.completedTaskCount || 0 },
  { label: '掌握题目', value: stats.value.masteredQuestionCount || 0 },
  { label: '平均得分', value: stats.value.avgScore || 0 },
  { label: '最高得分', value: stats.value.bestScore || 0 },
  { label: '最近得分', value: stats.value.latestScore || 0 }
])

watch(() => form.scoringMode, value => {
  if (value === 1) {
    form.needAiScore = 0
    form.allowSelfAssessment = 1
    return
  }
  if (value === 2) {
    form.needAiScore = 1
    form.allowSelfAssessment = 0
    return
  }
  form.needAiScore = 1
  form.allowSelfAssessment = 1
}, { immediate: true })

async function fetchBaseData() {
  const [categoryRes, statRes, historyRes] = await Promise.all([
    getStudyCategories(),
    getStudyCheckStatistics(),
    getStudyCheckHistory({ page: 1, pageSize: 5 })
  ])
  categories.value = categoryRes.data || []
  stats.value = statRes.data || {}
  history.value = historyRes.data?.list || []
}

async function createTask() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (!form.taskName.trim()) {
    ElMessage.warning('请输入任务名称')
    return
  }
  if (form.checkMode === 2 && !form.categoryId) {
    ElMessage.warning('专项抽查请选择分类')
    return
  }
  creating.value = true
  try {
    const res = await createStudyCheckTask({
      ...form,
      taskName: form.taskName.trim()
    })
    ElMessage.success('抽查任务已创建')
    router.push(`/study/check/${res.data.id}`)
  } finally {
    creating.value = false
  }
}

function statusText(value) {
  const map = { 0: '待开始', 1: '进行中', 2: '已完成', 3: '已终止', 4: '已过期' }
  return map[value] || '未知状态'
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchBaseData()
  }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.study-check-page { display: flex; flex-direction: column; gap: 24px; min-height: 520px; }
.check-hero, .config-panel, .history-panel, .login-panel { padding: 24px; }
.check-hero { display: flex; justify-content: space-between; gap: 20px; align-items: center; }
.hero-badge {
  width: fit-content; padding: 6px 12px; border-radius: 999px; background: rgba($primary-color, 0.12);
  color: $primary-color; font-size: 12px; font-weight: 600; margin-bottom: 12px;
}
h1 { color: var(--text-primary); font-size: 28px; margin-bottom: 10px; }
p { color: var(--text-secondary); line-height: 1.8; }
.hero-links { display: flex; gap: 12px; flex-wrap: wrap; }
.stats-grid { display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 16px; }
.stats-card {
  padding: 18px;
  span { display: block; color: var(--text-muted); font-size: 13px; margin-bottom: 8px; }
  strong { color: var(--text-primary); font-size: 24px; }
}
.panel-title {
  margin-bottom: 20px;
  h2 { color: var(--text-primary); font-size: 20px; margin-bottom: 6px; }
  p { color: var(--text-muted); font-size: 13px; }
}
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px 18px; }
.switch-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; margin-top: 6px; }
.switch-item {
  display: flex; justify-content: space-between; align-items: center; gap: 18px; padding: 14px 16px; border: 1px solid var(--border-color);
  border-radius: 14px; background: var(--bg-card-hover); color: var(--text-secondary);
}
.switch-item--info {
  align-items: flex-start;
  grid-column: 1 / -1;
}
.switch-copy { display: flex; flex-direction: column; gap: 4px; }
.switch-title { color: var(--text-primary); font-weight: 600; }
.switch-note { color: var(--text-muted); font-size: 12px; line-height: 1.5; }
.config-tips { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; margin-top: 18px; }
.tip-card {
  padding: 14px 16px; border-radius: 14px; border: 1px solid var(--border-color); background: var(--bg-card-hover);
  h3 { color: var(--text-primary); font-size: 15px; margin-bottom: 6px; }
  p { color: var(--text-muted); font-size: 13px; line-height: 1.8; }
}
.submit-row { margin-top: 22px; display: flex; justify-content: flex-end; }
.history-list { display: grid; gap: 14px; }
.history-card { padding: 18px; border: 1px solid var(--border-color); border-radius: 16px; background: var(--bg-card-hover); }
.history-head, .history-metrics { display: flex; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.history-head {
  margin-bottom: 10px;
  h3 { color: var(--text-primary); font-size: 17px; margin-bottom: 4px; }
  p { color: var(--text-muted); font-size: 12px; }
}
.history-metrics { color: var(--text-secondary); font-size: 13px; }
.login-panel {
  text-align: center;
  h2 { color: var(--text-primary); margin-bottom: 10px; }
  .el-button { margin-top: 18px; }
}
@media (max-width: 1100px) {
  .stats-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .form-grid, .switch-grid, .config-tips { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .check-hero { flex-direction: column; align-items: flex-start; }
  .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .form-grid, .switch-grid, .config-tips { grid-template-columns: 1fr; }
  
  .hero-links {
    width: 100%;
    display: flex;
    gap: 12px;
    
    .el-button {
      flex: 1;
      margin: 0 !important;
    }
  }
  
  .submit-row {
    .el-button {
      width: 100%;
    }
  }
}
</style>
