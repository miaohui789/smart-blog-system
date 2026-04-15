<template>
  <div class="write-page">
    <div class="write-header">
      <el-input 
        v-model="form.title" 
        placeholder="请输入文章标题..." 
        class="title-input"
        maxlength="100"
      />
      <span class="title-count">{{ form.title.length }} / 100</span>
      <div class="header-actions">
        <input
          ref="markdownInputRef"
          type="file"
          webkitdirectory
          directory
          multiple
          class="markdown-file-input"
          @change="handleMarkdownImport"
        />
        <el-button @click="triggerMarkdownImport" :loading="importingMarkdown">
          <el-icon><Upload /></el-icon>
          导入 Markdown 包
        </el-button>
        <el-button @click="handleSave(0)" :loading="saving">
          <el-icon><Document /></el-icon>
          保存草稿
        </el-button>
        <el-button @click="handleReset" :disabled="saving || importingMarkdown">
          <el-icon><RefreshLeft /></el-icon>
          重置
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
        <button class="fullscreen-btn" @click="toggleFullscreen" title="全屏编辑">
          <el-icon><FullScreen /></el-icon>
          <span>全屏</span>
        </button>
        <MdEditor 
          v-model="form.content" 
          :theme="editorTheme"
          :preview-theme="'github'"
          :code-theme="'atom'"
          :transform-img-url="transformMarkdownImageUrl"
          @onUploadImg="handleUploadImg"
        />
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
            :transform-img-url="transformMarkdownImageUrl"
            @onUploadImg="handleUploadImg"
          />
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Setting, Plus, FullScreen, Close, Upload, RefreshLeft } from '@element-plus/icons-vue'
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
const importingMarkdown = ref(false)
const isFullscreen = ref(false)
const markdownInputRef = ref(null)
const isEdit = computed(() => !!route.params.id)
let markdownImportContext = createEmptyMarkdownImportContext()

// 编辑器主题
const editorTheme = computed(() => themeStore.isDark ? 'dark' : 'light')

// 切换全屏
function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value
}

function createEmptyForm() {
  return {
    title: '',
    categoryId: null,
    tagIds: [],
    cover: '',
    summary: '',
    content: ''
  }
}

const form = ref(createEmptyForm())

// 上传配置
const uploadUrl = '/api/upload/image'
const uploadHeaders = computed(() => ({
  Authorization: userStore.token ? `Bearer ${userStore.token}` : ''
}))

function triggerMarkdownImport() {
  markdownInputRef.value?.click()
}

function isMarkdownFile(file) {
  return /\.(md|markdown)$/i.test(file.name || '')
}

function readFileAsText(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result || '')
    reader.onerror = () => reject(new Error('读取文件失败'))
    reader.readAsText(file, 'utf-8')
  })
}

function normalizeMarkdownContent(content) {
  return String(content || '')
    .replace(/^\uFEFF/, '')
    .replace(/\r\n?/g, '\n')
}

function splitMarkdownFrontmatter(content) {
  if (!content.startsWith('---\n')) {
    return { frontmatter: '', body: content }
  }

  const endIndex = content.indexOf('\n---\n', 4)
  if (endIndex === -1) {
    return { frontmatter: '', body: content }
  }

  return {
    frontmatter: content.slice(0, endIndex + 5),
    body: content.slice(endIndex + 5).replace(/^\n+/, '')
  }
}

