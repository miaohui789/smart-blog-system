import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTagsViewStore = defineStore('tagsView', () => {
  const visitedViews = ref([])
  const cachedViews = ref([])

  function addView(view) {
    // 跳过 redirect 路由和隐藏的路由
    if (view.path.includes('/redirect') || view.meta?.hidden) return
    if (visitedViews.value.some(v => v.path === view.path)) return
    visitedViews.value.push({ ...view, title: view.meta?.title || 'no-name' })
  }

  function delView(view) {
    const index = visitedViews.value.findIndex(v => v.path === view.path)
    if (index > -1) visitedViews.value.splice(index, 1)
  }

  function delOthersViews(view) {
    visitedViews.value = visitedViews.value.filter(v => v.meta?.affix || v.path === view.path)
  }

  function delLeftViews(view) {
    const index = visitedViews.value.findIndex(v => v.path === view.path)
    if (index > -1) {
      visitedViews.value = visitedViews.value.filter((v, i) => v.meta?.affix || i >= index)
    }
  }

  function delRightViews(view) {
    const index = visitedViews.value.findIndex(v => v.path === view.path)
    if (index > -1) {
      visitedViews.value = visitedViews.value.filter((v, i) => v.meta?.affix || i <= index)
    }
  }

  function delAllViews() {
    visitedViews.value = visitedViews.value.filter(v => v.meta?.affix)
  }

  return { 
    visitedViews, 
    cachedViews, 
    addView, 
    delView, 
    delOthersViews, 
    delLeftViews, 
    delRightViews, 
    delAllViews 
  }
})
