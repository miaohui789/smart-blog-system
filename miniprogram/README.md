# 智能博客系统 - 微信小程序

## 项目说明

这是智能博客系统的微信小程序版本，与 Web 端功能保持一致，提供原生小程序体验。

## 目录结构

```
miniprogram/
├── api/                    # API 接口
│   ├── article.js          # 文章接口
│   ├── user.js             # 用户接口
│   ├── category.js         # 分类接口
│   ├── tag.js              # 标签接口
│   ├── comment.js          # 评论接口
│   ├── message.js          # 私信接口
│   ├── notification.js     # 通知接口
│   └── vip.js              # VIP 接口
├── components/             # 公共组件（待创建）
├── images/                 # 图片资源（待添加）
├── pages/                  # 页面
│   └── index/              # 首页（已创建）
├── packageAI/              # AI 功能分包（待创建）
├── packageVip/             # VIP 功能分包（待创建）
├── styles/                 # 全局样式
│   ├── variables.wxss      # 样式变量
│   └── common.wxss         # 公共样式
├── utils/                  # 工具类
│   ├── config.js           # 配置文件
│   ├── request.js          # 网络请求封装
│   ├── auth.js             # 认证工具
│   ├── storage.js          # 本地存储
│   ├── format.js           # 格式化工具
│   └── websocket.js        # WebSocket 封装
├── app.js                  # 小程序逻辑
├── app.json                # 小程序配置
├── app.wxss                # 全局样式
├── project.config.json     # 项目配置
├── sitemap.json            # 索引配置
└── README.md               # 说明文档
```

## 快速开始

### 1. 配置后端地址

修改 `utils/config.js` 中的 API 地址：

```javascript
module.exports = {
  BASE_URL: 'https://your-api-domain.com/api',  // 修改为你的后端地址
  WS_URL: 'wss://your-api-domain.com/ws'
}
```

### 2. 配置 AppID

修改 `project.config.json` 中的 appid：

```json
{
  "appid": "your-appid-here"  // 修改为你的小程序 AppID
}
```

### 3. 导入项目

1. 打开微信开发者工具
2. 选择"导入项目"
3. 选择 `miniprogram` 目录
4. 填入 AppID
5. 点击"导入"

### 4. 运行项目

导入成功后，点击"编译"即可在模拟器中预览。

## 开发进度

### 已完成
- [x] 项目基础结构
- [x] 全局样式配置
- [x] 工具类封装
- [x] API 接口封装
- [x] 首页示例

### 待开发
- [ ] 其他页面
- [ ] 公共组件
- [ ] VIP 功能分包
- [ ] AI 功能分包
- [ ] 图片资源

## 开发规范

### 命名规范
- 页面文件：小写字母 + 连字符（kebab-case）
- 组件文件：小写字母 + 连字符（kebab-case）
- JS 变量：驼峰命名（camelCase）
- CSS 类名：小写字母 + 连字符（kebab-case）

### 代码规范
- 统一使用 2 空格缩进
- 函数注释使用 JSDoc 格式
- 组件属性添加类型和默认值

## 注意事项

1. **后端适配**：需要在后端添加微信登录接口 `/api/auth/wx-login`
2. **域名配置**：在小程序后台配置服务器域名和 WebSocket 域名
3. **权限申请**：如需使用位置信息，需在小程序后台申请权限
4. **分包加载**：VIP 和 AI 功能使用分包加载，减小主包体积

## 相关文档

- [微信小程序开发文档](../docs/微信小程序开发文档.md)
- [项目开发文档](../docs/项目开发文档.md)
- [后端 API 文档](http://localhost:8080/doc.html)

## 技术支持

如有问题，请查看开发文档或联系开发团队。
