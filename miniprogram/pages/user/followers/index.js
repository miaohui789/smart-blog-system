// pages/user/followers/index.js
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
      console.log('开始加载粉丝列表，参数:', {
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      const res = await userApi.getFollowers({
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      console.log('粉丝列表API原始返回:', res)

      let records = []
      let total = 0

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

      console.log('提取的粉丝记录数:', records.length)

      // 获取我的关注列表，用于判断是否已关注
      let myFollowingIds = []
      try {
        const followingRes = await userApi.getFollowing({ page: 1, pageSize: 999 })
        const followingList = followingRes.list || followingRes.records || []
        myFollowingIds = followingList.map(u => u.id)
        console.log('我关注的用户ID列表:', myFollowingIds)
      } catch (err) {
        console.error('获取关注列表失败:', err)
      }

      // 处理头像路径和关注状态
      records = records.map(user => ({
        ...user,
        avatar: imageUtil.getImageUrl(user.avatar),
        isFollowing: myFollowingIds.includes(user.id)
      }))

      console.log('处理后的粉丝列表:', records)

      this.setData({
        users: this.data.page === 1 ? records : [...this.data.users, ...records],
        total: total,
        hasMore: records.length === this.data.pageSize
      })
    } catch (err) {
      console.error('加载粉丝列表失败:', err)
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 关注/取消关注
  async handleFollow(e) {
    const id = e.currentTarget.dataset.id
    const isFollowing = e.currentTarget.dataset.following

    try {
      if (isFollowing) {
        await userApi.unfollow(id)
        wx.showToast({
          title: '已取消关注',
          icon: 'success'
        })
      } else {
        await userApi.follow(id)
        wx.showToast({
          title: '关注成功',
          icon: 'success'
        })
      }

      // 更新列表中的关注状态
      const users = this.data.users.map(user => {
        if (user.id === id) {
          return { ...user, isFollowing: !isFollowing }
        }
        return user
      })

      this.setData({ users })
    } catch (err) {
      console.error('操作失败:', err)
      wx.showToast({
        title: '操作失败',
        icon: 'none'
      })
    }
  },

  // 跳转用户主页
  goToUserProfile(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/user/detail/index?id=${id}`
    })
  }
})
