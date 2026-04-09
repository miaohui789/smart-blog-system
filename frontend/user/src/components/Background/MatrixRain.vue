<template>
  <div class="matrix-container">
    <div class="matrix-pattern">
      <div class="matrix-column" v-for="n in 25" :key="n"></div>
    </div>
    <div class="matrix-pattern">
      <div class="matrix-column" v-for="n in 25" :key="n"></div>
    </div>
    <div class="matrix-pattern">
      <div class="matrix-column" v-for="n in 25" :key="n"></div>
    </div>
  </div>
</template>

<style scoped>
.matrix-container {
  position: fixed;
  inset: 0;
  width: 100%;
  height: 100%;
  background: #000;
  display: flex;
  overflow: hidden;
  z-index: 0;
  pointer-events: none;
  will-change: transform;
}

.matrix-pattern {
  position: relative;
  width: 1000px;
  height: 100%;
  flex-shrink: 0;
}

.matrix-column {
  position: absolute;
  top: -100%;
  width: 20px;
  height: 100%;
  font-size: 14px;
  line-height: 16px;
  font-weight: 500;
  animation: fall linear infinite;
  white-space: nowrap;
  will-change: transform;
}

.matrix-column::before {
  content: "public class void if else for while return new this super extends implements interface abstract static final private protected try catch throw throws import package synchronized volatile transient native strictfp assert enum switch case break continue default do instanceof boolean byte char short int long float double String Object System out println";
  position: absolute;
  top: 0;
  left: 0;
  background: linear-gradient(to bottom, #ffffff 0%, #ffffff 5%, #00ff41 10%, #00ff41 20%, #00dd33 30%, #00bb22 40%, #009911 50%, #007700 60%, #005500 70%, #003300 80%, rgba(0, 255, 65, 0.5) 90%, transparent 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  writing-mode: vertical-lr;
  letter-spacing: 2px;
  text-rendering: optimizeLegibility;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.matrix-column:nth-child(1) { left: 8px; animation-delay: -3s; animation-duration: 5s; }
.matrix-column:nth-child(2) { left: 35px; animation-delay: -4s; animation-duration: 6s; }
.matrix-column:nth-child(3) { left: 72px; animation-delay: -2.5s; animation-duration: 4.5s; }
.matrix-column:nth-child(4) { left: 98px; animation-delay: -3.5s; animation-duration: 5.5s; }
.matrix-column:nth-child(5) { left: 143px; animation-delay: -2s; animation-duration: 5s; }
.matrix-column:nth-child(6) { left: 177px; animation-delay: -4.5s; animation-duration: 6.5s; }
.matrix-column:nth-child(7) { left: 215px; animation-delay: -3.2s; animation-duration: 4.8s; }
.matrix-column:nth-child(8) { left: 248px; animation-delay: -3.8s; animation-duration: 5.2s; }
.matrix-column:nth-child(9) { left: 291px; animation-delay: -4.2s; animation-duration: 5.8s; }
.matrix-column:nth-child(10) { left: 318px; animation-delay: -2.8s; animation-duration: 4.7s; }
.matrix-column:nth-child(11) { left: 362px; animation-delay: -4.8s; animation-duration: 6.2s; }
.matrix-column:nth-child(12) { left: 395px; animation-delay: -3.3s; animation-duration: 5.1s; }
.matrix-column:nth-child(13) { left: 437px; animation-delay: -4.1s; animation-duration: 5.6s; }
.matrix-column:nth-child(14) { left: 469px; animation-delay: -3.6s; animation-duration: 4.9s; }
.matrix-column:nth-child(15) { left: 512px; animation-delay: -4.7s; animation-duration: 6.1s; }
.matrix-column:nth-child(16) { left: 548px; animation-delay: -3.9s; animation-duration: 5.3s; }
.matrix-column:nth-child(17) { left: 583px; animation-delay: -4.3s; animation-duration: 5.7s; }
.matrix-column:nth-child(18) { left: 625px; animation-delay: -3.1s; animation-duration: 4.6s; }
.matrix-column:nth-child(19) { left: 657px; animation-delay: -4.9s; animation-duration: 6.3s; }
.matrix-column:nth-child(20) { left: 698px; animation-delay: -3.4s; animation-duration: 5.4s; }
.matrix-column:nth-child(21) { left: 733px; animation-delay: -2.7s; animation-duration: 4.4s; }
.matrix-column:nth-child(22) { left: 776px; animation-delay: -4.5s; animation-duration: 5.9s; }
.matrix-column:nth-child(23) { left: 809px; animation-delay: -3s; animation-duration: 5s; }
.matrix-column:nth-child(24) { left: 851px; animation-delay: -5s; animation-duration: 6.4s; }
.matrix-column:nth-child(25) { left: 888px; animation-delay: -2.6s; animation-duration: 4.3s; }

.matrix-column:nth-child(odd)::before {
  content: "public static void main String args System out println class extends implements interface abstract final private protected boolean int long double float char byte short if else for while return new this super try catch throw throws";
}

.matrix-column:nth-child(even)::before {
  content: "import package synchronized volatile transient native strictfp assert enum switch case break continue default do instanceof ArrayList HashMap HashSet LinkedList Vector Stack Queue Deque Collection List Set Map Iterator";
}

.matrix-column:nth-child(3n)::before {
  content: "Exception RuntimeException NullPointerException ArrayIndexOutOfBoundsException IllegalArgumentException IOException FileNotFoundException ClassNotFoundException Thread Runnable Callable Future ExecutorService";
}

.matrix-column:nth-child(4n)::before {
  content: "StringBuilder StringBuffer Integer Double Float Long Boolean Character Byte Short Math Random Scanner BufferedReader FileReader FileWriter PrintWriter InputStream OutputStream FileInputStream FileOutputStream";
}

.matrix-column:nth-child(5n)::before {
  content: "Override Deprecated SuppressWarnings FunctionalInterface Retention Target Documented Inherited SafeVarargs Native Transient Volatile Synchronized Static Final Abstract Public Private Protected Default";
}

@keyframes fall {
  0% {
    transform: translateY(-10%);
    opacity: 1;
  }
  100% {
    transform: translateY(200%);
    opacity: 0;
  }
}

@media (max-width: 768px) {
  .matrix-column {
    font-size: 12px;
    line-height: 14px;
    width: 16px;
  }
}

@media (max-width: 480px) {
  .matrix-column {
    font-size: 10px;
    line-height: 12px;
    width: 14px;
  }
}
</style>
