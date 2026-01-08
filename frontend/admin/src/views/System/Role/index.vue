<template>
  <div class="role-page">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="handleAdd">添加角色</el-button>
    </div>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="roleName" label="角色名称" />
      <el-table-column prop="roleKey" label="角色标识" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="success" @click="handlePermission(row)">权限</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑角色' : '添加角色'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model="form.roleKey" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
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
import { getRoleList, createRole, updateRole, deleteRole } from '@/api/role'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const form = ref({ id: null, roleName: '', roleKey: '', sort: 0, remark: '' })
const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getRoleList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  form.value = { id: null, roleName: '', roleKey: '', sort: 0, remark: '' }
  dialogVisible.value = true
}

function handleEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.value.id) {
    await updateRole(form.value.id, form.value)
  } else {
    await createRole(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchList()
}

function handlePermission(row) {
  ElMessage.info('权限分配功能待实现')
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该角色吗？', '提示', { type: 'warning' })
  await deleteRole(id)
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
