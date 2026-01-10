# 博客网站项目

一个现代化的博客网站系统，采用前后端分离架构，包含用户端和管理端，支持深色/浅色主题切换。

## ✨ 功能特性

### 用户端
- 文章浏览、搜索、分类、标签筛选
- 用户注册/登录、个人中心
- 文章点赞、收藏、评论
- 深色/浅色主题切换（圆形扩散动画）
- 响应式设计，支持移动端

### 管理端
- 仪表盘数据统计
- 文章管理（创建、编辑、发布）
- 分类/标签/评论管理
- 用户管理、角色权限控制
- 系统配置

### 权限控制
- **超级管理员**：全部功能权限
- **内容编辑**：仅文章、分类、标签、评论管理
- **普通用户**：注册自动分配，前台浏览和互动

## 🛠 技术栈

### 前端
- Vue 3 + Vue Router + Pinia
- Element Plus + Vite
- SCSS + 响应式布局

### 后端
- Java 17 + Spring Boot 2.7
- Spring Security + JWT
- MyBatis Plus + MySQL 8 + Redis

## 📁 项目结构

```
├── frontend/
│   ├── user/              # 用户端 (端口 5173)
│   │   └── src/
│   │       ├── views/     # 页面组件
│   │       ├── components/# 公共组件
│   │       ├── stores/    # Pinia状态
│   │       └── api/       # 接口请求
│   └── admin/             # 管理端 (端口 5174)
│       └── src/
│           ├── views/     # 页面组件
│           ├── layouts/   # 布局组件
│           └── stores/    # Pinia状态
├── backend/               # 后端服务 (端口 8080)
│   └── src/main/java/com/blog/
│       ├── controller/    # 控制器
│       ├── service/       # 业务逻辑
│       ├── entity/        # 实体类
│       └── security/      # 安全配置
├── docs/
│   ├── database/
│   │   └── blog_init.sql  # 数据库初始化脚本
│   └── 项目开发文档.md
└── uploads/               # 上传文件目录
```

## 🚀 快速开始

### 环境要求
- Node.js 16+
- Java 17+
- MySQL 8.0+
- Redis 7.0+

### 1. 初始化数据库

```sql
-- 创建数据库并导入初始数据
mysql -u root -p < docs/database/blog_init.sql
```

### 2. 配置后端

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog_db
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
```

### 3. 启动服务

```bash
# 启动后端
cd backend
mvn spring-boot:run

# 启动用户端
cd frontend/user
npm install
npm run dev

# 启动管理端
cd frontend/admin
npm install
npm run dev
```

### 4. 访问地址

| 服务 | 地址 |
|------|------|
| 用户端 | http://localhost:5173 |
| 管理端 | http://localhost:5174 |
| 后端API | http://localhost:8080 |
| API文档 | http://localhost:8080/swagger-ui.html |

## 👤 默认账号

| 角色 | 用户名 | 密码 | 权限 |
|------|--------|------|------|
| 超级管理员 | admin | admin123 | 全部权限 |
| 内容编辑 | editor | admin123 | 内容管理 |
| 普通用户 | zhangsan | admin123 | 前台功能 |

## 🎨 主题配置

项目支持深色/浅色主题切换，主题色可在 `variables.scss` 中配置：

```scss
// 主色调
$primary-color: #a855f7;
$primary-gradient: linear-gradient(135deg, #a855f7 0%, #ec4899 100%);

// 深色主题
$bg-dark: #0f0f0f;
$bg-card: rgba(255, 255, 255, 0.03);
```

## 📝 API 接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/info` - 获取用户信息

### 文章接口
- `GET /api/articles` - 文章列表
- `GET /api/articles/{id}` - 文章详情
- `POST /api/articles/{id}/like` - 点赞文章
- `POST /api/articles/{id}/favorite` - 收藏文章

### 管理接口
- `GET /api/admin/articles` - 文章管理
- `GET /api/admin/users` - 用户管理
- `GET /api/admin/roles` - 角色管理

## 📄 License

MIT License

