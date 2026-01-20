// iconMap.js - Element Plus 图标到 Vant Weapp 图标映射

/**
 * Element Plus 图标名称映射到 Vant Weapp 图标名称
 * 根据图标语义进行映射
 */
const iconMap = {
  // 设备类
  'Monitor': 'desktop-o',
  'Cpu': 'desktop-o',
  'Laptop': 'desktop-o',
  'Computer': 'desktop-o',
  
  // 设置/工具类
  'Setting': 'setting-o',
  'Tools': 'setting-o',
  'Operation': 'setting-o',
  'Wrench': 'setting-o',
  
  // 火焰/热门类
  'Hotspot': 'fire-o',
  'Fire': 'fire-o',
  'Hot': 'fire-o',
  
  // 手机/移动类
  'Iphone': 'phone-o',
  'Phone': 'phone-o',
  'Cellphone': 'phone-o',
  'Mobile': 'phone-o',
  
  // 文档/记录类
  'Document': 'records-o',
  'Files': 'records-o',
  'Folder': 'records-o',
  'Notebook': 'notes-o',
  'Memo': 'notes-o',
  'Edit': 'edit',
  
  // 钻石/宝石类
  'Gem': 'diamond-o',
  'Diamond': 'diamond-o',
  
  // 星星/收藏类
  'Star': 'star-o',
  'StarFilled': 'star',
  'Collection': 'star-o',
  'Like': 'good-job-o',
  
  // 应用/网格类
  'Grid': 'apps-o',
  'Menu': 'apps-o',
  'Apps': 'apps-o',
  
  // 数据库类
  'Coin': 'gold-coin-o',
  'Money': 'gold-coin-o',
  'Database': 'records-o',
  
  // 图片/媒体类
  'Picture': 'photo-o',
  'Camera': 'photograph',
  'Video': 'video-o',
  
  // 购物类
  'ShoppingCart': 'cart-o',
  'Shop': 'shop-o',
  'Goods': 'goods-collect-o',
  
  // 用户类
  'User': 'user-o',
  'UserFilled': 'contact',
  'Avatar': 'user-circle-o',
  
  // 位置类
  'Location': 'location-o',
  'MapLocation': 'location-o',
  
  // 时间类
  'Clock': 'clock-o',
  'Timer': 'clock-o',
  
  // 消息类
  'ChatDotRound': 'chat-o',
  'Message': 'comment-o',
  'Bell': 'bell',
  
  // 搜索类
  'Search': 'search',
  'Zoom': 'search',
  
  // 标签类
  'PriceTag': 'label-o',
  'Ticket': 'coupon-o',
  
  // 箭头类
  'ArrowRight': 'arrow',
  'ArrowLeft': 'arrow-left',
  'ArrowUp': 'arrow-up',
  'ArrowDown': 'arrow-down',
  
  // 其他常用
  'House': 'wap-home-o',
  'Home': 'wap-home-o',
  'Plus': 'plus',
  'Close': 'cross',
  'Check': 'success',
  'Delete': 'delete-o',
  'Share': 'share-o',
  'Download': 'down',
  'Upload': 'upgrade',
  'Refresh': 'replay',
  'More': 'ellipsis'
}

/**
 * 颜色映射 - 为不同类型的分类提供默认颜色
 */
const colorMap = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', // 蓝紫
  'linear-gradient(135deg, #10b981 0%, #059669 100%)', // 绿色
  'linear-gradient(135deg, #a855f7 0%, #7c3aed 100%)', // 紫色
  'linear-gradient(135deg, #ec4899 0%, #db2777 100%)', // 粉色
  'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)', // 橙色
  'linear-gradient(135deg, #06b6d4 0%, #0891b2 100%)', // 青色
  'linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%)', // 深紫
  'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)'  // 蓝色
]

/**
 * 将 Element Plus 图标名称转换为 Vant Weapp 图标名称
 * @param {string} elementIcon - Element Plus 图标名称
 * @returns {string} Vant Weapp 图标名称
 */
function mapIcon(elementIcon) {
  if (!elementIcon) {
    return 'apps-o' // 默认图标
  }
  
  // 移除可能的前缀
  const iconName = elementIcon.replace(/^(el-icon-|icon-)/i, '')
  
  // 查找映射
  const vantIcon = iconMap[iconName]
  
  if (vantIcon) {
    return vantIcon
  }
  
  // 如果没有找到映射，尝试模糊匹配
  const lowerIconName = iconName.toLowerCase()
  
  if (lowerIconName.includes('phone') || lowerIconName.includes('mobile')) {
    return 'phone-o'
  }
  if (lowerIconName.includes('computer') || lowerIconName.includes('desktop')) {
    return 'desktop-o'
  }
  if (lowerIconName.includes('fire') || lowerIconName.includes('hot')) {
    return 'fire-o'
  }
  if (lowerIconName.includes('star')) {
    return 'star-o'
  }
  if (lowerIconName.includes('setting') || lowerIconName.includes('config')) {
    return 'setting-o'
  }
  if (lowerIconName.includes('user') || lowerIconName.includes('person')) {
    return 'user-o'
  }
  if (lowerIconName.includes('home') || lowerIconName.includes('house')) {
    return 'wap-home-o'
  }
  
  // 默认返回应用图标
  return 'apps-o'
}

/**
 * 获取颜色
 * @param {number} index - 索引
 * @returns {string} 渐变色
 */
function getColor(index) {
  return colorMap[index % colorMap.length]
}

module.exports = {
  mapIcon,
  getColor,
  iconMap,
  colorMap
}
