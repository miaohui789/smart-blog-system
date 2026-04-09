<template>
  <div class="study-question-detail" v-loading="loading">
    <div class="detail-grid" v-if="detail">
      <section class="detail-main glass-card">
        <div class="title-row">
          <div class="title-main">
            <div class="meta-row">
              <span class="category-chip">{{ detail.categoryName || '未分类' }}</span>
              <span class="meta-chip">#{{ detail.questionNo || detail.id }}</span>
              <span class="meta-chip">{{ difficultyText(detail.difficulty) }}</span>
              <span class="meta-chip">{{ statusText(detail.studyStatus) }}</span>
            </div>
            <h1>{{ detail.title }}</h1>
          </div>
          <div class="title-actions">
            <div class="action-label">快捷操作</div>
            <div class="action-grid">
              <el-button class="detail-action-btn detail-action-btn--back" @click="handleGoBack">
                返回上一页
              </el-button>
              <el-button class="detail-action-btn detail-action-btn--study" @click="handleRecordStudy">
                <el-icon><Reading /></el-icon>
                记录学习
              </el-button>
              <el-button
                class="detail-action-btn"
                :class="detail.isFavorite === 1 ? 'detail-action-btn--favorite-active' : 'detail-action-btn--favorite'"
                @click="handleToggleFavorite"
              >
                <el-icon><Star /></el-icon>
                {{ detail.isFavorite === 1 ? '已收藏' : '收藏题目' }}
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="hasQuestionContent" class="content-block">
          <div class="block-header">
            <h2>题目内容</h2>
          </div>
          <div class="rich-text" v-html="renderPlain(detail.questionStem)"></div>
        </div>

        <div class="content-block">
          <div class="block-header">
            <div class="block-heading">
              <h2>标准答案</h2>
              <p>默认收起，确认思路后再展开查看。</p>
            </div>
            <div class="answer-switch-wrap">
              <span class="switch-caption">{{ answerVisible ? '已展开' : '已收起' }}</span>
              <el-switch v-model="answerVisible" inline-prompt active-text="展开" inactive-text="收起" />
            </div>
          </div>
          <div v-if="answerVisible" class="rich-text answer-panel" v-html="renderPlain(detail.standardAnswer)"></div>
          <el-empty v-else description="点击右上角开关查看标准答案" />
        </div>

        <div class="content-block">
          <div class="block-header">
            <h2>学习笔记</h2>
            <p>支持记录自己的答题框架、易错点和复盘结论。</p>
          </div>
          <div class="note-editor">
            <el-select v-model="noteForm.noteType" style="width: 180px">
              <el-option label="普通笔记" :value="1" />
              <el-option label="复习笔记" :value="2" />
              <el-option label="错题笔记" :value="3" />
              <el-option label="模板总结" :value="4" />
            </el-select>
            <el-switch v-model="notePinned" inline-prompt active-text="置顶" inactive-text="普通" />
          </div>
          <el-input
            v-model="noteForm.content"
            type="textarea"
            :rows="5"
            maxlength="20000"
            show-word-limit
            placeholder="记录这道题的知识结构、关键词和答题表达..."
          />
          <div class="note-submit">
            <el-button type="primary" @click="handleSaveNote">保存笔记</el-button>
          </div>

          <div class="note-list" v-if="detail.notes?.length">
            <div v-for="note in detail.notes" :key="note.id" class="note-card">
              <div class="note-head">
                <span class="note-type">{{ noteTypeText(note.noteType) }}</span>
                <span class="note-time">{{ formatDateTime(note.updateTime || note.createTime) }}</span>
              </div>
              <p>{{ note.content }}</p>
            </div>
          </div>
          <el-empty v-else description="还没有学习笔记，先记下你的第一条总结吧" />
        </div>
      </section>

      <aside class="detail-side">
        <section class="side-card glass-card">
          <div class="side-title">学习进度</div>
          <div class="progress-row">
            <span>学习状态</span>
            <el-tag>{{ statusText(statusForm.studyStatus) }}</el-tag>
          </div>
          <div class="progress-row">
            <span>掌握度</span>
            <strong>{{ statusForm.masteryLevel }}/5</strong>
          </div>
          <div class="progress-row">
            <span>复习优先级</span>
            <strong>{{ statusForm.reviewPriority }}</strong>
          </div>
          <div class="progress-row">
            <span>最近学习</span>
            <span>{{ formatDateTime(detail.lastStudyTime) || '暂无' }}</span>
          </div>
          <div class="progress-row">
            <span>最近抽查</span>
            <span>{{ formatDateTime(detail.lastCheckTime) || '暂无' }}</span>
          </div>
          <div class="progress-row">
            <span>下次复习</span>
            <span>{{ formatDateTime(detail.nextReviewTime) || '待安排' }}</span>
          </div>
        </section>

        <section class="side-card glass-card">
          <div class="side-title">更新状态</div>
          <el-form label-position="top">
            <el-form-item label="学习状态">
              <el-select v-model="statusForm.studyStatus">
                <el-option label="未开始" :value="0" />
                <el-option label="学习中" :value="1" />
                <el-option label="已掌握" :value="2" />
                <el-option label="待复习" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="掌握度">
              <el-slider v-model="statusForm.masteryLevel" :min="0" :max="5" show-stops />
            </el-form-item>
            <el-form-item label="复习优先级">
              <el-slider v-model="statusForm.reviewPriority" :min="1" :max="5" show-stops />
            </el-form-item>
          </el-form>
          <el-button type="primary" class="full-btn" @click="handleUpdateStatus">保存学习状态</el-button>
        </section>

        <section class="side-card glass-card stat-side">
          <div class="mini-stat">
            <span>学习次数</span>
            <strong>{{ detail.studyCount || 0 }}</strong>
          </div>
          <div class="mini-stat">
            <span>抽查次数</span>
            <strong>{{ detail.checkCount || 0 }}</strong>
          </div>
          <div class="mini-stat">
            <span>最佳得分</span>
            <strong>{{ detail.bestScore || 0 }}</strong>
          </div>
          <div class="mini-stat">
            <span>平均得分</span>
            <strong>{{ detail.avgScore || 0 }}</strong>
          </div>
        </section>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading, Star } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/format'
