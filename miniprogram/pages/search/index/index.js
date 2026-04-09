// pages/search/index/index.js
const searchApi = require('../../../api/search')
const imageUtil = require('../../../utils/image')
const formatUtil = require('../../../utils/format')

const STORAGE_KEY = 'searchHistory'
const MAX_HISTORY = 10

function extractArticleRecords(result) {
  if (!result) {
    return []
  }
  if (result.articles) {
    if (Array.isArray(result.articles.list)) {
      return result.articles.list
    }
    if (Array.isArray(result.articles.records)) {
      return result.articles.records
    }
    if (Array.isArray(result.articles)) {
      return result.articles
    }
  }
  if (Array.isArray(result.list)) {
    return result.list
  }
  if (Array.isArray(result.records)) {
    return result.records
  }
  if (Array.isArray(result)) {
    return result
  }
  return []
}

function extractUserRecords(result) {
  if (!result || !Array.isArray(result.users)) {
    return []
  }
  return result.users
}

Page({
  data: {
    keyword: '',
    articles: [],
    loading: false,
    searched: false,
    searchHistory: [],
    searchTimer: null,
    hotKeywords: [],
    hotSearchBoard: {
      date: '',
      timezone: '北京时间',
      period: '00:00 - 24:00'
    },
    hotSearchLoading: false,
    discoveries: [],
    suggestions: [], // 搜索建议
    showSuggestions: false // 是否显示建议
  },

  onLoad() {
    this.loadSearchHistory()
    this.loadHotKeywords()
    this.loadDiscoveries()
  },

  onInput(e) {
    const keyword = e.detail.value
    this.setData({ keyword })
    
    // 清除之前的定时器
    if (this.data.searchTimer) {
      clearTimeout(this.data.searchTimer)
    }
    
    // 如果输入为空，重置状态
    if (!keyword.trim()) {
      this.setData({ 
        searched: false,
        articles: [],
        showSuggestions: false,
        suggestions: []
      })
      return
    }
    
    // 设置新的定时器，300ms 后加载建议
    const timer = setTimeout(() => {
      this.loadSuggestions(keyword)
    }, 300)
    
    this.setData({ searchTimer: timer })
  },

  clearKeyword() {
    this.setData({ 
      keyword: '',
      searched: false,
      articles: [],
      showSuggestions: false,
      suggestions: []
    })
  },

  goBack() {
    wx.navigateBack()
  },

  // 加载搜索历史
  loadSearchHistory() {
    try {
      const history = wx.getStorageSync(STORAGE_KEY) || []
      this.setData({ searchHistory: history })
    } catch (err) {
      console.error('加载搜索历史失败:', err)
    }
  },

  // 加载热门搜索（访问量最高的8篇文章标题）
  async loadHotKeywords() {
    this.setData({ hotSearchLoading: true })
    try {
      const res = await searchApi.getHotSearchBoard({ limit: 8 })
      const list = Array.isArray(res.list) ? res.list : []
      const hotKeywords = list.map((item, index) => ({
        keyword: item.keyword,
        rank: item.rank || index + 1,
        score: item.score || 0,
        hot: (item.rank || index + 1) <= 3
      }))

      this.setData({
        hotKeywords,
        hotSearchBoard: {
          date: res.date || '',
          timezone: res.timezone || '北京时间',
          period: res.period || '00:00 - 24:00'
        }
      })
    } catch (err) {
      console.error('加载热门搜索失败:', err)
      this.setData({
        hotKeywords: [],
        hotSearchBoard: {
          date: '',
          timezone: '北京时间',
          period: '00:00 - 24:00'
        }
      })
    } finally {
      this.setData({ hotSearchLoading: false })
    }
  },

  // 加载搜索发现（分类统计）
  async loadDiscoveries() {
    try {
      const categoryApi = require('../../../api/category')
      const res = await categoryApi.getList()

      let categories = []
      if (Array.isArray(res)) {
        categories = res
      } else if (res.data && Array.isArray(res.data)) {
        categories = res.data
      }

      // 颜色列表
      const colors = ['#667eea', '#10b981', '#8b5cf6', '#ec4899', '#f59e0b', '#06b6d4']

      const discoveries = categories.slice(0, 4).map((category, index) => ({
        id: category.id,
        name: category.name,
        color: colors[index % colors.length],
        count: `${category.articleCount || 0} 篇文章`
      }))

      this.setData({ discoveries })
    } catch (err) {
      console.error('加载搜索发现失败:', err)
    }
  },

  // 保存搜索历史
  saveSearchHistory(keyword) {
    try {
      let history = wx.getStorageSync(STORAGE_KEY) || []
      
      // 移除重复项
      history = history.filter(item => item !== keyword)
      
      // 添加到开头
      history.unshift(keyword)
      
      // 限制数量
      if (history.length > MAX_HISTORY) {
        history = history.slice(0, MAX_HISTORY)
      }
      
      wx.setStorageSync(STORAGE_KEY, history)
      this.setData({ searchHistory: history })
    } catch (err) {
      console.error('保存搜索历史失败:', err)
    }
  },

  // 清空搜索历史
  clearHistory() {
    wx.showModal({
      title: '提示',
      content: '确定清空搜索历史吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync(STORAGE_KEY)
          this.setData({ searchHistory: [] })
        }
      }
    })
  },

  // 删除单个搜索历史
  deleteHistory(e) {
    const index = e.currentTarget.dataset.index
    let history = this.data.searchHistory
    history.splice(index, 1)
    
    wx.setStorageSync(STORAGE_KEY, history)
    this.setData({ searchHistory: history })
  },

  // 加载搜索建议
  async loadSuggestions(keyword) {
    try {
      const tagApi = require('../../../api/tag')
      
      const [searchRes, tagRes] = await Promise.all([
        searchApi.searchAll({ keyword, page: 1, pageSize: 3 }),
        tagApi.getList()
      ])

      const articles = extractArticleRecords(searchRes)

      // 处理标签结果
      let tags = []
      if (Array.isArray(tagRes)) {
        tags = tagRes
      } else if (tagRes.data && Array.isArray(tagRes.data)) {
        tags = tagRes.data
      }

      // 过滤匹配的标签
      const matchedTags = tags.filter(tag => 
        tag.name.toLowerCase().includes(keyword.toLowerCase())
      ).slice(0, 2)

      const users = extractUserRecords(searchRes)

      // 构建建议列表
      const suggestions = []

      // 添加文章建议
      articles.slice(0, 2).forEach(article => {
        suggestions.push({
          type: 'article',
          id: article.id,
          title: article.title,
          subtitle: '文章',
          icon: 'notes-o'
        })
      })

      // 添加标签建议
      matchedTags.forEach(tag => {
        suggestions.push({
          type: 'tag',
          id: tag.id,
          title: tag.name,
          subtitle: '标签',
          icon: 'label-o'
        })
      })

      // 添加用户建议
      users.slice(0, 2).forEach(user => {
        suggestions.push({
          type: 'user',
          id: user.id,
          title: user.nickname || user.username,
          subtitle: '用户',
          icon: 'user-o',
          avatar: imageUtil.getImageUrl(user.avatar)
        })
      })

      this.setData({ 
        suggestions,
        showSuggestions: suggestions.length > 0
      })
    } catch (err) {
      console.error('加载搜索建议失败:', err)
    }
  },

  // 点击搜索建议
  selectSuggestion(e) {
    const { type, title, id } = e.currentTarget.dataset
    
    if (type === 'article') {
      // 直接跳转到文章详情
      wx.navigateTo({
        url: `/pages/article/detail/index?id=${id}`
      })
    } else if (type === 'tag') {
      // 搜索该标签
      this.setData({ keyword: title })
      this.handleSearch(true)
    } else if (type === 'user') {
      // 跳转到用户主页
      wx.navigateTo({
        url: `/pages/user/detail/index?id=${id}`,
        fail: () => {
          wx.showToast({
            title: '用户主页开发中',
            icon: 'none'
          })
        }
      })
    }
  },
  searchHot(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.setData({ keyword })
    this.handleSearch(true)
  },

  // 点击搜索发现
  searchDiscovery(e) {
    const name = e.currentTarget.dataset.name
    this.setData({ keyword: name })
    this.handleSearch(true)
  },

  // 点击搜索历史
  searchHistory(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.setData({ keyword })
    this.handleSearch(true)
  },

  async handleSearch(arg = true) {
    const saveHistory = typeof arg === 'boolean' ? arg : true
    const keyword = this.data.keyword.trim()
    if (!keyword) {
      return
    }

    // 隐藏建议
    this.setData({ showSuggestions: false })

    // 只有手动搜索时才保存历史
    if (saveHistory) {
      this.saveSearchHistory(keyword)
    }

    this.setData({ loading: true, searched: true })

    try {
      const res = await searchApi.searchAll({
        keyword,
        page: 1,
        pageSize: 10,
        recordHot: saveHistory
      })
      
      console.log('搜索返回数据:', res)
      
      let records = extractArticleRecords(res)

      records = imageUtil.processArticleList(records)
      
      records = records.map(article => ({
        ...article,
        createTime: formatUtil.formatRelativeTime(article.createTime),
        publishTime: formatUtil.formatRelativeTime(article.publishTime)
      }))

      this.setData({
        articles: records,
        loading: false
      })
      if (saveHistory) {
        this.loadHotKeywords()
      }
    } catch (err) {
      console.error('搜索失败:', err)
      this.setData({ loading: false })
      
      wx.showToast({
        title: '搜索失败',
        icon: 'none'
      })
    }
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/article/detail/index?id=${id}`
    })
  }
})
