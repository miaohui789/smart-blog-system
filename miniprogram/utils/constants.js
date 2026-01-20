// constants.js - 常量定义

// 默认头像（使用微信默认头像或网络图片）
const DEFAULT_AVATAR = 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'

// VIP 等级配置
const VIP_LEVELS = {
  1: {
    name: '普通VIP',
    color: '#cd7f32',
    downloadLimit: 5,
    heatLimit: 3,
    heatValue: 10
  },
  2: {
    name: '高级VIP',
    color: '#c0c0c0',
    downloadLimit: 20,
    heatLimit: 10,
    heatValue: 30
  },
  3: {
    name: '超级VIP',
    color: '#ffd700',
    downloadLimit: -1, // -1 表示无限制
    heatLimit: 30,
    heatValue: 100
  }
}

// 文章状态
const ARTICLE_STATUS = {
  DRAFT: 0,
  PUBLISHED: 1,
  OFFLINE: 2
}

// 评论状态
const COMMENT_STATUS = {
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 2
}

// 通知类型
const NOTIFICATION_TYPE = {
  LIKE_ARTICLE: 'LIKE_ARTICLE',
  FAVORITE_ARTICLE: 'FAVORITE_ARTICLE',
  COMMENT: 'COMMENT',
  REPLY: 'REPLY',
  FOLLOW: 'FOLLOW',
  SYSTEM: 'SYSTEM'
}

// 消息类型
const MESSAGE_TYPE = {
  TEXT: 'text',
  IMAGE: 'image',
  SYSTEM: 'system'
}

module.exports = {
  DEFAULT_AVATAR,
  VIP_LEVELS,
  ARTICLE_STATUS,
  COMMENT_STATUS,
  NOTIFICATION_TYPE,
  MESSAGE_TYPE
}
