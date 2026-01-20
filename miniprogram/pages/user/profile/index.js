// pages/user/profile/index.js
const auth = require('../../../utils/auth')
const userApi = require('../../../api/user')
const imageUtil = require('../../../utils/image')

Page({
  data: {
    userInfo: null,
    isLoggedIn: false,
    // 我的内容菜单
    myContentMenus: [
      {
        icon: 'bookmark-o',
        iconBg: '#667eea',
        title: '我的收藏',
        count: 0,
        url: '/pages/user/favorites/index'
      },
      {
        icon: 'like-o',
        iconBg: '#f093fb',
        title: '我的点赞',
        count: 0,
        url: '/pages/user/likes/index'
      },
      {
        icon: 'comment-o',
        iconBg: '#4facfe',
        title: '我的评论',
        count: 0,
        url: '/pages/user/comments/index'
      }
    ],
    // 社交互动菜单
    socialMenus: [
      {
        icon: 'friends-o',
        iconBg: '#a8edea',
        title: '我的关注',
        count: 0,
        url: '/pages/user/following/index'
      },
      {
        icon: 'friends-o',
        iconBg: '#ffecd2',
        title: '我的粉丝',
        count: 0,
        url: '/pages/user/followers/index'
      },
      {
        icon: 'bell',
        iconBg: '#ff9a9e',
        title: '消息通知',
        badge: 0,
        url: '/pages/notification/index/index'
      }
    ]
  },

  onShow() {
    this.checkLoginStatus()
    
    // 设置 tabBar 选中状态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 3
      })
    }
  },

  // 检查登录状态
  checkLoginStatus() {
    const isLoggedIn = auth.isLoggedIn()
    
    if (isLoggedIn) {
      const userInfo = auth.getUserInfo()
      this.setData({
        isLoggedIn: true,
        userInfo
      })
      
      // 刷新用户信息
      this.refreshUserInfo()
    } else {
      this.setData({
        isLoggedIn: false,
        userInfo: null
      })
    }
  },

  // 刷新用户信息
  async refreshUserInfo() {
    try {
      let userInfo = await userApi.getUserInfo()
      
      // 处理图片路径
      userInfo = imageUtil.processUserImages(userInfo)
      
      // 判断是否是VIP
      userInfo.isVip = userInfo.vipLevel && userInfo.vipLevel > 0
      
      // 格式化VIP到期日期
      if (userInfo.vipExpireTime) {
        const date = new Date(userInfo.vipExpireTime)
        userInfo.vipExpireDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
      }
      
      // 保存用户信息
      auth.setUserInfo(userInfo)
      this.setData({ userInfo })
      
      // 更新菜单数据
      this.updateMenuCounts(userInfo)
      
      // 加载统计数据
      this.loadUserStats()
    } catch (err) {
      console.error('刷新用户信息失败:', err)
    }
  },

  // 加载用户统计数据
  async loadUserStats() {
    try {
      const userId = this.data.userInfo.id
      
      // 获取用户文章数量
      const articleApi = require('../../../api/article')
      const articleRes = await userApi.getUserArticles(userId, {
        page: 1,
        pageSize: 1
      })
      
      let articleCount = 0
      if (articleRes.total !== undefined) {
        articleCount = articleRes.total
      } else if (articleRes.data && articleRes.data.total !== undefined) {
        articleCount = articleRes.data.total
      } else if (Array.isArray(articleRes)) {
        articleCount = articleRes.length
      }
      
      // 获取关注数量
      let followCount = 0
      try {
        const followRes = await userApi.getFollowing({ page: 1, pageSize: 1 })
        if (followRes.total !== undefined) {
          followCount = followRes.total
        } else if (followRes.data && followRes.data.total !== undefined) {
          followCount = followRes.data.total
        }
      } catch (err) {
        console.error('获取关注数量失败:', err)
        followCount = this.data.userInfo.followCount || 0
      }
      
      // 获取粉丝数量
      let fansCount = 0
      try {
        const fansRes = await userApi.getFollowers({ page: 1, pageSize: 1 })
        if (fansRes.total !== undefined) {
          fansCount = fansRes.total
        } else if (fansRes.data && fansRes.data.total !== undefined) {
          fansCount = fansRes.data.total
        }
      } catch (err) {
        console.error('获取粉丝数量失败:', err)
        fansCount = this.data.userInfo.fansCount || 0
      }
      
      // 获取收藏数量
      let favoriteCount = 0
      try {
        const favRes = await userApi.getFavorites({ page: 1, pageSize: 1 })
        if (favRes.total !== undefined) {
          favoriteCount = favRes.total
        } else if (favRes.data && favRes.data.total !== undefined) {
          favoriteCount = favRes.data.total
        }
      } catch (err) {
        console.error('获取收藏数量失败:', err)
        favoriteCount = this.data.userInfo.favoriteCount || 0
      }
      
      // 获取文章被点赞总数（所有文章获得的点赞数之和）
      let totalArticleLikes = 0
      try {
        // 获取用户所有文章
        const allArticlesRes = await userApi.getUserArticles(userId, {
          page: 1,
          pageSize: 999 // 获取所有文章
        })
        
        let articles = []
        if (allArticlesRes.list) {
          articles = allArticlesRes.list
        } else if (allArticlesRes.records) {
          articles = allArticlesRes.records
        } else if (Array.isArray(allArticlesRes)) {
          articles = allArticlesRes
        } else if (allArticlesRes.data) {
          if (Array.isArray(allArticlesRes.data)) {
            articles = allArticlesRes.data
          } else if (allArticlesRes.data.list) {
            articles = allArticlesRes.data.list
          } else if (allArticlesRes.data.records) {
            articles = allArticlesRes.data.records
          }
        }
        
        // 计算所有文章的点赞数之和
        totalArticleLikes = articles.reduce((sum, article) => {
          return sum + (article.likeCount || 0)
        }, 0)
      } catch (err) {
        console.error('获取文章点赞总数失败:', err)
        totalArticleLikes = 0
      }
      
      // 获取我的点赞数量（用户点赞过的文章数）
      let myLikeCount = 0
      try {
        const likeRes = await userApi.getLikes({ page: 1, pageSize: 1 })
        if (likeRes.total !== undefined) {
          myLikeCount = likeRes.total
        } else if (likeRes.data && likeRes.data.total !== undefined) {
          myLikeCount = likeRes.data.total
        }
      } catch (err) {
        console.error('获取我的点赞数量失败:', err)
        myLikeCount = 0
      }
      
      // 获取评论数量
      let commentCount = 0
      try {
        const commentRes = await userApi.getComments({ page: 1, pageSize: 1 })
        if (commentRes.total !== undefined) {
          commentCount = commentRes.total
        } else if (commentRes.data && commentRes.data.total !== undefined) {
          commentCount = commentRes.data.total
        }
      } catch (err) {
        console.error('获取评论数量失败:', err)
        commentCount = this.data.userInfo.commentCount || 0
      }
      
      // 更新用户信息
      this.setData({
        'userInfo.articleCount': articleCount,
        'userInfo.followCount': followCount,
        'userInfo.fansCount': fansCount,
        'userInfo.favoriteCount': favoriteCount,
        'userInfo.totalArticleLikes': totalArticleLikes,
        'userInfo.myLikeCount': myLikeCount,
        'userInfo.commentCount': commentCount
      })
      
      // 更新菜单计数
      this.updateMenuCounts({
        ...this.data.userInfo,
        articleCount,
        followCount,
        fansCount,
        favoriteCount,
        myLikeCount,
        commentCount
      })
    } catch (err) {
      console.error('加载统计数据失败:', err)
    }
  },

  // 更新菜单计数
  updateMenuCounts(userInfo) {
    const myContentMenus = this.data.myContentMenus.map(item => {
      if (item.title === '我的收藏') {
        return { ...item, count: userInfo.favoriteCount || 0 }
      }
      if (item.title === '我的点赞') {
        return { ...item, count: userInfo.myLikeCount || 0 }
      }
      if (item.title === '我的评论') {
        return { ...item, count: userInfo.commentCount || 0 }
      }
      return item
    })

    const socialMenus = this.data.socialMenus.map(item => {
      if (item.title === '我的关注') {
        return { ...item, count: userInfo.followCount || 0 }
      }
      if (item.title === '我的粉丝') {
        return { ...item, count: userInfo.fansCount || 0 }
      }
      if (item.title === '消息通知') {
        return { ...item, badge: userInfo.unreadNotification || 0 }
      }
      return item
    })

    this.setData({ myContentMenus, socialMenus })
  },

  // 跳转登录
  goToLogin() {
    wx.navigateTo({
      url: '/pages/login/index/index'
    })
  },

  // 跳转VIP中心
  goToVipCenter() {
    wx.navigateTo({
      url: '/packageVip/pages/center/index'
    })
  },

  // 跳转设置
  goToSettings() {
    if (!this.data.isLoggedIn) {
      this.goToLogin()
      return
    }
    wx.navigateTo({
      url: '/pages/user/settings/index'
    })
  },

  // 跳转菜单页面
  goToPage(e) {
    const url = e.currentTarget.dataset.url
    
    if (!this.data.isLoggedIn) {
      this.goToLogin()
      return
    }

    wx.navigateTo({ url })
  },

  // 查看我的主页
  goToMyProfile() {
    if (!this.data.isLoggedIn) {
      this.goToLogin()
      return
    }

    wx.navigateTo({
      url: `/pages/user/detail/index?id=${this.data.userInfo.id}`
    })
  },

  // 跳转我的文章
  goToMyArticles() {
    if (!this.data.isLoggedIn) {
      this.goToLogin()
      return
    }
    wx.navigateTo({
      url: `/pages/user/detail/index?id=${this.data.userInfo.id}`
    })
  },
  
  // 跳转我的点赞
  goToMyLikes() {
    if (!this.data.isLoggedIn) {
      this.goToLogin()
      return
    }
    wx.navigateTo({
      url: '/pages/user/favorites/index?tab=like'
    })
  },

  // 跳转关注列表
  goToFollowing() {
    if (!this.data.isLoggedIn) {
      this.goToLogin()
      return
    }
    wx.navigateTo({
      url: '/pages/user/following/index'
    })
  },

  // 跳转粉丝列表
  goToFollowers() {
    if (!this.data.isLoggedIn) {
      this.goToLogin()
      return
    }
    wx.navigateTo({
      url: '/pages/user/followers/index'
    })
  },

  // 退出登录
  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          auth.clearAuth()
          this.setData({
            isLoggedIn: false,
            userInfo: null
          })
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          })
        }
      }
    })
  }
})
