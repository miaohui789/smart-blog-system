// pages/user/following/index.js
const auth = require('../../../utils/auth')
const userApi = require('../../../api/user')
const imageUtil = require('../../../utils/image')

Page({
  data: {
    users: [],
    total: 0,
    loading: false,
    page: 1,
    pageSize: 20,
    hasMore: true
  },

  onLoad() {
    this.loadData()
  },

  onPullDownRefresh() {
    this.setData({
      page: 1,
      users: [],
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
    if (!auth.isLoggedIn()) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }

    this.setData({ loading: true })

    try {
      console.log('开始加载关注列表，参数:', {
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      const res = await userApi.getFollowing({
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      console.log('关注列表API原始返回:', res)
      console.log('返回数据类型:', typeof res)
      console.log('是否为数组:', Array.isArray(res))

      let records = []
      let total = 0

      if (Array.isArray(res)) {
        records = res
        total = res.length
        console.log('解析方式: 直接数组')
      } else if (res.list) {
        records = res.list
        total = res.total || records.length
        console.log('解析方式: res.list')
      } else if (res.records) {
        records = res.records
        total = res.total || records.length
        console.log('解析方式: res.records')
      } else if (res.data) {
        if (Array.isArray(res.data)) {
          records = res.data
          total = res.data.length
          console.log('解析方式: res.data 数组')
        } else if (res.data.list) {
          records = res.data.list
          total = res.data.total || records.length
          console.log('解析方式: res.data.list')
        } else if (res.data.records) {
          records = res.data.records
          total = res.data.total || records.length
          console.log('解析方式: res.data.records')
        }
      }

      console.log('提取的记录数:', records.length)
      console.log('总数:', total)
      console.log('第一条记录:', records.length > 0 ? records[0] : '无数据')

      // 处理头像路径
      records = records.map(user => ({
        ...user,
        avatar: imageUtil.getImageUrl(user.avatar)
      }))

      console.log('处理后的记录:', records)

      this.setData({
        users: this.data.page === 1 ? records : [...this.data.users, ...records],
        total: total,
        hasMore: records.length === this.data.pageSize
      })

      console.log('页面数据已更新, users数量:', this.data.users.length)
    } catch (err) {
      console.error('加载关注列表失败:', err)
      console.error('错误详情:', err.message)
      console.error('错误堆栈:', err.stack)
      wx.showToast({
        title: '加载失败: ' + (err.message || '未知错误'),
        icon: 'none',
        duration: 3000
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 取消关注
  async handleUnfollow(e) {
    const id = e.currentTarget.dataset.id

    wx.showModal({
      title: '提示',
      content: '确定要取消关注吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await userApi.unfollow(id)
            
            // 从列表中移除
            const users = this.data.users.filter(item => item.id !== id)
            this.setData({
              users,
              total: this.data.total - 1
            })

            wx.showToast({
              title: '已取消关注',
              icon: 'success'
            })
          } catch (err) {
            console.error('取消关注失败:', err)
            wx.showToast({
              title: '操作失败',
              icon: 'none'
            })
          }
        }
      }
    })
  },

  // 跳转用户主页
  goToUserProfile(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/user/detail/index?id=${id}`
    })
  }
})
