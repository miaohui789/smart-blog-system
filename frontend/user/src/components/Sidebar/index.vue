<template>
  <div
    class="sidebar-shell"
    :class="{
      'is-study-mode': studyMode,
      'is-collapsed': studyMode && collapsed
    }"
  >
    <div
      v-if="studyMode"
      class="study-dock"
      @mousemove="handleDockMouseMove"
      @mouseleave="resetDockScale"
    >
      <button
        v-for="item in dockItems"
        :key="item.key"
        :ref="el => setDockRef(el, item.key)"
        class="dock-item"
        :class="{ active: !collapsed && activeSection === item.key }"
        :style="{ '--dock-scale': dockScaleMap[item.key] || 1 }"
        :title="item.label"
        type="button"
        @click="handleDockClick(item.key)"
      >
        <el-icon>
          <component :is="item.icon" />
        </el-icon>
      </button>
    </div>

    <div class="sidebar" :class="{ 'is-hidden': studyMode && collapsed }">
      <section
        ref="weatherRef"
        class="sidebar-section"
        :class="{ 'is-active-section': !collapsed && activeSection === 'weather' }"
      >
        <Weather />
      </section>

      <div
        ref="hotRef"
        class="sidebar-card glass-card sidebar-section"
        :class="{ 'is-active-section': !collapsed && activeSection === 'hot' }"
      >
        <h3 class="card-title">
          <el-icon><TrendCharts /></el-icon>
          热门文章
          <router-link to="/archive" class="more-link">更多</router-link>
        </h3>
        <ul class="hot-list">
          <li v-for="(article, index) in displayedArticles" :key="article.id" class="hot-item">
            <span class="hot-rank" :class="getRankClass(index)">{{ index + 1 }}</span>
            <router-link :to="`/article/${article.id}`" class="hot-title">
              {{ article.title }}
            </router-link>
          </li>
        </ul>
        <el-empty v-if="!hotArticles.length" description="暂无热门文章" :image-size="60" />
      </div>

      <div
        ref="tagRef"
        class="sidebar-card glass-card sidebar-section"
        :class="{ 'is-active-section': !collapsed && activeSection === 'tag' }"
      >
        <h3 class="card-title">
          <el-icon><PriceTag /></el-icon>
          标签云
          <router-link to="/tag" class="more-link">更多</router-link>
        </h3>
        <TagCloud :limit="20" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { TrendCharts, PriceTag, Sunny } from '@element-plus/icons-vue'
import { getHotArticles } from '@/api/article'
import TagCloud from '@/components/TagCloud/index.vue'
import Weather from '@/components/Weather/index.vue'

