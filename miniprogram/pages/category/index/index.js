// pages/category/index/index.js
const categoryApi = require('../../../api/category')
const tagApi = require('../../../api/tag')
const iconUtil = require('../../../utils/iconMap')

Page({
  data: {
    activeTab: 'category',
    viewMode: 'grid',
    categories: [],
    tags: [],
    hotTags: [], // 热门标签
    loading: true
  },

  onLoad() {
    this.loadCategories()
    this.loadTags()
  },

  onShow() {
    // 设置 tabBar 选中状态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 1
      })
    }
  },

  // 返回
  goBack() {
    wx.navigateBack({
      fail: () => {
        wx.switchTab({
          url: '/pages/index/index'
        })
      }
    })
  },

  // 切换视图模式
  toggleViewMode() {
    this.setData({
      viewMode: this.data.viewMode === 'grid' ? 'list' : 'grid'
    })
  },

  // 切换标签
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab })
  },

  // 加载分类
  async loadCategories() {
    try {
      let categories = await categoryApi.getList()
      
      // 处理数据格式
      if (categories.data && Array.isArray(categories.data)) {
        categories = categories.data
      } else if (!Array.isArray(categories)) {
        categories = []
      }

      console.log('原始分类数据:', categories)

      // 为每个分类映射图标和颜色，并加载热门文章
      const categoriesWithArticles = await Promise.all(
        categories.map(async (category, index) => {
          // 使用后端返回的图标，映射为Vant图标
          const vantIcon = iconUtil.mapIcon(category.icon)
          const bgColor = iconUtil.getColor(index)
          
          console.log(`分类 ${category.name}: Element图标=${category.icon}, Vant图标=${vantIcon}`)
          
          // 加载该分类的热门文章（前3篇）
          let hotArticles = []
          try {
            const articleApi = require('../../../api/article')
            const res = await articleApi.getList({
              categoryId: category.id,
              page: 1,
              pageSize: 3,
              sortBy: 'view',
              sortOrder: 'desc'
            })
            
            // 处理文章数据
            if (Array.isArray(res)) {
              hotArticles = res
            } else if (res.records) {
              hotArticles = res.records
            } else if (res.list) {
              hotArticles = res.list
            } else if (res.data) {
              if (Array.isArray(res.data)) {
                hotArticles = res.data
              } else if (res.data.records) {
                hotArticles = res.data.records
              } else if (res.data.list) {
                hotArticles = res.data.list
              }
            }
          } catch (err) {
            console.error(`加载分类 ${category.name} 的热门文章失败:`, err)
          }
          
          return {
            ...category,
            icon: vantIcon,
            bgColor: bgColor,
            hotArticles: hotArticles
          }
        })
      )

      this.setData({
        categories: categoriesWithArticles,
        loading: false
      })
    } catch (err) {
      console.error('加载分类失败:', err)
      this.setData({ loading: false })
    }
  },

  // 加载标签
  async loadTags() {
    try {
      let tags = await tagApi.getList()
      
      // 处理数据格式
      if (tags.data && Array.isArray(tags.data)) {
        tags = tags.data
      } else if (!Array.isArray(tags)) {
        tags = []
      }

      // 按文章数量排序
      tags.sort((a, b) => (b.articleCount || 0) - (a.articleCount || 0))

      // 提取热门标签（前4个）
      const hotTags = tags.slice(0, 4)

      this.setData({ 
        tags,
        hotTags
      })
    } catch (err) {
      console.error('加载标签失败:', err)
    }
  },

  // 跳转到分类详情
  goToCategoryDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/category/detail/index?id=${id}`
    })
  },

  // 跳转到标签详情
  goToTagDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/tag/detail/index?id=${id}`
    })
  },

  // 跳转到文章详情
  goToArticleDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/article/detail/index?id=${id}`
    })
  }
})
