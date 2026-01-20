// pages/user/favorites/index.js
const auth = require('../../../utils/auth')
const userApi = require('../../../api/user')
const articleApi = require('../../../api/article')
const imageUtil = require('../../../utils/image')
const formatUtil = require('../../../utils/format')

Page({
  data: {
    articles: [],
    total: 0,
    loading: false,
    page: 1,
    pageSize: 10,
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
      articles: [],
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
      console.log('=== 开始加载收藏文章 ===')
      console.log('请求参数:', {
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      const res = await userApi.getFavorites({
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      console.log('API返回原始数据:', JSON.stringify(res, null, 2))

      let records = []
      let total = 0

      // 解析数据结构
      if (Array.isArray(res)) {
        records = res
        total = res.length
      } else if (res.list) {
        records = res.list
        total = res.total || records.length
      } else if (res.records) {
        records = res.records
        total = res.total || records.length
      } else if (res.data) {
        if (Array.isArray(res.data)) {
          records = res.data
          total = res.data.length
        } else if (res.data.list) {
          records = res.data.list
          total = res.data.total || records.length
        } else if (res.data.records) {
          records = res.data.records
          total = res.data.total || records.length
        }
      }

      console.log('解析后的记录数:', records.length)
      console.log('总数:', total)

      // 处理图片路径和时间
      records = imageUtil.processArticleList(records)
      records = records.map(article => ({
        ...article,
        createTime: formatUtil.formatRelativeTime(article.createTime)
      }))

      this.setData({
        articles: this.data.page === 1 ? records : [...this.data.articles, ...records],
        total,
        hasMore: records.length === this.data.pageSize
      })

      console.log('页面数据已更新, articles数量:', this.data.articles.length)
      console.log('=== 收藏文章加载完成 ===')
    } catch (err) {
      console.error('加载收藏文章失败:', err)
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 取消收藏
  async handleUnfavorite(e) {
    const id = e.currentTarget.dataset.id

    wx.showModal({
      title: '提示',
      content: '确定要取消收藏吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await articleApi.unfavorite(id)
            
            // 从列表中移除
            const articles = this.data.articles.filter(item => item.id !== id)
            this.setData({
              articles,
              total: this.data.total - 1
            })

            wx.showToast({
              title: '已取消收藏',
              icon: 'success'
            })
          } catch (err) {
            console.error('取消收藏失败:', err)
            wx.showToast({
              title: '操作失败',
              icon: 'none'
            })
          }
        }
      }
    })
  },

  // 跳转文章详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/article/detail/index?id=${id}`
    })
  }
})
