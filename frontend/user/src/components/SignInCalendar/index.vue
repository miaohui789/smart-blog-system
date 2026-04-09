<template>
  <div class="sign-in-calendar">
    <el-tooltip
      v-if="showTrigger"
      effect="dark"
      placement="bottom"
      :content="calendarData.todaySigned ? `今日已签到 · 连签 ${calendarData.continuousDays} 天` : '每日签到'"
    >
      <button
        class="sign-in-trigger"
        :class="{ 'is-signed': calendarData.todaySigned }"
        type="button"
        @click="openDialog"
      >
        <el-icon><Calendar /></el-icon>
        <span v-if="calendarData.todaySigned" class="sign-in-badge">✓</span>
      </button>
    </el-tooltip>

    <el-dialog
      v-model="visible"
      width="860px"
      align-center
      append-to-body
      class="sign-calendar-dialog"
    >
      <template #header>
        <div class="dialog-header">
          <div class="dialog-title-group">
            <h3>每日签到</h3>
            <p>点击今天的日期即可完成签到并领取经验奖励</p>
          </div>
          <div class="dialog-status" :class="{ 'is-signed': calendarData.todaySigned }">
            {{ calendarData.todaySigned ? '今日已签' : '今日待签' }}
          </div>
        </div>
      </template>

      <div v-loading="loading" class="sign-calendar-panel">
        <div class="sign-calendar-aside">
          <div class="sign-overview">
            <div class="overview-card primary">
              <span class="overview-label">连续签到</span>
              <strong class="overview-value">{{ calendarData.continuousDays }}</strong>
              <span class="overview-extra">正在积累连签奖励</span>
            </div>
            <div class="overview-card">
              <span class="overview-label">本月已签</span>
              <strong class="overview-value">{{ calendarData.monthSignedCount }}</strong>
              <span class="overview-extra">记录当前月签到天数</span>
            </div>
            <div class="overview-card">
              <span class="overview-label">基础奖励</span>
              <strong class="overview-value">50</strong>
              <span class="overview-extra">每日签到固定经验</span>
            </div>
          </div>

          <div class="reward-panel">
            <div class="reward-panel-title">连续签到奖励</div>
            <div class="reward-rules">
              <div
                v-for="rule in rewardRules"
                :key="rule.days"
                class="reward-chip"
                :class="{ active: calendarData.continuousDays >= rule.days }"
              >
                连签 {{ rule.days }} 天 +{{ rule.exp }}
              </div>
            </div>
          </div>

          <div class="action-panel">
            <div class="footer-tip">
              点击右侧今天的日期即可完成签到并获得经验
            </div>
            <el-button
              type="primary"
              :disabled="calendarData.todaySigned || signing || !isViewingCurrentMonth"
              :loading="signing"
              @click="signByButton"
              round
            >
              {{ calendarData.todaySigned ? '今日已签到' : '立即签到' }}
            </el-button>
          </div>
        </div>

        <div class="sign-calendar-main">
          <div class="calendar-toolbar">
            <button class="toolbar-btn" type="button" @click="changeMonth(-1)">
              <el-icon><ArrowLeftBold /></el-icon>
            </button>
            <div class="toolbar-title">{{ monthLabel }}</div>
            <button class="toolbar-btn" type="button" @click="changeMonth(1)">
              <el-icon><ArrowRightBold /></el-icon>
            </button>
          </div>

          <div class="calendar-weekdays">
            <span v-for="week in weekLabels" :key="week">{{ week }}</span>
          </div>

          <div class="calendar-grid">
            <button
              v-for="day in calendarDays"
              :key="day.date"
              class="calendar-cell"
              :class="{
                'is-other-month': !day.currentMonth,
                'is-today': day.today,
                'is-signed': day.signed,
                'is-future': day.future,
                'can-sign': day.canSign
              }"
              type="button"
              :disabled="!day.canSign"
              @click="handleDateClick(day)"
            >
              <span class="cell-day">{{ day.dayNumber }}</span>
              <span v-if="day.signed" class="cell-tag">已签</span>
              <span v-else-if="day.canSign" class="cell-tag accent">签到</span>
              <span v-else-if="day.today && !day.signed" class="cell-tag subtle">今天</span>
            </button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ArrowLeftBold, ArrowRightBold, Calendar } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSignInCalendar, signInToday } from '@/api/exp'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  showTrigger: {
    type: Boolean,
    default: true
  }
})

