<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.redirect ? null : item.path">
      {{ item.meta?.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const breadcrumbs = computed(() => {
  return route.matched.filter(item => item.meta?.title)
})
</script>

<style lang="scss" scoped>
:deep(.el-breadcrumb__item) {
  .el-breadcrumb__inner {
    color: var(--text-muted);
    transition: color 0.2s;
    
    &.is-link:hover {
      color: var(--primary-color);
    }
  }
  
  &:last-child .el-breadcrumb__inner {
    color: var(--text-secondary);
  }
}

:deep(.el-breadcrumb__separator) {
  color: var(--text-disabled);
}
</style>
