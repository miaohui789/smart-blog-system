<template>
  <MdEditor
    v-model="content"
    :theme="editorTheme"
    @onChange="handleChange"
    @onUploadImg="handleUploadImg"
  />
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { uploadImage } from '@/api/system'
import { useThemeStore } from '@/stores/theme'

const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])
const themeStore = useThemeStore()

const content = ref(props.modelValue)
const editorTheme = computed(() => themeStore.isDark ? 'dark' : 'light')

watch(() => props.modelValue, (val) => {
  content.value = val
})

function handleChange(val) {
  emit('update:modelValue', val)
}

async function handleUploadImg(files, callback) {
  const res = await Promise.all(
    files.map(file => {
      const formData = new FormData()
      formData.append('file', file)
      return uploadImage(formData)
    })
  )
  callback(res.map(item => item.data))
}
</script>
