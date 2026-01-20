<template>
  <div class="moving-dots-container">
    <div class="dots-pattern"></div>
  </div>
</template>

<style scoped>
.moving-dots-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: lightblue;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.dots-pattern {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: 
    radial-gradient(circle, #3498db 10%, transparent 20%),
    radial-gradient(circle, transparent 10%, #3498db 20%);
  background-size: 30px 30px;
  animation: moveBackground 8s linear infinite;
}

@keyframes moveBackground {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(20%, 20%);
  }
}
</style>
