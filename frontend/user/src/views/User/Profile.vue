<template>
  <div class="profile-page">
    <h1 class="page-title">个人资料</h1>
    <div class="profile-form glass-card">
      <el-form :model="form" label-width="100px" label-position="top">
        <el-form-item label="头像">
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
              :before-upload="beforeAvatarUpload"
              accept="image/*"
            >
              <div class="avatar-wrapper">
                <el-avatar :size="100" :src="form.avatar">
                  {{ form.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <div class="avatar-overlay">
                  <el-icon><Camera /></el-icon>
                  <span>更换头像</span>
                </div>
              </div>
            </el-upload>
            <p class="avatar-tip">支持 JPG、PNG 格式，文件小于 2MB</p>
          </div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="form.intro" type="textarea" :rows="4" placeholder="介绍一下自己吧" />
        </el-form-item>
        <el-form-item label="个人网站">
          <el-input v-model="form.website" placeholder="https://" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSubmit">
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { getProfile, updateProfile } from '@/api/user'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const form = ref({
  nickname: '',
  email: '',
  intro: '',
  website: '',
  avatar: ''
})

const saving = ref(false)

const uploadUrl = computed(() => {
  return import.meta.env.VITE_APP_BASE_API + '/user/avatar'
})

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

async function fetchProfile() {
  try {
    const res = await getProfile()
    Object.assign(form.value, res.data)
  } catch (e) {
    console.error(e)
  }
}

function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

function handleAvatarSuccess(response) {
  if (response.code === 200) {
    form.value.avatar = response.data.url
    // 同步更新 store 中的用户信息
    userStore.updateUserInfo({ avatar: response.data.url })
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

function handleAvatarError() {
  ElMessage.error('头像上传失败，请重试')
}

async function handleSubmit() {
  saving.value = true
  try {
    await updateProfile(form.value)
    // 同步更新 store 中的用户信息
    userStore.updateUserInfo(form.value)
    ElMessage.success('保存成功')
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

onMounted(fetchProfile)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.profile-page {
  max-width: 600px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: $spacing-xl;
}

.profile-form {
  padding: $spacing-xl;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: $spacing-sm;
}

.avatar-uploader {
  cursor: pointer;
}

.avatar-wrapper {
  position: relative;
  border-radius: 50%;
  overflow: hidden;

  &:hover .avatar-overlay {
    opacity: 1;
  }
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: white;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s;

  .el-icon {
    font-size: 24px;
  }
}

.avatar-tip {
  font-size: 12px;
  color: var(--text-muted);
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-primary) !important;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  box-shadow: none;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: var(--border-light);
  }
  
  &:focus,
  &.is-focus {
    border-color: $primary-color;
    box-shadow: 0 0 0 3px rgba($primary-color, 0.1);
  }
}

:deep(.el-button--primary) {
  background: $primary-color;
  border-color: $primary-color;
  border-radius: 10px;
  
  &:hover {
    background: $primary-dark;
    border-color: $primary-dark;
  }
}
</style>
