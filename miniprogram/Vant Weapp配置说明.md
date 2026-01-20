# Vant Weapp 配置说明

## 📦 安装步骤

### 1. 安装依赖
已在 `package.json` 中配置 Vant Weapp 依赖：

```bash
cd miniprogram
npm install
```

### 2. 构建 npm
在微信开发者工具中：

1. 点击菜单栏：**工具** → **构建 npm**
2. 等待构建完成，会生成 `miniprogram_npm` 目录
3. 勾选 **使用 npm 模块** 选项（在详情 → 本地设置中）

### 3. 配置说明
已在 `project.config.json` 中启用 npm：

```json
{
  "setting": {
    "nodeModules": true
  }
}
```

## 🎨 TabBar 图标更新

### 使用的 Vant 图标

| 页面 | 未选中图标 | 选中图标 | 说明 |
|------|-----------|---------|------|
| 首页 | `wap-home-o` | `wap-home` | 房屋图标 |
| 分类 | `apps-o` | `apps` | 应用网格图标 |
| 消息 | `chat-o` | `chat` | 聊天气泡图标 |
| 我的 | `user-o` | `user` | 用户图标 |

### 图标特点
- **线性/实心切换**：未选中时显示线性图标（-o 后缀），选中时显示实心图标
- **颜色动态**：根据选中状态自动切换颜色
- **尺寸统一**：所有图标统一使用 24px 尺寸
- **动画效果**：选中时图标放大 1.1 倍，顶部显示蓝色渐变指示条

## 🎯 自定义图标

如需更换其他图标，可参考 [Vant Weapp 图标列表](https://vant-contrib.gitee.io/vant-weapp/#/icon)

### 推荐图标组合

#### 方案一：简约风格
```javascript
{
  icon: "home-o",
  selectedIcon: "home"
}
```

#### 方案二：科技风格
```javascript
{
  icon: "fire-o",
  selectedIcon: "fire"
}
```

#### 方案三：商务风格
```javascript
{
  icon: "shop-o",
  selectedIcon: "shop"
}
```

## 📝 使用示例

### 在其他页面使用 Vant 图标

1. **在页面 JSON 中引入**：
```json
{
  "usingComponents": {
    "van-icon": "@vant/weapp/icon/index"
  }
}
```

2. **在 WXML 中使用**：
```xml
<van-icon name="success" size="20px" color="#07c160" />
<van-icon name="star-o" size="24px" />
<van-icon name="like-o" size="28px" color="#3b82f6" />
```

### 常用图标

| 图标名称 | 说明 | 使用场景 |
|---------|------|---------|
| `like-o` / `like` | 点赞 | 文章点赞 |
| `star-o` / `star` | 收藏 | 文章收藏 |
| `comment-o` / `comment` | 评论 | 评论功能 |
| `share-o` / `share` | 分享 | 分享功能 |
| `search` | 搜索 | 搜索框 |
| `arrow-left` | 返回 | 返回按钮 |
| `arrow-right` | 前进 | 下一步 |
| `close` | 关闭 | 关闭弹窗 |
| `success` | 成功 | 成功提示 |
| `fail` | 失败 | 失败提示 |
| `info-o` | 信息 | 信息提示 |
| `warning-o` | 警告 | 警告提示 |
| `bell-o` / `bell` | 通知 | 通知消息 |
| `setting-o` / `setting` | 设置 | 设置页面 |
| `edit` | 编辑 | 编辑功能 |
| `delete-o` | 删除 | 删除功能 |
| `add-o` / `add` | 添加 | 添加功能 |
| `photo-o` / `photo` | 图片 | 图片上传 |
| `location-o` / `location` | 定位 | 位置信息 |
| `clock-o` / `clock` | 时间 | 时间显示 |
| `eye-o` / `eye` | 查看 | 查看详情 |
| `fire-o` / `fire` | 热门 | 热门内容 |

## 🔧 故障排除

### 问题 1：图标不显示
**解决方案**：
1. 确认已执行 `npm install`
2. 在开发者工具中点击 **工具** → **构建 npm**
3. 检查是否生成了 `miniprogram_npm` 目录
4. 重启微信开发者工具

### 问题 2：找不到组件
**解决方案**：
1. 检查 `project.config.json` 中 `nodeModules` 是否为 `true`
2. 检查组件路径是否正确：`@vant/weapp/icon/index`
3. 清除缓存后重新编译

### 问题 3：样式异常
**解决方案**：
1. 检查是否有全局样式覆盖了 Vant 样式
2. 使用 `!important` 提高自定义样式优先级
3. 在 `app.wxss` 中引入 Vant 公共样式（可选）

## 📚 更多资源

- [Vant Weapp 官方文档](https://vant-contrib.gitee.io/vant-weapp/)
- [Vant Weapp GitHub](https://github.com/youzan/vant-weapp)
- [图标列表](https://vant-contrib.gitee.io/vant-weapp/#/icon)
- [组件列表](https://vant-contrib.gitee.io/vant-weapp/#/intro)

## 🎨 样式定制

### 修改图标颜色
```javascript
// 在 custom-tab-bar/index.js 中修改
data: {
  color: "#64748b",        // 未选中颜色
  selectedColor: "#3b82f6" // 选中颜色
}
```

### 修改图标大小
```xml
<!-- 在 custom-tab-bar/index.wxml 中修改 -->
<van-icon size="24px" />  <!-- 改为你想要的尺寸 -->
```

### 添加动画效果
已在 `custom-tab-bar/index.wxss` 中添加：
- 选中时图标放大 1.1 倍
- 顶部显示蓝色渐变指示条
- 所有动画时长 0.3s

---

**更新日期**：2026-01-17  
**Vant Weapp 版本**：v1.11.6
