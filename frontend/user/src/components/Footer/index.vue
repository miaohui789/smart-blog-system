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
        <router-link to="/version-history">版本历史</router-link>
        <a href="#">隐私政策</a>
        <a href="#" @click.prevent="showContactDialog = true">联系我们</a>
      </div>
    </div>

    <!-- 联系我们弹窗 -->
    <el-dialog
      v-model="showContactDialog"
      title="联系我们"
      width="500px"
      center
      class="contact-dialog"
    >
      <div class="contact-content">
        <div class="developers-section">
          <div class="developer-card">
            <h4>原作者</h4>
            <div class="qr-code-wrapper">
              <el-image 
                :src="authorImg" 
                :preview-src-list="[authorImg]" 
                :preview-teleported="true"
                fit="contain"
                alt="原作者" 
              />
            </div>
          </div>
          <div class="developer-card">
            <h4>最大贡献者</h4>
            <div class="qr-code-wrapper">
              <el-image 
                :src="contributorImg" 
                :preview-src-list="[contributorImg]" 
                :preview-teleported="true"
                fit="contain"
                alt="最大贡献者" 
              />
            </div>
          </div>
        </div>
        
        <div class="support-section">
          <el-button type="primary" @click="showSupport = !showSupport">
            支持一下作者 <el-icon class="el-icon--right"><ArrowDown v-if="!showSupport"/><ArrowUp v-else/></el-icon>
          </el-button>
          
          <el-collapse-transition>
            <div v-show="showSupport" class="support-qrs">
              <div class="qr-item">
                <el-image 
                  :src="support1Img" 
                  :preview-src-list="[support1Img]" 
                  :preview-teleported="true"
                  fit="contain"
                  alt="支持我们" 
                />
              </div>
              <div class="qr-item">
                <el-image 
                  :src="support2Img" 
                  :preview-src-list="[support2Img]" 
                  :preview-teleported="true"
                  fit="contain"
                  alt="支持我们" 
                />
              </div>
            </div>
          </el-collapse-transition>
        </div>
      </div>
    </el-dialog>
  </footer>
</template>

<script setup>
import { ref } from 'vue'
import { useConfigStore } from '@/stores/config'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'

import authorImg from '@/assets/images/contact/author.jpg'
import contributorImg from '@/assets/images/contact/contributor.jpg'
import support1Img from '@/assets/images/contact/support1.jpg'
import support2Img from '@/assets/images/contact/support2.jpg'

const configStore = useConfigStore()
const currentYear = new Date().getFullYear()

const showContactDialog = ref(false)
const showSupport = ref(false)
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

:deep(.contact-dialog) {
  .el-dialog__body {
    padding-top: 10px;
  }
}

.contact-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 10px;

  .developers-section {
    display: flex;
    justify-content: space-around;
    gap: 20px;

    .developer-card {
      text-align: center;
      flex: 1;

      h4 {
        margin: 0 0 12px;
        color: var(--text-primary);
        font-size: 16px;
      }

      .qr-code-wrapper {
        width: 100%;
        max-width: 180px;
        margin: 0 auto;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        border: 1px solid var(--border-color);

        :deep(.el-image) {
          width: 100%;
          height: auto;
          display: block;
        }
      }
    }
  }

  .support-section {
    text-align: center;
    border-top: 1px dashed var(--border-color);
    padding-top: 24px;

    .support-qrs {
      display: flex;
      justify-content: space-around;
      gap: 20px;
      margin-top: 20px;

      .qr-item {
        flex: 1;
        max-width: 180px;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        border: 1px solid var(--border-color);

        :deep(.el-image) {
          width: 100%;
          height: auto;
          display: block;
        }
      }
    }
  }
}
</style>
