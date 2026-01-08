<template>
  <div class="loading-container">
    <div class="loading-spinner">
      <div class="spinner-ring"></div>
      <div class="spinner-ring"></div>
      <div class="spinner-ring"></div>
    </div>
    <p v-if="text" class="loading-text">{{ text }}</p>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: '加载中...' }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-2xl;
}

.loading-spinner {
  position: relative;
  width: 50px;
  height: 50px;
}

.spinner-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid transparent;
  border-radius: 50%;
  animation: spin 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;

  &:nth-child(1) {
    border-top-color: $primary-color;
    animation-delay: -0.45s;
  }

  &:nth-child(2) {
    border-top-color: #6366f1;
    animation-delay: -0.3s;
    width: 80%;
    height: 80%;
    top: 10%;
    left: 10%;
  }

  &:nth-child(3) {
    border-top-color: #3b82f6;
    animation-delay: -0.15s;
    width: 60%;
    height: 60%;
    top: 20%;
    left: 20%;
  }
}

.loading-text {
  margin-top: $spacing-lg;
  color: var(--text-muted);
  font-size: 14px;
  transition: color 0.3s;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
