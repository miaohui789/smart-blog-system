<template>
  <div class="study-category-page">
    <div class="page-header">
      <div>
        <h2>学习分类</h2>
        <p>维护学习模块的树形分类结构，支撑题库学习与专项抽查。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新增分类
      </el-button>
    </div>

    <div class="table-card">
      <el-table :data="categoryTree" row-key="id" default-expand-all v-loading="loading">
        <el-table-column prop="categoryName" label="分类名称" min-width="240" />
        <el-table-column prop="categoryCode" label="分类编码" min-width="160" />
        <el-table-column prop="categoryLevel" label="层级" width="90" align="center" />
        <el-table-column prop="questionCount" label="题目数" width="90" align="center" />
        <el-table-column prop="sortOrder" label="排序" width="90" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增分类' : '编辑分类'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="上级分类">
          <el-select v-model="form.parentId" clearable placeholder="顶级分类">
            <el-option v-for="item in flatCategories" :key="item.id" :label="item.label" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称">
          <el-input v-model="form.categoryName" maxlength="100" />
        </el-form-item>
        <el-form-item label="分类编码">
          <el-input v-model="form.categoryCode" maxlength="100" />
        </el-form-item>
        <el-form-item label="分类描述">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createAdminStudyCategory, getAdminStudyCategoryList, updateAdminStudyCategory } from '@/api/study'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const categoryTree = ref([])
const form = reactive({
  parentId: null,
  categoryName: '',
  categoryCode: '',
  description: '',
  sortOrder: 0,
  status: 1
})

const flatCategories = computed(() => {
  const result = []
  const walk = (list = [], level = 0) => {
    list.forEach(item => {
      result.push({ id: item.id, label: `${'　'.repeat(level)}${item.categoryName}` })
      if (item.children?.length) walk(item.children, level + 1)
    })
  }
  walk(categoryTree.value)
  return result
})

function resetForm() {
  form.parentId = null
  form.categoryName = ''
  form.categoryCode = ''
  form.description = ''
  form.sortOrder = 0
  form.status = 1
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getAdminStudyCategoryList({ includeDisabled: true })
    categoryTree.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  dialogMode.value = 'create'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  form.parentId = row.parentId && row.parentId !== 0 ? row.parentId : null
  form.categoryName = row.categoryName
  form.categoryCode = row.categoryCode
  form.description = row.description
  form.sortOrder = row.sortOrder || 0
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.categoryName.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  submitting.value = true
  try {
    const payload = { ...form }
    if (dialogMode.value === 'create') {
      await createAdminStudyCategory(payload)
      ElMessage.success('分类已创建')
    } else {
      await updateAdminStudyCategory(editingId.value, payload)
      ElMessage.success('分类已更新')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

onMounted(fetchList)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.study-category-page { padding: 20px; }
.page-header {
  display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 20px;
  h2 { color: var(--text-primary); font-size: 20px; margin-bottom: 6px; }
  p { color: var(--text-muted); font-size: 13px; }
}
.table-card {
  background: var(--bg-card); border: 1px solid var(--border-color); border-radius: $radius-lg; padding: 20px;
}
</style>
