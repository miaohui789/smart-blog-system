<template>
  <div class="config-page">
    <h2 class="page-title">系统配置</h2>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="网站设置" name="site">
        <el-form :model="siteConfig" label-width="120px" class="config-form">
          <el-form-item label="网站名称">
            <el-input v-model="siteConfig.siteName" />
          </el-form-item>
          <el-form-item label="网站描述">
            <el-input v-model="siteConfig.siteDescription" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="网站Logo">
            <ImageUpload v-model="siteConfig.siteLogo" />
          </el-form-item>
          <el-form-item label="备案号">
            <el-input v-model="siteConfig.icp" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSave">保存配置</el-button>
            <el-button @click="handleResetSite">恢复默认</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="其他设置" name="other">
        <el-form :model="otherConfig" label-width="120px" class="config-form">
          <el-form-item label="评论审核">
            <el-switch v-model="otherConfig.commentAudit" />
          </el-form-item>
          <el-form-item label="开放注册">
            <el-switch v-model="otherConfig.registerEnabled" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSave">保存配置</el-button>
            <el-button @click="handleResetOther">恢复默认</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConfigs, updateConfigs, resetConfigs } from '@/api/system'
import ImageUpload from '@/components/ImageUpload/index.vue'

const activeTab = ref('site')
const siteConfig = ref({ siteName: '', siteDescription: '', siteLogo: '', icp: '' })
const otherConfig = ref({ commentAudit: false, registerEnabled: true })

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

onMounted(fetchConfigs)
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
</style>
