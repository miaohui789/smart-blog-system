<template>
  <div class="study-task-page" v-loading="loading">
    <template v-if="task">
      <section class="task-header glass-card">
        <div>
          <div class="hero-badge">{{ task.status === 2 ? '已完成任务' : '进行中任务' }}</div>
          <h1>{{ task.taskName }}</h1>
          <p>{{ task.categoryName || '通用题库' }} · {{ modeText(task.checkMode) }} · 共 {{ task.questionCount }} 题</p>
        </div>
        <div class="task-actions">
          <el-button @click="$router.push('/study/check/history')">历史记录</el-button>
          <el-button type="primary" @click="handleFinishTask">完成任务</el-button>
        </div>
      </section>

      <section class="task-summary glass-card">
        <div class="summary-item">
          <span>已答题</span>
          <strong>{{ task.answeredCount }}/{{ task.questionCount }}</strong>
        </div>
        <div class="summary-item">
          <span>自评分</span>
          <strong>{{ formatScore(task.selfScore) }}</strong>
        </div>
        <div class="summary-item">
          <span>AI评分</span>
          <strong>{{ formatScore(task.aiScore) }}</strong>
        </div>
        <div class="summary-item">
          <span>最终得分</span>
          <strong>{{ formatScore(task.finalScore) }}</strong>
        </div>
      </section>

      <div class="task-grid">
        <aside class="task-sidebar glass-card">
          <div class="side-head">
            <h2>题目列表</h2>
            <span>{{ activeIndex + 1 }}/{{ task.items.length }}</span>
          </div>
          <div class="question-index-list">
            <button
              v-for="(item, index) in task.items"
              :key="item.id"
              class="index-btn"
              :class="{ active: index === activeIndex, done: item.finalScore != null || item.answerCount > 0 }"
              @click="activeIndex = index"
            >
              <span>{{ index + 1 }}</span>
              <small :class="['item-state', itemStateClass(item)]">{{ itemStatusText(item) }}</small>
            </button>
          </div>
        </aside>

        <section class="task-content glass-card" v-if="currentItem">
          <div class="question-head">
            <div>
              <div class="question-order">第 {{ activeIndex + 1 }} 题</div>
              <h2>{{ currentItem.questionTitleSnapshot }}</h2>
            </div>
            <div class="score-tags">
              <el-tag>满分 {{ formatScore(currentItem.scoreFullMark || 100) }}</el-tag>
              <el-tag type="success">及格 {{ formatScore(currentItem.scorePassMark || 60) }}</el-tag>
              <el-tag type="info">{{ scoringModeLabel }}</el-tag>
              <el-tag v-if="task.needAiScore === 1" type="warning">AI 已启用</el-tag>
            </div>
          </div>

          <div class="question-progress-card">
            <div class="question-progress-main">
              <div>
                <span class="question-progress-label">当前总分进度</span>
                <strong class="question-progress-value">{{ animatedTotalScorePercentText }}</strong>
              </div>
              <div class="question-progress-meta">
                已得 {{ formatScore(currentAccumulatedScore) }} / 总分 {{ formatScore(taskFullScore) }}
              </div>
            </div>
            <div class="question-progress-track">
              <div class="question-progress-fill" :style="{ width: `${displayedTotalScorePercent}%` }"></div>
            </div>
            <p class="question-progress-tip">
              当前显示的是整场任务累计得分占总满分的比例，例如 3 题满分共 300 分时，就按已得分 / 300 计算。
            </p>
          </div>

          <div class="content-section">
            <h3>题目内容</h3>
            <div class="question-content" v-html="renderPlain(currentItem.questionStemSnapshot || currentItem.questionTitleSnapshot)"></div>
          </div>

          <div class="content-section">
            <div class="section-title-row">
              <h3>我的作答</h3>
              <div class="section-actions">
                <span v-if="answerReadonly" class="readonly-state">已提交，仅可查看</span>
                <el-button v-if="task.showStandardAnswer === 1" @click="handleViewAnswer">
                  {{ currentItem.viewAnswerFlag === 1 ? '已查看标准答案' : '查看标准答案' }}
                </el-button>
              </div>
            </div>
            <el-input
              v-model="answerForm.answerContent"
              type="textarea"
              :rows="10"
              :readonly="answerReadonly"
              maxlength="50000"
              show-word-limit
              :placeholder="answerReadonly ? '本题已提交，当前仅支持查看历史作答。' : '先写下你的理解、知识点和面试表达，再决定是否查看标准答案。'"
            />
          </div>

          <div v-if="currentItem.viewAnswerFlag === 1" class="content-section answer-section">
            <h3>标准答案</h3>
            <div class="question-content" v-html="renderPlain(currentItem.standardAnswerSnapshot)"></div>
          </div>

          <div class="content-section">
            <h3>自评与评分</h3>
            <p class="score-rule">{{ aiTriggerNote }}</p>
            <div class="score-form">
              <template v-if="showSelfAssessment">
                <el-radio-group v-model="answerForm.selfAssessmentResult" :disabled="answerReadonly">
                  <el-radio-button :label="1">记得</el-radio-button>
                  <el-radio-button :label="2">模糊</el-radio-button>
                  <el-radio-button :label="3">忘记</el-radio-button>
                </el-radio-group>
                <el-input-number v-model="answerForm.selfScore" :min="0" :max="100" controls-position="right" :disabled="answerReadonly" />
              </template>
              <div v-else class="readonly-hint">
                当前任务关闭了自评，本题会直接按 AI 或系统规则计分。
              </div>
            </div>
            <el-input
              v-model="answerForm.selfComment"
              type="textarea"
              :rows="3"
              :readonly="answerReadonly"
              maxlength="1000"
              show-word-limit
              :placeholder="answerReadonly ? '本题复盘备注仅支持查看。' : '补充一下你本题的薄弱点或复盘结论...'"
            />
          </div>

          <div class="submit-actions">
            <el-button @click="goPrevQuestion" :disabled="activeIndex === 0 || submitting">上一题</el-button>
            <div class="submit-center">
              <el-button
                type="primary"
                :loading="submitting"
                :disabled="submitDisabled"
                @click="handleSubmitAnswer"
              >
                {{ submitButtonText }}
              </el-button>
              <div v-if="isCurrentItemAiThinking" class="submit-inline-state thinking">
                AI 正在思考中 · {{ aiThinkingElapsedText }}
              </div>
              <div v-else-if="currentItem.finalScore != null" class="submit-inline-state score">
                本题得分 {{ formatScore(currentItem.finalScore) }} / {{ formatScore(currentItem.scoreFullMark || 100) }}
              </div>
            </div>
            <el-button @click="goNextQuestion" :disabled="activeIndex >= task.items.length - 1 || submitting">下一题</el-button>
          </div>

          <div v-if="isCurrentItemAiThinking" class="ai-thinking-panel">
            <div class="thinking-orb"></div>
            <div class="thinking-copy">
              <h4>AI 正在思考中<span class="thinking-dots"><i></i><i></i><i></i></span></h4>
              <p>正在结合题目、标准答案和你的作答生成本题评测，请稍等 {{ aiThinkingElapsedText }}</p>
            </div>
          </div>

          <div v-if="currentItem.finalScore != null || currentItem.aiScoreDetail" class="result-panel">
            <div class="result-summary">
              <div class="result-item">
                <span>自评分</span>
                <strong>{{ currentItem.selfScore ?? '-' }}</strong>
              </div>
              <div class="result-item">
                <span>AI评分</span>
                <strong>{{ currentItem.aiScore ?? '-' }}</strong>
              </div>
              <div class="result-item">
                <span>最终得分</span>
                <strong>{{ currentItem.finalScore ?? '-' }}</strong>
              </div>
            </div>
            <div v-if="currentItem.aiScoreDetail?.scoreStatus === 3" class="ai-error">
              AI 评分失败：{{ currentItem.aiScoreDetail.errorMessage || '接口未返回有效评分结果' }}
            </div>
            <div v-if="currentItem.aiScoreDetail?.scoreStatus === 2" class="ai-meta">
              <span class="meta-pill" v-if="currentItem.aiScoreDetail.modelName">模型 {{ currentItem.aiScoreDetail.modelName }}</span>
              <span class="meta-pill" v-if="currentItem.aiScoreDetail.durationMs">耗时 {{ currentItem.aiScoreDetail.durationMs }}ms</span>
              <span class="meta-pill" v-if="currentItem.aiScoreDetail.totalTokens">Tokens {{ currentItem.aiScoreDetail.totalTokens }}</span>
            </div>
            <div class="ai-feedback" v-if="currentItem.aiScoreDetail">
              <div class="feedback-item" v-if="currentItem.aiScoreDetail.strengthsText">
                <h4>优点</h4>
                <p>{{ currentItem.aiScoreDetail.strengthsText }}</p>
              </div>
              <div class="feedback-item" v-if="currentItem.aiScoreDetail.weaknessesText">
                <h4>不足</h4>
                <p>{{ currentItem.aiScoreDetail.weaknessesText }}</p>
              </div>
              <div class="feedback-item" v-if="currentItem.aiScoreDetail.suggestionText">
                <h4>建议</h4>
                <p>{{ currentItem.aiScoreDetail.suggestionText }}</p>
              </div>
            </div>
          </div>
        </section>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { finishStudyTask, getStudyCheckTask, markStudyAnswerViewed, submitStudyAnswer } from '@/api/study'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const task = ref(null)