import { getStudyQuestionDetail, recordStudy, saveStudyNote, toggleStudyFavorite, updateStudyStatus } from '@/api/study'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const detail = ref(null)
const answerVisible = ref(false)
const notePinned = ref(false)
const noteForm = reactive({ noteType: 1, content: '' })
const statusForm = reactive({ studyStatus: 0, masteryLevel: 0, reviewPriority: 3 })

const hasQuestionContent = computed(() => {
  const stem = detail.value?.questionStem?.trim()
  const title = detail.value?.title?.trim()
  return !!stem && stem !== title
})

async function fetchDetail() {
  loading.value = true
  try {
    const res = await getStudyQuestionDetail(route.params.id)
    detail.value = res.data
    if (detail.value) {
      statusForm.studyStatus = detail.value.studyStatus ?? 0
      statusForm.masteryLevel = detail.value.masteryLevel ?? 0
      statusForm.reviewPriority = detail.value.reviewPriority ?? 3
    }
  } finally {
    loading.value = false
  }
}

function ensureLogin() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('登录后可保存学习进度和笔记')
    router.push('/login')
    return false
  }
  return true
}

async function handleRecordStudy() {
  if (!ensureLogin()) return
  await recordStudy(route.params.id)
  userStore.refreshExpSummaryWithRetry()
  ElMessage.success('已记录本次学习')
  await fetchDetail()
}

async function handleToggleFavorite() {
  if (!ensureLogin()) return
  const nextFavorite = detail.value?.isFavorite === 1 ? 0 : 1
  await toggleStudyFavorite(route.params.id, nextFavorite)
  ElMessage.success(nextFavorite ? '收藏成功' : '已取消收藏')
  await fetchDetail()
}

