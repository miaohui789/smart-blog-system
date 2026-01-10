<template>
  <div class="write-page">
    <div class="write-header">
      <el-input 
        v-model="form.title" 
        placeholder="请输入文章标题..." 
        class="title-input"
        maxlength="100"
        show-word-limit
      />
      <div class="header-actions">
        <el-button @click="handleSave(0)" :loading="saving">
          <el-icon><Document /></el-icon>
          保存草稿
        </el-button>
        <button class="publish-btn" @click="handleSave(1)" :disabled="saving">
          <div class="svg-wrapper-1">
            <div class="svg-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20">
                <path fill="none" d="M0 0h24v24H0z"></path>
                <path fill="currentColor" d="M1.946 9.315c-.522-.174-.527-.455.01-.634l19.087-6.362c.529-.176.832.12.684.638l-5.454 19.086c-.15.529-.455.547-.679.045L12 14l6-8-8 6-8.054-2.685z"></path>
              </svg>
            </div>
          </div>
          <span>{{ saving ? '发布中...' : '发布文章' }}</span>
        </button>
      </div>
    </div>

    <div class="write-body">
      <div class="editor-container">
        <MdEditor 
          v-model="form.content" 
          :theme="editorTheme"
          :preview-theme="'github'"
          :code-theme="'atom'"
          @onUploadImg="handleUploadImg"
        />
        <button class="fullscreen-btn" @click="toggleFullscreen" title="全屏编辑">
          <el-icon><FullScreen /></el-icon>
          <span>全屏</span>
        </button>
      </div>

      <div class="settings-panel glass-card">
        <h3 class="panel-title">
          <el-icon><Setting /></el-icon>
          文章设置
        </h3>

        <el-form label-position="top">
          <el-form-item label="分类">
            <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
              <el-option 
                v-for="item in categories" 
                :key="item.id" 
                :label="item.name" 
                :value="item.id" 
              />
            </el-select>
          </el-form-item>

          <el-form-item label="标签">
            <el-select 
              v-model="form.tagIds" 
              multiple 
              placeholder="请选择标签" 
              style="width: 100%"
              :max-collapse-tags="3"
            >
              <el-option 
                v-for="item in tags" 
                :key="item.id" 
                :label="item.name" 
                :value="item.id"
              >
                <span :style="{ color: item.color }">{{ item.name }}</span>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="封面图">
            <div class="cover-upload">
              <el-upload
                class="cover-uploader"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleCoverSuccess"
                :before-upload="beforeCoverUpload"
                accept="image/*"
              >
                <img v-if="form.cover" :src="form.cover" class="cover-preview" />
                <div v-else class="cover-placeholder">
                  <el-icon><Plus /></el-icon>
                  <span>上传封面</span>
                </div>
              </el-upload>
              <el-button v-if="form.cover" text type="danger" @click="form.cover = ''">
                移除封面
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="摘要">
            <el-input 
              v-model="form.summary" 
              type="textarea" 
              :rows="4" 
              placeholder="请输入文章摘要，不填则自动截取"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 全屏编辑器遮罩 -->
    <Teleport to="body">
      <Transition name="fullscreen-fade">
        <div v-if="isFullscreen" class="fullscreen-overlay">
          <button class="fullscreen-close-btn" @click="toggleFullscreen" title="退出全屏">
            <el-icon><Close /></el-icon>
            <span>退出全屏</span>
          </button>
          <MdEditor 
            v-model="form.content" 
            :theme="editorTheme"
            :preview-theme="'github'"
            :code-theme="'atom'"
            class="fullscreen-editor"
            @onUploadImg="handleUploadImg"
          />
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Setting, Plus, FullScreen, Close } from '@element-plus/icons-vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { createArticle, updateArticle, getArticleDetail } from '@/api/article'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const categories = ref([])
const tags = ref([])
const saving = ref(false)
const isFullscreen = ref(false)
const isEdit = computed(() => !!route.params.id)

// 编辑器主题
const editorTheme = computed(() => themeStore.isDark ? 'dark' : 'light')

// 切换全屏
function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value
}

