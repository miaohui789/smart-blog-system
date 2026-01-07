<template>
  <footer class="footer">
    <div class="container footer-content">
      <div class="footer-brand">
        <span class="logo-icon">✦</span>
        <span class="logo-text">My Blog</span>
      </div>
      <p class="copyright">© {{ currentYear }} My Blog. All rights reserved.</p>
      <div class="footer-links">
        <router-link to="/about">关于</router-link>
        <a href="#">隐私政策</a>
        <a href="#">联系我们</a>
      </div>
    </div>
  </footer>
</template>

<script setup>
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

.logo-icon {
  font-size: 18px;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.copyright {
  color: var(--text-muted);
  font-size: 14px;
  transition: color 0.3s;
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
