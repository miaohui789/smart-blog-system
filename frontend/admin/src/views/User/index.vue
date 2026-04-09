<template>
  <div class="user-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="handleAdd" :disabled="!canManageUsers">
        <el-icon><Plus /></el-icon>
        添加用户
      </el-button>
    </div>

    <!-- 无权限提示 -->
    <el-alert
      v-if="!canManageUsers"
      title="您当前角色没有用户管理权限，仅可查看用户列表"
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

    <div class="filter-card">
      <el-input v-model="query.keyword" placeholder="搜索用户名/昵称/邮箱" clearable style="width: 240px" @keyup.enter="fetchList">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
        <el-option label="已注销" :value="2" />
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
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column label="用户等级" min-width="180">
          <template #default="{ row }">
            <UserLevelBadge :level="row.userLevel || 1" />
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="120">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" :type="getRoleTagType(role)" style="margin-right: 4px; margin-bottom: 4px; white-space: nowrap">
              {{ role }}
            </el-tag>
            <span v-if="!row.roles?.length" class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag v-if="row.status === 2" type="info" size="small">已注销</el-tag>
            <el-tag v-else :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status !== 2">
              <div class="action-cell">
                <button 
                  class="user-action-btn user-edit-btn" 
                  :class="{ disabled: !canManageUsers }"
                  :disabled="!canManageUsers"
                  @click="handleEdit(row)"
                >编辑</button>
                <button 
                  v-if="row.status === 1" 
                  class="user-action-btn user-disable-btn"
                  :class="{ disabled: !canManageUsers }"
                  :disabled="!canManageUsers"
                  @click="handleToggleStatus(row)"
                >禁用</button>
                <button 
                  v-else 
                  class="user-action-btn user-enable-btn"
                  :class="{ disabled: !canManageUsers }"
                  :disabled="!canManageUsers"
                  @click="handleToggleStatus(row)"
                >启用</button>
                <button 
                  class="user-action-btn user-cancel-btn"
                  :class="{ disabled: !canManageUsers }"
                  :disabled="!canManageUsers"
                  @click="handleCancel(row.id)"
                >注销</button>
              </div>
            </template>
            <template v-else>
              <span class="cancelled-text">该用户已注销</span>
            </template>
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
        <el-form-item label="等级">
          <el-input-number v-model="form.userLevel" :min="1" :max="100" style="width: 100%" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getUserList, createUser, updateUser, deleteUser, updateUserStatus, cancelUser } from '@/api/user'
import { getAllRoles } from '@/api/role'
import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@/utils/format'
import UserLevelBadge from '@/components/UserLevelBadge/index.vue'

const userStore = useUserStore()

// 判断是否有用户管理权限
// 超级管理员有所有权限，内容编辑只能管理内容
const canManageUsers = computed(() => {
  const roles = userStore.roles || []
  const permissions = userStore.permissions || []
  
  // 检查是否有全部权限
  if (permissions.includes('*:*:*')) {
    return true
  }
  
  // 检查角色
  return roles.some(role => 
    role === '超级管理员' || 
    role === 'admin' || 
    role === 'ADMIN' ||
    role === 'super_admin'
  )
})

// 根据角色返回不同的标签颜色
function getRoleTagType(role) {
  if (role === '超级管理员' || role === 'admin') return 'danger'
  if (role === '内容编辑' || role === 'editor') return 'warning'
  if (role === '普通用户' || role === 'user') return 'info'
  return ''
}

const list = ref([])
const roles = ref([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const query = ref({ page: 1, pageSize: 10, keyword: '', status: null })

const form = ref({ id: null, username: '', nickname: '', email: '', password: '', userLevel: 1, roleIds: [] })
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
  const res = await getAllRoles()
  roles.value = res.data || []
}

function resetQuery() {
  query.value = { page: 1, pageSize: 10, keyword: '', status: null }
  fetchList()
}

function handleAdd() {
  form.value = { id: null, username: '', nickname: '', email: '', password: '', userLevel: 1, roleIds: [] }
  dialogVisible.value = true
}

function handleEdit(row) {
  form.value = { ...row, password: '', userLevel: row.userLevel || 1, roleIds: row.roleIds || [] }
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

async function handleCancel(id) {
  await ElMessageBox.confirm(
    '确定注销该用户吗？注销后用户将无法登录，但其发布的文章会保留，用户主页将显示"该用户已注销"', 
    '注销用户', 
    { type: 'warning', confirmButtonText: '确定注销', cancelButtonText: '取消' }
  )
  await cancelUser(id)
  ElMessage.success('用户已注销')
  fetchList()
}

onMounted(() => {
  fetchList()
  fetchRoles()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.user-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;

  h2 {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0;
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
}

.table-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-lg;
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

/* 操作按钮样式 */
.action-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.user-action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  
  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
    pointer-events: none;
  }
}

.user-edit-btn {
  background: rgba(59, 130, 246, 0.2);
  color: #3b82f6;
  
  &:hover:not(.disabled) {
    background: rgba(59, 130, 246, 0.3);
  }
}

.user-disable-btn {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
  
  &:hover:not(.disabled) {
    background: rgba(245, 158, 11, 0.3);
  }
}

.user-enable-btn {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
  
  &:hover:not(.disabled) {
    background: rgba(34, 197, 94, 0.3);
  }
}

.user-cancel-btn {
  background: rgba(107, 114, 128, 0.2);
  color: #6b7280;
  
  &:hover:not(.disabled) {
    background: rgba(107, 114, 128, 0.3);
  }
}

.cancelled-text {
  color: var(--text-muted);
  font-size: 12px;
  font-style: italic;
}

:deep(.el-table) {
  background: var(--bg-card);
  --el-table-bg-color: var(--bg-card);
  --el-table-tr-bg-color: var(--bg-card);
  --el-table-header-bg-color: var(--bg-darker);
  --el-table-row-hover-bg-color: var(--bg-hover);
  --el-table-border-color: var(--border-color);
  --el-table-text-color: var(--text-primary);
  --el-table-header-text-color: var(--text-secondary);
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  background: var(--bg-darker);
  box-shadow: none;
  border: 1px solid var(--border-color);
  
  &:hover, &:focus {
    border-color: var(--primary-color);
  }
}

:deep(.el-input__inner) {
  color: var(--text-primary);
  
  &::placeholder {
    color: var(--text-muted);
  }
}

:deep(.el-dialog) {
  --el-dialog-bg-color: var(--bg-card);
  border-radius: $radius-lg;
  
  .el-dialog__header {
    border-bottom: 1px solid var(--border-color);
    padding: 16px 20px;
  }
  
  .el-dialog__title {
    color: var(--text-primary);
  }
  
  .el-dialog__body {
    padding: 20px;
  }
}

:deep(.el-form-item__label) {
  color: var(--text-secondary);
}

:deep(.el-pagination) {
  margin-top: $spacing-lg;
  justify-content: flex-end;
  
  .el-pagination__total,
  .el-pagination__jump {
    color: var(--text-secondary);
  }
  
  .el-pager li {
    background: var(--bg-darker);
    color: var(--text-primary);
    
    &.is-active {
      background: var(--primary-color);
      color: #fff;
    }
  }
  
  .btn-prev, .btn-next {
    background: var(--bg-darker);
    color: var(--text-primary);
  }
}
</style>
