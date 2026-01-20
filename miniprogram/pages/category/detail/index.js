// pages/category/detail/index.js
const categoryApi = require('../../../api/category')
const imageUtil = require('../../../utils/image')
const formatUtil = require('../../../utils/format')
const config = require('../../../utils/config')

Page({
  data: {
    id: null,
    category: null,
    articles: [],
    page: 1,
    pageSize: config.PAGE_SIZE,
    hasMore: true,
    loading: true,
    refreshing: false
  },

  onLoad(options) {
    this.setData({ id: options.id })
    this.loadCategory()
    this.loadArticles()
  },

  onPullDownRefresh() {
    this.refreshArticles()
  },

  onReachBottom() {
    this.loadMore()
  },

  // 返回
  goBack() {
    wx.navigateBack()
  },

  // 加载分类信息
  async loadCategory() {
    try {
      // 从分类列表中获取分类信息（避免404）
      const res = await categoryApi.getList()

      let categories = []
      if (Array.isArray(res)) {
        categories = res
      } else if (res.data && Array.isArray(res.data)) {
        categories = res.data
      } else if (res.list && Array.isArray(res.list)) {
        categories = res.list
      }

      // 图标映射
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

      // 从列表中找到当前分类
      const category = categories.find(c => c.id == this.data.id)
      
      if (category) {
        // 添加图标
        category.icon = iconMap[category.name] || iconMap['默认']
        this.setData({ category })
      } else {
        // 如果找不到，使用默认值
        this.setData({ 
          category: {
            id: this.data.id,
            name: '分类',
            description: '',
            articleCount: 0,
            icon: 'label-o'
          }
        })
      }
    } catch (err) {
      console.error('加载分类失败:', err)
      // 使用默认值
      this.setData({ 
        category: {
          id: this.data.id,
          name: '分类',
          description: '',
          articleCount: 0,
          icon: 'label-o'
        }
      })
    }
  },

  // 加载文章列表
  async loadArticles() {
    if (this.data.loading && this.data.page > 1) return

    this.setData({ loading: true })

    try {
      const res = await categoryApi.getArticles(this.data.id, {
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      console.log('分类文章API原始返回:', res)

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

      console.log('提取的文章列表:', records)
      console.log('第一篇文章原始数据:', records.length > 0 ? JSON.stringify(records[0], null, 2) : '无数据')

      // 处理图片路径
      records = imageUtil.processArticleList(records)
      
      console.log('处理后的第一篇文章:', records.length > 0 ? JSON.stringify(records[0], null, 2) : '无数据')
      
      // 格式化时间
      records = records.map(article => ({
        ...article,
        createTime: formatUtil.formatRelativeTime(article.createTime)
      }))

      console.log('最终的文章数据:', records)

      this.setData({
        articles: [...this.data.articles, ...records],
        hasMore: records.length === this.data.pageSize,
        loading: false
      })

      console.log('setData后的文章列表:', this.data.articles)
    } catch (err) {
      console.error('加载文章失败:', err)
      this.setData({ loading: false })
      
      wx.showToast({
        title: '加载失败',
        icon: 'none'
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
      await this.loadCategory()
      await this.loadArticles()
      
      wx.stopPullDownRefresh()
      wx.showToast({
        title: '刷新成功',
        icon: 'success'
      })
    } catch (err) {
      console.error('刷新失败:', err)
      wx.stopPullDownRefresh()
      
      wx.showToast({
        title: '刷新失败',
        icon: 'none'
      })
    } finally {
      this.setData({ refreshing: false })
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
  }
})
