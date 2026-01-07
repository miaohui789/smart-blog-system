<template>
  <div class="article-create">
    <h2 class="page-title">创建文章</h2>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入文章标题" />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类">
          <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="标签">
        <el-select v-model="form.tagIds" multiple placeholder="请选择标签">
          <el-option v-for="item in tags" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="封面">
        <ImageUpload v-model="form.cover" />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="请输入文章摘要" />
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <Editor v-model="form.content" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleSave(0)">保存草稿</el-button>
        <el-button type="primary" @click="handleSave(1)">发布文章</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createArticle } from '@/api/article'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'
import ImageUpload from '@/components/ImageUpload/index.vue'
import Editor from '@/components/Editor/index.vue'

const router = useRouter()
const formRef = ref()
const categories = ref([])
const tags = ref([])

const form = ref({
  title: '',
  categoryId: null,
  tagIds: [],
  cover: '',
  summary: '',
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

async function handleSave(status) {
  await formRef.value.validate()
  await createArticle({ ...form.value, status })
  ElMessage.success(status === 1 ? '发布成功' : '保存成功')
  router.push('/article/list')
}

onMounted(async () => {
  const [catRes, tagRes] = await Promise.all([getCategoryList(), getTagList()])
  categories.value = catRes.data || []
  tags.value = tagRes.data || []
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.page-title {
  font-size: 20px;
  color: $text-primary;
  margin-bottom: $spacing-xl;
}

:deep(.el-form) {
  max-width: 900px;
}
</style>