function extractMarkdownTitle(content, fallbackTitle) {
  const { body } = splitMarkdownFrontmatter(content)
  const bodyContent = body.replace(/^\n+/, '')
  const headingMatch = bodyContent.match(/^#\s+(.+)$/m)
  if (headingMatch?.[1]) {
    return headingMatch[1].trim()
  }
  return fallbackTitle
}

function getFileNameWithoutExtension(fileName) {
  return (fileName || '未命名文章').replace(/\.[^/.]+$/, '')
}

function createEmptyMarkdownImportContext() {
  return {
    markdownPaths: [],
    markdownDirs: [],
    assets: new Map(),
    previewAssets: new Map(),
    missingImagePaths: [],
    resolvedImageCount: 0,
    previewContent: ''
  }
}

function resetMarkdownImportContext() {
  const releasedAssets = new Set(markdownImportContext.assets.values())
  for (const asset of releasedAssets) {
    if (asset.previewUrl) {
      URL.revokeObjectURL(asset.previewUrl)
    }
  }
  markdownImportContext = createEmptyMarkdownImportContext()
}

function safeDecodePath(path) {
  try {
    return decodeURI(path)
  } catch (error) {
    return path
  }
}

function normalizeImportPath(inputPath) {
  const rawPath = safeDecodePath(String(inputPath || '').trim())
    .replace(/\\/g, '/')
    .replace(/^\.\/+/, '')

  const normalizedSegments = []
  for (const segment of rawPath.split('/')) {
    if (!segment || segment === '.') {
      continue
    }
    if (segment === '..') {
      normalizedSegments.pop()
      continue
    }
    normalizedSegments.push(segment)
  }

  return normalizedSegments.join('/')
}

function stripLeadingPathSegment(filePath) {
  const normalizedPath = normalizeImportPath(filePath)
  if (!normalizedPath) {
    return ''
  }

  const segments = normalizedPath.split('/').filter(Boolean)
  if (segments.length <= 1) {
    return normalizedPath
  }

  return normalizeImportPath(segments.slice(1).join('/'))
}

function getImportPathCandidates(file) {
  const rawPath = String(file?.webkitRelativePath || file?.name || '').replace(/\\/g, '/')
  if (!rawPath) {
    return []
  }

  const normalizedPath = normalizeImportPath(rawPath)
  const strippedPath = stripLeadingPathSegment(normalizedPath)
  const candidates = []

  for (const candidate of [normalizedPath, strippedPath]) {
    if (candidate && !candidates.includes(candidate)) {
      candidates.push(candidate)
    }
  }

  return candidates
}

function getDirectoryPath(filePath) {
  const normalizedPath = normalizeImportPath(filePath)
  const lastSlashIndex = normalizedPath.lastIndexOf('/')
  return lastSlashIndex === -1 ? '' : normalizedPath.slice(0, lastSlashIndex)
}

function getPathDepth(filePath) {
  const normalizedPath = normalizeImportPath(filePath)
  return normalizedPath ? normalizedPath.split('/').length : 0
}

function compareImportFilePath(fileA, fileB) {
  const pathA = getImportPathCandidates(fileA)[0] || ''
  const pathB = getImportPathCandidates(fileB)[0] || ''
  const depthDiff = getPathDepth(pathA) - getPathDepth(pathB)
  if (depthDiff !== 0) {
    return depthDiff
  }
  return pathA.localeCompare(pathB, 'zh-CN')
}

function isHttpLikePath(path) {
  return /^(https?:)?\/\//i.test(path)
}

function isWindowsAbsolutePath(path) {
  return /^[a-zA-Z]:[\\/]/.test(path)
}

function isLocalMarkdownPath(path) {
  const targetPath = String(path || '').trim()
  if (!targetPath) {
    return false
  }

  return !isHttpLikePath(targetPath)
    && !/^data:/i.test(targetPath)
    && !/^blob:/i.test(targetPath)
    && !/^file:/i.test(targetPath)
    && !targetPath.startsWith('/')
    && !targetPath.startsWith('#')
    && !isWindowsAbsolutePath(targetPath)
}

function splitPathSuffix(path) {
  const hashIndex = path.indexOf('#')
  const queryIndex = path.indexOf('?')
  const indexes = [hashIndex, queryIndex].filter(index => index !== -1)
  const splitIndex = indexes.length ? Math.min(...indexes) : -1

  if (splitIndex === -1) {
    return { pathname: path, suffix: '' }
  }

  return {
    pathname: path.slice(0, splitIndex),
    suffix: path.slice(splitIndex)
  }
}

function resolveImportPathCandidates(markdownDirs, imgUrl) {
  const { pathname } = splitPathSuffix(String(imgUrl || '').trim())
  if (!isLocalMarkdownPath(pathname)) {
    return []
  }

  const normalizedPath = normalizeImportPath(pathname)
  if (!normalizedPath) {
    return []
  }

  const candidates = []
  const addCandidate = (candidate) => {
    const normalizedCandidate = normalizeImportPath(candidate)
    if (normalizedCandidate && !candidates.includes(normalizedCandidate)) {
      candidates.push(normalizedCandidate)
    }
  }

  addCandidate(normalizedPath)
  addCandidate(stripLeadingPathSegment(normalizedPath))

  for (const markdownDir of markdownDirs || []) {
    if (!markdownDir) {
      continue
    }
    addCandidate(`${markdownDir}/${normalizedPath}`)
    addCandidate(stripLeadingPathSegment(`${markdownDir}/${normalizedPath}`))
  }

  return candidates
}

function resolveMarkdownAsset(imgUrl) {
  const targetUrl = String(imgUrl || '').trim()
  if (!targetUrl) {
    return null
  }

  if (/^blob:/i.test(targetUrl)) {
    return markdownImportContext.previewAssets.get(targetUrl) || null
  }

  const candidates = resolveImportPathCandidates(markdownImportContext.markdownDirs, imgUrl)
  for (const candidate of candidates) {
    const asset = markdownImportContext.assets.get(candidate)
    if (asset) {
      return asset
    }
  }
  return null
}

function collectMarkdownImageUrls(content) {
  const urls = []
  const markdownImageRegex = /!\[[^\]]*]\((?:<([^>\n]+)>|([^) \t\n]+))(?:\s+[^)\n]+)?\)/g
  const htmlImageRegex = /<img\b[^>]*\bsrc=["']([^"']+)["'][^>]*>/gi

  for (const match of content.matchAll(markdownImageRegex)) {
    const imageUrl = (match[1] || match[2] || '').trim()
    if (imageUrl) {
      urls.push(imageUrl)
    }
  }

  for (const match of content.matchAll(htmlImageRegex)) {
    const imageUrl = (match[1] || '').trim()
    if (imageUrl) {
      urls.push(imageUrl)
    }
  }

  return urls
}

