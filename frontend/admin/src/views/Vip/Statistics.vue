<template>
  <div class="vip-statistics">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #a855f7, #ec4899)">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalMembers || 0 }}</div>
            <div class="stat-label">总会员数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #22c55e, #16a34a)">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.activeMembers || 0 }}</div>
            <div class="stat-label">有效会员</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #3b82f6, #1d4ed8)">
            <el-icon><Key /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalKeys || 0 }}</div>
            <div class="stat-label">总密钥数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f59e0b, #d97706)">
            <el-icon><Ticket /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.availableKeys || 0 }}</div>
            <div class="stat-label">可用密钥</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 等级分布 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>VIP等级分布</span>
          </template>
          <div class="level-distribution">
            <div v-for="item in stats.levelDistribution" :key="item.level" class="level-item">
              <div class="level-info">
                <el-tag :type="getLevelType(item.level)" size="small">{{ item.name }}</el-tag>
                <span class="level-count">{{ item.count }} 人</span>
              </div>
              <el-progress 
                :percentage="getPercentage(item.count)" 
                :color="getLevelColor(item.level)"
                :stroke-width="12"
              />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>密钥使用情况</span>
          </template>
          <div class="key-stats">
            <div class="key-stat-item">
              <div class="key-stat-label">已使用</div>
              <div class="key-stat-value">{{ stats.usedKeys || 0 }}</div>
              <el-progress 
                :percentage="getKeyPercentage(stats.usedKeys)" 
                color="#909399"
                :stroke-width="8"
              />
            </div>
            <div class="key-stat-item">
              <div class="key-stat-label">可用</div>
              <div class="key-stat-value">{{ stats.availableKeys || 0 }}</div>
              <el-progress 
                :percentage="getKeyPercentage(stats.availableKeys)" 
                color="#67c23a"
                :stroke-width="8"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getVipStatistics } from '@/api/vip'
import { User, CircleCheck, Key, Ticket } from '@element-plus/icons-vue'

const stats = ref({
  totalMembers: 0,
  activeMembers: 0,
  totalKeys: 0,
  usedKeys: 0,
  availableKeys: 0,
  levelDistribution: []
})

onMounted(() => {
  fetchStats()
})

const fetchStats = async () => {
  try {
    const res = await getVipStatistics()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const getPercentage = (count) => {
  const total = stats.value.activeMembers || 1
  return Math.round((count / total) * 100)
}

const getKeyPercentage = (count) => {
  const total = stats.value.totalKeys || 1
  return Math.round((count / total) * 100)
}

const getLevelType = (level) => {
  const types = { 1: 'warning', 2: '', 3: 'success' }
  return types[level] || 'info'
}

const getLevelColor = (level) => {
  const colors = { 1: '#cd7f32', 2: '#c0c0c0', 3: '#ffd700' }
  return colors[level] || '#a855f7'
}
</script>

<style scoped>
.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.level-distribution {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.level-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.level-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.level-count {
  font-size: 14px;
  color: #666;
}

.key-stats {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.key-stat-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.key-stat-label {
  font-size: 14px;
  color: #666;
}

.key-stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}
</style>
