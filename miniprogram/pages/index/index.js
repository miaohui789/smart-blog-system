// pages/index/index.js
const articleApi = require('../../api/article')
const config = require('../../utils/config')
const imageUtil = require('../../utils/image')
const formatUtil = require('../../utils/format')

Page({
  data: {
    // 轮播图数据（从后端获取）
    banners: [],
    
    // 快捷入口数据（从后端获取）
    quickEntries: [],
    
    articles: [],
    page: 1,
    pageSize: config.PAGE_SIZE,
    hasMore: true,
    loading: false,
    refreshing: false
  },

  onLoad() {
    this.loadBanners()
    this.loadCategories()
    this.loadArticles()
  },

  onShow() {
    // 页面显示时刷新 tabBar 徽标
    this.updateTabBarBadge()
    
    // 设置 tabBar 选中状态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 0
      })
    }
  },

  onPullDownRefresh() {
    this.refreshArticles()
  },

  onReachBottom() {
    this.loadMore()
  },

  // 轮播图点击
  onBannerTap(e) {
    const url = e.currentTarget.dataset.url
    if (url) {
      wx.navigateTo({ url })
    }
  },

  // 快捷入口点击
  onEntryTap(e) {
    const path = e.currentTarget.dataset.path
    if (path) {
      wx.navigateTo({ url: path })
    }
  },

  // 跳转到历史记录
  goToHistory() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    })
  },

  // 跳转到更多
  goToMore() {
    wx.navigateTo({
      url: '/pages/article/list/index?type=hot'
    })
  },

  // 加载轮播图（访问量最多的3篇文章）
  async loadBanners() {
    try {
      const res = await articleApi.getList({
        page: 1,
        pageSize: 3,
        sortBy: 'view',
        sortOrder: 'desc'
      })

      let records = []
      if (Array.isArray(res)) {
        records = res
      } else if (res.records && Array.isArray(res.records)) {
        records = res.records
      } else if (res.list && Array.isArray(res.list)) {
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

      // 处理图片路径
      records = imageUtil.processArticleList(records)

      // 转换为轮播图格式
      const banners = records.map(article => ({
        id: article.id,
        title: article.title,
        image: article.cover || 'https://via.placeholder.com/800x400?text=No+Image',
        url: `/pages/article/detail/index?id=${article.id}`
      }))

      this.setData({ banners })
    } catch (err) {
      console.error('加载轮播图失败:', err)
    }
  },

  // 加载分类数据
  async loadCategories() {
    try {
      const categoryApi = require('../../api/category')
      const res = await categoryApi.getList()

      let categories = []
      if (Array.isArray(res)) {
        categories = res
      } else if (res.data && Array.isArray(res.data)) {
        categories = res.data
      } else if (res.list && Array.isArray(res.list)) {
        categories = res.list
      }

      // 使用 Vant Weapp 图标
      const iconMap = {
        '技术分享': 'desktop-o',
        '生活随笔': 'flower-o',
        '学习笔记': 'edit',
        '前端开发': 'phone-o',
        '后端开发': 'setting-o',
        'Java学习': 'diamond-o',
        'Vue学习': 'star-o',
        '默认': 'label-o'
      }

      const bgColors = [
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
        'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
        'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
      ]

      // 取前7个分类
      const quickEntries = categories.slice(0, 7).map((category, index) => ({
        id: category.id,
        name: category.name,
        icon: iconMap[category.name] || iconMap['默认'],
        bgColor: bgColors[index] || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        path: `/pages/category/detail/index?id=${category.id}`
      }))

      // 添加"更多"入口
      quickEntries.push({
        id: 'more',
        name: '更多',
        icon: 'apps-o',
        bgColor: 'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)',
        path: '/pages/category/index/index'
      })

      this.setData({ quickEntries })
    } catch (err) {
      console.error('加载分类失败:', err)
    }
  },

  // 加载文章列表
  async loadArticles() {
    if (this.data.loading) return

    this.setData({ loading: true })

    try {
      const res = await articleApi.getList({
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      // 验证返回数据格式
      if (!res) {
        throw new Error('返回数据为空')
      }

      console.log('API 原始返回数据:', res)

      // 兼容不同的返回格式
      let records = []
      if (Array.isArray(res)) {
        // 直接返回数组
        records = res
      } else if (res.records && Array.isArray(res.records)) {
        // 分页对象格式
        records = res.records
      } else if (res.list && Array.isArray(res.list)) {
        // list 格式
        records = res.list
      } else if (res.data) {
        // data 格式
        if (Array.isArray(res.data)) {
          records = res.data
        } else if (res.data.records) {
          records = res.data.records
        } else if (res.data.list) {
          records = res.data.list
        }
      } else {
        console.warn('未知的数据格式:', res)
        records = []
      }

      console.log('提取的文章列表:', records)
      console.log('第一篇文章数据:', records.length > 0 ? records[0] : '无数据')

      // 处理图片路径
      records = imageUtil.processArticleList(records)
      
      // 格式化时间
      records = records.map(article => ({
        ...article,
        createTime: formatUtil.formatRelativeTime(article.createTime),
        publishTime: formatUtil.formatRelativeTime(article.publishTime)
      }))
      
      // 调试：打印处理后的数据
      console.log('处理后的文章数据:', records.length > 0 ? records[0] : '无数据')
      if (records.length > 0 && records[0].author) {
        console.log('作者信息:', records[0].author)
        console.log('作者头像URL:', records[0].author.avatar)
      }

      this.setData({
        articles: [...this.data.articles, ...records],
        hasMore: records.length === this.data.pageSize,
        loading: false
      })
    } catch (err) {
      console.error('加载文章失败:', err)
      this.setData({ loading: false })
      
      wx.showToast({
        title: err.message || '加载失败',
        icon: 'none',
        duration: 2000
      })
    }
  },

  // 刷新文章列表
  async refreshArticles() {
    this.setData({
      page: 1,
      articles: [],
      hasMore: true,
      refreshing: true
    })

    try {
      // 同时刷新轮播图和分类
      await Promise.all([
        this.loadBanners(),
        this.loadCategories()
      ])
      
      const res = await articleApi.getList({
        page: 1,
        pageSize: this.data.pageSize
      })

      // 验证返回数据格式
      let records = []
      if (Array.isArray(res)) {
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
        hasMore: records.length === this.data.pageSize,
        refreshing: false
      })

      wx.stopPullDownRefresh()
      wx.showToast({
        title: '刷新成功',
        icon: 'success'
      })
    } catch (err) {
      console.error('刷新失败:', err)
      this.setData({ refreshing: false })
      wx.stopPullDownRefresh()
      
      wx.showToast({
        title: '刷新失败',
        icon: 'none'
      })
    }
  },

  // 加载更多
  loadMore() {
    if (!this.data.hasMore || this.data.loading) return

    this.setData({ page: this.data.page + 1 })
    this.loadArticles()
  },

  // 跳转到文章详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/article/detail/index?id=${id}`
    })
  },

  // 跳转到搜索页
  goToSearch() {
    wx.navigateTo({
      url: '/pages/search/index/index'
    })
  },

  // 处理刷新按钮点击
  handleRefresh() {
    if (this.data.refreshing) return
    
    this.setData({ refreshing: true })
    
    // 延迟结束旋转动画，确保至少旋转一圈
    setTimeout(() => {
      this.refreshArticles()
    }, 1000)
  },

  // 更新 tabBar 徽标
  async updateTabBarBadge() {
    try {
      const auth = require('../../utils/auth')
      if (!auth.isLoggedIn()) return

      const messageApi = require('../../api/message')
      const notificationApi = require('../../api/notification')

      const [messageCount, notificationCount] = await Promise.all([
        messageApi.getUnreadCount(),
        notificationApi.getUnreadCount()
      ])

      const totalCount = (messageCount || 0) + (notificationCount || 0)

      if (totalCount > 0) {
        wx.setTabBarBadge({
          index: 2,
          text: totalCount > 99 ? '99+' : totalCount.toString()
        })
      } else {
        wx.removeTabBarBadge({ index: 2 })
      }
    } catch (err) {
      console.error('更新徽标失败:', err)
    }
  }
})
