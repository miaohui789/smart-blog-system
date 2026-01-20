# Vant Weapp 快速配置指南

## ⚡ 3 步完成配置

### 第 1 步：安装依赖（已完成 ✅）
```bash
cd miniprogram
npm install
```

### 第 2 步：构建 npm（必须执行 ⚠️）
在微信开发者工具中：

1. 点击顶部菜单：**工具** → **构建 npm**
2. 等待构建完成（几秒钟）
3. 看到 `miniprogram_npm` 目录生成即成功

![构建npm](https://img.yzcdn.cn/vant-weapp/quickstart-03.png)

### 第 3 步：启用 npm（已配置 ✅）
在微信开发者工具中：

1. 点击右上角 **详情**
2. 选择 **本地设置**
3. 勾选 **使用 npm 模块**

![启用npm](https://img.yzcdn.cn/vant-weapp/quickstart-04.png)

## ✅ 验证是否成功

### 方法 1：查看 TabBar
编译小程序后，底部导航栏应该显示清晰的图标：
- 🏠 首页（房屋图标）
- 📱 分类（网格图标）
- 💬 消息（聊天图标）
- 👤 我的（用户图标）

### 方法 2：检查目录
确认项目中存在 `miniprogram_npm/@vant` 目录

### 方法 3：查看控制台
如果配置正确，控制台不会有组件找不到的错误

## ❌ 常见错误

### 错误 1：图标不显示
```
Component is not found in path "@vant/weapp/icon/index"
```

**解决**：
1. 确认执行了 `npm install`
2. 在开发者工具中点击 **工具** → **构建 npm**
3. 重启微信开发者工具

### 错误 2：构建 npm 失败
```
没有找到可以构建的 npm 包
```

**解决**：
1. 确认 `package.json` 存在
2. 确认 `node_modules` 目录存在
3. 重新执行 `npm install`

### 错误 3：图标显示异常
**解决**：
1. 清除缓存：**清缓存** → **全部清除**
2. 重新编译项目
3. 检查网络连接

## 🎨 TabBar 图标说明

当前使用的 Vant 图标：

| 页面 | 图标名称 | 效果 |
|------|---------|------|
| 首页 | `wap-home-o` / `wap-home` | 线性/实心房屋 |
| 分类 | `apps-o` / `apps` | 线性/实心网格 |
| 消息 | `chat-o` / `chat` | 线性/实心聊天 |
| 我的 | `user-o` / `user` | 线性/实心用户 |

- 未选中：显示线性图标（-o 后缀）
- 选中：显示实心图标
- 颜色：自动切换为主题色

## 📝 下一步

配置完成后，你可以：

1. **开始开发**
   - 查看 [调试指南](./调试指南.md)
   - 阅读 [开发文档](../docs/微信小程序开发文档.md)

2. **使用更多组件**
   - 查看 [Vant Weapp 组件列表](https://vant-contrib.gitee.io/vant-weapp/#/intro)
   - 参考 [配置说明](./Vant%20Weapp配置说明.md)

3. **自定义样式**
   - 修改 `styles/variables.wxss` 中的颜色变量
   - 调整 `custom-tab-bar/index.wxss` 中的样式

## 🆘 需要帮助？

- 查看 [Vant Weapp 官方文档](https://vant-contrib.gitee.io/vant-weapp/)
- 查看 [微信小程序官方文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)
- 查看项目 [完整调试指南](./调试指南.md)

---

**提示**：每次修改 `package.json` 或安装新依赖后，都需要重新构建 npm！