function transformMarkdownImageUrl(imgUrl) {
  const asset = resolveMarkdownAsset(imgUrl)
  if (!asset) {
    return imgUrl
  }
  return asset?.previewUrl || imgUrl
}

function replaceMarkdownImageUrls(content, resolveUrl) {
  const markdownImageRegex = /!\[([^\]]*)\]\((?:<([^>\n]+)>|([^) \t\n]+))((?:\s+[^)\n]+)?)\)/g
  const htmlImageRegex = /(<img\b[^>]*\bsrc=["'])([^"']+)(["'][^>]*>)/gi

  let nextContent = String(content || '').replace(markdownImageRegex, (match, altText, angleUrl, plainUrl, suffix = '') => {
    const originalUrl = (angleUrl || plainUrl || '').trim()
    const nextUrl = resolveUrl(originalUrl)

    if (!nextUrl || nextUrl === originalUrl) {
      return match
    }

    const wrappedUrl = angleUrl ? `<${nextUrl}>` : nextUrl
    return `![${altText}](${wrappedUrl}${suffix || ''})`
  })

  nextContent = nextContent.replace(htmlImageRegex, (match, prefix, originalUrl, suffix) => {
    const nextUrl = resolveUrl(String(originalUrl || '').trim())

    if (!nextUrl || nextUrl === originalUrl) {
      return match
    }

    return `${prefix}${nextUrl}${suffix}`
  })

  return nextContent
}

