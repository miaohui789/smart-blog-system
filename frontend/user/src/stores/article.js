import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getArticleList, getHotArticles, getRecommendArticles } from '@/api/article'

export const useArticleStore = defineStore('article', () => {
  const articles = ref([])
  const hotArticles = ref([])
  const recommendArticles = ref([])
  const total = ref(0)
  const loading = ref(false)

  async function fetchArticles(params) {
    loading.value = true
    try {
      const res = await getArticleList(params)
      articles.value = res.data.list
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  async function fetchHotArticles() {
    const res = await getHotArticles()
    hotArticles.value = res.data
  }

  async function fetchRecommendArticles() {
    const res = await getRecommendArticles()
    recommendArticles.value = res.data
  }

  return {
    articles,
    hotArticles,
    recommendArticles,
    total,
    loading,
    fetchArticles,
    fetchHotArticles,
    fetchRecommendArticles
  }
})
