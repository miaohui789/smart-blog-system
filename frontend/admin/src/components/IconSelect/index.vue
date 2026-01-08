<template>
  <el-popover placement="bottom" :width="400" trigger="click">
    <template #reference>
      <el-input v-model="modelValue" placeholder="点击选择图标" readonly>
        <template #prefix>
          <el-icon v-if="modelValue"><component :is="modelValue" /></el-icon>
        </template>
      </el-input>
    </template>
    <div class="icon-list">
      <div
        v-for="icon in icons"
        :key="icon"
        class="icon-item"
        :class="{ active: modelValue === icon }"
        @click="handleSelect(icon)"
      >
        <el-icon><component :is="icon" /></el-icon>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import * as Icons from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const icons = Object.keys(Icons)

function handleSelect(icon) {
  emit('update:modelValue', icon)
}
</script>

<style lang="scss" scoped>
.icon-list {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
  max-height: 300px;
  overflow-y: auto;
}

.icon-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  cursor: pointer;
  border-radius: 4px;

  &:hover,
  &.active {
    background: rgba(168, 85, 247, 0.1);
    color: #a855f7;
  }
}
</style>
