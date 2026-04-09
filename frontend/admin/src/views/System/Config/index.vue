<template>
  <div class="config-page">
    <h2 class="page-title">系统配置</h2>

    <!-- 无权限提示 -->
    <el-alert
      v-if="!canManageConfig"
      title="您当前角色没有系统配置权限，仅可查看配置信息"
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

    <el-tabs v-model="activeTab">
      <el-tab-pane label="网站设置" name="site">
        <el-form :model="siteConfig" label-width="120px" class="config-form">
          <el-form-item label="网站名称">
            <el-input v-model="siteConfig.siteName" :disabled="!canManageConfig" />
          </el-form-item>
          <el-form-item label="网站描述">
            <el-input v-model="siteConfig.siteDescription" type="textarea" :rows="3" :disabled="!canManageConfig" />
          </el-form-item>
          <el-form-item label="网站Logo">
            <ImageUpload v-model="siteConfig.siteLogo" :disabled="!canManageConfig" />
          </el-form-item>
          <el-form-item label="备案号">
            <el-input v-model="siteConfig.icp" :disabled="!canManageConfig" />
          </el-form-item>
          <el-form-item v-if="canManageConfig">
            <el-button type="primary" @click="handleSave">保存配置</el-button>
            <el-button @click="handleResetSite">恢复默认</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="其他设置" name="other">
        <el-form :model="otherConfig" label-width="120px" class="config-form">
          <el-form-item label="评论审核">
            <el-switch v-model="otherConfig.commentAudit" :disabled="!canManageConfig" />
          </el-form-item>
          <el-form-item label="开放注册">
            <el-switch v-model="otherConfig.registerEnabled" :disabled="!canManageConfig" />
          </el-form-item>
          <el-form-item v-if="canManageConfig">
            <el-button type="primary" @click="handleSave">保存配置</el-button>
            <el-button @click="handleResetOther">恢复默认</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="搜索索引" name="search">
        <div class="search-panel">
          <div class="search-panel__header">
            <div>
              <h3>Elasticsearch 索引管理</h3>
              <p>用于检查当前搜索引擎状态，并手动触发文章、面试题、用户索引的重建。</p>
            </div>
            <div class="search-panel__actions">
              <el-button :loading="searchStatusLoading" @click="fetchSearchStatus">刷新状态</el-button>
              <el-button
                v-if="canManageConfig"
                type="primary"
                :loading="rebuildLoading"
                @click="handleRebuildIndex"
              >
                重建索引
              </el-button>
            </div>
          </div>

          <el-alert
            :title="searchStatus.message || '未获取到搜索状态'"
            :type="searchStatus.available ? 'success' : (searchStatus.enabled ? 'warning' : 'info')"
            :closable="false"
            show-icon
            class="search-status-alert"
          />

          <div class="search-status-grid">
            <div class="status-item">
              <span class="status-item__label">是否启用</span>
              <el-tag :type="searchStatus.enabled ? 'success' : 'info'">
                {{ searchStatus.enabled ? '已启用' : '未启用' }}
              </el-tag>
            </div>
            <div class="status-item">
              <span class="status-item__label">当前引擎</span>
              <el-tag :type="searchStatus.engine === 'elasticsearch' ? 'success' : 'warning'">
                {{ searchStatus.engine === 'elasticsearch' ? 'Elasticsearch' : '数据库兜底' }}
              </el-tag>
            </div>
            <div class="status-item">
              <span class="status-item__label">服务可用</span>
              <el-tag :type="searchStatus.available ? 'success' : 'danger'">
                {{ searchStatus.available ? '可用' : '不可用' }}
              </el-tag>
            </div>
            <div class="status-item">
              <span class="status-item__label">服务地址</span>
              <span class="status-item__value">{{ searchStatus.endpoint || '-' }}</span>
            </div>
            <div class="status-item">
              <span class="status-item__label">文章索引</span>
              <span class="status-item__value">{{ searchStatus.articleIndex || '-' }}</span>
            </div>
            <div class="status-item">
              <span class="status-item__label">题库索引</span>
              <span class="status-item__value">{{ searchStatus.studyQuestionIndex || '-' }}</span>
            </div>
            <div class="status-item">
              <span class="status-item__label">用户索引</span>
              <span class="status-item__value">{{ searchStatus.userIndex || '-' }}</span>
            </div>
            <div class="status-item">
              <span class="status-item__label">写入分词器</span>
              <span class="status-item__value">{{ searchStatus.analyzer || '-' }}</span>
            </div>
            <div class="status-item">
              <span class="status-item__label">搜索分词器</span>
              <span class="status-item__value">{{ searchStatus.searchAnalyzer || '-' }}</span>
            </div>
          </div>

          <div class="search-rebuild-box">
            <div class="search-rebuild-box__title">重建范围</div>
            <el-radio-group v-model="rebuildScope" :disabled="!canManageConfig || rebuildLoading">
              <el-radio label="all">全部索引</el-radio>
              <el-radio label="article">仅文章</el-radio>
              <el-radio label="study">仅面试题</el-radio>
              <el-radio label="user">仅用户</el-radio>
            </el-radio-group>
            <p class="search-rebuild-box__tip">
              建议在补齐历史数据或 ES 数据漂移后执行。重建过程会按当前范围重新回灌索引。
            </p>
          </div>

          <div v-if="rebuildResult" class="search-result-box">
            <div class="search-result-box__title">最近一次重建结果</div>
            <div class="search-result-grid">
              <div class="result-item">
                <span class="result-item__label">执行结果</span>
                <el-tag :type="rebuildResult.success ? 'success' : 'danger'">
                  {{ rebuildResult.success ? '成功' : '失败' }}
                </el-tag>
              </div>
              <div class="result-item">
                <span class="result-item__label">文章</span>
                <span class="result-item__value">{{ rebuildResult.articleIndexed ?? 0 }}</span>
              </div>
              <div class="result-item">
                <span class="result-item__label">面试题</span>
                <span class="result-item__value">{{ rebuildResult.studyQuestionIndexed ?? 0 }}</span>
              </div>
              <div class="result-item">
                <span class="result-item__label">用户</span>
                <span class="result-item__value">{{ rebuildResult.userIndexed ?? 0 }}</span>
              </div>
            </div>
            <p class="search-result-box__message">{{ rebuildResult.message }}</p>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConfigs, updateConfigs, resetConfigs, getSearchStatus, rebuildSearchIndex } from '@/api/system'