const weekLabels = ['一', '二', '三', '四', '五', '六', '日']
const rewardRules = [
  { days: 3, exp: 20 },
  { days: 7, exp: 50 },
  { days: 15, exp: 100 },
  { days: 30, exp: 200 }
]

const userStore = useUserStore()
const visible = ref(false)
const loading = ref(false)
const signing = ref(false)
const displayMonth = ref(dayjs().startOf('month'))
const calendarData = ref(createDefaultCalendar())

const signedDateSet = computed(() => new Set(calendarData.value.signedDates || []))
const todayDate = computed(() => calendarData.value.today || dayjs().format('YYYY-MM-DD'))
const monthLabel = computed(() => displayMonth.value.format('YYYY 年 M 月'))
const isViewingCurrentMonth = computed(() => displayMonth.value.isSame(dayjs(todayDate.value), 'month'))

const calendarDays = computed(() => {
  const monthStart = displayMonth.value.startOf('month')
  const leadingDays = (monthStart.day() + 6) % 7
  const daysInMonth = monthStart.daysInMonth()
  const totalCells = Math.ceil((leadingDays + daysInMonth) / 7) * 7

  return Array.from({ length: totalCells }, (_, index) => {
    const currentDate = monthStart.add(index - leadingDays, 'day')
    const dateText = currentDate.format('YYYY-MM-DD')
    const currentMonth = currentDate.isSame(displayMonth.value, 'month')
    const today = dateText === todayDate.value
    const signed = signedDateSet.value.has(dateText)
    const future = currentDate.isAfter(dayjs(todayDate.value), 'day')
    const canSign = currentMonth && today && !calendarData.value.todaySigned

    return {
      date: dateText,
      dayNumber: currentDate.date(),
      currentMonth,
      today,
      signed,
      future,
      canSign
    }
  })
})

function createDefaultCalendar() {
  return {
    year: dayjs().year(),
    month: dayjs().month() + 1,
    today: dayjs().format('YYYY-MM-DD'),
    todaySigned: false,
    continuousDays: 0,
    monthSignedCount: 0,
    signedDates: []
  }
}

async function loadCalendar(targetMonth = displayMonth.value, silent = false) {
  if (!userStore.isLoggedIn) return
  const month = dayjs(targetMonth).startOf('month')
  loading.value = true
  try {
    const res = await getSignInCalendar({
      year: month.year(),
      month: month.month() + 1
    })
    calendarData.value = {
      ...createDefaultCalendar(),
      ...res.data
    }
    displayMonth.value = dayjs(`${calendarData.value.year}-${calendarData.value.month}-01`)
  } catch (error) {
    if (!silent) {
      ElMessage.error(error.response?.data?.message || '获取签到日历失败')
    }
  } finally {
    loading.value = false
  }
}

async function openDialog() {
  visible.value = true
  await loadCalendar(dayjs(todayDate.value).startOf('month'))
}

async function changeMonth(step) {
  const nextMonth = displayMonth.value.add(step, 'month').startOf('month')
  await loadCalendar(nextMonth)
}

async function handleDateClick(day) {
  if (!day.canSign) return
  await performSignIn()
}

async function signByButton() {
  if (calendarData.value.todaySigned || !isViewingCurrentMonth.value) return
  await performSignIn()
}

async function performSignIn() {
  if (signing.value) return
  signing.value = true
  try {
    const res = await signInToday()
    await Promise.all([
      loadCalendar(displayMonth.value, true),
      userStore.fetchUserInfo({ noAuthRedirect: true })
    ])
    const message = res.data.streakBonusExp > 0
      ? `恭喜你签到成功，获得了${res.data.baseExp}经验，连续签到${res.data.continuousDays}天额外奖励${res.data.streakBonusExp}经验，共获得${res.data.totalExp}经验。`
      : `恭喜你签到成功，获得了${res.data.baseExp}经验。`
    await ElMessageBox.alert(message, '签到成功', {
      confirmButtonText: '知道了'
    })
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '签到失败')
  } finally {
    signing.value = false
  }
}