const activeIndex = ref(0)
const answerStartedAt = ref(Date.now())
const thinkingItemId = ref(null)
const aiThinkingSeconds = ref(0)
const displayedTotalScorePercent = ref(0)
let aiThinkingTimer = null
let totalPercentTimer = null

const answerForm = reactive({
  answerContent: '',
  selfAssessmentResult: 1,
  selfScore: 100,
  selfComment: ''
})

const currentItem = computed(() => task.value?.items?.[activeIndex.value] || null)
const scoringModeLabel = computed(() => {
  const map = { 1: '自评优先', 2: 'AI评分', 3: '自评 + AI 综合' }
  return map[task.value?.scoringMode] || '综合评分'
})
const showSelfAssessment = computed(() => task.value?.allowSelfAssessment !== 0)
const needAiScoring = computed(() => task.value?.needAiScore === 1 && task.value?.scoringMode !== 1)
const currentItemSubmitted = computed(() => {
  if (!currentItem.value) {
    return false
  }
  return currentItem.value.answerCount > 0
    || currentItem.value.status === 2
    || currentItem.value.status === 3
    || currentItem.value.finalScore != null
})
const answerReadonly = computed(() => !!currentItem.value && (task.value?.status === 2 || currentItemSubmitted.value))
const submitDisabled = computed(() => !currentItem.value || task.value?.status === 2 || currentItemSubmitted.value || isCurrentItemAiThinking.value)
const isCurrentItemAiThinking = computed(() => !!currentItem.value && submitting.value && thinkingItemId.value === currentItem.value.id)
const submitButtonText = computed(() => {
  if (task.value?.status === 2) {
    return '任务已完成'
  }
  if (isCurrentItemAiThinking.value || currentItem.value?.status === 2) {
    return 'AI评分中'
  }
  if (currentItemSubmitted.value) {
    return '本题已提交'
  }
  return '提交本题'
})
const aiTriggerNote = computed(() => {
  if (!task.value) {
    return ''
  }
  if (task.value.scoringMode === 1) {
    return '当前任务按自评结果计分，适合快速复盘。'
  }
  if (task.value.scoringMode === 2) {
    return '当前任务按 AI 评分计分，提交文字答案后会自动生成评分与建议。'
  }
  return '综合模式下，自评与 AI 会同时参与评分，提交文字作答后自动生成综合结果。'
})
const aiThinkingElapsedText = computed(() => `${aiThinkingSeconds.value} 秒`)
const currentAccumulatedScore = computed(() => {
  if (!task.value?.items?.length) {
    return 0
  }
  return task.value.items.reduce((sum, item) => {
    const score = Number(item?.finalScore ?? 0)
    return Number.isFinite(score) ? sum + score : sum
  }, 0)
})
const taskFullScore = computed(() => {
  const directFullScore = Number(task.value?.fullScore)
  if (Number.isFinite(directFullScore) && directFullScore > 0) {
    return directFullScore
  }
  if (!task.value?.items?.length) {
    return 0
  }
  return task.value.items.reduce((sum, item) => {
    const score = Number(item?.scoreFullMark ?? 100)
    return Number.isFinite(score) ? sum + score : sum
  }, 0)
})
const totalScorePercent = computed(() => getScorePercent(currentAccumulatedScore.value, taskFullScore.value))
const animatedTotalScorePercentText = computed(() => `${displayedTotalScorePercent.value}%`)

