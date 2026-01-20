// pages/search/index/index.js
const articleApi = require('../../../api/article')
const imageUtil = require('../../../utils/image')
const formatUtil = require('../../../utils/format')

const STORAGE_KEY = 'searchHistory'
const MAX_HISTORY = 10

Page({
  data: {
    keyword: '',
    articles: [],
    loading: false,
    searched: false,
    searchHistory: [],
    searchTimer: null,
    hotKeywords: [],
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
    try {
      const res = await articleApi.getList({
        page: 1,
        pageSize: 8,
        sortBy: 'view',
        sortOrder: 'desc'
      })

      let records = []
      if (Array.isArray(res)) {
        records = res
      } else if (res.records) {
        records = res.records
      } else if (res.list) {
        records = res.list
      } else if (res.data) {
        if (Array.isArray(res.data)) {
          records = res.data
        } else if (res.data.records) {
          records = res.data.records
        } else if (res.data.list) {
          records = res.data.list
        }
      }

      const hotKeywords = records.map((article, index) => ({
        id: article.id,
        title: article.title,
        hot: index < 3 // 前3个标记为热门
      }))

      this.setData({ hotKeywords })
    } catch (err) {
      console.error('加载热门搜索失败:', err)
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
      const userApi = require('../../../api/user')
      
      // 同时搜索文章、标签和用户
      const [articleRes, tagRes, userRes] = await Promise.all([
        articleApi.search(keyword, { page: 1, pageSize: 3 }),
        tagApi.getList(),
        userApi.search(keyword, { page: 1, pageSize: 3 })
      ])

      // 处理文章结果
      let articles = []
      if (articleRes.articles) {
        if (articleRes.articles.records) {
          articles = articleRes.articles.records
        } else if (articleRes.articles.list) {
          articles = articleRes.articles.list
        } else if (Array.isArray(articleRes.articles)) {
          articles = articleRes.articles
        }
      } else if (Array.isArray(articleRes)) {
        articles = articleRes
      } else if (articleRes.records) {
        articles = articleRes.records
      } else if (articleRes.list) {
        articles = articleRes.list
      }

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

      // 处理用户结果（已经是数组）
      let users = Array.isArray(userRes) ? userRes : []

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

  async handleSearch(saveHistory = true) {
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
      const res = await articleApi.search(keyword)
      
      console.log('搜索返回数据:', res)
      
      // 验证返回数据格式
      let records = []
      if (res.articles) {
        // 后端返回 {articles: {...}, users: [...]}
        if (res.articles.records) {
          records = res.articles.records
        } else if (res.articles.list) {
          records = res.articles.list
        } else if (Array.isArray(res.articles)) {
          records = res.articles
        }
      } else if (Array.isArray(res)) {
        records = res
      } else if (res.records && Array.isArray(res.records)) {
        records = res.records
      } else if (res.list && Array.isArray(res.list)) {
        records = res.list
      }

      // 处理图片路径
      records = imageUtil.processArticleList(records)
      
      // 格式化时间
      records = records.map(article => ({
        ...article,
        createTime: formatUtil.formatRelativeTime(article.createTime),
        publishTime: formatUtil.formatRelativeTime(article.publishTime)
      }))

      this.setData({
        articles: records,
        loading: false
      })
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
