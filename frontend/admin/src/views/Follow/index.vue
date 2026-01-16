<template>
  <div class="follow-page">
    <div class="page-header">
      <h2>关注管理</h2>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon total"><el-icon><Connection /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalFollows || 0 }}</span>
          <span class="stat-label">总关注数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon today"><el-icon><Calendar /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.todayFollows || 0 }}</span>
          <span class="stat-label">今日新增</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon active"><el-icon><UserFilled /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.activeUsers || 0 }}</span>
          <span class="stat-label">活跃用户</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon mutual"><el-icon><Switch /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.mutualFollows || 0 }}</span>
          <span class="stat-label">互相关注</span>
        </div>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="follow-tabs">
      <!-- 关注关系列表 -->
      <el-tab-pane label="关注关系" name="relations">
        <div class="filter-card">
          <el-select v-model="query.userId" placeholder="关注者" clearable filterable style="width: 180px">
            <el-option v-for="user in userOptions" :key="user.id" :label="user.nickname" :value="user.id" />
          </el-select>
          <el-select v-model="query.followUserId" placeholder="被关注者" clearable filterable style="width: 180px">
            <el-option v-for="user in userOptions" :key="user.id" :label="user.nickname" :value="user.id" />
          </el-select>
          <el-button type="primary" @click="fetchList"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>

        <div class="table-card">
          <el-table :data="list" v-loading="loading" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" />
            <el-table-column label="关注者" min-width="180">
              <template #default="{ row }">
                <div class="user-cell" v-if="row.user">
                  <el-avatar :size="36" :src="row.user.avatar">{{ row.user.nickname?.charAt(0) }}</el-avatar>
                  <div class="user-info">
                    <span class="nickname">{{ row.user.nickname }}</span>
                    <span class="username">@{{ row.user.username }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column width="80" align="center">
              <template #default>
                <el-icon class="arrow-icon"><Right /></el-icon>
              </template>
            </el-table-column>
            <el-table-column label="被关注者" min-width="180">
              <template #default="{ row }">
                <div class="user-cell" v-if="row.followUser">
                  <el-avatar :size="36" :src="row.followUser.avatar">{{ row.followUser.nickname?.charAt(0) }}</el-avatar>
                  <div class="user-info">
                    <span class="nickname">{{ row.followUser.nickname }}</span>
                    <span class="username">@{{ row.followUser.username }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="关注时间" width="180">
              <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" text size="small" @click="viewUserDetail(row)">详情</el-button>
                <el-popconfirm title="确定删除该关注关系吗？" @confirm="handleDelete(row.id)">
                  <template #reference>
                    <el-button type="danger" text size="small">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <div class="table-footer">
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
              批量删除 ({{ selectedIds.length }})
            </el-button>
            <el-pagination
              v-model:current-page="query.page"
              v-model:page-size="query.pageSize"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @current-change="fetchList"
              @size-change="fetchList"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 用户关注统计 -->
      <el-tab-pane label="用户统计" name="userStats">
        <div class="filter-card">
          <el-select v-model="statsQuery.userId" placeholder="选择用户查看详情" clearable filterable style="width: 240px" @change="fetchUserStats">
            <el-option v-for="user in userOptions" :key="user.id" :label="user.nickname" :value="user.id" />
          </el-select>
        </div>

        <div v-if="userStats.userId" class="user-stats-detail">
          <div class="stats-summary">
            <div class="summary-card">
              <span class="summary-value">{{ userStats.followingCount || 0 }}</span>
              <span class="summary-label">关注</span>
            </div>
            <div class="summary-card">
              <span class="summary-value">{{ userStats.followerCount || 0 }}</span>
              <span class="summary-label">粉丝</span>
            </div>
          </div>

          <el-tabs v-model="userStatsTab" class="inner-tabs">
            <el-tab-pane label="关注列表" name="following">
              <el-table :data="userFollowing" v-loading="userStatsLoading" max-height="400">
                <el-table-column label="用户" min-width="200">
                  <template #default="{ row }">
                    <div class="user-cell">
                      <el-avatar :size="32" :src="row.avatar">{{ row.nickname?.charAt(0) }}</el-avatar>
                      <span>{{ row.nickname }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="关注时间" width="180">
                  <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="粉丝列表" name="followers">
              <el-table :data="userFollowers" v-loading="userStatsLoading" max-height="400">
                <el-table-column label="用户" min-width="200">
                  <template #default="{ row }">
                    <div class="user-cell">
                      <el-avatar :size="32" :src="row.avatar">{{ row.nickname?.charAt(0) }}</el-avatar>
                      <span>{{ row.nickname }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="关注时间" width="180">
                  <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>

        <el-empty v-else description="请选择用户查看关注统计" />
      </el-tab-pane>

      <!-- 粉丝排行 -->
      <el-tab-pane label="粉丝排行" name="ranking">
        <div class="ranking-list">
          <div v-for="(item, index) in topUsers" :key="item.userId" class="ranking-item">
            <div class="rank-badge" :class="{ gold: index === 0, silver: index === 1, bronze: index === 2 }">
              {{ index + 1 }}
            </div>
            <div class="user-cell">
              <el-avatar :size="40" :src="item.avatar">{{ item.nickname?.charAt(0) }}</el-avatar>
              <div class="user-info">
                <span class="nickname">{{ item.nickname }}</span>
                <span class="username">@{{ item.username }}</span>
              </div>
            </div>
            <div class="fans-count">
              <span class="count">{{ item.fansCount }}</span>
              <span class="label">粉丝</span>
            </div>
          </div>
          <el-empty v-if="!topUsers.length" description="暂无数据" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="关注详情" width="500px">
      <div class="detail-content" v-if="currentDetail">
        <div class="detail-section">
          <h4>关注者</h4>
          <div class="user-detail-card">
            <el-avatar :size="48" :src="currentDetail.user?.avatar">{{ currentDetail.user?.nickname?.charAt(0) }}</el-avatar>
            <div class="info">
              <p class="name">{{ currentDetail.user?.nickname }}</p>
              <p class="username">@{{ currentDetail.user?.username }}</p>
            </div>
          </div>
        </div>
        <div class="detail-arrow">
          <el-icon><Bottom /></el-icon>
          <span>关注了</span>
        </div>
        <div class="detail-section">
          <h4>被关注者</h4>
          <div class="user-detail-card">
            <el-avatar :size="48" :src="currentDetail.followUser?.avatar">{{ currentDetail.followUser?.nickname?.charAt(0) }}</el-avatar>
            <div class="info">
              <p class="name">{{ currentDetail.followUser?.nickname }}</p>
              <p class="username">@{{ currentDetail.followUser?.username }}</p>
            </div>
          </div>
        </div>
        <div class="detail-time">
          <el-icon><Clock /></el-icon>
          <span>关注时间：{{ formatDateTime(currentDetail.createTime) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Connection, Calendar, UserFilled, Switch, Search, Right, Bottom, Clock } from '@element-plus/icons-vue'
import { getFollowList, getFollowStats, getUserFollowStats, getUserFollowing, getUserFollowers, deleteFollow, batchDeleteFollows } from '@/api/follow'
import { getUserList } from '@/api/user'
import { formatDateTime } from '@/utils/format'

const activeTab = ref('relations')
const userStatsTab = ref('following')
const loading = ref(false)
const userStatsLoading = ref(false)
const list = ref([])
const total = ref(0)
const stats = ref({})
const userOptions = ref([])
const selectedIds = ref([])
const detailDialogVisible = ref(false)
const currentDetail = ref(null)

// 用户统计相关
const userStats = ref({})
const userFollowing = ref([])
const userFollowers = ref([])
const topUsers = ref([])

const query = ref({ page: 1, pageSize: 20, userId: null, followUserId: null })
const statsQuery = ref({ userId: null })

async function fetchStats() {
  try {
    const res = await getFollowStats()
    stats.value = res.data || {}
    topUsers.value = res.data?.topUsers || []
  } catch (e) { console.error(e) }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getFollowList(query.value)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchUsers() {
  try {
    const res = await getUserList({ page: 1, pageSize: 1000 })
    userOptions.value = res.data?.list || []
  } catch (e) { console.error(e) }
}

async function fetchUserStats() {
  if (!statsQuery.value.userId) {
    userStats.value = {}
    userFollowing.value = []
    userFollowers.value = []
    return
  }
  
  userStatsLoading.value = true
  try {
    const [statsRes, followingRes, followersRes] = await Promise.all([
      getUserFollowStats(statsQuery.value.userId),
      getUserFollowing(statsQuery.value.userId, { page: 1, pageSize: 100 }),
      getUserFollowers(statsQuery.value.userId, { page: 1, pageSize: 100 })
    ])
    userStats.value = { userId: statsQuery.value.userId, ...statsRes.data }
    userFollowing.value = followingRes.data?.list || []
    userFollowers.value = followersRes.data?.list || []
  } finally {
    userStatsLoading.value = false
  }
}

function resetQuery() {
  query.value = { page: 1, pageSize: 20, userId: null, followUserId: null }
  fetchList()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function viewUserDetail(row) {
  currentDetail.value = row
  detailDialogVisible.value = true
}

async function handleDelete(id) {
  await deleteFollow(id)
  ElMessage.success('删除成功')
  fetchList()
  fetchStats()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条关注关系吗？`, '提示', { type: 'warning' })
  await batchDeleteFollows(selectedIds.value)
  ElMessage.success('删除成功')
  selectedIds.value = []
  fetchList()
  fetchStats()
}

onMounted(() => {
  fetchStats()
  fetchList()
  fetchUsers()
})
</script>


<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.follow-page { padding: 20px; }

.page-header {
  margin-bottom: $spacing-lg;
  h2 { font-size: 20px; font-weight: 600; color: var(--text-primary); margin: 0; }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-lg;
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-md;
  font-size: 24px;
  &.total { background: rgba($primary-color, 0.1); color: $primary-color; }
  &.today { background: rgba($success-color, 0.1); color: $success-color; }
  &.active { background: rgba($warning-color, 0.1); color: $warning-color; }
  &.mutual { background: rgba(#8b5cf6, 0.1); color: #8b5cf6; }
}

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 24px; font-weight: 700; color: var(--text-primary); }
.stat-label { font-size: 13px; color: var(--text-muted); }

.follow-tabs {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-lg;
}

.filter-card {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.table-card { background: transparent; }

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  .user-info {
    display: flex;
    flex-direction: column;
    .nickname { font-weight: 500; color: var(--text-primary); }
    .username { font-size: 12px; color: var(--text-muted); }
  }
}

.arrow-icon { font-size: 20px; color: var(--text-muted); }

.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: $spacing-lg;
}

.user-stats-detail {
  .stats-summary {
    display: flex;
    gap: $spacing-lg;
    margin-bottom: $spacing-lg;
  }
  .summary-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: $spacing-lg;
    background: var(--bg-darker);
    border-radius: $radius-md;
    .summary-value { font-size: 32px; font-weight: 700; color: $primary-color; }
    .summary-label { font-size: 14px; color: var(--text-muted); }
  }
}

.inner-tabs {
  :deep(.el-tabs__header) { margin-bottom: $spacing-md; }
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md $spacing-lg;
  background: var(--bg-darker);
  border-radius: $radius-md;
  
  .rank-badge {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    font-weight: 700;
    font-size: 14px;
    background: var(--bg-card);
    color: var(--text-secondary);
    &.gold { background: linear-gradient(135deg, #ffd700, #ffb700); color: #fff; }
    &.silver { background: linear-gradient(135deg, #c0c0c0, #a0a0a0); color: #fff; }
    &.bronze { background: linear-gradient(135deg, #cd7f32, #b87333); color: #fff; }
  }
  
  .user-cell { flex: 1; }
  
  .fans-count {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    .count { font-size: 20px; font-weight: 700; color: $primary-color; }
    .label { font-size: 12px; color: var(--text-muted); }
  }
}

.detail-content {
  .detail-section {
    h4 { margin: 0 0 $spacing-sm; color: var(--text-secondary); font-size: 13px; }
  }
  .user-detail-card {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    padding: $spacing-md;
    background: var(--bg-darker);
    border-radius: $radius-md;
    .info {
      .name { font-weight: 600; color: var(--text-primary); margin: 0; }
      .username { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }
    }
  }
  .detail-arrow {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: $spacing-md 0;
    color: var(--text-muted);
    font-size: 13px;
  }
  .detail-time {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: $spacing-lg;
    padding-top: $spacing-md;
    border-top: 1px solid var(--border-color);
    color: var(--text-muted);
    font-size: 13px;
  }
}

:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: var(--bg-darker);
  --el-table-row-hover-bg-color: var(--bg-hover);
  --el-table-border-color: var(--border-color);
}

:deep(.el-tabs__item) {
  color: var(--text-secondary);
  &.is-active { color: var(--primary-color); }
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  background: var(--bg-darker);
  box-shadow: none;
  border: 1px solid var(--border-color);
}

:deep(.el-dialog) {
  --el-dialog-bg-color: var(--bg-card);
  border-radius: $radius-lg;
}

@media (max-width: 1200px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .stats-row { grid-template-columns: 1fr; }
}
</style>