function resetCalendar() {
  displayMonth.value = dayjs().startOf('month')
  calendarData.value = createDefaultCalendar()
}

watch(
  () => userStore.isLoggedIn,
  async loggedIn => {
    if (loggedIn) {
      await loadCalendar(dayjs().startOf('month'), true)
      return
    }
    resetCalendar()
  },
  { immediate: true }
)

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await loadCalendar(dayjs().startOf('month'), true)
  }
})

defineExpose({
  openDialog
})
</script>

<style scoped lang="scss">
.sign-in-calendar {
  display: flex;
}

.sign-in-trigger {
  position: relative;
  width: 36px;
  height: 36px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: rgba(var(--bg-card-rgb), 0.7);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s ease;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.12);

  .el-icon {
    font-size: 17px;
  }

  &:hover {
    color: var(--primary-color);
    border-color: rgba(59, 130, 246, 0.35);
    transform: translateY(-1px);
    box-shadow: 0 12px 28px rgba(59, 130, 246, 0.18);
  }

  &.is-signed {
    color: #f59e0b;
    border-color: rgba(245, 158, 11, 0.35);
    background: linear-gradient(135deg, rgba(245, 158, 11, 0.12), rgba(59, 130, 246, 0.12));
  }
}

.sign-in-badge {
  position: absolute;
  right: -3px;
  top: -4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 999px;
  background: linear-gradient(135deg, #f59e0b, #f97316);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(245, 158, 11, 0.3);
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.dialog-title-group {
  h3 {
    font-size: 20px;
    color: var(--text-primary);
    line-height: 1.2;
  }

  p {
    margin-top: 4px;
    color: var(--text-muted);
    font-size: 12px;
  }
}

.dialog-status {
  flex-shrink: 0;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.12);
  color: var(--primary-color);
  font-size: 13px;
  font-weight: 600;
  border: 1px solid rgba(59, 130, 246, 0.2);

  &.is-signed {
    background: rgba(245, 158, 11, 0.12);
    color: #f59e0b;
    border-color: rgba(245, 158, 11, 0.24);
  }
}

.sign-calendar-panel {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.sign-calendar-aside {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sign-calendar-main {
  padding: 14px 16px;
  border-radius: 22px;
  border: 1px solid rgba(59, 130, 246, 0.12);
  background: linear-gradient(180deg, rgba(var(--bg-card-rgb), 0.96), rgba(var(--bg-card-rgb), 0.82));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04), 0 16px 40px rgba(15, 23, 42, 0.08);
}

.sign-overview {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.overview-card {
  position: relative;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid var(--border-color);
  background: linear-gradient(180deg, rgba(var(--bg-card-rgb), 0.98), rgba(var(--bg-card-rgb), 0.8));
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);

  &.primary {
    border-color: rgba(59, 130, 246, 0.26);
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.18), rgba(168, 85, 247, 0.12));
  }
}

.overview-label {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
}

.overview-value {
  display: block;
  margin-top: 6px;
  font-size: 24px;
  color: var(--text-primary);
  line-height: 1;
}

.overview-extra {
  display: block;
  margin-top: 6px;
  font-size: 11px;
  color: var(--text-secondary);
}


.reward-panel,
.action-panel {
  padding: 14px;
  border-radius: 18px;
  border: 1px solid var(--border-color);
  background: linear-gradient(180deg, rgba(var(--bg-card-rgb), 0.96), rgba(var(--bg-card-rgb), 0.84));
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.reward-panel-title {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-primary);
}
.reward-rules {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}


.reward-chip {
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid var(--border-color);
  background: var(--bg-card-hover);
  color: var(--text-secondary);
  font-size: 11px;
  transition: all 0.2s ease;

  &.active {
    color: #f59e0b;
    border-color: rgba(245, 158, 11, 0.28);
    background: rgba(245, 158, 11, 0.08);
  }
}

