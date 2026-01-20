// pages/tag/detail/index.js
const tagApi = require('../../../api/tag')
const imageUtil = require('../../../utils/image')
const formatUtil = require('../../../utils/format')
const config = require('../../../utils/config')

Page({
  data: {
    id: null,
    tag: null,
    articles: [],
    page: 1,
    pageSize: config.PAGE_SIZE,
    hasMore: true,
    loading: true,
    refreshing: false
  },

  onLoad(options) {
    this.setData({ id: options.id })
    this.loadTag()
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

  // 加载标签信息
  async loadTag() {
    try {
      const tag = await tagApi.getDetail(this.data.id)
      this.setData({ tag })
    } catch (err) {
      console.error('加载标签失败:', err)
    }
  },

  // 加载文章列表
  async loadArticles() {
    if (this.data.loading && this.data.page > 1) return

    this.setData({ loading: true })

    try {
      const res = await tagApi.getArticles(this.data.id, {
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      console.log('标签文章API原始返回:', res)

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
      
      // 格式化时间
      records = records.map(article => ({
        ...article,
        createTime: formatUtil.formatRelativeTime(article.createTime)
      }))

      this.setData({
        articles: [...this.data.articles, ...records],
        hasMore: records.length === this.data.pageSize,
        loading: false
      })
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
      await this.loadTag()
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
