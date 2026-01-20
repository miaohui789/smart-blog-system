# 图片资源说明

## TabBar 图标

当前使用的是 Emoji 表情作为 tabBar 图标（自定义 tabBar）。

如果需要使用图片图标，请按以下步骤操作：

### 1. 准备图标文件

在 `miniprogram/images/tabbar/` 目录下放置以下图标文件：

- `home.png` - 首页图标（未选中）
- `home-active.png` - 首页图标（选中）
- `category.png` - 分类图标（未选中）
- `category-active.png` - 分类图标（选中）
- `message.png` - 消息图标（未选中）
- `message-active.png` - 消息图标（选中）
- `user.png` - 我的图标（未选中）
- `user-active.png` - 我的图标（选中）

### 2. 图标规格要求

- 尺寸：81px * 81px
- 格式：PNG
- 背景：透明
- 颜色：
  - 未选中：#71717a（灰色）
  - 选中：#a855f7（紫色）

### 3. 修改配置

如果要使用图片图标，需要修改 `app.json`：

```json
{
  "tabBar": {
    "custom": false,  // 改为 false
    "color": "#71717a",
    "selectedColor": "#a855f7",
    "backgroundColor": "#1a1a1a",
    "borderStyle": "black",
    "list": [
      {
        "pagePath": "pages/index/index",
        "text": "首页",
        "iconPath": "images/tabbar/home.png",
        "selectedIconPath": "images/tabbar/home-active.png"
      },
      // ... 其他配置
    ]
  }
}
```

### 4. 图标设计建议

推荐使用以下图标库：

- [Iconfont](https://www.iconfont.cn/) - 阿里巴巴矢量图标库
- [IconPark](https://iconpark.oceanengine.com/) - 字节跳动图标库
- [Feather Icons](https://feathericons.com/) - 简洁线性图标

### 5. 在线图标生成工具

- [Figma](https://www.figma.com/) - 专业设计工具
- [Canva](https://www.canva.com/) - 在线设计平台
- [Photopea](https://www.photopea.com/) - 在线 PS

## 其他图片资源

### 默认头像
- 路径：`images/default-avatar.png`
- 尺寸：200px * 200px

### Logo
- 路径：`images/logo.png`
- 尺寸：512px * 512px

### 占位图
- 路径：`images/placeholder.png`
- 尺寸：根据实际需求

## 注意事项

1. 所有图片都应该经过压缩优化
2. 建议使用 WebP 格式以减小体积
3. 图片总大小不要超过 2MB（小程序主包限制）
4. 大图片建议使用 CDN 外链
