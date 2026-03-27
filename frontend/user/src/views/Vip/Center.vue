<template>
  <div class="vip-center">
    <div v-if="loading" class="loading">加载中...</div>
    
    <template v-else>
      <!-- 非VIP提示 -->
      <div v-if="!vipInfo.isVip" class="not-vip">
        <div class="not-vip-icon">👑</div>
        <h2>您还不是VIP会员</h2>
        <p>激活VIP，享受专属特权</p>
        <router-link to="/vip/activate" class="activate-link">立即激活</router-link>
      </div>

      <!-- VIP翻转卡片 -->
      <div v-else class="flip-card" :class="'level-' + vipInfo.level">
        <div class="flip-content">
          <!-- 卡片背面（默认显示） -->
          <div class="flip-back">
            <div class="back-glow"></div>
            <div class="back-inner">
              <div class="crown-icon">
                <svg viewBox="0 0 24 24" width="60" height="60">
                  <path fill="currentColor" d="M5 16L3 5l5.5 5L12 4l3.5 6L21 5l-2 11H5m14 3c0 .6-.4 1-1 1H6c-.6 0-1-.4-1-1v-1h14v1z"/>
                </svg>
              </div>
              <strong class="hover-text">悬停查看详情</strong>
            </div>
          </div>
          
          <!-- 卡片正面（悬停显示） -->
          <div class="flip-front">
            <div class="front-bg">
              <div class="circle c1"></div>
              <div class="circle c2"></div>
              <div class="circle c3"></div>
            </div>
            <div class="front-content">
              <div class="vip-badge-tag">{{ vipInfo.levelName }}</div>
              
              <div class="user-info">
                <img :src="userInfo?.avatar || defaultAvatar" class="avatar" />
                <div class="info">
                  <h3>{{ userInfo?.nickname || userInfo?.username }}</h3>
                  <p class="expire-time">
                    <span v-if="isExpiringSoon" class="warning">
                      <el-icon><WarningFilled /></el-icon>
                    </span>
                    到期：{{ formatDate(vipInfo.expireTime) }}
                  </p>
                </div>
              </div>

              <div class="stats-row">
                <div class="stat-item">
                  <el-icon class="stat-icon"><Download /></el-icon>
                  <span class="stat-value">{{ vipInfo.downloadCountToday }}/{{ vipInfo.downloadLimit === -1 ? '∞' : vipInfo.downloadLimit }}</span>
                  <span class="stat-label">今日下载</span>
                </div>
                <div class="stat-item">
                  <el-icon class="stat-icon"><Sunny /></el-icon>
                  <span class="stat-value">{{ vipInfo.heatCountToday }}/{{ vipInfo.heatLimit }}</span>
                  <span class="stat-label">今日加热</span>
                </div>
              </div>

              <router-link to="/vip/activate" class="renew-btn">续费/升级</router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- VIP权益说明 -->
      <div class="privileges-section">
        <h3>VIP权益说明</h3>
        <div class="privilege-cards">
          <div class="privilege-card bronze">
            <div class="card-header">
              <span class="level-badge">铜</span>
              <span class="level-name">普通VIP</span>
            </div>
            <ul class="privilege-list">
              <li><el-icon><Download /></el-icon> 每日下载 5 篇</li>
              <li><el-icon><Sunny /></el-icon> 每日加热 3 次</li>
              <li><el-icon><Lightning /></el-icon> 加热值 +10</li>
              <li><el-icon><Medal /></el-icon> 铜色专属标识</li>
            </ul>
          </div>
          <div class="privilege-card silver">
            <div class="card-header">
              <span class="level-badge">银</span>
              <span class="level-name">高级VIP</span>
            </div>
            <ul class="privilege-list">
              <li><el-icon><Download /></el-icon> 每日下载 20 篇</li>
              <li><el-icon><Sunny /></el-icon> 每日加热 10 次</li>
              <li><el-icon><Lightning /></el-icon> 加热值 +30</li>
              <li><el-icon><Medal /></el-icon> 银色专属标识</li>
            </ul>
          </div>
          <div class="privilege-card gold">
            <div class="card-header">
              <span class="level-badge">金</span>
              <span class="level-name">超级VIP</span>
            </div>
            <ul class="privilege-list">
              <li><el-icon><Download /></el-icon> 每日下载 无限</li>
              <li><el-icon><Sunny /></el-icon> 每日加热 30 次</li>
              <li><el-icon><Lightning /></el-icon> 加热值 +100</li>
              <li><el-icon><Medal /></el-icon> 金色专属标识</li>
            </ul>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Download, Sunny, Medal, WarningFilled } from '@element-plus/icons-vue'
import { getVipInfo } from '@/api/vip'
import { useUserStore } from '@/stores/user'