async function buildMarkdownImportContext(markdownFile, files) {
  const markdownPaths = getImportPathCandidates(markdownFile)
  const markdownDirs = Array.from(new Set(markdownPaths.map(getDirectoryPath).filter(Boolean)))
  const assets = new Map()
  const previewAssets = new Map()

  for (const file of files) {
    const isImageFile = file.type?.startsWith('image/')
      || /\.(png|jpe?g|gif|bmp|webp|svg|avif)$/i.test(file.name || '')

    if (!isImageFile) {
      continue
    }

    const filePaths = getImportPathCandidates(file)
    if (!filePaths.length) {
      continue
    }

    const asset = {
      file,
      canonicalPath: filePaths[0],
      previewUrl: URL.createObjectURL(file),
      uploadedUrl: ''
    }

    for (const filePath of filePaths) {
      assets.set(filePath, asset)
    }
    previewAssets.set(asset.previewUrl, asset)
  }

  const rawContent = await readFileAsText(markdownFile)
  const content = normalizeMarkdownContent(rawContent)
  const referencedImagePaths = Array.from(new Set(
    collectMarkdownImageUrls(content)
      .map(imgUrl => resolveImportPathCandidates(markdownDirs, imgUrl))
      .flat()
  ))
  const missingImagePaths = collectMarkdownImageUrls(content).filter(imgUrl => {
    return isLocalMarkdownPath(splitPathSuffix(imgUrl).pathname) && !resolveImportPathCandidates(markdownDirs, imgUrl).some(assetPath => assets.has(assetPath))
  })
  const resolvedImageCount = Array.from(new Set(
    referencedImagePaths
      .map(assetPath => assets.get(assetPath)?.canonicalPath)
      .filter(Boolean)
  )).length
  const previewContent = replaceMarkdownImageUrls(content, (originalUrl) => {
    const candidates = resolveImportPathCandidates(markdownDirs, originalUrl)
    for (const candidate of candidates) {
      const asset = assets.get(candidate)
      if (asset?.previewUrl) {
        return asset.previewUrl
      }
    }
    return originalUrl
  })

  return {
    markdownPaths,
    markdownDirs,
    assets,
    previewAssets,
    missingImagePaths,
    resolvedImageCount,
    content,
    previewContent
  }
}

async function uploadImageFile(file) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch('/api/upload/image', {
    method: 'POST',
    headers: {
      Authorization: userStore.token ? `Bearer ${userStore.token}` : ''
    },
    body: formData
  })

  const result = await response.json()
  if (result.code !== 200) {
    throw new Error(result.message || '图片上传失败')
  }

  return result.data
}

async function replaceLocalImagesBeforeSubmit(content) {
  if (!markdownImportContext.assets.size) {
    return content
  }

  const usedLocalAssets = Array.from(new Set(
    collectMarkdownImageUrls(content)
      .map(resolveMarkdownAsset)
      .filter(Boolean)
  ))

  const missingLocalImages = collectMarkdownImageUrls(content).filter(imgUrl => {
    return isLocalMarkdownPath(splitPathSuffix(imgUrl).pathname) && !resolveMarkdownAsset(imgUrl)
  })
  if (missingLocalImages.length) {
    throw new Error('检测到未导入的本地图片，请重新选择包含 Markdown 和图片资源的文件夹')
  }

  const uploadTasks = usedLocalAssets.map(async (asset) => {
    if (!asset || asset.uploadedUrl) {
      return
    }
    asset.uploadedUrl = await uploadImageFile(asset.file)
  })
  await Promise.all(uploadTasks)

  return replaceMarkdownImageUrls(content, (originalUrl) => {
    const asset = resolveMarkdownAsset(originalUrl)
    return asset?.uploadedUrl || originalUrl
  })
}

