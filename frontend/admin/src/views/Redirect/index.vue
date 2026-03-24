<template>
  <div></div>
</template>

<script setup>
import { onBeforeMount } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

onBeforeMount(() => {
  const { path } = route.params
  const targetPath = Array.isArray(path) ? '/' + path.join('/') : '/' + path
  router.replace(targetPath)
})
</script>
