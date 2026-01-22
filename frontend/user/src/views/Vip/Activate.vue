<template>
  <div class="vip-activate">
    <!-- 激活表单卡片 -->
    <div class="activate-card">
      <div class="card-border"></div>
      <div class="card-inner">
        <div class="card-header">
          <h2>
            <el-icon><Medal /></el-icon>
            激活VIP会员
          </h2>
          <p>输入激活密钥，开启专属特权</p>
        </div>
        
        <div class="activate-form">
          <div class="input-group">
            <input 
              v-model="keyCode" 
              type="text" 
              placeholder="请输入激活密钥 (VIP-XXXX-XXXX-XXXX-XXXX)"
              :disabled="loading"
              @keyup.enter="handleActivate"
            />
            <button 
              class="activate-btn" 
              :disabled="loading || !keyCode"
              @click="handleActivate"
            >
              {{ loading ? '激活中...' : '立即激活' }}
            </button>
          </div>
          <p class="hint">密钥格式：VIP-XXXX-XXXX-XXXX-XXXX</p>
        </div>
      </div>
    </div>

    <!-- VIP权益介绍 -->
    <div class="privileges-section">
      <h3>VIP专属权益</h3>
      <div class="privilege-cards">
        <div class="vip-card bronze">
          <div class="card-border"></div>
          <div class="card-inner">
            <div class="card-title">
              <span class="level-badge">普通VIP</span>
              <p class="card-desc">基础会员权益</p>
            </div>
            <hr class="divider" />
            <ul class="feature-list">
              <li><span class="check"><el-icon><Check /></el-icon></span><span>每日下载 5 篇文章</span></li>
              <li><span class="check"><el-icon><Check /></el-icon></span><span>每日加热 3 次</span></li>
              <li><span class="check"><el-icon><Check /></el-icon></span><span>单次加热 +10 热度</span></li>
              <li><span class="check"><el-icon><Check /></el-icon></span><span>铜色专属VIP标识</span></li>
            </ul>
          </div>
        </div>
        <div class="vip-card silver">
          <div class="card-border"></div>
          <div class="card-inner">
            <div class="card-title">
              <span class="level-badge">高级VIP</span>
              <p class="card-desc">进阶会员权益</p>
            </div>
            <hr class="divider" />
            <ul class="feature-list">
              <li><span class="check"><el-icon><Check /></el-icon></span><span>每日下载 20 篇文章</span></li>
              <li><span class="check"><el-icon><Check /></el-icon></span><span>每日加热 10 次</span></li>
              <li><span class="check"><el-icon><Check /></el-icon></span><span>单次加热 +30 热度</span></li>
              <li><span class="check"><el-icon><Check /></el-icon></span><span>银色专属VIP标识</span></li>
            </ul>
          </div>
        </div>
        <div class="vip-card gold">
          <div class="card-border"></div>
          <div class="card-inner">
            <div class="card-title">
              <span class="level-badge">超级VIP</span>
              <p class="card-desc">尊享会员权益</p>
            </div>
            <hr class="divider" />
            <ul class="feature-list">
              <li><span class="check"><el-icon><Check /></el-icon></span><span>每日下载 无限 文章</span></li>
              <li><span class="check"><el-icon><Check /></el-icon></span><span>每日加热 30 次</span></li>
              <li><span class="check"><el-icon><Check /></el-icon></span><span>单次加热 +100 热度</span></li>
              <li><span class="check"><el-icon><Check /></el-icon></span><span>金色专属VIP标识</span></li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- 激活成功弹窗 -->
    <div v-if="showSuccess" class="success-modal" @click="showSuccess = false">
      <div class="success-content" @click.stop>
        <div class="success-icon-wrapper">
          <div class="success-icon-bg"></div>
          <el-icon class="success-icon"><CircleCheck /></el-icon>
        </div>
        <h3>激活成功！</h3>
        <p>恭喜您成为 <span :style="{ color: getLevelColor(activatedLevel) }">{{ getLevelName(activatedLevel) }}</span></p>
        <button class="confirm-btn" @click="goToCenter">
          <span>查看我的VIP</span>
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Medal, Check, CircleCheck, ArrowRight } from '@element-plus/icons-vue'
import { activateVip } from '@/api/vip'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const keyCode = ref('')
const loading = ref(false)
const showSuccess = ref(false)
const activatedLevel = ref(1)

