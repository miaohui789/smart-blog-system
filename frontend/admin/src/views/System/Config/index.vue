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
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigs, updateConfigs } from '@/api/system'
import ImageUpload from '@/components/ImageUpload/index.vue'

const activeTab = ref('site')
const siteConfig = ref({ siteName: '', siteDescription: '', siteLogo: '', icp: '' })
const otherConfig = ref({ commentAudit: false, registerEnabled: true })

async function fetchConfigs() {
  const res = await getConfigs()
  const data = res.data || {}
  siteConfig.value = { ...siteConfig.value, ...data.site }
  otherConfig.value = { ...otherConfig.value, ...data.other }
}

async function handleSave() {
  await updateConfigs({ site: siteConfig.value, other: otherConfig.value })
  ElMessage.success('保存成功')
}

onMounted(fetchConfigs)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.page-title {
  font-size: 20px;
  color: $text-primary;
  margin-bottom: $spacing-xl;
}

.config-form {
  max-width: 600px;
}
</style>
