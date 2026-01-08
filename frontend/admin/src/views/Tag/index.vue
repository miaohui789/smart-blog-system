<template>
  <div class="tag-page">
    <div class="page-header">
      <h2>标签管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加标签
      </el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading">
        <el-table-column label="标签" min-width="200">
          <template #default="{ row }">
            <el-tag :color="row.color" effect="dark" class="tag-preview">
              {{ row.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="颜色" width="120">
          <template #default="{ row }">
            <div class="color-info">
              <span class="color-dot" :style="{ background: row.color }"></span>
              <span class="color-value">{{ row.color }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="articleCount" label="文章数" width="100">
          <template #default="{ row }">
            <span>{{ row.articleCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑标签' : '添加标签'" width="400px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="颜色">
          <div class="color-picker-row">
            <el-color-picker v-model="form.color" />
            <span class="color-text">{{ form.color }}</span>
          </div>
        </el-form-item>
        <el-form-item label="预览">
          <el-tag :color="form.color" effect="dark">{{ form.name || '标签预览' }}</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getTagList, createTag, updateTag, deleteTag } from '@/api/tag'

const list = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const form = ref({ id: null, name: '', color: '#a855f7' })
const rules = { name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }] }

async function fetchList() {
  loading.value = true
  try {
    const res = await getTagList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  form.value = { id: null, name: '', color: '#a855f7' }
  dialogVisible.value = true
}

function handleEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.value.id) {
      await updateTag(form.value.id, form.value)
    } else {
      await createTag(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该标签吗？', '提示', { type: 'warning' })
  await deleteTag(id)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(fetchList)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;

  h2 {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-primary);
    transition: color 0.3s;
  }
}

.table-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-lg;
  transition: background-color 0.3s, border-color 0.3s;
}

.tag-preview {
  border: none;
}

.color-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-dot {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  flex-shrink: 0;
}

.color-value {
  font-size: 12px;
  color: var(--text-muted);
  font-family: monospace;
}

.color-picker-row {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .color-text {
    font-size: 13px;
    color: var(--text-muted);
    font-family: monospace;
  }
}
</style>