async function fetchTask() {
  loading.value = true
  try {
    const res = await getStudyCheckTask(route.params.taskId)
    task.value = res.data
    syncAnswerForm()
  } finally {
    loading.value = false
  }
}

function syncAnswerForm() {
  if (!currentItem.value) return
  answerForm.answerContent = currentItem.value.answerContent || ''
  answerForm.selfAssessmentResult = currentItem.value.selfAssessmentResult || 1
  answerForm.selfScore = currentItem.value.selfScore ?? defaultSelfScore(currentItem.value.selfAssessmentResult || 1)
  answerForm.selfComment = currentItem.value.selfComment || ''
  answerStartedAt.value = Date.now()
}

function startAiThinking(itemId) {
  stopAiThinking()
  thinkingItemId.value = itemId
  aiThinkingSeconds.value = 0
  aiThinkingTimer = window.setInterval(() => {
    aiThinkingSeconds.value += 1
  }, 1000)
}

function stopAiThinking() {
  if (aiThinkingTimer) {
    window.clearInterval(aiThinkingTimer)
    aiThinkingTimer = null
  }
  thinkingItemId.value = null
  aiThinkingSeconds.value = 0
}

function syncDisplayedTotalScorePercent(targetPercent) {
  if (totalPercentTimer) {
    window.clearInterval(totalPercentTimer)
    totalPercentTimer = null
  }

  const target = Number(targetPercent ?? 0)
  if (!Number.isFinite(target)) {
    displayedTotalScorePercent.value = 0
    return
  }

  const safeTarget = Math.max(0, Math.min(100, Math.round(target)))
  const start = displayedTotalScorePercent.value
  const distance = safeTarget - start
  if (distance === 0) {
    return
  }

  const steps = 18
  let currentStep = 0
  totalPercentTimer = window.setInterval(() => {
    currentStep += 1
    const nextValue = start + (distance * currentStep) / steps
    displayedTotalScorePercent.value = Math.round(nextValue)
    if (currentStep >= steps) {
      displayedTotalScorePercent.value = safeTarget
      window.clearInterval(totalPercentTimer)
      totalPercentTimer = null
    }
  }, 22)
}

