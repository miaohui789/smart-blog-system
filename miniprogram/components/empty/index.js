// components/empty/index.js
Component({
  properties: {
    icon: {
      type: String,
      value: '📝'
    },
    text: {
      type: String,
      value: '暂无数据'
    },
    buttonText: {
      type: String,
      value: ''
    }
  },

  methods: {
    handleButtonTap() {
      this.triggerEvent('buttontap')
    }
  }
})