async function handleSaveNote() {
  if (!ensureLogin()) return
  if (!noteForm.content.trim()) {
    ElMessage.warning('请输入笔记内容')
    return
  }
  await saveStudyNote(route.params.id, {
    ...noteForm,
    isPinned: notePinned.value ? 1 : 0
  })
  ElMessage.success('笔记已保存')
  noteForm.content = ''
  notePinned.value = false
  await fetchDetail()
}

async function handleUpdateStatus() {
  if (!ensureLogin()) return
  await updateStudyStatus(route.params.id, { ...statusForm })
  ElMessage.success('学习状态已更新')
  await fetchDetail()
}

function handleGoBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/study/learn')
}

function renderPlain(text) {
  if (!text) return '<p>暂无内容</p>'
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br/>')
}

function difficultyText(value) {
  const map = { 1: '入门', 2: '进阶', 3: '较难', 4: '高阶', 5: '专家' }
  return map[value] || '未分级'
}

function statusText(value) {
  const map = { 0: '未开始', 1: '学习中', 2: '已掌握', 3: '待复习' }
  return map[value] || '未开始'
}

function noteTypeText(value) {
  const map = { 1: '普通笔记', 2: '复习笔记', 3: '错题笔记', 4: '模板总结' }
  return map[value] || '学习笔记'
}

