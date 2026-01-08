<template>
  <div class="right-panel-container" :class="{ show: visible }">
    <div class="right-panel-background" @click="close"></div>
    <div class="right-panel">
      <div class="right-panel-header">
        <span>{{ title }}</span>
        <el-icon class="close-btn" @click="close"><Close /></el-icon>
      </div>
      <div class="right-panel-content">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Close } from '@element-plus/icons-vue'

defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '' }
})

const emit = defineEmits(['close'])

function close() {
  emit('close')
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.right-panel-container {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 1000;
  pointer-events: none;

  &.show {
    pointer-events: auto;

    .right-panel-background {
      opacity: 1;
    }

    .right-panel {
      transform: translateX(0);
    }
  }
}

.right-panel-background {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s;
}

.right-panel {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 300px;
  background: $bg-card;
  transform: translateX(100%);
  transition: transform 0.3s;
}

.right-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md;
  border-bottom: 1px solid $border-color;
  color: $text-primary;
}

.close-btn {
  cursor: pointer;

  &:hover {
    color: $primary-color;
  }
}

.right-panel-content {
  padding: $spacing-md;
}
</style>
