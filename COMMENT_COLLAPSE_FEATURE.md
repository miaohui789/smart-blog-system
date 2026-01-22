# 评论回复折叠/展开功能实现说明

## 功能描述
评论区的回复默认折叠，显示回复数量，用户可以点击展开/收起按钮来查看或隐藏回复内容。

## 实现细节

### 1. 状态管理
```javascript
const expandedComments = ref(new Set()) // 存储展开的评论ID
```
使用 `Set` 数据结构存储已展开的评论ID，便于快速查询和操作。

### 2. 切换函数
```javascript
function toggleReplies(commentId) {
  if (expandedComments.value.has(commentId)) {
    expandedComments.value.delete(commentId)
  } else {
    expandedComments.value.add(commentId)
  }
  // 触发响应式更新
  expandedComments.value = new Set(expandedComments.value)
}
```

### 3. UI组件结构
```vue
<div class="replies-container">
  <!-- 展开/收起按钮 -->
  <div class="replies-toggle" @click="toggleReplies(comment.id)">
    <el-icon class="toggle-icon" :class="{ expanded: expandedComments.has(comment.id) }">
      <ArrowRight />
    </el-icon>
    <span class="toggle-text">
      {{ expandedComments.has(comment.id) ? '收起' : '展开' }}
      {{ comment.replies.length }} 条回复
    </span>
  </div>
  
  <!-- 回复列表 - 使用过渡动画 -->
  <transition name="replies-expand">
    <div v-show="expandedComments.has(comment.id)" class="replies-list">
      <!-- 回复内容 -->
    </div>
  </transition>
</div>
```

## 样式特性

### 1. 展开/收起按钮样式
- **默认状态**: 灰色背景，边框，圆角
- **悬停状态**: 紫色边框，紫色文字，背景变化
- **图标动画**: 展开时箭头旋转90度

### 2. 回复列表样式
- 半透明背景 `rgba(var(--bg-card-rgb), 0.3)`
- 边框和圆角设计
- 内边距优化

### 3. 过渡动画
使用 Vue 的 `<transition>` 组件实现平滑的展开/收起动画：
- **进入动画**: 从上方淡入，透明度从0到1
- **离开动画**: 向上淡出，透明度从1到0
- **动画时长**: 0.3秒
- **缓动函数**: ease

```scss
.replies-expand-enter-active,
.replies-expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.replies-expand-enter-from,
.replies-expand-leave-to {
  opacity: 0;
  max-height: 0;
  margin-top: 0;
  transform: translateY(-10px);
}

.replies-expand-enter-to,
.replies-expand-leave-from {
  opacity: 1;
  max-height: 2000px;
  margin-top: 8px;
  transform: translateY(0);
}
```

## 用户体验优化

### 1. 默认折叠
- 所有回复默认处于折叠状态
- 减少页面初始加载的内容量
- 提升页面滚动性能

### 2. 回复数量显示
- 清晰显示每条评论的回复数量
- 帮助用户快速了解讨论热度

### 3. 交互反馈
- 按钮悬停效果
- 图标旋转动画
- 平滑的展开/收起过渡

### 4. 视觉层次
- 回复列表使用半透明背景
- 与主评论形成视觉区分
- 保持整体设计一致性

## 响应式设计
- 移动端自适应
- 触摸友好的按钮大小
- 流畅的动画效果

## 浏览器兼容性
- 支持所有现代浏览器
- CSS过渡动画广泛支持
- Vue transition组件兼容性好

## 使用示例

### 查看回复
1. 找到有回复的评论
2. 点击"展开 X 条回复"按钮
3. 回复列表平滑展开显示

### 隐藏回复
1. 在已展开的回复列表上方
2. 点击"收起 X 条回复"按钮
3. 回复列表平滑收起隐藏

## 技术栈
- Vue 3 Composition API
- Element Plus 图标
- SCSS 样式预处理
- CSS Transitions 动画