const props = defineProps({
  studyMode: {
    type: Boolean,
    default: false
  },
  collapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle'])

const route = useRoute()
const hotArticles = ref([])
const weatherRef = ref(null)
const hotRef = ref(null)
const tagRef = ref(null)
const activeSection = ref('weather')
const dockRefs = new Map()
const dockScaleMap = ref({
  weather: 1,
  hot: 1,
  tag: 1
})

const dockItems = [
  { key: 'weather', label: '天气卡片', icon: Sunny },
  { key: 'hot', label: '热门文章', icon: TrendCharts },
  { key: 'tag', label: '标签云', icon: PriceTag }
]

const displayedArticles = computed(() => hotArticles.value.slice(0, 8))

function getSectionRef(key) {
  const sectionMap = {
    weather: weatherRef.value,
    hot: hotRef.value,
    tag: tagRef.value
  }
  return sectionMap[key] || null
}

function getRankClass(index) {
  if (index === 0) return 'gold'
  if (index === 1) return 'silver'
  if (index === 2) return 'bronze'
  return ''
}

async function fetchHotArticles() {
  try {
    const res = await getHotArticles()
    hotArticles.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

function setDockRef(el, key) {
  if (el) {
    dockRefs.set(key, el)
    return
  }
  dockRefs.delete(key)
}

function resetDockScale() {
  dockScaleMap.value = {
    weather: 1,
    hot: 1,
    tag: 1
  }
}

function handleDockMouseMove(event) {
  const nextScaleMap = {}
  dockItems.forEach(item => {
    const el = dockRefs.get(item.key)
    if (!el) {
      nextScaleMap[item.key] = 1
      return
    }
    const rect = el.getBoundingClientRect()
    const centerY = rect.top + rect.height / 2
    const distance = Math.abs(event.clientY - centerY)
    const intensity = Math.max(0, 1 - distance / 130)
    nextScaleMap[item.key] = Number((1 + intensity * 0.48).toFixed(3))
  })
  dockScaleMap.value = nextScaleMap
}

function handleDockClick(key) {
  if (props.collapsed) {
    activeSection.value = key
    emit('toggle', false)
    return
  }

  if (activeSection.value === key) {
    emit('toggle', true)
    return
  }

  activeSection.value = key
}

async function scrollToActiveSection(behavior = 'smooth') {
  if (props.collapsed) return
  await nextTick()
  const target = getSectionRef(activeSection.value)
  if (!target) return
  target.scrollIntoView({
    behavior,
    block: 'nearest',
    inline: 'nearest'
  })
}

onMounted(fetchHotArticles)

watch(
  () => route.name,
  (newName, oldName) => {
    if (oldName === 'Article') {
      fetchHotArticles()
    }
  }
)

watch(
  () => props.collapsed,
  (value) => {
    if (value) {
      resetDockScale()
      return
    }

    scrollToActiveSection('auto')
  }
)

watch(
  activeSection,
  () => {
    scrollToActiveSection()
  }
)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.sidebar-shell {
  position: relative;
  --study-dock-panel-bg: rgba(var(--bg-card-rgb), 0.78);
  --study-dock-panel-border: var(--border-light);
  --study-dock-panel-shadow: 0 18px 50px var(--shadow-color);
  --study-dock-item-bg: linear-gradient(180deg, rgba($primary-color, 0.12), rgba(var(--bg-card-rgb), 0.94));
  --study-dock-item-border: var(--border-color);
  --study-dock-item-color: var(--text-secondary);
  --study-dock-item-hover-color: var(--text-primary);
  --study-dock-item-hover-border: rgba(96, 165, 250, 0.42);
  --study-dock-item-hover-shadow: 0 12px 26px rgba(37, 99, 235, 0.2);
  --study-section-hover-bg: rgba($primary-color, 0.05);
  --study-card-hover-border: rgba($primary-color, 0.2);
}

:global(:root[data-theme='light']) .sidebar-shell {
  --study-dock-panel-bg: rgba(255, 255, 255, 0.92);
  --study-dock-panel-border: rgba(15, 23, 42, 0.1);
  --study-dock-panel-shadow: 0 20px 44px rgba(15, 23, 42, 0.12);
  --study-dock-item-bg: linear-gradient(180deg, rgba(59, 130, 246, 0.08), rgba(248, 250, 252, 0.98));
  --study-dock-item-border: rgba(15, 23, 42, 0.08);
  --study-dock-item-color: #64748b;
  --study-dock-item-hover-color: #1e293b;
  --study-dock-item-hover-border: rgba(59, 130, 246, 0.34);
  --study-dock-item-hover-shadow: 0 14px 28px rgba(59, 130, 246, 0.18);
  --study-section-hover-bg: rgba(59, 130, 246, 0.06);
  --study-card-hover-border: rgba(59, 130, 246, 0.24);
}

:global(body.has-custom-bg) .sidebar-shell {
  --study-dock-panel-bg: rgba(var(--bg-card-rgb), 0.66);
}

:global(:root[data-theme='dark']) :global(body.has-custom-bg) .sidebar-shell {
  --study-dock-panel-bg: rgba(var(--bg-card-rgb), 0.34);
  --study-dock-panel-border: rgba(255, 255, 255, 0.08);
}

:global(:root[data-theme='light']) :global(body.has-custom-bg) .sidebar-shell {
  --study-dock-panel-bg: rgba(255, 255, 255, 0.72);
  --study-dock-panel-border: rgba(148, 163, 184, 0.24);
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
  transition: opacity 0.35s ease, transform 0.35s ease;
}

.sidebar-shell.is-study-mode .sidebar {
  max-height: calc(100vh - 136px);
  overflow-y: auto;
  padding-right: 2px;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.45) transparent;
}

.sidebar-shell.is-study-mode .sidebar::-webkit-scrollbar {
  width: 5px;
  background: transparent;
}

.sidebar-shell.is-study-mode .sidebar::-webkit-scrollbar-button {
  display: none;
  width: 0;
  height: 0;
}

.sidebar-shell.is-study-mode .sidebar::-webkit-scrollbar-track {
  background: transparent;
  box-shadow: none;
}

.sidebar-shell.is-study-mode .sidebar::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.45);
}

.sidebar-shell.is-study-mode .sidebar::-webkit-scrollbar-corner {
  background: transparent;
}

.sidebar.is-hidden {
  opacity: 0;
  transform: translateX(42px);
  pointer-events: none;
}

.sidebar-section {
  scroll-margin-top: 92px;
  transition: border-color 0.22s ease, box-shadow 0.22s ease, background-color 0.22s ease;
}

.sidebar-section.is-active-section {
  border-color: rgba($primary-color, 0.28);
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.12);
}

