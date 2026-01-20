// pages/user/comments/index.js
const auth = require('../../../utils/auth')
const userApi = require('../../../api/user')
const formatUtil = require('../../../utils/format')
const imageUtil = require('../../../utils/image')

Page({
  data: {
    comments: [],
    total: 0,
    loading: false,
    page: 1,
    pageSize: 20,
    hasMore: true
  },

  onLoad() {
    if (!auth.isLoggedIn()) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
      return
    }
    this.loadData()
  },

  onPullDownRefresh() {
    this.setData({
      page: 1,
      comments: [],
      hasMore: true
    })
    this.loadData()
    wx.stopPullDownRefresh()
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ page: this.data.page + 1 })
      this.loadData()
    }
  },

  // 返回
  goBack() {
    wx.navigateBack()
  },

  // 加载数据
  async loadData() {
    this.setData({ loading: true })

    try {
      console.log('=== 开始加载我的评论 ===')
      console.log('请求参数:', {
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      const res = await userApi.getComments({
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      console.log('API返回原始数据:', JSON.stringify(res, null, 2))

      let records = []
      let total = 0

      // 解析数据结构 - 优先使用 list (PageResult格式)
      if (Array.isArray(res)) {
        console.log('解析方式: 直接数组')
        records = res
        total = res.length
      } else if (res.list) {
        console.log('解析方式: res.list (PageResult)')
        records = res.list
        total = res.total || records.length
      } else if (res.records) {
        console.log('解析方式: res.records')
        records = res.records
        total = res.total || records.length
      } else if (res.data) {
        if (Array.isArray(res.data)) {
          console.log('解析方式: res.data 数组')
          records = res.data
          total = res.data.length
        } else if (res.data.list) {
          console.log('解析方式: res.data.list (PageResult)')
          records = res.data.list
          total = res.data.total || records.length
        } else if (res.data.records) {
          console.log('解析方式: res.data.records')
          records = res.data.records
          total = res.data.total || records.length
        }
      }

      console.log('解析后的记录数:', records.length)
      console.log('总数:', total)

      // 处理图片和时间
      records = records.map(comment => ({
        ...comment,
        createTime: formatUtil.formatRelativeTime(comment.createTime),
        article: comment.article ? {
          ...comment.article,
          cover: imageUtil.getImageUrl(comment.article.cover)
        } : null
      }))

      this.setData({
        comments: this.data.page === 1 ? records : [...this.data.comments, ...records],
        total,
        hasMore: records.length === this.data.pageSize
      })

      console.log('页面数据已更新, comments数量:', this.data.comments.length)
      console.log('=== 我的评论加载完成 ===')
    } catch (err) {
      console.error('加载评论失败:', err)
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 跳转文章详情
  goToArticle(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/article/detail/index?id=${id}`
    })
  }
})
