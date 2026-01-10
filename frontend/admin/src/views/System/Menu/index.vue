<template>
  <div class="menu-page">
    <div class="page-header">
      <h2>菜单管理</h2>
      <el-button type="primary" @click="handleAdd">添加菜单</el-button>
    </div>

    <el-table :data="list" v-loading="loading" row-key="id" default-expand-all>
      <el-table-column prop="menuName" label="菜单名称" />
      <el-table-column prop="icon" label="图标" width="80" />
      <el-table-column prop="path" label="路由地址" />
      <el-table-column prop="component" label="组件路径" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <button class="action-btn edit-btn" @click="handleEdit(row)">编辑</button>
          <button class="action-btn delete-btn" @click="handleDelete(row.id)">删除</button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑菜单' : '添加菜单'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-radio-group v-model="form.menuType">
            <el-radio label="M">目录</el-radio>
            <el-radio label="C">菜单</el-radio>
            <el-radio label="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="父级菜单">
          <el-select v-model="form.parentId" placeholder="无" clearable>
            <el-option v-for="item in menuOptions" :key="item.id" :label="item.menuName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="路由地址">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input v-model="form.component" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.perms" />
        </el-form-item>
        <el-form-item label="图标">
          <IconSelect v-model="form.icon" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuList, createMenu, updateMenu, deleteMenu } from '@/api/menu'
import IconSelect from '@/components/IconSelect/index.vue'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const form = ref({
  id: null, menuName: '', menuType: 'C', parentId: null,
  path: '', component: '', perms: '', icon: '', sort: 0
})

const rules = { menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }] }

const menuOptions = computed(() => {
  const flatten = (items) => items.reduce((acc, item) => {
    acc.push(item)
    if (item.children) acc.push(...flatten(item.children))
    return acc
  }, [])
  return flatten(list.value)
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getMenuList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  form.value = { id: null, menuName: '', menuType: 'C', parentId: null, path: '', component: '', perms: '', icon: '', sort: 0 }
  dialogVisible.value = true
}

function handleEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.value.id) {
    await updateMenu(form.value.id, form.value)
  } else {
    await createMenu(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该菜单吗？', '提示', { type: 'warning' })
  await deleteMenu(id)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(fetchList)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.menu-page {
  padding: 20px;
  background: var(--bg-card);
  border-radius: $radius-lg;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;

  h2 {
    font-size: 20px;
    color: var(--text-primary);
    margin: 0;
  }
}

:deep(.el-table) {
  background: transparent;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: var(--bg-darker);
  --el-table-row-hover-bg-color: var(--bg-hover);
  --el-table-border-color: var(--border-color);
  --el-table-text-color: var(--text-primary);
  --el-table-header-text-color: var(--text-secondary);
}

:deep(.el-dialog) {
  --el-dialog-bg-color: var(--bg-card);
  --el-dialog-title-font-size: 18px;
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

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__inner) {
  background: var(--bg-darker);
  box-shadow: none;
  border: 1px solid var(--border-color);
  
  &:hover, &:focus {
    border-color: var(--primary-color);
  }
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  color: var(--text-primary);
  
  &::placeholder {
    color: var(--text-muted);
  }
}

:deep(.el-radio__label) {
  color: var(--text-primary);
}

:deep(.el-input-number) {
  .el-input__wrapper {
    background: var(--bg-darker);
  }
}
</style>
