<template>
  <div class="category-page">
    <div class="page-header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="handleAdd">添加分类</el-button>
    </div>

    <el-table :data="list" v-loading="loading" row-key="id" default-expand-all>
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="icon" label="图标" width="100" />
      <el-table-column prop="articleCount" label="文章数" width="100" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '添加分类'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="父级分类">
          <el-select v-model="form.parentId" placeholder="无" clearable>
            <el-option v-for="item in list" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryList, createCategory, updateCategory, deleteCategory } from '@/api/category'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const form = ref({ id: null, name: '', parentId: null, icon: '', sort: 0 })
const rules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

async function fetchList() {
  loading.value = true
  try {
    const res = await getCategoryList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  form.value = { id: null, name: '', parentId: null, icon: '', sort: 0 }
  dialogVisible.value = true
}

function handleEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.value.id) {
    await updateCategory(form.value.id, form.value)
  } else {
    await createCategory(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该分类吗？', '提示', { type: 'warning' })
  await deleteCategory(id)
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
    color: $text-primary;
  }
}
</style>
