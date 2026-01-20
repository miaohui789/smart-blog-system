// packageVip/pages/center/index.js
const auth = require('../../../utils/auth')
const vipApi = require('../../../api/vip')
const formatUtil = require('../../../utils/format')

Page({
  data: {
    isVip: false,
    vipLevel: 0,
    vipExpireDate: '',
    vipProgress: 0,
    remainDays: 0,
    keyCode: '',
    
    // VIP特权
    privileges: [
      {
        id: 1,
        icon: 'fire',
        name: '文章加热',
        desc: '提升文章曝光',
        color: 'linear-gradient(135deg, #f59e0b 0%, #f97316 100%)'
      },
      {
        id: 2,
        icon: 'star',
        name: '专属标识',
        desc: 'VIP身份标识',
        color: 'linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)'
      },
      {
        id: 3,
        icon: 'eye',
        name: '优先展示',
        desc: '内容优先推荐',
        color: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)'
      },
      {
        id: 4,
        icon: 'medal',
        name: '专属特权',
        desc: '更多功能解锁',
        color: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)'
      }
    ],

    // VIP等级
    levels: [
      {
        level: 1,
        name: '青铜会员',
        desc: '基础特权，文章加热次数有限',
        color: 'linear-gradient(135deg, #cd7f32 0%, #b87333 100%)'
      },
      {
        level: 2,
        name: '白银会员',
        desc: '进阶特权，更多加热次数',
        color: 'linear-gradient(135deg, #c0c0c0 0%, #a8a8a8 100%)'
      },
      {
        level: 3,
        name: '黄金会员',
        desc: '高级特权，无限加热次数',
        color: 'linear-gradient(135deg, #ffd700 0%, #ffed4e 100%)'
      }
    ],

    // 使用记录
    records: [],

    // 常见问题
    faqs: [
      {
        id: 1,
        question: '如何获取VIP激活码？',
        answer: '您可以通过参与平台活动、完成任务或联系管理员获取VIP激活码。',
        expanded: false
      },
      {
        id: 2,
        question: 'VIP有效期如何计算？',
        answer: 'VIP有效期从激活之日起计算，不同等级的VIP有效期不同，具体以激活码说明为准。',
        expanded: false
      },
      {
        id: 3,
        question: '文章加热功能如何使用？',
        answer: '在文章详情页点击"加热"按钮即可使用，加热后文章将获得更多曝光机会。',
        expanded: false
      },
      {
        id: 4,
        question: 'VIP到期后会怎样？',
        answer: 'VIP到期后，您将失去VIP特权，但之前的使用记录和数据不会丢失。',
        expanded: false
      }
    ]
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
    this.loadVipInfo()
  },

  onShow() {
    // 每次显示时刷新VIP信息
    if (auth.isLoggedIn()) {
      this.loadVipInfo()
    }
  },

  // 返回
  goBack() {
    wx.navigateBack()
  },

  // 加载VIP信息
  async loadVipInfo() {
    try {
      const userInfo = auth.getUserInfo()
      
      if (userInfo && userInfo.vipLevel && userInfo.vipLevel > 0) {
        const expireTime = new Date(userInfo.vipExpireTime)
        const now = new Date()
        const remainMs = expireTime.getTime() - now.getTime()
        const remainDays = Math.max(0, Math.ceil(remainMs / (1000 * 60 * 60 * 24)))
        
        // 计算进度（假设VIP总时长为365天）
        const totalDays = 365
        const usedDays = totalDays - remainDays
        const progress = Math.min(100, Math.max(0, (usedDays / totalDays) * 100))

        this.setData({
          isVip: true,
          vipLevel: userInfo.vipLevel,
          vipExpireDate: formatUtil.formatTime(userInfo.vipExpireTime, 'YYYY-MM-DD'),
          remainDays,
          vipProgress: progress
        })

        // 加载使用记录
        this.loadRecords()
      } else {
        this.setData({
          isVip: false,
          vipLevel: 0
        })
      }
    } catch (err) {
      console.error('加载VIP信息失败:', err)
    }
  },

  // 加载使用记录
  async loadRecords() {
    // 这里应该调用API获取使用记录
    // 暂时使用模拟数据
    this.setData({
      records: []
    })
  },

  // 输入激活码
  onKeyCodeChange(e) {
    this.setData({
      keyCode: e.detail
    })
  },

  // 激活VIP
  async handleActivate() {
    const keyCode = this.data.keyCode.trim()

    if (!keyCode) {
      wx.showToast({
        title: '请输入激活码',
        icon: 'none'
      })
      return
    }

    wx.showLoading({
      title: '激活中...',
      mask: true
    })

    try {
      const res = await vipApi.activate(keyCode)
      
      wx.hideLoading()
      
      wx.showToast({
        title: '激活成功',
        icon: 'success'
      })

      // 清空输入框
      this.setData({ keyCode: '' })

      // 刷新用户信息
      setTimeout(async () => {
        try {
          const userApi = require('../../../api/user')
          const userInfo = await userApi.getUserInfo()
          auth.setUserInfo(userInfo)
          this.loadVipInfo()
        } catch (err) {
          console.error('刷新用户信息失败:', err)
        }
      }, 1000)
    } catch (err) {
      wx.hideLoading()
      console.error('激活失败:', err)
      wx.showToast({
        title: err.message || '激活失败',
        icon: 'none'
      })
    }
  },

  // 切换FAQ展开状态
  toggleFaq(e) {
    const id = e.currentTarget.dataset.id
    const faqs = this.data.faqs.map(item => {
      if (item.id === id) {
        return { ...item, expanded: !item.expanded }
      }
      return item
    })
    this.setData({ faqs })
  }
})