<template>
  <div class="default-layout">
    <Header />
    <main class="main-content">
      <div class="container">
        <div class="content-wrapper">
          <div class="content-main">
            <router-view />
          </div>
          <aside class="content-sidebar">
            <Sidebar />
          </aside>
        </div>
      </div>
    </main>
    <Footer />
    
    <!-- 回到顶部按钮 -->
    <BackToTop />
  </div>
</template>

<script setup>
import Header from '@/components/Header/index.vue'
import Footer from '@/components/Footer/index.vue'
import Sidebar from '@/components/Sidebar/index.vue'
import BackToTop from '@/components/BackToTop/index.vue'
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.default-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  background: var(--bg-dark);
  transition: background-color 0.3s;
}

.main-content {
  flex: 1;
  padding: $spacing-xl 0;
  position: relative;
  z-index: 1;
}

.content-wrapper {
  display: flex;
  gap: $spacing-xl;
}

.content-main {
  flex: 1;
  min-width: 0;
}

.content-sidebar {
  width: 320px;
  flex-shrink: 0;

  @media (max-width: 992px) {
    display: none;
  }
}
</style>