import ImageUpload from '@/components/ImageUpload/index.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 判断是否有系统配置权限
const canManageConfig = computed(() => {
  const roles = userStore.roles || []
  const permissions = userStore.permissions || []
  
  // 检查是否有全部权限
  if (permissions.includes('*:*:*')) {
    return true
  }
  
  // 检查角色
  return roles.some(role => 
    role === '超级管理员' || 
    role === 'admin' || 
    role === 'ADMIN' ||
    role === 'super_admin'
  )
})

const activeTab = ref('site')
const siteConfig = ref({ siteName: '', siteDescription: '', siteLogo: '', icp: '' })
const otherConfig = ref({ commentAudit: false, registerEnabled: true })
const searchStatusLoading = ref(false)
const rebuildLoading = ref(false)
const rebuildScope = ref('all')
const searchStatus = ref({
  enabled: false,
  available: false,
  engine: 'database',
  message: '',
  endpoint: '',
  articleIndex: '',
  studyQuestionIndex: '',
  userIndex: '',
  analyzer: '',
  searchAnalyzer: ''
})
const rebuildResult = ref(null)

async function fetchConfigs() {
  try {
    const res = await getConfigs()
    const data = res.data || {}
    if (data.site) {
      siteConfig.value = { 
        siteName: data.site.siteName || '',
        siteDescription: data.site.siteDescription || '',
        siteLogo: data.site.siteLogo || '',
        icp: data.site.icp || ''
      }
    }
    if (data.other) {
      otherConfig.value = {
        commentAudit: data.other.commentAudit || false,
        registerEnabled: data.other.registerEnabled !== false
      }
    }
  } catch (e) {
    console.error('获取配置失败:', e)
  }
}

async function fetchSearchStatus() {
  searchStatusLoading.value = true
  try {
    const res = await getSearchStatus()
    searchStatus.value = {
      ...searchStatus.value,
      ...(res.data || {})
    }
  } catch (e) {
    console.error('获取搜索状态失败:', e)
    searchStatus.value.message = '获取搜索状态失败，请稍后重试'
  } finally {
    searchStatusLoading.value = false
  }
}

