<template>
  <div class="profile-page">
    <div class="page-header">
      <h2>个人中心</h2>
    </div>

    <div class="profile-content">
      <!-- 左侧：头像和基本信息 -->
      <div class="profile-card">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-avatar :size="100" :src="userInfo.avatar">
              {{ userInfo.nickname?.charAt(0) || 'A' }}
            </el-avatar>
            <div class="avatar-upload" @click="triggerUpload">
              <el-icon><Camera /></el-icon>
            </div>
            <input ref="fileInput" type="file" accept="image/*" hidden @change="handleAvatarChange" />
          </div>
          <h3 class="nickname">{{ userInfo.nickname || '管理员' }}</h3>
          <p class="username">@{{ userInfo.username }}</p>
          <p class="role">
            <el-tag v-for="role in userInfo.roles" :key="role" size="small" type="primary">{{ role }}</el-tag>
          </p>
        </div>
        <div class="info-section">
          <div class="info-item">
            <el-icon><Message /></el-icon>
            <span>{{ userInfo.email || '未设置邮箱' }}</span>
          </div>
          <div class="info-item">
            <el-icon><Calendar /></el-icon>
            <span>注册于 {{ userInfo.createTime || '-' }}</span>
          </div>
          <div class="info-item">
            <el-icon><Location /></el-icon>
            <span>最后登录 {{ userInfo.loginTime || '-' }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧：编辑表单 -->
      <div class="edit-card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="info">
            <el-form :model="infoForm" :rules="infoRules" ref="infoFormRef" label-width="80px">
              <el-form-item label="用户名">
                <el-input v-model="userInfo.username" disabled />
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="infoForm.nickname" placeholder="请输入昵称" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="infoForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="个人简介">
                <el-input v-model="infoForm.intro" type="textarea" :rows="3" placeholder="介绍一下自己吧" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUpdateInfo" :loading="infoLoading">保存修改</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="修改密码" name="password">
            <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="80px">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUpdatePassword" :loading="pwdLoading">修改密码</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera, Message, Calendar, Location } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getProfile, updateProfile, updatePassword, uploadAvatar, updateAvatar } from '@/api/profile'

const userStore = useUserStore()
const activeTab = ref('info')
const fileInput = ref(null)
const infoFormRef = ref(null)
const pwdFormRef = ref(null)
const infoLoading = ref(false)
const pwdLoading = ref(false)

const userInfo = ref({
  username: '',
  nickname: '',
  email: '',
  avatar: '',
  intro: '',
  roles: [],
  createTime: '',
  loginTime: ''
})

const infoForm = reactive({
  nickname: '',
  email: '',
  intro: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const infoRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function fetchProfile() {
  try {
    const res = await getProfile()
    if (res.data) {
      userInfo.value = res.data
      infoForm.nickname = res.data.nickname || ''
      infoForm.email = res.data.email || ''
      infoForm.intro = res.data.intro || ''
    }
  } catch (e) {
    console.error('获取个人信息失败:', e)
  }
}

function triggerUpload() {
  fileInput.value?.click()
}

async function handleAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }
  
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const res = await uploadAvatar(formData)
    if (res.data) {
      // 上传成功后，调用接口保存头像URL到数据库
      await updateAvatar(res.data)
      userInfo.value.avatar = res.data
      userStore.updateAvatar(res.data)
      ElMessage.success('头像更新成功')
    }
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
  
  e.target.value = ''
}

async function handleUpdateInfo() {
  await infoFormRef.value.validate()
  infoLoading.value = true
  try {
    await updateProfile(infoForm)
    userInfo.value.nickname = infoForm.nickname
    userInfo.value.email = infoForm.email
    userInfo.value.intro = infoForm.intro
    userStore.updateNickname(infoForm.nickname)
    ElMessage.success('信息更新成功')
  } catch (e) {
    ElMessage.error('更新失败')
  } finally {
    infoLoading.value = false
  }
}

async function handleUpdatePassword() {
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await updatePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '密码修改失败')
  } finally {
    pwdLoading.value = false
  }
}

onMounted(fetchProfile)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.profile-page {
  padding: 20px;
}

.page-header {
  margin-bottom: $spacing-lg;

  h2 {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0;
  }
}

.profile-content {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: $spacing-lg;
  
  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

.profile-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-xl;
}

.avatar-section {
  text-align: center;
  padding-bottom: $spacing-lg;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: $spacing-lg;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: $spacing-md;
  
  .el-avatar {
    border: 3px solid var(--border-color);
  }
  
  .avatar-upload {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 32px;
    height: 32px;
    background: var(--primary-color);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s;
    
    .el-icon {
      color: #fff;
      font-size: 16px;
    }
    
    &:hover {
      transform: scale(1.1);
      background: var(--primary-dark);
    }
  }
}

.nickname {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px 0;
}

.username {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0 0 $spacing-sm 0;
}

.role {
  margin: 0;
  
  .el-tag {
    margin: 0 4px;
  }
}

.info-section {
  .info-item {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-sm 0;
    color: var(--text-secondary);
    font-size: 14px;
    
    .el-icon {
      color: var(--text-muted);
      font-size: 16px;
    }
  }
}

.edit-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-xl;
}

:deep(.el-tabs__item) {
  color: var(--text-secondary);
  
  &.is-active {
    color: var(--primary-color);
  }
  
  &:hover {
    color: var(--primary-color);
  }
}

:deep(.el-tabs__active-bar) {
  background-color: var(--primary-color);
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: var(--border-color);
}

:deep(.el-form) {
  max-width: 500px;
  margin-top: $spacing-lg;
}

:deep(.el-form-item__label) {
  color: var(--text-secondary);
}

:deep(.el-input__wrapper),
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

:deep(.el-input.is-disabled .el-input__wrapper) {
  background: var(--bg-darker);
  opacity: 0.6;
}
</style>
