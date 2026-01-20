// components/vip-badge/index.js
Component({
  properties: {
    level: {
      type: Number,
      value: 1
    },
    size: {
      type: String,
      value: 'small' // small, medium, large
    },
    showText: {
      type: Boolean,
      value: true
    }
  },

  data: {
    levelText: {
      1: '普通VIP',
      2: '高级VIP',
      3: '超级VIP'
    }
  }
})