async function handleSave() {
  await updateConfigs({ site: siteConfig.value, other: otherConfig.value })
  ElMessage.success('保存成功')
}

async function handleResetSite() {
  await ElMessageBox.confirm('确定要恢复网站设置为默认值吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await resetConfigs('site')
  await fetchConfigs()
  ElMessage.success('已恢复默认')
}

async function handleResetOther() {
  await ElMessageBox.confirm('确定要恢复其他设置为默认值吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await resetConfigs('other')
  await fetchConfigs()
  ElMessage.success('已恢复默认')
}

async function handleRebuildIndex() {
  try {
    await ElMessageBox.confirm(
      `确定要重建${rebuildScopeText.value}吗？该操作会按当前范围重新同步 Elasticsearch 索引。`,
      '重建索引',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }
  rebuildLoading.value = true
  try {
    const res = await rebuildSearchIndex(rebuildScope.value)
    rebuildResult.value = res.data || null
    if (rebuildResult.value?.success === false) {
      ElMessage.error(rebuildResult.value?.message || '重建索引失败')
    } else {
      ElMessage.success(rebuildResult.value?.message || '重建完成')
    }
    await fetchSearchStatus()
  } catch (e) {
    console.error('重建索引失败:', e)
    ElMessage.error('重建索引失败')
  } finally {
    rebuildLoading.value = false
  }
}

const rebuildScopeText = computed(() => {
  const map = {
    all: '全部索引',
    article: '文章索引',
    study: '面试题索引',
    user: '用户索引'
  }
  return map[rebuildScope.value] || '当前索引'
})

onMounted(() => {
  fetchConfigs()
  fetchSearchStatus()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.config-page {
  padding: 20px;
  background: var(--bg-card);
  border-radius: $radius-lg;
}

.page-title {
  font-size: 20px;
  color: var(--text-primary);
  margin: 0 0 $spacing-xl 0;
}

.config-form {
  max-width: 600px;
}

.search-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-panel__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;

  h3 {
    margin: 0 0 6px;
    color: var(--text-primary);
    font-size: 18px;
  }

  p {
    margin: 0;
    color: var(--text-muted);
    line-height: 1.6;
    font-size: 13px;
  }
}

.search-panel__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.search-status-alert {
  margin-top: 4px;
}

.search-status-grid,
.search-result-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.status-item,
.result-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
  border-radius: 12px;
  background: var(--bg-darker);
  border: 1px solid var(--border-color);
}

.status-item__label,
.result-item__label {
  color: var(--text-muted);
  font-size: 12px;
}

.status-item__value,
.result-item__value {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.6;
  word-break: break-all;
}

.search-rebuild-box,
.search-result-box {
  padding: 16px 18px;
  border-radius: 14px;
  border: 1px solid var(--border-color);
  background: var(--bg-darker);
}

.search-rebuild-box__title,
.search-result-box__title {
  margin-bottom: 12px;
  color: var(--text-primary);
  font-weight: 600;
}

.search-rebuild-box__tip,
.search-result-box__message {
  margin: 12px 0 0;
  color: var(--text-muted);
  line-height: 1.7;
  font-size: 13px;
}

:deep(.el-tabs__item) {
  color: var(--text-secondary);
  
  &.is-active {
    color: var(--primary-color);
  }
  
  &:hover {
    color: var(--primary-color);
  }
}

:deep(.el-tabs__active-bar) {
  background-color: var(--primary-color);
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: var(--border-color);
}

:deep(.el-form-item__label) {
  color: var(--text-secondary);
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  background: var(--bg-darker);
  box-shadow: none;
  border: 1px solid var(--border-color);
  
  &:hover, &:focus {
    border-color: var(--primary-color);
  }
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  color: var(--text-primary);
  
  &::placeholder {
    color: var(--text-muted);
  }
}

:deep(.el-switch.is-checked .el-switch__core) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

@media (max-width: 992px) {
  .search-status-grid,
  .search-result-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .search-panel__header {
    flex-direction: column;
  }

  .search-status-grid,
  .search-result-grid {
    grid-template-columns: 1fr;
  }
}
</style>
