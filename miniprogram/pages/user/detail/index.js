// pages/user/detail/index.js
const userApi = require('../../../api/user')
const imageUtil = require('../../../utils/image')
const formatUtil = require('../../../utils/format')
const auth = require('../../../utils/auth')
const config = require('../../../utils/config')

Page({
  data: {
    id: null,
    user: null,
    articles: [],
    page: 1,
    pageSize: config.PAGE_SIZE,
    hasMore: true,
    loading: true,
    isFollowing: false,
    isSelf: false,
    activeTab: 0, // 0: 文章, 1: 关注, 2: 粉丝
    tabs: ['文章', '关注', '粉丝']
  },

  onLoad(options) {
    this.setData({ id: options.id })
    
    // 检查是否是自己
    if (auth.isLoggedIn()) {
      const currentUser = auth.getUserInfo()
      if (currentUser && currentUser.id == options.id) {
        this.setData({ isSelf: true })
      }
    }
    
    this.loadUser()
    this.loadArticles()
  },

  onPullDownRefresh() {
    this.refreshData()
  },

  onReachBottom() {
    this.loadMore()
  },

  // 返回
  goBack() {
    wx.navigateBack()
  },

  // 加载用户信息
  async loadUser() {
    try {
      const user = await userApi.getUserProfile(this.data.id)
      
      console.log('=== 用户信息API返回 ===')
      console.log('完整用户数据:', user)
      
      // 处理头像
      user.avatar = imageUtil.getImageUrl(user.avatar)
      
      // 获取文章数量
      try {
        const articleRes = await userApi.getUserArticles(this.data.id, { page: 1, pageSize: 1 })
        user.articleCount = articleRes.total || 0
        console.log('文章数量:', user.articleCount)
      } catch (err) {
        console.error('获取文章数量失败:', err)
        user.articleCount = 0
      }
      
      // 获取关注数量
      try {
        const followingRes = await userApi.getUserFollowing(this.data.id, { page: 1, pageSize: 1 })
        user.followingCount = followingRes.total || (followingRes.list ? followingRes.list.length : 0)
        console.log('关注数量:', user.followingCount)
      } catch (err) {
        console.error('获取关注数量失败:', err)
        user.followingCount = 0
      }
      
      // 获取粉丝数量
      try {
        const followersRes = await userApi.getUserFollowers(this.data.id, { page: 1, pageSize: 1 })
        user.followerCount = followersRes.total || (followersRes.list ? followersRes.list.length : 0)
        console.log('粉丝数量:', user.followerCount)
      } catch (err) {
        console.error('获取粉丝数量失败:', err)
        user.followerCount = 0
      }
      
      // 获取获赞数量（暂时设为0，如果后端有接口可以调用）
      user.likeCount = 0
      
      // 检查是否已关注（如果是自己，则不需要检查）
      if (auth.isLoggedIn()) {
        const currentUser = auth.getUserInfo()
        if (currentUser && currentUser.id != this.data.id) {
          // 通过获取当前用户的关注列表来判断是否已关注
          try {
            const myFollowingRes = await userApi.getFollowing({ page: 1, pageSize: 999 })
            const followingList = myFollowingRes.list || myFollowingRes.records || []
            user.isFollowing = followingList.some(u => u.id == this.data.id)
            console.log('是否已关注:', user.isFollowing)
          } catch (err) {
            console.error('检查关注状态失败:', err)
            user.isFollowing = false
          }
        } else {
          user.isFollowing = false
        }
      } else {
        user.isFollowing = false
      }
      
      this.setData({
        user,
        isFollowing: user.isFollowing
      })
      
      console.log('页面数据已更新')
      console.log('最终用户数据:', this.data.user)
    } catch (err) {
      console.error('加载用户信息失败:', err)
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    }
  },

  // 加载文章列表
  async loadArticles() {
    if (this.data.loading && this.data.page > 1) return

    this.setData({ loading: true })

    try {
      const res = await userApi.getUserArticles(this.data.id, {
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      console.log('用户文章API原始返回:', res)

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

  // 刷新数据
  async refreshData() {
    this.setData({
      page: 1,
      articles: [],
      hasMore: true
    })

    try {
      await this.loadUser()
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
    }
  },

  // 加载更多
  loadMore() {
    if (!this.data.hasMore || this.data.loading) return

    this.setData({ page: this.data.page + 1 })
    this.loadArticles()
  },

  // 关注/取消关注
  async handleFollow() {
    if (!auth.checkLogin()) return

    try {
      if (this.data.isFollowing) {
        await userApi.unfollow(this.data.id)
        this.setData({
          isFollowing: false,
          'user.followerCount': this.data.user.followerCount - 1
        })
        wx.showToast({
          title: '已取消关注',
          icon: 'success'
        })
      } else {
        await userApi.follow(this.data.id)
        this.setData({
          isFollowing: true,
          'user.followerCount': this.data.user.followerCount + 1
        })
        wx.showToast({
          title: '关注成功',
          icon: 'success'
        })
      }
    } catch (err) {
      console.error('关注操作失败:', err)
      wx.showToast({
        title: err.message || '操作失败',
        icon: 'none'
      })
    }
  },

  // 发送消�?
  sendMessage() {
    if (!auth.checkLogin()) return

    wx.navigateTo({
      url: `/pages/message/chat/index?userId=${this.data.id}`,
      fail: () => {
        wx.showToast({
          title: '功能开发中',
          icon: 'none'
        })
      }
    })
  },

  // 切换标签
  onTabChange(e) {
    const index = e.detail.index || e.detail.name
    
    // 如果切换到同一个标签，不重新加载
    if (this.data.activeTab === index) {
      return
    }
    
    console.log('切换标签到:', index, '当前articles数量:', this.data.articles.length)
    
    this.setData({
      activeTab: index,
      page: 1,
      articles: [],
      hasMore: true,
      loading: false
    })

    console.log('已清空articles，准备加载新数据')

    if (index === 0) {
      console.log('加载文章列表')
      this.loadArticles()
    } else if (index === 1) {
      console.log('加载关注列表')
      this.loadFollowing()
    } else if (index === 2) {
      console.log('加载粉丝列表')
      this.loadFollowers()
    }
  },

  // 加载关注列表
  async loadFollowing() {
    if (this.data.loading && this.data.page > 1) return
    
    this.setData({ loading: true })

    try {
      const res = await userApi.getUserFollowing(this.data.id, {
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      console.log('关注列表API返回:', res)

      let records = []
      if (Array.isArray(res)) {
        records = res
      } else if (res.list) {
        records = res.list
      } else if (res.records) {
        records = res.records
      } else if (res.data) {
        if (Array.isArray(res.data)) {
          records = res.data
        } else if (res.data.list) {
          records = res.data.list
        } else if (res.data.records) {
          records = res.data.records
        }
      }

      console.log('提取的关注列表:', records)

      // 处理头像
      records = records.map(user => ({
        ...user,
        avatar: imageUtil.getImageUrl(user.avatar)
      }))

      this.setData({
        articles: this.data.page === 1 ? records : [...this.data.articles, ...records],
        hasMore: records.length === this.data.pageSize,
        loading: false
      })
    } catch (err) {
      console.error('加载关注列表失败:', err)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    }
  },

  // 加载粉丝列表
  async loadFollowers() {
    if (this.data.loading && this.data.page > 1) return
    
    this.setData({ loading: true })

    try {
      const res = await userApi.getUserFollowers(this.data.id, {
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      console.log('粉丝列表API返回:', res)

      let records = []
      if (Array.isArray(res)) {
        records = res
      } else if (res.list) {
        records = res.list
      } else if (res.records) {
        records = res.records
      } else if (res.data) {
        if (Array.isArray(res.data)) {
          records = res.data
        } else if (res.data.list) {
          records = res.data.list
        } else if (res.data.records) {
          records = res.data.records
        }
      }

      console.log('提取的粉丝列表:', records)

      // 处理头像
      records = records.map(user => ({
        ...user,
        avatar: imageUtil.getImageUrl(user.avatar)
      }))

      this.setData({
        articles: this.data.page === 1 ? records : [...this.data.articles, ...records],
        hasMore: records.length === this.data.pageSize,
        loading: false
      })
    } catch (err) {
      console.error('加载粉丝列表失败:', err)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    }
  },

  // 跳转到文章详�?
  goToDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/article/detail/index?id=${id}`
    })
  },

  // 跳转到用户详�?
  goToUserDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/user/detail/index?id=${id}`
    })
  }
})