const form = ref({
  title: '',
  categoryId: null,
  tagIds: [],
  cover: '',
  summary: '',
  content: ''
})

// 上传配置
const uploadUrl = '/api/upload/image'
const uploadHeaders = computed(() => ({
  Authorization: userStore.token ? `Bearer ${userStore.token}` : ''
}))

// 上传封面成功
function handleCoverSuccess(response) {
  if (response.code === 200) {
    form.value.cover = response.data
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传前校验
function beforeCoverUpload(file) {
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

// 编辑器图片上传
async function handleUploadImg(files, callback) {
  const results = []
  
  for (const file of files) {
    const formData = new FormData()
    formData.append('file', file)

    try {
      const response = await fetch('/api/upload/image', {
        method: 'POST',
        headers: {
          Authorization: userStore.token ? `Bearer ${userStore.token}` : ''
        },
        body: formData
      })
      const result = await response.json()
      if (result.code === 200) {
        results.push(result.data)
      } else {
        ElMessage.error(result.message || '图片上传失败')
      }
    } catch (error) {
      ElMessage.error('图片上传失败')
    }
  }
  
  callback(results)
}

// 保存文章
async function handleSave(status) {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }
  if (status === 1 && !form.value.categoryId) {
    ElMessage.warning('发布文章请选择分类')
    return
  }

  saving.value = true
  try {
    const data = {
      ...form.value,
      status
    }

    if (isEdit.value) {
      await updateArticle(route.params.id, data)
      ElMessage.success(status === 1 ? '更新并发布成功' : '保存成功')
    } else {
      await createArticle(data)
      ElMessage.success(status === 1 ? '发布成功' : '保存草稿成功')
    }

    router.push('/user/articles')
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 加载文章数据（编辑模式）
async function loadArticle() {
  if (!isEdit.value) return

  try {
    const res = await getArticleDetail(route.params.id)
    const article = res.data
    form.value = {
      title: article.title,
      categoryId: article.categoryId,
      tagIds: article.tags?.map(t => t.id) || [],
      cover: article.cover || '',
      summary: article.summary || '',
      content: article.content || ''
    }
  } catch (error) {
    ElMessage.error('加载文章失败')
    router.push('/user/articles')
  }
}

onMounted(async () => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  // 加载分类和标签
  const [catRes, tagRes] = await Promise.all([
    getCategoryList(),
    getTagList()
  ])
  categories.value = catRes.data || []
  tags.value = tagRes.data || []

  // 编辑模式加载文章
  await loadArticle()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.write-page {
  min-height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
}

.write-header {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  padding: $spacing-lg;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: background-color 0.3s, border-color 0.3s;
}

.title-input {
  flex: 1;
  
  :deep(.el-input__wrapper) {
    background: transparent !important;
    box-shadow: none !important;
    border: none !important;
    padding: 0;
  }
  
  :deep(.el-input__inner) {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-primary);
    
    &::placeholder {
      color: var(--text-muted);
    }
  }
}

.header-actions {
  display: flex;
  gap: $spacing-sm;
}

/* 发布文章按钮 - 和写文章按钮一样的样式 */
.publish-btn {
  display: flex;
  align-items: center;
  padding: 0.6em 1em;
  padding-left: 0.8em;
  background: $primary-color;
  border-radius: 12px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  overflow: hidden;
  transition: all 0.2s ease;
  cursor: pointer;
  border: none;
  
  .svg-wrapper-1 {
    display: flex;
    align-items: center;
  }
  
  .svg-wrapper {
    display: flex;
    align-items: center;
    transition: transform 0.3s ease-in-out;
  }
  
  svg {
    display: block;
    transform-origin: center center;
    transition: transform 0.3s ease-in-out;
  }
  
  span {
    display: block;
    margin-left: 0.3em;
    transition: all 0.3s ease-in-out;
  }
  
  &:hover:not(:disabled) {
    background: $primary-dark;
    box-shadow: 0 4px 12px rgba($primary-color, 0.3);
    
    .svg-wrapper {
      animation: fly-1 0.6s ease-in-out infinite alternate;
    }
    
    svg {
      transform: translateX(1.2em) rotate(45deg) scale(1.1);
    }
    
    span {
      transform: translateX(4em);
    }
  }
  
  &:active:not(:disabled) {
    transform: scale(0.95);
  }
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

@keyframes fly-1 {
  from {
    transform: translateY(0.1em);
  }
  to {
    transform: translateY(-0.1em);
  }
}

.write-body {
  flex: 1;
  display: flex;
  gap: $spacing-lg;
  padding: $spacing-lg;
}

.editor-container {
  flex: 1;
  min-width: 0;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  
  :deep(.md-editor) {
    flex: 1;
    height: calc(100vh - 220px);
    border-radius: $radius-lg;
    border: 1px solid var(--border-color);
    
    .md-editor-toolbar {
      background: var(--bg-card);
      border-bottom: 1px solid var(--border-color);
    }
    
    .md-editor-content {
      background: var(--bg-card);
    }
    
    .md-editor-input-wrapper {
      background: var(--bg-card);
    }
    
    .md-editor-preview-wrapper {
      background: var(--bg-darker);
    }
    
    .md-editor-footer {
      background: var(--bg-card);
      border-top: 1px solid var(--border-color);
    }
  }
}

.fullscreen-btn {
  align-self: flex-end;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-muted);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  
  .el-icon {
    font-size: 14px;
  }
  
  &:hover {
    background: var(--bg-card-hover);
    border-color: var(--border-light);
    color: var(--text-primary);
  }
}

// 全屏遮罩
.fullscreen-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  background: var(--bg-dark);
  
  .fullscreen-editor {
    width: 100%;
    height: 100vh;
    border-radius: 0;
    border: none;
    
    // 全屏模式下左右面板不同背景色
    :deep(.md-editor-content) {
      .md-editor-input-wrapper {
        background: var(--bg-card);
        
        textarea {
          background: transparent !important;
        }
      }
      
      .md-editor-preview-wrapper {
        background: var(--bg-darker);
        border-left: 1px solid var(--border-color);
      }
    }
    
    :deep(.md-editor-toolbar) {
      background: var(--bg-card);
      border-bottom: 1px solid var(--border-color);
    }
    
    :deep(.md-editor-footer) {
      background: var(--bg-card);
      border-top: 1px solid var(--border-color);
    }
  }
}

.fullscreen-close-btn {
  position: fixed;
  bottom: 50px;
  right: 20px;
  z-index: 10000;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  
  .el-icon {
    font-size: 16px;
  }
  
  &:hover {
    background: #ef4444;
    border-color: #ef4444;
    color: #fff;
  }
}

.fullscreen-fade-enter-active,
.fullscreen-fade-leave-active {
  transition: opacity 0.2s ease;
}

.fullscreen-fade-enter-from,
.fullscreen-fade-leave-to {
  opacity: 0;
}

.settings-panel {
  width: 320px;
  padding: $spacing-lg;
  height: fit-content;
  position: sticky;
  top: 100px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: background-color 0.3s, border-color 0.3s;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-md;
  border-bottom: 1px solid var(--border-color);
  transition: color 0.3s, border-color 0.3s;
  
  .el-icon {
    color: $primary-color;
  }
}

.cover-upload {
  width: 100%;
}

.cover-uploader {
  width: 100%;
  
  :deep(.el-upload) {
    width: 100%;
    border: 2px dashed var(--border-color);
    border-radius: $radius-md;
    cursor: pointer;
    transition: all 0.3s;
    overflow: hidden;
    
    &:hover {
      border-color: $primary-color;
    }
  }
}

.cover-preview {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  color: var(--text-muted);
  background: var(--bg-input);
  transition: background-color 0.3s, color 0.3s;
  
  .el-icon {
    font-size: 32px;
  }
}

:deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
}

// 响应式
@media (max-width: 1024px) {
  .write-body {
    flex-direction: column;
  }
  
  .settings-panel {
    width: 100%;
    position: static;
  }
  
  .editor-container {
    :deep(.md-editor) {
      height: 500px;
    }
  }
}
</style>
