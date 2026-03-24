# Smart Blog System

一个功能完善的现代化博客系统，采用前后端分离架构，支持 Web 端（用户端 + 管理端）和微信小程序端。

## 功能特性

### 用户端
- 文章浏览、搜索、分类、标签筛选
- 用户注册/登录（支持邮箱验证码）
- 文章点赞、收藏、评论
- 私信系统（WebSocket 实时通信）
- 通知系统
- 文章发布（Markdown 编辑器）
- AI 对话功能
- VIP 会员系统
- 深色/浅色主题切换（圆形扩散动画）
- 响应式设计

### 管理端
- 数据统计仪表盘（ECharts 图表）
- 文章/分类/标签/评论管理
- 用户管理、角色权限控制
- 私信与通知管理
- VIP 会员管理
- AI 配置管理
- 系统配置与版本管理

### 微信小程序
- 文章浏览与详情
- 用户中心（关注/粉丝/收藏/点赞）
- 私信与通知
- VIP 中心
- AI 聊天

### 权限控制
| 角色 | 权限范围 |
|------|----------|
| 超级管理员 | 全部功能权限 |
| 内容编辑 | 文章、分类、标签、评论管理 |
| 普通用户 | 前台浏览和互动 |

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 2.7.15 | 核心框架 |
| Spring Security | - | 安全认证 |
| MyBatis Plus | 3.5.4 | ORM 框架 |
| MySQL | 8.0 | 主数据库 |
| Redis | 7.0+ | 缓存 |
| JWT | 0.11.5 | Token 认证 |
| WebSocket | - | 实时通信 |
| Knife4j | 4.3.0 | API 文档 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.3.4 | 前端框架 |
| Vue Router | 4.2.4 | 路由管理 |
| Pinia | 2.1.4 | 状态管理 |
| Element Plus | 2.4.4 | UI 组件库 |
| Vite | 4.4.9 | 构建工具 |
| ECharts | 5.4.3 | 图表（管理端） |
| md-editor-v3 | 4.0.0 | Markdown 编辑器 |
| SASS | 1.66.1 | CSS 预处理 |

### 微信小程序
| 技术 | 版本 | 说明 |
|------|------|------|
| 原生小程序 | - | 开发框架 |
| Vant Weapp | 1.11.6 | UI 组件库 |

## 项目结构

```
smart-blog-system/
├── backend/                    # 后端服务 (端口 8080)
│   └── src/main/java/com/blog/
│       ├── controller/
│       │   ├── admin/          # 管理端 API
│       │   └── web/            # 用户端 API
│       ├── service/            # 业务逻辑层
│       ├── entity/             # 实体类
│       ├── mapper/             # MyBatis 映射
│       ├── security/           # 安全配置
│       ├── config/             # 配置类
│       └── websocket/          # WebSocket 服务
├── frontend/
│   ├── user/                   # 用户端 (端口 5173)
│   │   └── src/
│   │       ├── views/          # 页面组件
│   │       ├── components/     # 公共组件
│   │       ├── stores/         # Pinia 状态
│   │       └── api/            # 接口请求
│   └── admin/                  # 管理端 (端口 5174)
│       └── src/
│           ├── views/          # 页面组件
│           ├── layouts/        # 布局组件
│           └── stores/         # Pinia 状态
├── miniprogram/                # 微信小程序
│   ├── pages/                  # 主包页面
│   ├── packageA/               # 分包 (VIP/AI)
│   └── components/             # 组件
├── deploy/                     # Docker 部署配置
├── docs/
│   ├── database/
│   │   └── blog_init.sql       # 数据库初始化脚本
│   └── 项目开发文档.md
└── uploads/                    # 上传文件目录
```

## 快速开始

### 环境要求
- Node.js 16+
- Java 17+
- MySQL 8.0+
- Redis 7.0+

### 1. 初始化数据库

```bash
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
npm install && npm run dev

# 启动管理端
cd frontend/admin
npm install && npm run dev
```

### 4. 访问地址

| 服务 | 地址 |
|------|------|
| 用户端 | http://localhost:5173 |
| 管理端 | http://localhost:5174 |
| 后端 API | http://localhost:8080 |
| API 文档 | http://localhost:8080/doc.html |

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 超级管理员 | admin | admin123 |
| 内容编辑 | editor | admin123 |
| 普通用户 | zhangsan | admin123 |

## Docker 部署

项目提供 Docker Compose 配置，针对 2 核 CPU + 2G 内存环境优化：

```bash
cd deploy
docker-compose up -d
```

## 主题配置

支持深色/浅色主题切换，主题色可在 `variables.scss` 中配置：

```scss
$primary-color: #a855f7;
$primary-gradient: linear-gradient(135deg, #a855f7 0%, #ec4899 100%);
```

## API 接口

### 认证
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/info` - 获取用户信息

### 文章
- `GET /api/articles` - 文章列表
- `GET /api/articles/{id}` - 文章详情
- `POST /api/articles/{id}/like` - 点赞
- `POST /api/articles/{id}/favorite` - 收藏

### 管理
- `GET /api/admin/articles` - 文章管理
- `GET /api/admin/users` - 用户管理
- `GET /api/admin/dashboard/stats` - 统计数据

完整 API 文档请访问 http://localhost:8080/doc.html

## License

MIT License