watch(() => route.params.id, fetchDetail)
onMounted(fetchDetail)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.study-question-detail {
  min-height: 560px;
  position: relative;
  font-weight: 400;
  --detail-action-neutral-bg: rgba(var(--bg-card-rgb), 0.34);
  --detail-action-neutral-border: rgba(255, 255, 255, 0.08);
  --detail-action-neutral-text: var(--text-primary);
  --detail-action-neutral-shadow: 0 12px 30px rgba(0, 0, 0, 0.18);
  --detail-action-study-bg: linear-gradient(135deg, rgba(59, 130, 246, 0.24), rgba(37, 99, 235, 0.18));
  --detail-action-study-border: rgba(96, 165, 250, 0.42);
  --detail-action-study-text: #ffffff;
  --detail-action-study-shadow: 0 14px 34px rgba(37, 99, 235, 0.22);
  --detail-action-favorite-bg: linear-gradient(135deg, rgba(245, 158, 11, 0.14), rgba(251, 191, 36, 0.08));
  --detail-action-favorite-border: rgba(245, 158, 11, 0.28);
  --detail-action-favorite-text: #fcd34d;
  --detail-action-favorite-shadow: 0 14px 34px rgba(245, 158, 11, 0.18);
  --detail-action-favorite-active-bg: linear-gradient(135deg, rgba(245, 158, 11, 0.24), rgba(251, 191, 36, 0.18));
  --detail-action-favorite-active-border: rgba(251, 191, 36, 0.4);
  --detail-action-favorite-active-text: #fde68a;
  --detail-action-favorite-active-shadow: 0 16px 36px rgba(245, 158, 11, 0.24);
  --detail-action-primary-shadow: 0 16px 38px rgba(59, 130, 246, 0.24);
}
.detail-grid { display: grid; grid-template-columns: minmax(0, 1fr) 320px; gap: 24px; }
.detail-main, .side-card { padding: 24px; }
.title-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 248px;
  gap: 24px;
  align-items: start;
  margin-bottom: 24px;
}
.title-main { min-width: 0; }
.meta-row { display: flex; gap: 8px; flex-wrap: wrap; }
.title-actions {
  min-width: 0;
  display: grid;
  gap: 10px;
  align-content: start;
}
.action-label {
  font-size: 12px;
  font-weight: 400;
  letter-spacing: 0.08em;
  color: var(--text-muted);
  text-transform: uppercase;
}
.action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}
.action-grid :deep(.el-button) {
  margin-left: 0;
}
.detail-action-btn {
  --el-button-font-weight: 400;
  --el-button-bg-color: var(--detail-action-neutral-bg);
  --el-button-border-color: var(--detail-action-neutral-border);
  --el-button-text-color: var(--detail-action-neutral-text);
  --el-button-hover-bg-color: rgba(var(--bg-card-rgb), 0.52);
  --el-button-hover-border-color: rgba(59, 130, 246, 0.28);
  --el-button-hover-text-color: var(--text-primary);
  width: 100%;
  height: 50px;
  border-radius: 16px;
  border: 1px solid var(--el-button-border-color) !important;
  background: var(--el-button-bg-color) !important;
  color: var(--el-button-text-color) !important;
  font-weight: 400;
  font-size: 15px;
  letter-spacing: 0.01em;
  justify-content: center;
  box-shadow: var(--detail-action-neutral-shadow);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  transition: background-color 0.28s ease, border-color 0.28s ease, color 0.28s ease, transform 0.28s ease, box-shadow 0.28s ease;
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(120deg, transparent 18%, rgba(255, 255, 255, 0.18) 50%, transparent 82%);
    transform: translateX(-135%);
    transition: transform 0.6s ease;
    pointer-events: none;
  }
  &::after {
    content: '';
    position: absolute;
    inset: 1px;
    border-radius: 15px;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.08), transparent 58%);
    pointer-events: none;
    opacity: 0.85;
  }
  :deep(.el-icon) {
    margin-right: 6px;
    font-size: 15px;
    transition: transform 0.28s ease;
    color: currentColor !important;
    position: relative;
    z-index: 1;
  }
  :deep(.el-icon svg) {
    color: currentColor !important;
    fill: currentColor;
  }
  :deep(span) {
    color: currentColor !important;
    position: relative;
    z-index: 1;
    font-weight: 400 !important;
  }
  &:hover {
    transform: translateY(-2px);
    color: var(--el-button-hover-text-color) !important;
    border-color: var(--el-button-hover-border-color) !important;
    background: var(--el-button-hover-bg-color) !important;
    box-shadow: 0 16px 34px rgba(0, 0, 0, 0.2);
    &::before {
      transform: translateX(135%);
    }
    :deep(.el-icon) {
      transform: translateX(1px) scale(1.04);
    }
  }
  &:active {
    transform: translateY(0);
  }
  &:focus-visible {
    outline: none;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.18), var(--detail-action-neutral-shadow);
  }
}
.detail-action-btn--back {
  --el-button-bg-color: linear-gradient(135deg, rgba(var(--bg-card-rgb), 0.2), rgba(var(--bg-card-rgb), 0.34));
  grid-column: 1 / -1;
}
.detail-action-btn--study {
  --el-button-bg-color: var(--detail-action-study-bg);
  --el-button-border-color: var(--detail-action-study-border);
  --el-button-text-color: var(--detail-action-study-text);
  --el-button-hover-bg-color: linear-gradient(135deg, rgba(59, 130, 246, 0.3), rgba(37, 99, 235, 0.24));
  --el-button-hover-border-color: rgba(96, 165, 250, 0.58);
  --el-button-hover-text-color: #ffffff;
  box-shadow: var(--detail-action-study-shadow);
  color: var(--detail-action-study-text) !important;
  :deep(.el-icon),
  :deep(span),
  :deep(.el-icon svg) {
    color: var(--detail-action-study-text) !important;
    fill: currentColor;
  }
  &:hover {
    box-shadow: var(--detail-action-primary-shadow);
  }
}
.detail-action-btn--favorite {
  --el-button-bg-color: var(--detail-action-favorite-bg);
  --el-button-border-color: var(--detail-action-favorite-border);
  --el-button-text-color: var(--detail-action-favorite-text);
  --el-button-hover-bg-color: linear-gradient(135deg, rgba(245, 158, 11, 0.2), rgba(251, 191, 36, 0.14));
  --el-button-hover-border-color: rgba(245, 158, 11, 0.42);
  --el-button-hover-text-color: #fff1ba;
  box-shadow: var(--detail-action-favorite-shadow);
  color: var(--detail-action-favorite-text) !important;
  :deep(.el-icon),
  :deep(span),
  :deep(.el-icon svg) {
    color: var(--detail-action-favorite-text) !important;
    fill: currentColor;
  }
  &:hover {
    box-shadow: 0 16px 36px rgba(245, 158, 11, 0.22);
  }
}
.detail-action-btn--favorite-active {
  --el-button-bg-color: var(--detail-action-favorite-active-bg);
  --el-button-border-color: var(--detail-action-favorite-active-border);
  --el-button-text-color: var(--detail-action-favorite-active-text);
  --el-button-hover-bg-color: linear-gradient(135deg, rgba(245, 158, 11, 0.28), rgba(251, 191, 36, 0.22));
  --el-button-hover-border-color: rgba(251, 191, 36, 0.54);
  --el-button-hover-text-color: #ffffff;
  box-shadow: var(--detail-action-favorite-active-shadow);
  color: var(--detail-action-favorite-active-text) !important;
  :deep(.el-icon),
  :deep(span),
  :deep(.el-icon svg) {
    color: var(--detail-action-favorite-active-text) !important;
    fill: currentColor;
  }
  &:hover {
    box-shadow: 0 18px 40px rgba(245, 158, 11, 0.28);
  }
}
:global(:root[data-theme="light"]) {
  .study-question-detail,
  .study-question-detail p,
  .study-question-detail span,
  .study-question-detail label,
  .study-question-detail .el-form-item__label {
    font-weight: 400;
  }
  .study-question-detail {
    --detail-action-neutral-bg: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(237, 245, 255, 0.98));
    --detail-action-neutral-border: rgba(59, 130, 246, 0.22);
    --detail-action-neutral-text: #0f172a;
    --detail-action-neutral-shadow: 0 14px 30px rgba(15, 23, 42, 0.1);
    --detail-action-study-bg: linear-gradient(135deg, rgba(96, 165, 250, 0.48), rgba(191, 219, 254, 0.88));
    --detail-action-study-border: rgba(37, 99, 235, 0.48);
    --detail-action-study-text: #1e40af;
    --detail-action-study-shadow: 0 16px 34px rgba(59, 130, 246, 0.18);
    --detail-action-favorite-bg: linear-gradient(135deg, rgba(254, 226, 226, 0.96), rgba(255, 241, 242, 0.98));
    --detail-action-favorite-border: rgba(251, 113, 133, 0.38);
    --detail-action-favorite-text: #be123c;
    --detail-action-favorite-shadow: 0 16px 34px rgba(244, 63, 94, 0.12);
    --detail-action-favorite-active-bg: linear-gradient(135deg, rgba(254, 205, 211, 0.98), rgba(255, 228, 230, 0.98));
    --detail-action-favorite-active-border: rgba(244, 63, 94, 0.44);
    --detail-action-favorite-active-text: #9f1239;
    --detail-action-favorite-active-shadow: 0 18px 38px rgba(244, 63, 94, 0.16);
    --detail-action-primary-shadow: 0 18px 38px rgba(59, 130, 246, 0.2);
  }
  .action-label {
    color: #64748b;
  }
  .detail-action-btn {
    --el-button-font-weight: 400;
    font-weight: 400;
    &:hover {
      background: linear-gradient(180deg, rgba(255, 255, 255, 0.99), rgba(239, 246, 255, 0.97));
      box-shadow: 0 16px 32px rgba(15, 23, 42, 0.12);
    }
  }
  .detail-action-btn--back {
    --el-button-bg-color: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(241, 245, 249, 1));
    --el-button-text-color: #475569;
    --el-button-hover-text-color: #0f172a;
    color: #475569;
  }
  .detail-action-btn--study {
    --el-button-bg-color: linear-gradient(135deg, rgba(147, 197, 253, 0.34), rgba(239, 246, 255, 0.98));
    --el-button-border-color: rgba(96, 165, 250, 0.42);
    --el-button-text-color: #1e40af;
    --el-button-hover-bg-color: linear-gradient(135deg, rgba(147, 197, 253, 0.46), rgba(219, 234, 254, 0.98));
    --el-button-hover-text-color: #1d4ed8;
    color: #1e40af !important;
  }
  .detail-action-btn--study:hover {
    color: #1d4ed8 !important;
  }
  .detail-action-btn--favorite,
  .detail-action-btn--favorite-active {
    --el-button-bg-color: linear-gradient(135deg, rgba(254, 226, 226, 0.96), rgba(255, 241, 242, 0.98));
    --el-button-border-color: rgba(251, 113, 133, 0.38);
    --el-button-text-color: #be123c;
    --el-button-hover-bg-color: linear-gradient(135deg, rgba(254, 205, 211, 0.98), rgba(255, 228, 230, 0.98));
    --el-button-hover-text-color: #9f1239;
    color: #be123c !important;
  }
  .detail-action-btn--favorite:hover,
  .detail-action-btn--favorite-active:hover {
    color: #9f1239 !important;
  }
  .meta-chip,
  .block-header p,
  .switch-caption,
  .note-type,
  .note-time {
    color: #64748b;
  }
  .rich-text,
  .progress-row,
  .mini-stat,
  .study-question-detail :deep(.el-form-item__label) {
    color: #475569;
  }
  .progress-row strong,
  .mini-stat strong,
  .side-title,
  h1,
  .block-header h2 {
    color: #0f172a;
  }
  .study-question-detail :deep(.el-form-item__label) {
    font-weight: 400 !important;
  }
  .study-question-detail :deep(.el-tag) {
    color: #2563eb;
    border-color: rgba(96, 165, 250, 0.28);
    background: rgba(219, 234, 254, 0.88);
  }
}

:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite),
:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite-active) {
  background: linear-gradient(135deg, rgba(254, 226, 226, 0.96), rgba(255, 241, 242, 0.98)) !important;
  border-color: rgba(251, 113, 133, 0.38) !important;
  color: #be123c !important;
}

:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite:hover),
:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite-active:hover) {
  background: linear-gradient(135deg, rgba(254, 205, 211, 0.98), rgba(255, 228, 230, 0.98)) !important;
  border-color: rgba(244, 63, 94, 0.44) !important;
  color: #9f1239 !important;
}

:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite span),
:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite-active span),
:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite .el-icon),
:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite-active .el-icon),
:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite .el-icon svg),
:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite-active .el-icon svg) {
  color: #be123c !important;
  fill: currentColor !important;
}

:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite:hover span),
:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite-active:hover span),
:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite:hover .el-icon),
:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite-active:hover .el-icon),
:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite:hover .el-icon svg),
:global(:root[data-theme="light"] .study-question-detail .detail-action-btn--favorite-active:hover .el-icon svg) {
  color: #9f1239 !important;
  fill: currentColor !important;
}
.category-chip, .meta-chip { display: inline-flex; align-items: center; padding: 5px 12px; border-radius: 999px; font-size: 12px; font-weight: 400; }
.category-chip { background: rgba($primary-color, 0.12); color: $primary-color; }
.meta-chip { background: var(--bg-card-hover); color: var(--text-secondary); }
h1 { color: var(--text-primary); font-size: 28px; line-height: 1.4; margin: 14px 0 0; font-weight: 600; }
.content-block + .content-block { margin-top: 24px; }
.block-header {
  display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 14px;
  h2 { color: var(--text-primary); font-size: 18px; font-weight: 600; }
  p { color: var(--text-muted); font-size: 13px; }
}
.block-heading { display: grid; gap: 6px; }
.answer-switch-wrap {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid var(--border-color);
  background: var(--bg-card-hover);
  flex-shrink: 0;
}
.switch-caption {
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1;
}
.rich-text { padding: 18px; border-radius: 16px; background: var(--bg-card-hover); color: var(--text-secondary); line-height: 1.9; font-size: 14px; }
.answer-panel { border: 1px solid rgba($primary-color, 0.18); }
.note-editor, .note-submit { display: flex; gap: 12px; margin-bottom: 14px; align-items: center; }
.note-submit { margin-top: 14px; justify-content: flex-end; }
.note-list { display: grid; gap: 12px; margin-top: 16px; }
.note-card {
  padding: 16px; border: 1px solid var(--border-color); border-radius: 14px; background: var(--bg-card-hover);
  p { color: var(--text-secondary); line-height: 1.8; }
}
.note-head, .progress-row, .mini-stat { display: flex; align-items: center; justify-content: space-between; }
.note-head { margin-bottom: 10px; }
.note-type, .note-time { font-size: 12px; color: var(--text-muted); }
.detail-side { display: flex; flex-direction: column; gap: 16px; }
.side-title { color: var(--text-primary); font-size: 16px; font-weight: 500; margin-bottom: 14px; }
.progress-row {
  padding: 10px 0; color: var(--text-secondary); border-bottom: 1px solid var(--border-color); font-size: 13px;
  &:last-child { border-bottom: none; }
}
.full-btn {
  width: 100%;
  height: 46px;
  border-radius: 16px;
  font-weight: 500;
  letter-spacing: 0.02em;
  border: none;
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  box-shadow: 0 16px 36px rgba(59, 130, 246, 0.24);
  transition: transform 0.28s ease, box-shadow 0.28s ease, filter 0.28s ease;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 18px 40px rgba(59, 130, 246, 0.3);
    filter: saturate(1.06);
  }
  &:active {
    transform: translateY(0);
  }
}
:global(:root[data-theme="light"]) {
  .full-btn {
    box-shadow: 0 16px 34px rgba(59, 130, 246, 0.18);
  }
}
.stat-side { display: grid; gap: 12px; }
.mini-stat {
  padding: 14px 16px; border-radius: 14px; background: rgba($primary-color, 0.06); color: var(--text-secondary);
  strong { color: var(--text-primary); font-size: 18px; }
}
:deep(.el-loading-mask) {
  border-radius: 20px;
}
@media (max-width: 1100px) {
  .detail-grid { grid-template-columns: 1fr; }
  .title-row { grid-template-columns: 1fr; }
  
  .title-actions {
    .action-grid {
      grid-template-columns: repeat(3, 1fr);
    }
    .detail-action-btn--back {
      grid-column: 1 / 2;
    }
  }
}
@media (max-width: 768px) {
  .detail-main, .side-card { padding: 16px; }
  .block-header { 
    flex-direction: column; 
    align-items: flex-start;
    gap: 12px;
  }
  h1 { font-size: 22px; margin-top: 12px; }
  
  .title-row {
    margin-bottom: 20px;
    gap: 16px;
  }
  
  .title-actions {
    .action-grid {
      grid-template-columns: 1fr;
      gap: 12px;
    }
    .detail-action-btn--back {
      grid-column: auto;
    }
  }
  
  .detail-action-btn {
    height: 46px;
    font-size: 15px;
    border-radius: 12px;
  }
  
  .answer-switch-wrap { 
    width: 100%; 
    justify-content: space-between;
    padding: 10px 16px;
    border-radius: 12px;
  }
  
  .note-editor {
    flex-direction: column;
    align-items: stretch;
    width: 100%;
    gap: 12px;
    
    .el-select {
      width: 100% !important;
    }
  }
  
  .note-submit {
    .el-button {
      width: 100%;
      height: 40px;
    }
  }
  
  .rich-text {
    padding: 16px;
    font-size: 14px;
    border-radius: 12px;
  }
  
  .side-card {
    border-radius: 16px;
  }
  
  .mini-stat {
    padding: 12px 16px;
  }
}
</style>
