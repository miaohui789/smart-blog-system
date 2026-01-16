<template>
  <div class="weather-card glass-card">
    <div class="weather-header">
      <svg class="header-icon" viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="12" r="4" fill="currentColor"/>
        <path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <span>实时天气</span>
    </div>
    
    <div v-if="loading" class="weather-loading">
      <div class="loading-spinner"></div>
      <span>获取天气中...</span>
    </div>
    
    <div v-else-if="error" class="weather-error">
      <span>{{ error }}</span>
    </div>
    
    <div v-else-if="weather" class="weather-content">
      <div class="weather-location">
        <svg class="loc-icon" viewBox="0 0 24 24" fill="none">
          <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" fill="currentColor"/>
          <circle cx="12" cy="9" r="2.5" fill="var(--bg-card)"/>
        </svg>
        <span>{{ weather.city }}</span>
      </div>
      
      <div class="weather-main">
        <div class="weather-icon" v-html="getWeatherSvg(weather.text)"></div>
        <div class="weather-temp">
          <span class="temp-value">{{ weather.temp }}</span>
          <span class="temp-unit">°C</span>
        </div>
      </div>
      
      <div class="weather-desc">{{ weather.text }}</div>
      
      <div class="weather-details">
        <div class="detail-item">
          <span class="label">体感</span>
          <span class="value">{{ weather.feelsLike }}°C</span>
        </div>
        <div class="detail-item">
          <span class="label">湿度</span>
          <span class="value">{{ weather.humidity }}%</span>
        </div>
        <div class="detail-item">
          <span class="label">风向</span>
          <span class="value">{{ weather.windDir }}</span>
        </div>
        <div class="detail-item">
          <span class="label">风速</span>
          <span class="value">{{ weather.windSpeed }}km/h</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWeather } from '@/api/weather'

const weather = ref(null)
const loading = ref(true)
const error = ref('')

// 天气SVG图标
function getWeatherSvg(text) {
  if (!text) return getSunnySvg()
  if (text.includes('晴') || text.includes('Sunny') || text.includes('Clear')) return getSunnySvg()
  if (text.includes('云') || text.includes('阴') || text.includes('Cloud') || text.includes('Overcast')) return getCloudySvg()
  if (text.includes('雨') || text.includes('Rain')) return getRainySvg()
  if (text.includes('雪') || text.includes('Snow')) return getSnowySvg()
  if (text.includes('雾') || text.includes('霾') || text.includes('Fog') || text.includes('Mist')) return getFoggySvg()
  if (text.includes('雷') || text.includes('Thunder')) return getThunderSvg()
  return getCloudySvg()
}

function getSunnySvg() {
  return `<svg viewBox="0 0 64 64" class="weather-svg sunny">
    <circle cx="32" cy="32" r="12" fill="#FFD93D"/>
    <g stroke="#FFD93D" stroke-width="3" stroke-linecap="round">
      <line x1="32" y1="8" x2="32" y2="16"/>
      <line x1="32" y1="48" x2="32" y2="56"/>
      <line x1="8" y1="32" x2="16" y2="32"/>
      <line x1="48" y1="32" x2="56" y2="32"/>
      <line x1="15" y1="15" x2="20" y2="20"/>
      <line x1="44" y1="44" x2="49" y2="49"/>
      <line x1="15" y1="49" x2="20" y2="44"/>
      <line x1="44" y1="20" x2="49" y2="15"/>
    </g>
  </svg>`
}

function getCloudySvg() {
  return `<svg viewBox="0 0 64 64" class="weather-svg cloudy">
    <circle cx="22" cy="24" r="8" fill="#FFD93D"/>
    <path d="M46 44H20c-6.6 0-12-5.4-12-12s5.4-12 12-12c1.2 0 2.4.2 3.5.5C25.3 16.2 29.3 14 34 14c7.2 0 13.1 5.4 13.9 12.3C53.5 27.5 58 32.8 58 39c0 2.8-2.2 5-5 5h-7z" fill="#E8E8E8"/>
  </svg>`
}

function getRainySvg() {
  return `<svg viewBox="0 0 64 64" class="weather-svg rainy">
    <path d="M46 36H20c-6.6 0-12-5.4-12-12s5.4-12 12-12c1.2 0 2.4.2 3.5.5C25.3 8.2 29.3 6 34 6c7.2 0 13.1 5.4 13.9 12.3C53.5 19.5 58 24.8 58 31c0 2.8-2.2 5-5 5h-7z" fill="#A0AEC0"/>
    <g stroke="#60A5FA" stroke-width="2.5" stroke-linecap="round">
      <line x1="20" y1="42" x2="16" y2="52"/>
      <line x1="30" y1="42" x2="26" y2="52"/>
      <line x1="40" y1="42" x2="36" y2="52"/>
      <line x1="25" y1="50" x2="21" y2="58"/>
      <line x1="35" y1="50" x2="31" y2="58"/>
    </g>
  </svg>`
}

