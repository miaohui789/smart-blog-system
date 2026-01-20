// image.js - 图片处理工具
const config = require('./config')

/**
 * 获取完整的图片 URL
 * @param {String} path 图片路径
 * @returns {String} 完整的图片 URL
 */
function getImageUrl(path) {
  if (!path) {
    return config.DEFAULT_AVATAR
  }

  // 如果已经是完整的 URL，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }

  // 如果是相对路径，拼接服务器地址
  const baseUrl = config.BASE_URL.replace('/api', '')
  
  // 确保路径以 / 开头
  const imagePath = path.startsWith('/') ? path : `/${path}`
  
  return `${baseUrl}${imagePath}`
}

/**
 * 获取用户头像 URL
 * @param {String} avatar 头像路径
 * @returns {String} 完整的头像 URL
 */
function getAvatarUrl(avatar) {
  if (!avatar) {
    return config.DEFAULT_AVATAR
  }
  return getImageUrl(avatar)
}

/**
 * 获取文章封面 URL
 * @param {String} cover 封面路径
 * @returns {String} 完整的封面 URL
 */
function getCoverUrl(cover) {
  if (!cover) {
    return '' // 封面可以为空
  }
  return getImageUrl(cover)
}

/**
 * 处理文章对象中的图片路径
 * @param {Object} article 文章对象
 * @returns {Object} 处理后的文章对象
 */
function processArticleImages(article) {
  if (!article) return article

  // 兼容多种作者字段名：author, user, createUser, authorInfo
  let author = article.author || article.user || article.createUser || article.authorInfo || null
  
  // 如果 author 是字符串（可能是 JSON 字符串），尝试解析
  if (typeof author === 'string') {
    try {
      author = JSON.parse(author)
    } catch (e) {
      author = null
    }
  }
  
  // 如果没有 author 对象，但有 userId，创建一个基础的 author 对象
  // 注意：这种情况下只能显示默认信息，需要后端返回完整的 author 对象
  if (!author && article.userId) {
    console.warn(`文章 ${article.id} 缺少作者信息，只有 userId: ${article.userId}`)
    author = {
      id: article.userId,
      nickname: article.authorName || article.createUserName || '匿名用户',
      avatar: article.authorAvatar || '',
      vipLevel: article.vipLevel || 0
    }
  }
  
  // 如果还是没有 author，使用默认值
  if (!author) {
    console.warn(`文章 ${article.id} 完全缺少作者信息`)
    author = {
      id: 0,
      nickname: '匿名用户',
      avatar: '',
      vipLevel: 0
    }
  }
  
  // 获取作者ID，兼容多种字段名
  const authorId = author.id || author.userId || article.userId || article.authorId || article.createUserId || 0
  
  // 获取作者昵称，兼容多种字段名
  const authorNickname = author.nickname || author.username || author.name || article.authorName || article.createUserName || '匿名用户'
  
  // 获取作者头像，兼容多种字段名
  const authorAvatar = author.avatar || author.avatarUrl || article.authorAvatar || ''
  
  // 获取VIP等级
  const vipLevel = author.vipLevel || author.vip_level || article.vipLevel || 0
  
  return {
    ...article,
    cover: getCoverUrl(article.cover),
    author: {
      id: authorId,
      nickname: authorNickname,
      avatar: getAvatarUrl(authorAvatar),
      vipLevel: vipLevel,
      isVip: vipLevel > 0
    }
  }
}

/**
 * 批量处理文章列表中的图片路径
 * @param {Array} articles 文章列表
 * @returns {Array} 处理后的文章列表
 */
function processArticleList(articles) {
  if (!Array.isArray(articles)) {
    console.warn('processArticleList: 输入不是数组', articles)
    return []
  }
  
  console.log(`processArticleList: 处理 ${articles.length} 篇文章`)
  
  const processed = articles.map((article, index) => {
    if (index === 0) {
      console.log('第一篇文章处理前:', JSON.stringify(article, null, 2))
    }
    
    const result = processArticleImages(article)
    
    if (index === 0) {
      console.log('第一篇文章处理后:', JSON.stringify(result, null, 2))
    }
    
    return result
  })
  
  return processed
}

/**
 * 处理用户对象中的图片路径
 * @param {Object} user 用户对象
 * @returns {Object} 处理后的用户对象
 */
function processUserImages(user) {
  if (!user) return user

  return {
    ...user,
    avatar: getAvatarUrl(user.avatar)
  }
}

/**
 * 处理评论对象中的图片路径
 * @param {Object} comment 评论对象
 * @returns {Object} 处理后的评论对象
 */
function processCommentImages(comment) {
  if (!comment) return comment

  return {
    ...comment,
    user: processUserImages(comment.user)
  }
}

/**
 * 批量处理评论列表中的图片路径
 * @param {Array} comments 评论列表
 * @returns {Array} 处理后的评论列表
 */
function processCommentList(comments) {
  if (!Array.isArray(comments)) return []
  
  return comments.map(comment => processCommentImages(comment))
}

module.exports = {
  getImageUrl,
  getAvatarUrl,
  getCoverUrl,
  processArticleImages,
  processArticleList,
  processUserImages,
  processCommentImages,
  processCommentList
}