// Lightning 图标用 SVG 代替，因为 element-plus 没有
const Lightning = {
  template: `<svg viewBox="0 0 1024 1024"><path fill="currentColor" d="M288 671.36L544 287.36V480h192l-256 384V671.36H288z"/></svg>`
}

const userStore = useUserStore()
const loading = ref(true)
const vipInfo = ref({ isVip: false })
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const userInfo = computed(() => userStore.userInfo)

const isExpiringSoon = computed(() => {
  if (!vipInfo.value.expireTime) return false
  const expire = new Date(vipInfo.value.expireTime)
  const now = new Date()
  const days = (expire - now) / (1000 * 60 * 60 * 24)
  return days <= 7 && days > 0
})

onMounted(async () => {
  await fetchVipInfo()
})

const fetchVipInfo = async () => {
  try {
    const res = await getVipInfo()
    if (res.code === 200) {
      vipInfo.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>

<style scoped>
.vip-center {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
}

.loading {
  text-align: center;
  padding: 60px;
  color: var(--text-secondary);
}

/* 非VIP */
.not-vip {
  text-align: center;
  padding: 60px 20px;
  background: var(--bg-card);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.not-vip-icon {
  font-size: 60px;
  margin-bottom: 20px;
}

.not-vip h2 {
  font-size: 24px;
  margin-bottom: 10px;
  color: var(--text-primary);
}

.not-vip p {
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.activate-link {
  display: inline-block;
  padding: 12px 30px;
  background: linear-gradient(135deg, #a855f7, #ec4899);
  color: #fff;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  transition: transform 0.3s, box-shadow 0.3s;
}

.activate-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(168, 85, 247, 0.4);
}

/* ========== 翻转卡片 ========== */
.flip-card {
  width: 100%;
  height: 380px;
  margin-bottom: 30px;
  perspective: 1000px;
}

.flip-content {
  width: 100%;
  height: 100%;
  position: relative;
  transform-style: preserve-3d;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  border-radius: 16px;
}

.flip-card:hover .flip-content {
  transform: rotateY(180deg);
}

.flip-front, .flip-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
  border-radius: 16px;
  overflow: hidden;
}

/* 卡片背面 */
.flip-back {
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1a1a1a, #2d2d2d);
}

.back-glow {
  position: absolute;
  width: 250px;
  height: 200%;
  animation: rotateGlow 4s linear infinite;
}

.flip-card.level-1 .back-glow {
  background: linear-gradient(90deg, transparent, #cd7f32, #cd7f32, transparent);
}
.flip-card.level-2 .back-glow {
  background: linear-gradient(90deg, transparent, #c0c0c0, #e8e8e8, #c0c0c0, transparent);
}
.flip-card.level-3 .back-glow {
  background: linear-gradient(90deg, transparent, #ffd700, #ffb700, #ffd700, transparent);
}

@keyframes rotateGlow {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.back-inner {
  position: absolute;
  width: calc(100% - 6px);
  height: calc(100% - 6px);
  background: linear-gradient(135deg, #1a1a1a, #252525);
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 20px;
}

.flip-card.level-1 .back-inner .crown-icon { color: #cd7f32; }
.flip-card.level-2 .back-inner .crown-icon { color: #c0c0c0; }
.flip-card.level-3 .back-inner .crown-icon { color: #ffd700; }

.hover-text {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

/* 卡片正面 */
.flip-front {
  transform: rotateY(180deg);
}

.flip-card.level-1 .flip-front { background: linear-gradient(135deg, #cd7f32, #b8860b); }
.flip-card.level-2 .flip-front { background: linear-gradient(135deg, #d4d4d4, #a8a8a8); }
.flip-card.level-3 .flip-front { background: linear-gradient(135deg, #ffd700, #ffb700); }

.front-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(25px);
  animation: float 3s ease-in-out infinite;
}

.flip-card.level-1 .c1 { width: 150px; height: 150px; background: #daa520; top: -40px; left: -40px; }
.flip-card.level-1 .c2 { width: 80px; height: 80px; background: #b8860b; bottom: 30px; right: -20px; animation-delay: -1s; }
.flip-card.level-1 .c3 { width: 120px; height: 120px; background: #cd7f32; bottom: -50px; left: 40%; animation-delay: -2s; }

.flip-card.level-2 .c1 { width: 150px; height: 150px; background: #e8e8e8; top: -40px; left: -40px; }
.flip-card.level-2 .c2 { width: 80px; height: 80px; background: #b0b0b0; bottom: 30px; right: -20px; animation-delay: -1s; }
.flip-card.level-2 .c3 { width: 120px; height: 120px; background: #d0d0d0; bottom: -50px; left: 40%; animation-delay: -2s; }

.flip-card.level-3 .c1 { width: 150px; height: 150px; background: #ffe44d; top: -40px; left: -40px; }
.flip-card.level-3 .c2 { width: 80px; height: 80px; background: #ff9500; bottom: 30px; right: -20px; animation-delay: -1s; }
.flip-card.level-3 .c3 { width: 120px; height: 120px; background: #ffcc00; bottom: -50px; left: 40%; animation-delay: -2s; }

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(10px); }
}

.front-content {
  position: relative;
  height: 100%;
  padding: 24px 30px;
  display: flex;
  flex-direction: column;
  color: #fff;
}

.flip-card.level-2 .front-content { color: #333; }

.vip-badge-tag {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
  width: fit-content;
  backdrop-filter: blur(4px);
  margin-bottom: 16px;
}

.flip-card.level-1 .vip-badge-tag { background: rgba(0, 0, 0, 0.3); }
.flip-card.level-2 .vip-badge-tag { background: rgba(0, 0, 0, 0.15); color: #333; }
.flip-card.level-3 .vip-badge-tag { background: rgba(0, 0, 0, 0.25); }

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.5);
  object-fit: cover;
}

.flip-card.level-2 .avatar { border-color: rgba(0, 0, 0, 0.2); }

.user-info h3 {
  font-size: 22px;
  margin-bottom: 6px;
  font-weight: 700;
}

.expire-time {
  font-size: 14px;
  opacity: 0.9;
  display: flex;
  align-items: center;
  gap: 4px;
}

.warning { 
  color: #ff4444;
  display: flex;
  align-items: center;
}

.stats-row {
  display: flex;
  gap: 16px;
  flex: 1;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  backdrop-filter: blur(4px);
}

.flip-card.level-2 .stat-item { background: rgba(0, 0, 0, 0.08); }

.stat-icon { 
  font-size: 28px; 
  margin-bottom: 8px;
}
.stat-value { 
  font-size: 20px; 
  font-weight: 700; 
}
.stat-label { 
  font-size: 12px; 
  opacity: 0.8;
  margin-top: 4px;
}

.renew-btn {
  display: block;
  text-align: center;
  padding: 14px;
  background: rgba(0, 0, 0, 0.25);
  border-radius: 10px;
  color: inherit;
  text-decoration: none;
  font-weight: 600;
  font-size: 15px;
  transition: background 0.3s;
  backdrop-filter: blur(4px);
  margin-top: 16px;
}

.renew-btn:hover {
  background: rgba(0, 0, 0, 0.35);
}

/* ========== 权益说明 ========== */
.privileges-section {
  background: var(--bg-card);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.privileges-section h3 {
  text-align: center;
  margin-bottom: 24px;
  color: var(--text-primary);
  font-size: 20px;
}

.privilege-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.privilege-card {
  border-radius: 12px;
  padding: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.privilege-card:hover {
  transform: translateY(-4px);
}

.privilege-card.bronze {
  background: linear-gradient(135deg, rgba(205, 127, 50, 0.15), rgba(184, 134, 11, 0.1));
  border: 1px solid rgba(205, 127, 50, 0.3);
}
.privilege-card.bronze:hover { box-shadow: 0 8px 20px rgba(205, 127, 50, 0.2); }

.privilege-card.silver {
  background: linear-gradient(135deg, rgba(192, 192, 192, 0.15), rgba(168, 168, 168, 0.1));
  border: 1px solid rgba(192, 192, 192, 0.3);
}
.privilege-card.silver:hover { box-shadow: 0 8px 20px rgba(192, 192, 192, 0.2); }

.privilege-card.gold {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.15), rgba(255, 183, 0, 0.1));
  border: 1px solid rgba(255, 215, 0, 0.3);
}
.privilege-card.gold:hover { box-shadow: 0 8px 20px rgba(255, 215, 0, 0.3); }

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.level-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 12px;
}

.bronze .level-badge { background: #cd7f32; color: #fff; }
.silver .level-badge { background: #c0c0c0; color: #333; }
.gold .level-badge { background: #ffd700; color: #333; }

.level-name {
  font-weight: 600;
  color: var(--text-primary);
}

.privilege-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.privilege-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  font-size: 13px;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
}

.privilege-list li:last-child {
  border-bottom: none;
}

.privilege-list .el-icon {
  font-size: 16px;
  color: var(--text-muted);
}

.bronze .privilege-list .el-icon { color: #cd7f32; }
.silver .privilege-list .el-icon { color: #888; }
.gold .privilege-list .el-icon { color: #d4a000; }

@media (max-width: 768px) {
  .flip-card {
    height: 380px;
  }
  
  .privilege-cards {
    grid-template-columns: 1fr;
  }
  
  .stat-item {
    padding: 12px;
  }
}
</style>
