<template>
  <el-upload
    class="image-uploader"
    :action="uploadUrl"
    :show-file-list="false"
    :on-success="handleSuccess"
    :before-upload="beforeUpload"
  >
    <img v-if="imageUrl" :src="imageUrl" class="image" />
    <el-icon v-else class="uploader-icon"><Plus /></el-icon>
  </el-upload>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const uploadUrl = '/api/admin/upload/image'
const imageUrl = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  imageUrl.value = val
})

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt2M) ElMessage.error('图片大小不能超过 2MB')
  return isImage && isLt2M
}

function handleSuccess(res) {
  if (res.code === 200) {
    imageUrl.value = res.data
    emit('update:modelValue', res.data)
  }
}
</script>

<style lang="scss" scoped>
.image-uploader {
  :deep(.el-upload) {
    border: 1px dashed #333;
    border-radius: 8px;
    cursor: pointer;
    width: 150px;
    height: 150px;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      border-color: #a855f7;
    }
  }
}

.image {
  width: 150px;
  height: 150px;
  object-fit: cover;
}

.uploader-icon {
  font-size: 28px;
  color: #666;
}
</style>