function defaultSelfScore(result) {
  if (result === 2) return 60
  if (result === 3) return 0
  return 100
}

function formatScore(value) {
  if (value === null || value === undefined || value === '') {
    return '0'
  }
  const score = Number(value)
  if (!Number.isFinite(score)) {
    return '0'
  }
  return Number.isInteger(score) ? `${score}` : score.toFixed(2).replace(/\.?0+$/, '')
}

function getScorePercent(score, fullScore) {
  const safeScore = Number(score ?? 0)
  const safeFullScore = Number(fullScore ?? 100)
  if (!safeFullScore || !Number.isFinite(safeFullScore) || !Number.isFinite(safeScore)) {
    return 0
  }
  return Math.max(0, Math.min(100, Math.round((safeScore / safeFullScore) * 100)))
}

function itemStatusText(item) {
  if (!item) {
    return '待答题'
  }
  if (submitting.value && thinkingItemId.value === item.id) {
    return 'AI评分中'
  }
  if (item.finalScore != null) {
    return `${getScorePercent(item.finalScore, item.scoreFullMark)}%`
  }
  if (item.status === 2) {
    return '评分中'
  }
  if (item.answerCount > 0) {
    return '已作答'
  }
  return '待答题'
}

function itemStateClass(item) {
  if (!item) {
    return 'pending'
  }
  if (submitting.value && thinkingItemId.value === item.id) {
    return 'thinking'
  }
  if (item.finalScore != null) {
    return 'scored'
  }
  if (item.answerCount > 0) {
    return 'answered'
  }
  return 'pending'
}

