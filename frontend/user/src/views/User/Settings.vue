<template>
  <div class="settings-page">
    <h1 class="page-title">账号设置</h1>
    <div class="settings-card glass-card">
      <h3 class="card-title">
        <el-icon><Lock /></el-icon>
        修改密码
      </h3>
      <el-form :model="passwordForm" :rules="rules" ref="formRef" label-width="100px" label-position="top">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleChangePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { updatePassword } from '@/api/user'

const formRef = ref()
const loading = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleChangePassword() {
  await formRef.value.validate()
  loading.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.settings-page {
  max-width: 500px;
}

.page-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: $spacing-xl;
  transition: color 0.3s;
}

.settings-card {
  padding: $spacing-xl;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: background-color 0.3s, border-color 0.3s;
}

.card-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: $spacing-xl;
  padding-bottom: $spacing-md;
  border-bottom: 1px solid var(--border-color);
  transition: color 0.3s, border-color 0.3s;

  .el-icon {
    color: $primary-color;
  }
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-primary) !important;
}
</style>