function syncMarkdownHeading(content, title) {
  const normalizedTitle = (title || '').trim()
  const { frontmatter, body } = splitMarkdownFrontmatter(content)
  const bodyContent = body.replace(/^\n+/, '')
  const headingLine = `# ${normalizedTitle}`

  let nextBody = ''
  if (!bodyContent) {
    nextBody = headingLine
  } else if (/^#\s+.+$/m.test(bodyContent) && bodyContent.match(/^#\s+.+$/m)?.index === 0) {
    nextBody = bodyContent.replace(/^#\s+.+$/, headingLine)
  } else {
    nextBody = `${headingLine}\n\n${bodyContent}`
  }

  return frontmatter ? `${frontmatter}\n\n${nextBody}` : nextBody
}

async function handleMarkdownImport(event) {
  const files = Array.from(event.target.files || [])
  if (!files.length) {
    return
  }

  try {
    const markdownFiles = files.filter(isMarkdownFile).sort(compareImportFilePath)
    const markdownFile = markdownFiles[0]

    if (!markdownFile) {
      ElMessage.warning('请选择包含 Markdown 文件的文件夹')
      return
    }

    if (form.value.content.trim()) {
      await ElMessageBox.confirm(
        '导入 Markdown 会覆盖当前正文内容，是否继续？',
        '导入确认',
        {
          confirmButtonText: '继续导入',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    }

    importingMarkdown.value = true
    resetMarkdownImportContext()
    markdownImportContext = await buildMarkdownImportContext(markdownFile, files)

    const fallbackTitle = getFileNameWithoutExtension(markdownFile.name)
    const articleTitle = form.value.title.trim() || extractMarkdownTitle(markdownImportContext.content, fallbackTitle)

    form.value.title = articleTitle
    form.value.content = syncMarkdownHeading(markdownImportContext.previewContent || markdownImportContext.content, articleTitle)

    if (markdownFiles.length > 1) {
      ElMessage.warning(`检测到多个 Markdown 文件，已导入 ${markdownFile.name}`)
    }

    if (markdownImportContext.missingImagePaths.length) {
      ElMessage.warning(`Markdown 已导入，未找到 ${markdownImportContext.missingImagePaths.length} 张本地图片，请确认选择的是包含 .assets 目录的文件夹`)
    } else if (markdownImportContext.resolvedImageCount) {
      ElMessage.success(`Markdown 已导入，已解析 ${markdownImportContext.resolvedImageCount} 张本地图片`)
    } else {
      ElMessage.success('Markdown 内容已导入')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || 'Markdown 导入失败')
    }
  } finally {
    importingMarkdown.value = false
    if (event.target) {
      event.target.value = ''
    }
  }
}

function hasFormContent() {
  return Boolean(
    form.value.title.trim()
    || form.value.categoryId
    || form.value.tagIds.length
    || form.value.cover
    || form.value.summary.trim()
    || form.value.content.trim()
  )
}

async function handleReset() {
  if (!hasFormContent()) {
    resetMarkdownImportContext()
    form.value = createEmptyForm()
    return
  }

  try {
    await ElMessageBox.confirm(
      '重置后将清空当前表单的标题、正文、分类、标签、封面和摘要，是否继续？',
      '重置确认',
      {
        confirmButtonText: '确认重置',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    resetMarkdownImportContext()
    form.value = createEmptyForm()
    ElMessage.success('表单内容已清空')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('重置失败')
    }
  }
}

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
    try {
      results.push(await uploadImageFile(file))
    } catch (error) {
      ElMessage.error(error.message || '图片上传失败')
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
    const content = await replaceLocalImagesBeforeSubmit(form.value.content)
    const data = {
      ...form.value,
      content,
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

onUnmounted(() => {
  resetMarkdownImportContext()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.write-page {
  min-height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
  padding: $spacing-lg;
  gap: $spacing-lg;
}

.write-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-lg;
  background: rgba(var(--bg-card-rgb), 0.86);
  border: 1px solid var(--border-color);
  border-radius: 26px;
  box-shadow: $shadow-md;
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  position: sticky;
  top: $spacing-lg;
  z-index: 100;
  transition: background-color 0.3s, border-color 0.3s, box-shadow 0.3s, transform 0.3s;

  &:hover {
    box-shadow: $shadow-lg;
  }
  
  .title-input {
    flex: 1;
    min-width: 0;
  }
  
  .header-actions {
    flex-shrink: 0;
  }
}

.title-input {
  flex: 1 1 0;
  
  :deep(.el-input__wrapper) {
    background: transparent !important;
    box-shadow: none !important;
    border: none !important;
    padding: 0;
  }
  
  :deep(.el-input__inner) {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-primary);
    
    &::placeholder {
      color: var(--text-muted);
    }
  }
}

.title-count {
  flex-shrink: 0;
  min-width: 56px;
  color: var(--text-muted);
  font-size: 13px;
  font-weight: 500;
  line-height: 1;
  text-align: right;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: $spacing-sm;

  :deep(.el-button) {
    height: 40px;
    padding: 0 14px;
    border-radius: 12px;
    font-size: 13px;
  }
}

.markdown-file-input {
  display: none;
}

/* 发布文章按钮 - 和写文章按钮一样的样式 */
.publish-btn {
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 16px;
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
}

.editor-container {
  flex: 1;
  min-width: 0;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
  
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

@media (max-width: 768px) {
  .write-page {
    padding: 12px;
    gap: 12px;
  }

  .write-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    padding: 12px;
    border-radius: 20px;
    top: 12px;
    
    .header-actions {
      justify-content: flex-end;
      flex-wrap: wrap;
    }
  }
  
  .title-input :deep(.el-input__inner) {
    font-size: 18px;
  }
}
</style>