watch(activeIndex, syncAnswerForm)
watch(totalScorePercent, value => {
  syncDisplayedTotalScorePercent(value)
}, { immediate: true })
watch(() => answerForm.selfAssessmentResult, value => {
  if (answerReadonly.value) {
    return
  }
  answerForm.selfScore = defaultSelfScore(value)
})

async function handleViewAnswer() {
  if (!currentItem.value || currentItem.value.viewAnswerFlag === 1) return
  await markStudyAnswerViewed(task.value.id, currentItem.value.id)
  currentItem.value.viewAnswerFlag = 1
  ElMessage.success('已标记查看标准答案')
}

async function handleSubmitAnswer() {
  if (!currentItem.value) return
  if (submitDisabled.value) {
    ElMessage.warning(currentItem.value?.status === 2 ? '本题正在评分处理中，请勿重复提交' : '本题已提交，无需重复作答')
    return
  }

  const answerContent = answerForm.answerContent.trim()
  const shouldTriggerAi = needAiScoring.value
  const submitItemId = currentItem.value.id

  if (shouldTriggerAi && !answerContent) {
    ElMessage.warning('启用 AI 评分时，请先填写本题的文字作答')
    return
  }

  submitting.value = true
  if (shouldTriggerAi) {
    startAiThinking(submitItemId)
  }

  try {
    const res = await submitStudyAnswer(task.value.id, submitItemId, {
      ...answerForm,
      answerContent,
      triggerAiScore: shouldTriggerAi ? 1 : 0,
      standardAnswerViewed: currentItem.value.viewAnswerFlag,
      answerDurationSeconds: Math.max(1, Math.floor((Date.now() - answerStartedAt.value) / 1000))
    })
    const scoreDetail = res.data?.aiScoreDetail
    if (scoreDetail?.scoreStatus === 3) {
      ElMessage.warning(scoreDetail.errorMessage || 'AI 评分未成功，本题已按可用分值完成计分')
    } else if (scoreDetail?.scoreStatus === 2) {
      ElMessage.success('本题已提交，AI 评分已生成，可直接查看报告')
    } else {
      ElMessage.success('本题已提交')
    }
    await fetchTask()
    const refreshedIndex = task.value?.items?.findIndex(item => item.id === submitItemId)
    if (refreshedIndex >= 0) {
      activeIndex.value = refreshedIndex
    }
  } catch (error) {
    const message = error?.message || ''
    if (error?.code === 'ECONNABORTED' || /timeout/i.test(message)) {
      ElMessage.error('AI 评测等待超时，请稍后刷新页面查看本题评分结果')
    } else {
      ElMessage.error(message || '提交失败，请稍后重试')
    }
  } finally {
    submitting.value = false
    stopAiThinking()
  }
}

async function handleFinishTask() {
  await finishStudyTask(route.params.taskId)
  ElMessage.success('任务已完成')
  navigateAfterFinish()
}

function navigateAfterFinish() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/study/check/history')
}

function goPrevQuestion() {
  if (activeIndex.value > 0) activeIndex.value -= 1
}

function goNextQuestion() {
  if (task.value && activeIndex.value < task.value.items.length - 1) activeIndex.value += 1
}

function modeText(value) {
  const map = { 1: '随机抽查', 2: '专项抽查', 3: '收藏抽查', 4: '薄弱题抽查', 5: '错题抽查', 6: '混合抽查' }
  return map[value] || '抽查'
}

function renderPlain(text) {
  if (!text) return '<p>暂无内容</p>'
  return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br/>')
}

