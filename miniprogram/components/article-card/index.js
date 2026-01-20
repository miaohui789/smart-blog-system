// components/article-card/index.js
Component({
  properties: {
    article: {
      type: Object,
      value: {}
    }
  },

  methods: {
    // 点击卡片
    handleTap() {
      this.triggerEvent('tap', { id: this.data.article.id })
    },

    // 点击作者
    handleAuthorTap(e) {
      e.stopPropagation()
      this.triggerEvent('authortap', { userId: this.data.article.author.id })
    }
  }
})
