<template>
  <div class="tags-view">
    <router-link
      v-for="tag in tagsViewStore.visitedViews"
      :key="tag.path"
      :to="tag.path"
      class="tag-item"
      :class="{ active: isActive(tag) }"
      @contextmenu.prevent="openContextMenu($event, tag)"
    >
      {{ tag.title }}
      <el-icon v-if="!tag.meta?.affix" class="close-icon" @click.prevent.stop="closeTag(tag)">
        <Close />
      </el-icon>
    </router-link>
    
    <!-- 右键菜单 -->
    <Teleport to="body">
      <div
        v-if="contextMenuVisible"
        class="context-menu"
        :style="{ left: contextMenuLeft + 'px', top: contextMenuTop + 'px' }"
        @click="closeContextMenu"
      >
        <div class="menu-item" @click="refreshTag">
          <el-icon><Refresh /></el-icon>
          <span>刷新页面</span>
        </div>
        <div class="menu-item" @click="closeCurrentTag" v-if="!selectedTag?.meta?.affix">
          <el-icon><Close /></el-icon>
          <span>关闭当前</span>
        </div>
        <div class="menu-item" @click="closeOtherTags">
          <el-icon><CircleClose /></el-icon>
          <span>关闭其他</span>
        </div>
        <div class="menu-item" @click="closeLeftTags">
          <el-icon><Back /></el-icon>
          <span>关闭左侧</span>
        </div>
        <div class="menu-item" @click="closeRightTags">
          <el-icon><Right /></el-icon>
          <span>关闭右侧</span>
        </div>
        <div class="menu-item" @click="closeAllTags">
          <el-icon><CircleCloseFilled /></el-icon>
          <span>全部关闭</span>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Close, Refresh, CircleClose, Back, Right, CircleCloseFilled } from '@element-plus/icons-vue'
import { useTagsViewStore } from '@/stores/tagsView'

const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()

const contextMenuVisible = ref(false)
const contextMenuLeft = ref(0)
const contextMenuTop = ref(0)
const selectedTag = ref(null)

function isActive(tag) {
  return tag.path === route.path
}

function closeTag(tag) {
  tagsViewStore.delView(tag)
}

// 打开右键菜单
function openContextMenu(e, tag) {
  contextMenuVisible.value = true
  contextMenuLeft.value = e.clientX
  contextMenuTop.value = e.clientY
  selectedTag.value = tag
}

// 关闭右键菜单
function closeContextMenu() {
  contextMenuVisible.value = false
  selectedTag.value = null
}

// 刷新页面
function refreshTag() {
  if (selectedTag.value) {
    router.replace({
      path: '/redirect' + selectedTag.value.path,
      query: selectedTag.value.query
    })
  }
}

// 关闭当前标签
function closeCurrentTag() {
  if (selectedTag.value && !selectedTag.value.meta?.affix) {
    tagsViewStore.delView(selectedTag.value)
  }
}

// 关闭其他标签
function closeOtherTags() {
  if (selectedTag.value) {
    tagsViewStore.delOthersViews(selectedTag.value)
    if (selectedTag.value.path !== route.path) {
      router.push(selectedTag.value.path)
    }
  }
}

// 关闭左侧标签
function closeLeftTags() {
  if (selectedTag.value) {
    tagsViewStore.delLeftViews(selectedTag.value)
  }
}

// 关闭右侧标签
function closeRightTags() {
  if (selectedTag.value) {
    tagsViewStore.delRightViews(selectedTag.value)
  }
}

// 关闭全部标签
function closeAllTags() {
  tagsViewStore.delAllViews()
  router.push('/dashboard')
}

// 点击其他地方关闭菜单
function handleClickOutside() {
  closeContextMenu()
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

watch(route, () => {
  tagsViewStore.addView(route)
}, { immediate: true })
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.tags-view {
  display: flex;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-lg;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  overflow-x: auto;
  transition: background-color 0.3s, border-color 0.3s;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 12px;
  color: var(--text-secondary);
  text-decoration: none;
  white-space: nowrap;
  transition: all 0.2s;

  &:hover {
    background: var(--bg-card-hover);
  }

  &.active {
    background: var(--primary-color);
    border-color: var(--primary-color);
    color: #fff;
  }
}

.close-icon {
  font-size: 12px;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: $error-color;
  }
}

// 右键菜单
.context-menu {
  position: fixed;
  z-index: 9999;
  min-width: 140px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 4px;
  
  .menu-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    font-size: 13px;
    color: var(--text-primary);
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.2s;
    
    .el-icon {
      font-size: 14px;
      color: var(--text-muted);
    }
    
    &:hover {
      background: var(--bg-card-hover);
      color: $primary-color;
      
      .el-icon {
        color: $primary-color;
      }
    }
  }
}
</style>
