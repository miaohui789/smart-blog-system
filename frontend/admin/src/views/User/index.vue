<template>
  <div class="user-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加用户
      </el-button>
    </div>

    <div class="filter-card">
      <el-input v-model="query.keyword" placeholder="搜索用户名/昵称/邮箱" clearable style="width: 240px" @keyup.enter="fetchList">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="fetchList">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading">
        <el-table-column label="用户信息" min-width="240">
          <template #default="{ row }">
            <div class="user-info-cell">
              <el-avatar :src="row.avatar" :size="40">{{ row.nickname?.charAt(0) }}</el-avatar>
              <div class="user-detail">
                <span class="nickname">{{ row.nickname }}</span>
                <span class="username">@{{ row.username }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" style="margin-right: 4px">
              {{ role }}
            </el-tag>
            <span v-if="!row.roles?.length" class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '添加用户'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
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
import { Plus, Search } from '@element-plus/icons-vue'
import { getUserList, createUser, updateUser, deleteUser, updateUserStatus } from '@/api/user'
import { getRoleList } from '@/api/role'

const list = ref([])
const roles = ref([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const query = ref({ page: 1, pageSize: 10, keyword: '', status: null })

const form = ref({ id: null, username: '', nickname: '', email: '', password: '', roleIds: [] })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getUserList(query.value)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchRoles() {
  const res = await getRoleList()
  roles.value = res.data || []
}

function resetQuery() {
  query.value = { page: 1, pageSize: 10, keyword: '', status: null }
  fetchList()
}

function handleAdd() {
  form.value = { id: null, username: '', nickname: '', email: '', password: '', roleIds: [] }
  dialogVisible.value = true
}

function handleEdit(row) {
  form.value = { ...row, password: '', roleIds: row.roleIds || [] }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.value.id) {
      await updateUser(form.value.id, form.value)
    } else {
      await createUser(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}该用户吗？`, '提示', { type: 'warning' })
  await updateUserStatus(row.id, newStatus)
  ElMessage.success('操作成功')
  fetchList()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该用户吗？删除后不可恢复', '提示', { type: 'warning' })
  await deleteUser(id)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(() => {
  fetchList()
  fetchRoles()
})
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

.filter-card {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
  padding: $spacing-lg;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  margin-bottom: $spacing-lg;
  transition: background-color 0.3s, border-color 0.3s;
}

.table-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-lg;
  transition: background-color 0.3s, border-color 0.3s;
}

.user-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;
  
  .nickname {
    font-weight: 500;
    color: var(--text-primary);
  }
  
  .username {
    font-size: 12px;
    color: var(--text-muted);
  }
}

.text-muted {
  color: var(--text-muted);
}

:deep(.el-pagination) {
  margin-top: $spacing-lg;
  justify-content: flex-end;
}
</style>