const handleActivate = async () => {
  if (!keyCode.value) {
    ElMessage.warning('请输入激活密钥')
    return
  }
  loading.value = true
  try {
    const res = await activateVip({ keyCode: keyCode.value.toUpperCase() })
    if (res.code === 200) {
      activatedLevel.value = res.data.level
      showSuccess.value = true
      keyCode.value = ''
      // 刷新用户信息，更新右上角VIP状态
      await userStore.fetchUserInfo()
    } else {
      ElMessage.error(res.message || '激活失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '激活失败')
  } finally {
    loading.value = false
  }
}

const getLevelName = (level) => {
  const names = { 1: '普通VIP', 2: '高级VIP', 3: '超级VIP' }
  return names[level] || 'VIP'
}

const getLevelColor = (level) => {
  const colors = { 1: '#cd7f32', 2: '#c0c0c0', 3: '#ffd700' }
  return colors[level] || '#a855f7'
}

const goToCenter = () => {
  showSuccess.value = false
  router.push('/vip/center')
}
</script>

<style scoped>
.vip-activate {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
}

.activate-card {
  position: relative;
  border-radius: 16px;
  margin-bottom: 30px;
  overflow: hidden;
}

.activate-card .card-border {
  position: absolute;
  z-index: -1;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: calc(100% + 2px);
  height: calc(100% + 2px);
  background: linear-gradient(0deg, rgba(255,255,255,0.1) -50%, rgba(255,255,255,0.4) 100%);
  border-radius: 16px;
  overflow: hidden;
}

.activate-card .card-border::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200%;
  height: 10rem;
  background: linear-gradient(0deg, transparent 0%, #a855f7 40%, #ec4899 60%, transparent 100%);
  animation: rotateBorder 6s linear infinite;
}

@keyframes rotateBorder {
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

.activate-card .card-inner {
  position: relative;
  background: var(--bg-card);
  border-radius: 15px;
  padding: 40px;
  margin: 1px;
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.card-header h2 {
  font-size: 26px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: linear-gradient(135deg, #a855f7, #ec4899);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-header h2 .el-icon {
  font-size: 28px;
  color: #a855f7;
}

.card-header p {
  color: var(--text-secondary);
  font-size: 14px;
}

.activate-form {
  max-width: 600px;
  margin: 0 auto;
}

.input-group {
  display: flex;
  gap: 12px;
}

.input-group input {
  flex: 1;
  padding: 14px 20px;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.3s;
  background: var(--bg-input, var(--bg-card));
  color: var(--text-primary);
}

.input-group input::placeholder {
  color: var(--text-muted);
}

.input-group input:focus {
  outline: none;
  border-color: #a855f7;
  box-shadow: 0 0 0 3px rgba(168, 85, 247, 0.1);
}

.activate-btn {
  padding: 14px 28px;
  background: linear-gradient(135deg, #a855f7, #ec4899);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  box-shadow: inset 0 -2px 20px -4px rgba(255,255,255,0.3);
}

.activate-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(168, 85, 247, 0.4);
}

.activate-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.hint {
  text-align: center;
  color: var(--text-muted);
  font-size: 13px;
  margin-top: 12px;
}

.privileges-section h3 {
  text-align: center;
  font-size: 20px;
  margin-bottom: 24px;
  color: var(--text-primary);
}

.privilege-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.vip-card {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  transition: transform 0.3s;
}

.vip-card:hover {
  transform: translateY(-5px);
}

.vip-card .card-border {
  position: absolute;
  z-index: -1;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: calc(100% + 2px);
  height: calc(100% + 2px);
  border-radius: 16px;
  overflow: hidden;
}

.vip-card .card-border::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200%;
  height: 10rem;
  animation: rotateBorder 8s linear infinite;
}

.vip-card.bronze .card-border {
  background: linear-gradient(0deg, rgba(205,127,50,0.2) -50%, rgba(205,127,50,0.5) 100%);
}
.vip-card.bronze .card-border::before {
  background: linear-gradient(0deg, transparent 0%, #cd7f32 40%, #daa520 60%, transparent 100%);
}
.vip-card.bronze .card-inner {
  background: var(--bg-card);
  background-image: radial-gradient(at 0% 100%, rgba(205,127,50,0.15) 0px, transparent 60%);
}

.vip-card.silver .card-border {
  background: linear-gradient(0deg, rgba(192,192,192,0.2) -50%, rgba(192,192,192,0.5) 100%);
}
.vip-card.silver .card-border::before {
  background: linear-gradient(0deg, transparent 0%, #a8a8a8 40%, #e0e0e0 60%, transparent 100%);
}
.vip-card.silver .card-inner {
  background: var(--bg-card);
  background-image: radial-gradient(at 0% 100%, rgba(192,192,192,0.15) 0px, transparent 60%);
}

.vip-card.gold .card-border {
  background: linear-gradient(0deg, rgba(255,215,0,0.2) -50%, rgba(255,215,0,0.5) 100%);
}
.vip-card.gold .card-border::before {
  background: linear-gradient(0deg, transparent 0%, #ffd700 40%, #ffb700 60%, transparent 100%);
}
.vip-card.gold .card-inner {
  background: var(--bg-card);
  background-image: radial-gradient(at 0% 100%, rgba(255,215,0,0.15) 0px, transparent 60%),
                    radial-gradient(at 100% 0%, rgba(255,183,0,0.1) 0px, transparent 60%);
}

.vip-card .card-inner {
  position: relative;
  border-radius: 15px;
  padding: 24px;
  margin: 1px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-title { text-align: center; }

.level-badge {
  display: inline-block;
  padding: 6px 18px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
  color: #fff;
}

.bronze .level-badge { background: linear-gradient(135deg, #cd7f32, #b8860b); }
.silver .level-badge { background: linear-gradient(135deg, #c0c0c0, #a8a8a8); color: #333; }
.gold .level-badge { background: linear-gradient(135deg, #ffd700, #ffb700); color: #333; }

.card-desc {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-muted);
}

.divider {
  border: none;
  height: 1px;
  background: var(--border-color);
  margin: 0;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--text-secondary);
}

.feature-list .check {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  flex-shrink: 0;
}

.bronze .check { background: #cd7f32; color: #fff; }
.silver .check { background: #c0c0c0; color: #333; }
.gold .check { background: #ffd700; color: #333; }

.feature-list .check .el-icon { font-size: 12px; }

.success-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.success-content {
  background: var(--bg-card);
  border-radius: 20px;
  padding: 48px 40px;
  text-align: center;
  animation: popIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(168, 85, 247, 0.2);
  max-width: 400px;
  position: relative;
  overflow: hidden;
}

.success-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #a855f7, #ec4899, #ffd700);
}

@keyframes popIn {
  0% { 
    transform: scale(0.8) translateY(20px); 
    opacity: 0; 
  }
  60% { 
    transform: scale(1.05) translateY(-5px); 
  }
  100% { 
    transform: scale(1) translateY(0); 
    opacity: 1; 
  }
}

.success-icon-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-icon-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.2), rgba(236, 72, 153, 0.2));
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.success-icon {
  position: relative;
  font-size: 60px;
  color: #a855f7;
  animation: checkmark 0.6s ease 0.3s both;
  filter: drop-shadow(0 4px 12px rgba(168, 85, 247, 0.4));
}

@keyframes checkmark {
  0% {
    transform: scale(0) rotate(-45deg);
    opacity: 0;
  }
  50% {
    transform: scale(1.2) rotate(10deg);
  }
  100% {
    transform: scale(1) rotate(0deg);
    opacity: 1;
  }
}

.success-content h3 { 
  font-size: 28px; 
  margin-bottom: 12px; 
  color: var(--text-primary);
  font-weight: 700;
  background: linear-gradient(135deg, #a855f7, #ec4899);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.success-content p { 
  color: var(--text-secondary); 
  margin-bottom: 32px;
  font-size: 16px;
  line-height: 1.6;
}

.success-content p span {
  font-weight: 600;
  font-size: 18px;
}

.confirm-btn {
  padding: 14px 32px;
  background: linear-gradient(135deg, #a855f7, #ec4899);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 16px rgba(168, 85, 247, 0.3);
  position: relative;
  overflow: hidden;
}

.confirm-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.confirm-btn:hover::before {
  left: 100%;
}

.confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(168, 85, 247, 0.5);
}

.confirm-btn:active {
  transform: translateY(0);
}

.confirm-btn .el-icon {
  font-size: 18px;
  transition: transform 0.3s;
}

.confirm-btn:hover .el-icon {
  transform: translateX(4px);
}

@media (max-width: 768px) {
  .privilege-cards { grid-template-columns: 1fr; }
  .input-group { flex-direction: column; }
  .activate-card .card-inner { padding: 24px; }
}
</style>