.study-dock {
  position: fixed;
  right: 18px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 30;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 12px 10px;
  border-radius: 999px;
  border: 1px solid var(--study-dock-panel-border);
  background: var(--study-dock-panel-bg);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  box-shadow: var(--study-dock-panel-shadow);
  transition: background-color 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
}

.dock-item {
  width: 42px;
  height: 42px;
  border: 1px solid var(--study-dock-item-border);
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--study-dock-item-bg);
  color: var(--study-dock-item-color);
  cursor: pointer;
  transform: translateX(calc((var(--dock-scale, 1) - 1) * -14px)) scale(var(--dock-scale, 1));
  transition: transform 0.18s ease, color 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease, background-color 0.18s ease;

  .el-icon {
    font-size: 18px;
  }

  &:hover,
  &.active {
    color: var(--study-dock-item-hover-color);
    border-color: var(--study-dock-item-hover-border);
    box-shadow: var(--study-dock-item-hover-shadow);
  }
}

.sidebar-card {
  padding: $spacing-lg;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: all 0.2s ease;

  &:hover {
    border-color: var(--study-card-hover-border);
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-md;
  border-bottom: 1px solid var(--border-color);

  .el-icon {
    color: $primary-color;
  }

  .more-link {
    margin-left: auto;
    font-size: 12px;
    font-weight: 400;
    color: var(--text-muted);
    text-decoration: none;
    transition: color 0.2s;

    &:hover {
      color: $primary-color;
    }
  }
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 6px;
  border-radius: 8px;
  transition: background 0.2s ease;

  &:hover {
    background: var(--study-section-hover-bg);

    .hot-title {
      color: $primary-color;
    }
  }
}

.hot-rank {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-disabled);
  background: var(--bg-card-hover);
  border-radius: 6px;
  flex-shrink: 0;

  &.gold {
    background: linear-gradient(135deg, #fbbf24, #f59e0b);
    color: #18181b;
  }

  &.silver {
    background: linear-gradient(135deg, #9ca3af, #6b7280);
    color: #18181b;
  }

  &.bronze {
    background: linear-gradient(135deg, #d97706, #b45309);
    color: #18181b;
  }
}

.hot-title {
  flex: 1;
  font-size: 13px;
  color: var(--text-secondary);
  text-decoration: none;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s ease;
}

@media (max-width: 992px) {
  .study-dock {
    display: none;
  }
}
</style>