onMounted(fetchTask)
onUnmounted(() => {
  stopAiThinking()
  if (totalPercentTimer) {
    window.clearInterval(totalPercentTimer)
    totalPercentTimer = null
  }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.study-task-page { display: flex; flex-direction: column; gap: 20px; min-height: 560px; position: relative; }
.task-header, .task-summary, .task-sidebar, .task-content { padding: 22px; }
.task-header { display: flex; justify-content: space-between; gap: 16px; align-items: center; }
.hero-badge {
  width: fit-content; padding: 6px 12px; border-radius: 999px; background: rgba($primary-color, 0.12);
  color: $primary-color; font-size: 12px; margin-bottom: 10px;
}
h1 { color: var(--text-primary); font-size: 28px; margin-bottom: 8px; }
p { color: var(--text-secondary); }
.task-actions { display: flex; gap: 12px; }
.task-summary { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; }
.summary-item {
  padding: 16px; border-radius: 16px; background: rgba($primary-color, 0.06);
  span { display: block; color: var(--text-muted); font-size: 12px; margin-bottom: 6px; }
  strong { color: var(--text-primary); font-size: 24px; }
}
.task-grid { display: grid; grid-template-columns: 260px minmax(0, 1fr); gap: 20px; }
.side-head, .question-head, .section-title-row, .result-summary { display: flex; justify-content: space-between; gap: 12px; align-items: center; }
.side-head {
  margin-bottom: 14px;
  h2 { color: var(--text-primary); font-size: 18px; }
  span { color: var(--text-muted); }
}
.question-index-list { display: grid; gap: 10px; }
.index-btn {
  padding: 12px 14px; border-radius: 14px; border: 1px solid var(--border-color); background: transparent; color: var(--text-secondary);
  text-align: left; cursor: pointer; transition: all 0.2s ease; display: flex; justify-content: space-between; align-items: center;
  span { font-weight: 600; color: var(--text-primary); }
  small { font-size: 12px; }
  &.active { border-color: rgba($primary-color, 0.42); background: rgba($primary-color, 0.08); }
  &.done .item-state.scored { color: #22c55e; }
}
.item-state {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.04);
  color: var(--text-muted);
}
.item-state.pending { color: var(--text-muted); }
.item-state.answered { color: #f59e0b; background: rgba(#f59e0b, 0.12); }
.item-state.scored { color: #22c55e; background: rgba(#22c55e, 0.12); }
.item-state.thinking { color: $primary-color; background: rgba($primary-color, 0.12); }
.question-head {
  margin-bottom: 20px;
  h2 { color: var(--text-primary); font-size: 22px; margin-top: 6px; line-height: 1.6; }
}
.question-progress-card {
  margin: -2px 0 22px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba($primary-color, 0.14);
  background: linear-gradient(180deg, rgba($primary-color, 0.08), rgba($primary-color, 0.03));
}
.question-progress-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}
.question-progress-label {
  display: block;
  margin-bottom: 6px;
  color: var(--text-muted);
  font-size: 12px;
}
.question-progress-value {
  color: var(--text-primary);
  font-size: 28px;
  line-height: 1;
}
.question-progress-meta {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 600;
}
.question-progress-track {
  height: 10px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
}
.question-progress-fill {
  height: 100%;
  width: 0;
  border-radius: inherit;
  background: linear-gradient(90deg, #2563eb, #60a5fa);
  transition: width 0.45s ease;
}
.question-progress-tip {
  margin-top: 10px;
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.8;
}
.question-order { color: $primary-color; font-size: 13px; font-weight: 600; }
.score-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.content-section + .content-section { margin-top: 20px; }
.content-section h3 { color: var(--text-primary); font-size: 17px; margin-bottom: 12px; }
.question-content { padding: 16px; border-radius: 14px; background: var(--bg-card-hover); line-height: 1.9; color: var(--text-secondary); }
.section-actions { display: flex; gap: 8px; }
.readonly-state {
  display: inline-flex; align-items: center; padding: 8px 12px; border-radius: 999px;
  background: rgba($primary-color, 0.1); color: $primary-color; font-size: 12px; font-weight: 600;
}
.answer-section { border-top: 1px dashed var(--border-color); padding-top: 20px; }
.score-form { display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 14px; }
.score-rule { color: var(--text-muted); font-size: 13px; line-height: 1.8; margin-bottom: 12px; }
.readonly-hint {
  padding: 10px 14px; border-radius: 12px; background: var(--bg-card-hover);
  border: 1px dashed var(--border-color); color: var(--text-muted); font-size: 13px;
}
.submit-actions {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: center;
  margin-top: 20px;
}
.submit-center {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.submit-inline-state {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}
.submit-inline-state.score {
  background: rgba(#22c55e, 0.12);
  color: #22c55e;
}
.submit-inline-state.thinking {
  background: rgba($primary-color, 0.12);
  color: $primary-color;
}
.ai-thinking-panel {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 18px;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid rgba($primary-color, 0.22);
  background: rgba($primary-color, 0.07);
}
.thinking-orb {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #60a5fa;
  box-shadow: 0 0 0 rgba(#60a5fa, 0.45);
  animation: pulseOrb 1.6s ease infinite;
  flex-shrink: 0;
}
.thinking-copy h4 {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 6px;
  color: var(--text-primary);
  font-size: 16px;
}
.thinking-copy p {
  color: var(--text-secondary);
  line-height: 1.8;
}
.thinking-dots {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.thinking-dots i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: $primary-color;
  opacity: 0.25;
  animation: blinkDot 1.2s ease infinite;
}
.thinking-dots i:nth-child(2) { animation-delay: 0.2s; }
.thinking-dots i:nth-child(3) { animation-delay: 0.4s; }
.result-panel { margin-top: 22px; border-top: 1px solid var(--border-color); padding-top: 18px; }
.result-summary { margin-bottom: 16px; }
.result-item {
  flex: 1; padding: 14px; border-radius: 14px; background: rgba($primary-color, 0.06);
  span { display: block; color: var(--text-muted); font-size: 12px; margin-bottom: 6px; }
  strong { color: var(--text-primary); font-size: 20px; }
}
.ai-error {
  margin-bottom: 14px; padding: 12px 14px; border-radius: 14px; background: rgba(#ef4444, 0.08);
  border: 1px solid rgba(#ef4444, 0.2); color: #ef4444; line-height: 1.7;
}
.ai-meta { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 14px; }
.meta-pill {
  padding: 5px 10px; border-radius: 999px; background: var(--bg-card-hover);
  border: 1px solid var(--border-color); color: var(--text-muted); font-size: 12px;
}
.ai-feedback { display: grid; gap: 12px; }
.feedback-item {
  padding: 16px; border-radius: 14px; background: var(--bg-card-hover);
  h4 { color: var(--text-primary); margin-bottom: 8px; font-size: 15px; }
  p { color: var(--text-secondary); line-height: 1.8; }
}
:deep(.el-loading-mask) {
  border-radius: 20px;
}
@keyframes pulseOrb {
  0% { transform: scale(0.9); box-shadow: 0 0 0 0 rgba(#60a5fa, 0.45); }
  70% { transform: scale(1); box-shadow: 0 0 0 12px rgba(#60a5fa, 0); }
  100% { transform: scale(0.9); box-shadow: 0 0 0 0 rgba(#60a5fa, 0); }
}
@keyframes blinkDot {
  0%, 80%, 100% { opacity: 0.25; transform: translateY(0); }
  40% { opacity: 1; transform: translateY(-2px); }
}
@media (max-width: 1100px) {
  .task-summary { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .task-grid { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .task-header, .task-actions, .question-head, .section-title-row, .result-summary {
    flex-direction: column; align-items: flex-start;
  }
  .task-summary { grid-template-columns: 1fr; }
  .question-progress-main {
    flex-direction: column;
    align-items: flex-start;
  }
  .submit-actions {
    grid-template-columns: 1fr;
  }
  .submit-center {
    justify-content: flex-start;
  }
  .ai-thinking-panel {
    align-items: flex-start;
  }
}
</style>
