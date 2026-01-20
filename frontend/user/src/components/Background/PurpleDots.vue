<template>
  <div class="purple-dots-container">
    <div class="dots-overlay"></div>
  </div>
</template>

<style scoped>
.purple-dots-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 0;
  pointer-events: none;
  --color: #c09bd8;
  background-color: #9f84bd;
}

.dots-overlay {
  position: absolute;
  inset: 0;
  opacity: 0.8;
  background: 
    radial-gradient(circle, var(--color) 15%, transparent 15%),
    radial-gradient(circle, var(--color) 15%, transparent 15%) 6px -6px,
    radial-gradient(circle, var(--color) 15%, transparent 15%) 6px -1px,
    radial-gradient(circle, var(--color) 15%, transparent 15%) 10px 2px,
    radial-gradient(circle, var(--color) 15%, transparent 15%) 14px 5px,
    radial-gradient(circle, var(--color) 15%, transparent 15%) 18px 8px,
    radial-gradient(circle, var(--color) 15%, transparent 15%) 18px 12px,
    radial-gradient(circle, var(--color) 15%, transparent 15%) 23px 6px;
  background-size: 50px 50px;
}
</style>
