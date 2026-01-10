<template>
  <el-popover placement="bottom" :width="480" trigger="click" v-model:visible="popoverVisible">
    <template #reference>
      <el-input :model-value="modelValue" placeholder="点击选择图标" readonly class="icon-input">
        <template #prefix>
          <el-icon v-if="modelValue" class="selected-icon"><component :is="modelValue" /></el-icon>
          <span v-else class="placeholder-icon">📦</span>
        </template>
        <template #suffix>
          <el-icon v-if="modelValue" class="clear-icon" @click.stop="handleClear"><Close /></el-icon>
        </template>
      </el-input>
    </template>
    <div class="icon-select-panel">
      <div class="search-box">
        <el-input v-model="searchKey" placeholder="搜索图标名称" clearable size="small">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div class="icon-list">
        <div
          v-for="icon in filteredIcons"
          :key="icon"
          class="icon-item"
          :class="{ active: modelValue === icon }"
          :title="icon"
          @click="handleSelect(icon)"
        >
          <el-icon><component :is="icon" /></el-icon>
          <span class="icon-name">{{ icon }}</span>
        </div>
      </div>
      <div v-if="filteredIcons.length === 0" class="no-result">
        未找到匹配的图标
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, computed } from 'vue'
import * as Icons from '@element-plus/icons-vue'
import { Search, Close } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const popoverVisible = ref(false)
const searchKey = ref('')

const icons = Object.keys(Icons)

const filteredIcons = computed(() => {
  if (!searchKey.value) return icons
  const key = searchKey.value.toLowerCase()
  return icons.filter(icon => icon.toLowerCase().includes(key))
})

function handleSelect(icon) {
  emit('update:modelValue', icon)
  popoverVisible.value = false
  searchKey.value = ''
}

function handleClear() {
  emit('update:modelValue', '')
}
</script>

<style lang="scss" scoped>
.icon-input {
  cursor: pointer;
  
  :deep(.el-input__wrapper) {
    cursor: pointer;
  }
  
  .selected-icon {
    font-size: 18px;
    color: var(--primary-color);
  }
  
  .placeholder-icon {
    font-size: 14px;
    opacity: 0.5;
  }
  
  .clear-icon {
    cursor: pointer;
    color: var(--text-muted);
    
    &:hover {
      color: var(--text-primary);
    }
  }
}

.icon-select-panel {
  .search-box {
    margin-bottom: 12px;
  }
}

.icon-list {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 6px;
  max-height: 280px;
  overflow-y: auto;
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: var(--border-color);
    border-radius: 2px;
  }
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px 4px;
  cursor: pointer;
  border-radius: 6px;
  border: 1px solid transparent;
  transition: all 0.2s;
  
  .el-icon {
    font-size: 20px;
    margin-bottom: 4px;
  }
  
  .icon-name {
    font-size: 10px;
    color: var(--text-muted);
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &:hover {
    background: rgba(168, 85, 247, 0.08);
    border-color: rgba(168, 85, 247, 0.2);
  }
  
  &.active {
    background: rgba(168, 85, 247, 0.15);
    border-color: var(--primary-color);
    color: var(--primary-color);
    
    .icon-name {
      color: var(--primary-color);
    }
  }
}

.no-result {
  text-align: center;
  padding: 24px;
  color: var(--text-muted);
  font-size: 13px;
}
</style>
