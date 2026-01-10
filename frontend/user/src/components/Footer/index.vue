<template>
  <footer class="footer">
    <div class="container footer-content">
      <div class="footer-brand">
        <img v-if="configStore.siteLogo" :src="configStore.siteLogo" alt="logo" class="logo-img" />
        <span v-else class="logo-icon">✦</span>
        <span class="logo-text">{{ configStore.siteName }}</span>
      </div>
      <div class="footer-info">
        <p class="copyright">{{ configStore.siteFooter || `© ${currentYear} ${configStore.siteName}. All rights reserved.` }}</p>
        <p v-if="configStore.icp" class="icp">
          <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer">{{ configStore.icp }}</a>
        </p>
      </div>
      <div class="footer-links">
        <router-link to="/about">关于</router-link>
        <a href="#">隐私政策</a>
        <a href="#">联系我们</a>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { useConfigStore } from '@/stores/config'

const configStore = useConfigStore()
const currentYear = new Date().getFullYear()
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.footer {
  background: var(--bg-card);
  border-top: 1px solid var(--border-color);
  padding: $spacing-xl 0;
  margin-top: auto;
  transition: background-color 0.3s, border-color 0.3s;
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: space-between;

  @media (max-width: 768px) {
    flex-direction: column;
    gap: $spacing-md;
    text-align: center;
  }
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.logo-img {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.logo-icon {
  font-size: 18px;
  color: $primary-color;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.footer-info {
  text-align: center;
}

.copyright {
  color: var(--text-muted);
  font-size: 14px;
  transition: color 0.3s;
}

.icp {
  margin-top: 4px;
  font-size: 13px;
  
  a {
    color: var(--text-muted);
    text-decoration: none;
    transition: color 0.3s;
    
    &:hover {
      color: $primary-color;
    }
  }
}

.footer-links {
  display: flex;
  gap: $spacing-lg;

  a {
    color: var(--text-secondary);
    font-size: 14px;
    text-decoration: none;
    transition: color 0.3s;

    &:hover {
      color: $primary-light;
    }
  }
}
</style>
