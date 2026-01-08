<template>
  <div class="tag-cloud">
    <router-link
      v-for="tag in tags"
      :key="tag.id"
      :to="`/tag/${tag.id}`"
      class="tag-item"
      :style="getTagStyle(tag)"
    >
      {{ tag.name }}
    </router-link>
    <el-empty v-if="!tags.length" description="暂无标签" :image-size="40" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTagList } from '@/api/tag'

const tags = ref([])

const colors = [
  'rgba(59, 130, 246, 0.15)',
  'rgba(139, 92, 246, 0.15)',
  'rgba(34, 197, 94, 0.15)',
  'rgba(245, 158, 11, 0.15)',
  'rgba(239, 68, 68, 0.15)'
]

const textColors = [
  '#60a5fa',
  '#a78bfa',
  '#4ade80',
  '#fbbf24',
  '#f87171'
]

function getTagStyle(tag) {
  const index = tag.id % colors.length
  return {
    background: colors[index],
    color: textColors[index]
  }
}

onMounted(async () => {
  try {
    const res = await getTagList()
    tags.value = res.data || []
  } catch (e) {
    console.error(e)
  }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  border-radius: 20px;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s;
  border: 1px solid transparent;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  }
}
</style>