.calendar-toolbar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 12px;
}

.toolbar-btn {
  width: 36px;
  height: 36px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: var(--bg-card-hover);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    color: var(--primary-color);
    border-color: rgba(59, 130, 246, 0.28);
  }
}

.toolbar-title {
  min-width: 140px;
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.calendar-weekdays,
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 8px;
}

.calendar-weekdays span {
  text-align: center;
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 600;
  padding: 4px 0;
}

.calendar-cell {
  min-height: 72px;
  padding: 8px;
  border: 1px solid var(--border-color);
  border-radius: 14px;
  background: rgba(var(--bg-card-rgb), 0.72);
  color: var(--text-primary);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: space-between;
  text-align: left;
  transition: all 0.25s ease;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);

  &:disabled {
    cursor: default;
  }

  &.is-other-month {
    opacity: 0.45;
  }

  &.is-today {
    border-color: rgba(59, 130, 246, 0.35);
    box-shadow: 0 12px 32px rgba(59, 130, 246, 0.12);
  }

  &.is-signed {
    background: linear-gradient(135deg, rgba(245, 158, 11, 0.12), rgba(59, 130, 246, 0.1));
    border-color: rgba(245, 158, 11, 0.3);
  }

  &.can-sign {
    cursor: pointer;
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.18), rgba(168, 85, 247, 0.12));
    border-color: rgba(59, 130, 246, 0.32);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 16px 36px rgba(59, 130, 246, 0.18);
    }
  }
}

.cell-day {
  font-size: 16px;
  font-weight: 700;
  line-height: 1;
}

.cell-tag {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(245, 158, 11, 0.14);
  color: #f59e0b;
  font-size: 10px;
  font-weight: 600;
  transform: scale(0.9);
  transform-origin: left bottom;

  &.accent {
    background: rgba(59, 130, 246, 0.14);
    color: var(--primary-color);
  }

  &.subtle {
    background: var(--bg-card-hover);
    color: var(--text-muted);
  }
}

.calendar-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-top: 12px;
}

.footer-tip {
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.6;
}

.action-panel :deep(.el-button) {
  width: 100%;
  height: 40px;
  margin-top: 12px;
  font-weight: 600;
  box-shadow: 0 12px 28px rgba(59, 130, 246, 0.2);
}

:deep(.sign-calendar-dialog .el-dialog) {
  border: 1px solid var(--border-color);
  border-radius: 24px;
  background: rgba(var(--bg-card-rgb), 0.96);
  backdrop-filter: blur(22px);
  -webkit-backdrop-filter: blur(22px);
  box-shadow: 0 28px 80px rgba(15, 23, 42, 0.28);
}

:deep(.sign-calendar-dialog .el-dialog__header) {
  padding: 20px 24px 0;
}

:deep(.sign-calendar-dialog .el-dialog__body) {
  padding: 16px 24px 20px;
}

:deep(.sign-calendar-dialog .el-dialog__headerbtn) {
  top: 18px;
  right: 18px;
}

@media (max-width: 768px) {
  .sign-calendar-panel {
    grid-template-columns: 1fr;
  }

  .sign-calendar-main {
    padding: 14px 12px;
    border-radius: 18px;
  }

  .sign-in-trigger {
    width: 34px;
    height: 34px;
  }

  .sign-overview {
    grid-template-columns: 1fr;
  }

  .calendar-weekdays,
  .calendar-grid {
    gap: 6px;
  }

  .calendar-cell {
    min-height: 60px;
    padding: 6px;
    border-radius: 12px;
  }

  .footer-tip {
    text-align: center;
  }

  :deep(.sign-calendar-dialog .el-dialog) {
    width: calc(100vw - 24px) !important;
    margin: 0 12px;
  }

  :deep(.sign-calendar-dialog .el-dialog__header) {
    padding: 20px 18px 0;
  }

  :deep(.sign-calendar-dialog .el-dialog__body) {
    padding: 18px;
  }

  .dialog-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
