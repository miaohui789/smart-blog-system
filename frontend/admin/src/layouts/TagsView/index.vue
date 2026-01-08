<template>
  <div class="tags-view">
    <router-link
      v-for="tag in tagsViewStore.visitedViews"
      :key="tag.path"
      :to="tag.path"
      class="tag-item"
      :class="{ active: isActive(tag) }"
    >
      {{ tag.title }}
      <el-icon v-if="!tag.meta?.affix" class="close-icon" @click.prevent.stop="closeTag(tag)">
        <Close />
      </el-icon>
    </router-link>
  </div>
</template>

<script setup>
import { watch } from 'vue'
import { useRoute } from 'vue-router'
import { Close } from '@element-plus/icons-vue'
import { useTagsViewStore } from '@/stores/tagsView'

const route = useRoute()
const tagsViewStore = useTagsViewStore()

function isActive(tag) {
  return tag.path === route.path
}

function closeTag(tag) {
  tagsViewStore.delView(tag)
}

watch(route, () => {
  tagsViewStore.addView(route)
}, { immediate: true })
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.tags-view {
  display: flex;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-lg;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  overflow-x: auto;
  transition: background-color 0.3s, border-color 0.3s;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 12px;
  color: var(--text-secondary);
  text-decoration: none;
  white-space: nowrap;
  transition: all 0.2s;

  &:hover {
    background: var(--bg-card-hover);
  }

  &.active {
    background: var(--primary-color);
    border-color: var(--primary-color);
    color: #fff;
  }
}

.close-icon {
  font-size: 12px;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: $error-color;
  }
}
</style>
