// mock.js - 模拟数据（用于开发测试）

// 是否启用模拟数据
const ENABLE_MOCK = false // 改为 true 启用模拟数据

// 模拟文章列表
const mockArticles = [
  {
    id: 1,
    title: '欢迎使用智能博客系统',
    summary: '这是一个现代化的博客系统，支持 Markdown 编辑、评论互动、VIP 会员等功能。',
    cover: 'https://picsum.photos/400/300?random=1',
    content: '# 欢迎\n\n这是文章内容...',
    contentHtml: '<h1>欢迎</h1><p>这是文章内容...</p>',
    viewCount: 1234,
    likeCount: 56,
    commentCount: 12,
    favoriteCount: 23,
    createTime: '2026-01-17 10:00:00',
    author: {
      id: 1,
      nickname: '系统管理员',
      avatar: 'https://picsum.photos/100/100?random=1',
      isVip: true,
      vipLevel: 3
    },
    tags: [
      { id: 1, name: '公告' },
      { id: 2, name: '教程' }
    ],
    isLiked: false,
    isFavorited: false
  },
  {
    id: 2,
    title: 'Vue 3 开发实战指南',
    summary: '深入学习 Vue 3 的核心特性，包括 Composition API、响应式系统、组件化开发等内容。',
    cover: 'https://picsum.photos/400/300?random=2',
    content: '# Vue 3\n\n内容...',
    contentHtml: '<h1>Vue 3</h1><p>内容...</p>',
    viewCount: 2345,
    likeCount: 89,
    commentCount: 34,
    favoriteCount: 67,
    createTime: '2026-01-16 15:30:00',
    author: {
      id: 2,
      nickname: '前端开发者',
      avatar: 'https://picsum.photos/100/100?random=2',
      isVip: true,
      vipLevel: 2
    },
    tags: [
      { id: 3, name: 'Vue' },
      { id: 4, name: '前端' }
    ],
    isLiked: false,
    isFavorited: false
  },
  {
    id: 3,
    title: 'Spring Boot 微服务架构实践',
    summary: '从零开始构建 Spring Boot 微服务应用，涵盖服务注册、配置中心、网关等核心组件。',
    cover: 'https://picsum.photos/400/300?random=3',
    content: '# Spring Boot\n\n内容...',
    contentHtml: '<h1>Spring Boot</h1><p>内容...</p>',
    viewCount: 1890,
    likeCount: 45,
    commentCount: 23,
    favoriteCount: 34,
    createTime: '2026-01-15 09:20:00',
    author: {
      id: 3,
      nickname: '后端工程师',
      avatar: 'https://picsum.photos/100/100?random=3',
      isVip: false,
      vipLevel: 0
    },
    tags: [
      { id: 5, name: 'Java' },
      { id: 6, name: '后端' }
    ],
    isLiked: false,
    isFavorited: false
  }
]

// 模拟分类列表
const mockCategories = [
  { id: 1, name: '前端开发', icon: '💻', articleCount: 45 },
  { id: 2, name: '后端开发', icon: '⚙️', articleCount: 38 },
  { id: 3, name: '移动开发', icon: '📱', articleCount: 23 },
  { id: 4, name: '数据库', icon: '🗄️', articleCount: 19 },
  { id: 5, name: '运维部署', icon: '🚀', articleCount: 15 },
  { id: 6, name: '算法', icon: '🧮', articleCount: 27 }
]

// 模拟评论列表
const mockComments = [
  {
    id: 1,
    content: '写得很好，学到了很多！',
    createTime: '2026-01-17 11:30:00',
    likeCount: 5,
    user: {
      id: 4,
      nickname: '读者A',
      avatar: 'https://picsum.photos/100/100?random=4',
      isVip: false,
      vipLevel: 0
    }
  },
  {
    id: 2,
    content: '期待更多这样的教程',
    createTime: '2026-01-17 12:00:00',
    likeCount: 3,
    user: {
      id: 5,
      nickname: '读者B',
      avatar: 'https://picsum.photos/100/100?random=5',
      isVip: true,
      vipLevel: 1
    }
  }
]

/**
 * 模拟 API 请求
 */
function mockRequest(data, delay = 500) {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(data)
    }, delay)
  })
}

/**
 * 获取文章列表
 */
function getArticleList(params = {}) {
  const { page = 1, pageSize = 10 } = params
  const start = (page - 1) * pageSize
  const end = start + pageSize
  const list = mockArticles.slice(start, end)
  
  return mockRequest({
    records: list,
    total: mockArticles.length,
    page,
    pageSize
  })
}

/**
 * 获取文章详情
 */
function getArticleDetail(id) {
  const article = mockArticles.find(item => item.id == id)
  return mockRequest(article || mockArticles[0])
}

/**
 * 搜索文章
 */
function searchArticles(keyword) {
  const list = mockArticles.filter(item => 
    item.title.includes(keyword) || item.summary.includes(keyword)
  )
  return mockRequest({ records: list })
}

/**
 * 获取分类列表
 */
function getCategoryList() {
  return mockRequest(mockCategories)
}

/**
 * 获取评论列表
 */
function getCommentList() {
  return mockRequest({ records: mockComments })
}

module.exports = {
  ENABLE_MOCK,
  mockRequest,
  getArticleList,
  getArticleDetail,
  searchArticles,
  getCategoryList,
  getCommentList
}