function getSnowySvg() {
  return `<svg viewBox="0 0 64 64" class="weather-svg snowy">
    <path d="M46 36H20c-6.6 0-12-5.4-12-12s5.4-12 12-12c1.2 0 2.4.2 3.5.5C25.3 8.2 29.3 6 34 6c7.2 0 13.1 5.4 13.9 12.3C53.5 19.5 58 24.8 58 31c0 2.8-2.2 5-5 5h-7z" fill="#A0AEC0"/>
    <g fill="#E0F2FE">
      <circle cx="18" cy="46" r="3"/>
      <circle cx="32" cy="44" r="3"/>
      <circle cx="46" cy="46" r="3"/>
      <circle cx="25" cy="54" r="3"/>
      <circle cx="39" cy="54" r="3"/>
    </g>
  </svg>`
}

function getFoggySvg() {
  return `<svg viewBox="0 0 64 64" class="weather-svg foggy">
    <g stroke="#A0AEC0" stroke-width="4" stroke-linecap="round">
      <line x1="10" y1="24" x2="54" y2="24"/>
      <line x1="14" y1="34" x2="50" y2="34"/>
      <line x1="10" y1="44" x2="54" y2="44"/>
      <line x1="18" y1="54" x2="46" y2="54"/>
    </g>
  </svg>`
}

function getThunderSvg() {
  return `<svg viewBox="0 0 64 64" class="weather-svg thunder">
    <path d="M46 32H20c-6.6 0-12-5.4-12-12s5.4-12 12-12c1.2 0 2.4.2 3.5.5C25.3 4.2 29.3 2 34 2c7.2 0 13.1 5.4 13.9 12.3C53.5 15.5 58 20.8 58 27c0 2.8-2.2 5-5 5h-7z" fill="#64748B"/>
    <polygon points="36,30 28,44 34,44 30,58 42,40 36,40 40,30" fill="#FBBF24"/>
  </svg>`
}

async function fetchWeather() {
  loading.value = true
  error.value = ''
  
  try {
    const res = await getWeather()
    if (res.code === 200 && res.data) {
      weather.value = res.data
    } else {
      // 静默处理，显示默认数据
      weather.value = getDefaultWeather()
    }
  } catch (e) {
    console.warn('获取天气失败，使用默认数据')
    // 静默处理，不弹出错误提示，显示默认数据
    weather.value = getDefaultWeather()
  } finally {
    loading.value = false
  }
}

// 默认天气数据
function getDefaultWeather() {
  return {
    city: '未知',
    temp: '--',
    feelsLike: '--',
    humidity: '--',
    windSpeed: '--',
    windDir: '--',
    weatherCode: '113',
    text: '暂无数据'
  }
}

onMounted(() => {
  fetchWeather()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.weather-card {
  padding: $spacing-lg;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: rgba($primary-color, 0.2);
  }
}

.weather-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-md;
  border-bottom: 1px solid var(--border-color);
  
  .header-icon {
    width: 18px;
    height: 18px;
    color: #fbbf24;
  }
}

.weather-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 30px;
  color: var(--text-muted);
  font-size: 13px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--border-color);
  border-top-color: $primary-color;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.weather-error {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #f87171;
  font-size: 13px;
}

.weather-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.weather-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
  
  .loc-icon {
    width: 16px;
    height: 16px;
    color: $primary-color;
  }
}

.weather-main {
  display: flex;
  align-items: center;
  gap: 16px;
}

.weather-icon {
  width: 64px;
  height: 64px;
  
  :deep(.weather-svg) {
    width: 100%;
    height: 100%;
  }
}

.weather-temp {
  display: flex;
  align-items: flex-start;
  
  .temp-value {
    font-size: 42px;
    font-weight: 700;
    color: var(--text-primary);
    line-height: 1;
  }
  
  .temp-unit {
    font-size: 18px;
    color: var(--text-secondary);
    margin-top: 4px;
  }
}

.weather-desc {
  font-size: 15px;
  color: var(--text-secondary);
  font-weight: 500;
}

.weather-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px 16px;
  width: 100%;
  padding: 12px;
  background: var(--bg-card-hover);
  border-radius: 8px;
  margin-top: 4px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  
  .label {
    color: var(--text-muted);
  }
  
  .value {
    color: var(--text-secondary);
    font-weight: 500;
  }
}
</style>
