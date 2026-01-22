# 自定义背景下卡片透明度功能实现说明

## 功能描述
当用户在"我的皮肤"页面选择非默认背景皮肤时，所有页面中的卡片会自动降低透明度，以便更好地展示背景效果。

## 实现方式

### 1. CSS变量扩展 (`frontend/user/src/assets/styles/global.scss`)
- 添加了 `--bg-card-rgb` CSS变量，用于支持rgba透明度
- 暗色主题: `--bg-card-rgb: 26, 26, 26;`
- 亮色主题: `--bg-card-rgb: 255, 255, 255;`

### 2. 透明度样式规则
当 `body` 元素具有 `has-custom-bg` 类时，应用以下透明度：

#### 通用卡片 (`.glass-card`)
- 背景: `rgba(var(--bg-card-rgb), 0.65)` (65%透明度)
- 毛玻璃效果: `backdrop-filter: blur(12px)`

#### 特殊卡片 (更高透明度)
- `.article-card`, `.comment-section`, `.profile-card`, `.articles-section`
- 背景: `rgba(var(--bg-card-rgb), 0.7)` (70%透明度)

#### Header导航栏
- 背景: `rgba(var(--bg-card-rgb), 0.75)` (75%透明度)
- 毛玻璃效果: `backdrop-filter: blur(16px)`

#### Element Plus组件
- **卡片**: 65%透明度 + 12px模糊
- **输入框/文本域**: 60%透明度 + 8px模糊
- **下拉菜单**: 75%透明度 + 12px模糊
- **分页器**: 65%透明度 + 8px模糊
- **侧边栏卡片**: 65%透明度 + 12px模糊

### 3. 状态管理 (`frontend/user/src/stores/theme.js`)
- `checkCustomBackground()` 函数检测当前是否使用自定义背景
- 当皮肤ID不是 'default' 且有组件时，添加 `has-custom-bg` 类到 body
- 当切换回默认皮肤时，移除该类

### 4. 触发时机
- 页面加载时检查
- 切换皮肤时触发
- 切换主题模式时触发

## 支持的自定义背景

### 暗色主题
1. **黑客帝国** (matrix) - MatrixRain组件
2. **彩色斑点** (dark-2) - ColorGrid组件
3. **方块** (dark-3) - HexagonPattern组件

### 亮色主题
1. **清新蓝** (light-1) - MovingDots组件
2. **紫色骨头** (light-2) - PurpleDots组件
3. **立体** (light-3) - GeometricPattern组件

## 使用效果
- 默认背景: 卡片保持原有不透明样式
- 自定义背景: 卡片自动变为半透明，展示背景动画效果
- 平滑过渡: 所有透明度变化都有0.3s的过渡动画

## 浏览器兼容性
- 使用 `backdrop-filter` 实现毛玻璃效果
- 同时提供 `-webkit-backdrop-filter` 前缀支持Safari
- 所有现代浏览器均支持

## 测试步骤
1. 访问"我的皮肤"页面 (`/user/skin`)
2. 选择任意非默认背景皮肤
3. 观察页面卡片变为半透明
4. 切换回默认皮肤，卡片恢复不透明
5. 切换主题模式，透明度效果保持一致
