<template>
  <div class="ai-logo-page">
    <!-- Logo列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI 模型 Logo 管理</span>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon> 添加 Logo
          </el-button>
        </div>
      </template>
      
      <el-table :data="logos" v-loading="loading" style="width: 100%">
        <el-table-column label="预览" width="100" align="center">
          <template #default="{ row }">
            <el-image 
              :src="getFullUrl(row.logoUrl)" 
              style="width: 40px; height: 40px; border-radius: 8px;"
              fit="contain"
              :preview-src-list="[getFullUrl(row.logoUrl)]"
              preview-teleported
            >
              <template #error>
                <div class="logo-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column label="名称" prop="name" min-width="150" />
        <el-table-column label="服务商" width="150">
          <template #default="{ row }">
            <el-tag :type="getProviderTagType(row.provider)" disable-transitions>
              {{ getProviderLabel(row.provider) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.logoType === 'upload' ? 'success' : 'primary'" size="small">
              {{ row.logoType === 'upload' ? '上传图片' : '网络地址' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Logo地址" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="logo-url-text">{{ row.logoUrl }}</span>
          </template>
        </el-table-column>
        <el-table-column label="排序" width="80" align="center" prop="sortOrder" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && logos.length === 0" description="暂无Logo，点击上方按钮添加" />
    </el-card>

    <!-- 使用说明 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <span>使用说明</span>
      </template>
      <div class="help-content">
        <h4>1. Logo 用途</h4>
        <ul>
          <li>为每个 AI 服务商配置专属 Logo，在用户端模型选择列表中展示</li>
          <li>每个服务商只需设置一个 Logo，所有使用该服务商的模型共享此 Logo</li>
        </ul>
        <h4>2. 添加方式</h4>
        <ul>
          <li><strong>网络地址：</strong>直接粘贴图片 URL（如 PNG、SVG 链接），适合使用 CDN 图标</li>
          <li><strong>上传图片：</strong>从本地选择图片文件上传，支持 PNG / JPG / SVG / WebP 格式</li>
        </ul>
        <h4>3. 推荐尺寸</h4>
        <ul>
          <li>建议使用 <strong>64×64</strong> 或 <strong>128×128</strong> 的正方形图片</li>
          <li>支持透明背景的 PNG 或 SVG 效果更佳</li>
        </ul>
      </div>
    </el-card>

    <!-- 新增/编辑 Logo 弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑 Logo' : '添加 Logo'" 
      width="580px"
      destroy-on-close
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="服务商" prop="provider">
          <el-select v-model="form.provider" placeholder="选择服务商" style="width: 350px">
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="OpenAI" value="openai" />
            <el-option label="硅基流动 SiliconFlow" value="siliconflow" />
            <el-option label="智谱AI (GLM)" value="zhipu" />
            <el-option label="通义千问" value="dashscope" />
            <el-option label="百度千帆" value="qianfan" />
            <el-option label="Moonshot (Kimi)" value="moonshot" />
          </el-select>
        </el-form-item>

        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如: DeepSeek Logo" style="width: 350px" />
        </el-form-item>

        <el-form-item label="Logo来源" prop="logoType">
          <el-radio-group v-model="form.logoType" @change="onLogoTypeChange">
            <el-radio-button value="url">网络地址</el-radio-button>
            <el-radio-button value="upload">上传图片</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 网络地址输入 -->
        <el-form-item v-if="form.logoType === 'url'" label="Logo URL" prop="logoUrl">
          <el-input v-model="form.logoUrl" placeholder="输入图片网络地址" style="width: 400px" />
        </el-form-item>

        <!-- 上传图片 -->
        <el-form-item v-if="form.logoType === 'upload'" label="选择图片" prop="logoUrl">
          <el-upload
            class="logo-uploader"
            :http-request="handleHttpUpload"
            :show-file-list="false"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <el-image 
              v-if="form.logoUrl" 
              :src="getFullUrl(form.logoUrl)" 
              class="logo-preview"
              fit="contain"
            />
            <div v-else class="logo-upload-trigger">
              <el-icon class="upload-icon" v-if="!uploading"><Plus /></el-icon>
              <el-icon class="upload-icon is-loading" v-else><Loading /></el-icon>
              <span>{{ uploading ? '上传中...' : '点击上传' }}</span>
            </div>
          </el-upload>
          <div class="form-tip">支持 PNG / JPG / SVG / WebP，不超过 5MB</div>
        </el-form-item>

        <!-- Logo预览 -->
        <el-form-item v-if="form.logoUrl" label="预览">
          <div class="logo-preview-box">
            <el-image 
              :src="getFullUrl(form.logoUrl)" 
              style="width: 64px; height: 64px; border-radius: 10px;"
              fit="contain"
            >
              <template #error>
                <div class="logo-placeholder large">
                  <el-icon><Picture /></el-icon>
                  <span>加载失败</span>
                </div>
              </template>
            </el-image>
            <span class="preview-label">{{ form.provider ? getProviderLabel(form.provider) : '预览' }}</span>
          </div>
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" :step="1" />
          <span class="form-tip-inline">越小越靠前</span>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture, Loading } from '@element-plus/icons-vue'
import { getAiLogos, addAiLogo, updateAiLogo, deleteAiLogo } from '@/api/ai'
import { uploadImage } from '@/api/system'

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const logos = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const baseApiUrl = import.meta.env.VITE_API_BASE_URL || ''

const defaultForm = {
  id: null,
  provider: '',
  name: '',
  logoType: 'url',
  logoUrl: '',
  sortOrder: 0
}

const form = ref({ ...defaultForm })

const formRules = {
  provider: [{ required: true, message: '请选择服务商', trigger: 'change' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  logoType: [{ required: true, message: '请选择Logo来源', trigger: 'change' }],
  logoUrl: [{ required: true, message: '请输入或上传Logo', trigger: 'blur' }]
}

const providerMap = {
  deepseek: { label: 'DeepSeek', type: '' },
  openai: { label: 'OpenAI', type: 'success' },
  siliconflow: { label: '硅基流动', type: 'primary' },
  zhipu: { label: '智谱AI', type: 'warning' },
  dashscope: { label: '通义千问', type: 'danger' },
  qianfan: { label: '百度千帆', type: 'info' },
  moonshot: { label: 'Moonshot', type: '' }
}

function getProviderLabel(provider) {
  return providerMap[provider]?.label || provider
}

function getProviderTagType(provider) {
  return providerMap[provider]?.type || 'info'
}

function getFullUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('//')) {
    return url
  }
  return baseApiUrl + url
}

function onLogoTypeChange() {
  form.value.logoUrl = ''
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function handleHttpUpload({ file }) {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await uploadImage(formData)
    if (res.code === 200) {
      form.value.logoUrl = res.data
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e) {
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
  }
}

async function fetchLogos() {
  loading.value = true
  try {
    const res = await getAiLogos()
    if (res.code === 200) {
      logos.value = res.data || []
    }
  } catch (e) {
    console.error('获取Logo列表失败', e)
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  isEdit.value = false
  form.value = { ...defaultForm }
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    if (isEdit.value) {
      const res = await updateAiLogo(form.value)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchLogos()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      const res = await addAiLogo(form.value)
      if (res.code === 200) {
        ElMessage.success('添加成功')
        dialogVisible.value = false
        fetchLogos()
      } else {
        ElMessage.error(res.message || '添加失败')
      }
    }
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除 "${row.name}" 的 Logo 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteAiLogo(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchLogos()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchLogos()
})
</script>

<style lang="scss" scoped>
.ai-logo-page {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #c0c4cc;
  font-size: 18px;

  &.large {
    width: 64px;
    height: 64px;
    flex-direction: column;
    gap: 4px;
    font-size: 14px;

    span {
      font-size: 10px;
    }
  }
}

.logo-url-text {
  font-size: 12px;
  color: #909399;
  word-break: break-all;
}

.logo-uploader {
  :deep(.el-upload) {
    border: 2px dashed #dcdfe6;
    border-radius: 12px;
    cursor: pointer;
    overflow: hidden;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }
}

.logo-upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  color: #8c939d;
  gap: 8px;

  .upload-icon {
    font-size: 28px;
  }

  span {
    font-size: 12px;
  }
}

.logo-preview {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 10px;
}

.logo-preview-box {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 12px;
  border: 1px solid #ebeef5;
}

.preview-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.6;
}

.form-tip-inline {
  font-size: 12px;
  color: #909399;
  margin-left: 12px;
}

.help-content {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;

  h4 {
    margin: 16px 0 8px;
    color: #303133;

    &:first-child {
      margin-top: 0;
    }
  }

  ul {
    margin: 0;
    padding-left: 20px;
  }
}
</style>
