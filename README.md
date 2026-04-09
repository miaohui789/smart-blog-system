# Smart Blog System

一个基于前后端分离架构的智能博客系统，包含 `backend` 后端、`frontend/user` 用户端、`frontend/admin` 管理端，以及 `miniprogram` 微信小程序端。

当前仓库已经不只是基础博客，代码里还包含了学习中心、经验等级、签到、VIP、AI、私信通知、操作日志、搜索扩展等模块。

## 项目概览

### 用户端
- 文章首页、文章详情、分类、标签、归档、关于页、版本历史
- 用户注册、账号密码登录、邮箱验证码登录、找回密码
- 文章评论、点赞、收藏、发布、编辑
- 用户主页、个人中心、我的文章、我的收藏、关注/粉丝
- 私信、通知、用户资料维护
- 学习中心、题库学习、抽查任务、抽查历史
- 经验等级、经验流水、每日签到、升级弹窗
- VIP 激活与 VIP 中心
- AI 助手

### 管理端
- 仪表盘数据统计
- 文章、分类、标签、评论管理
- 学习分类、学习题库、抽查记录管理
- 用户管理
- 角色、菜单、系统配置、版本管理、操作日志
- 关注、私信、通知管理
- VIP 会员、密钥、加热记录、统计管理
- AI 模型配置、Logo 管理

### 微信小程序
- 文章浏览与基础用户中心能力
- AI、VIP 等分包页面
- 基于 `@vant/weapp` 的移动端交互

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 2.7.15 | 核心框架 |
| Spring Security | - | 认证鉴权 |
| MyBatis Plus | 3.5.4 | 数据访问 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 7.0+ | 缓存 |
| RabbitMQ | - | 异步消息处理 |
| JWT | 0.11.5 | Token 认证 |
| WebSocket | - | 私信/通知实时能力 |
| Knife4j + SpringDoc | 4.3.0 | API 文档 |
| OkHttp | 4.12.0 | AI 接口调用 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.3.4 | 前端框架 |
| Vue Router | 4.2.4 | 路由管理 |
| Pinia | 2.1.4 | 状态管理 |
| Element Plus | 2.4.4 | UI 组件库 |
| Vite | 4.4.9 | 构建工具 |
| Axios | 1.5.0 | 请求库 |
| ECharts | 5.4.3 | 管理端图表 |
| md-editor-v3 | 4.0.0 | Markdown 编辑器 |
| Sass | 1.66.1 | 样式预处理 |
| marked / highlight.js | - | 富文本渲染与代码高亮 |

### 微信小程序
| 技术 | 版本 | 说明 |
|------|------|------|
| 原生微信小程序 | - | 小程序主体 |
| @vant/weapp | 1.11.6 | 小程序 UI 组件 |

## 项目结构

```text
smart-blog-system/
├── backend/                         # Spring Boot 后端，默认端口 8080
│   ├── src/main/java/com/blog/
│   │   ├── controller/admin/        # 管理端接口
│   │   ├── controller/web/          # 用户端接口
│   │   ├── service/                 # 业务层
│   │   ├── mapper/                  # MyBatis Plus / Mapper
│   │   ├── entity/                  # 实体类
│   │   ├── config/                  # 配置类
│   │   ├── mq/                      # RabbitMQ 消息处理
│   │   └── websocket/               # WebSocket 相关
│   └── src/main/resources/          # application.yml / application-dev.yml 等
├── frontend/
│   ├── user/                        # 用户端，默认端口 5173
│   │   └── src/
│   │       ├── api/
│   │       ├── components/
│   │       ├── layouts/
│   │       ├── router/
│   │       ├── stores/
│   │       └── views/
│   └── admin/                       # 管理端，默认端口 5174，生产基础路径 /admin/
│       └── src/
│           ├── api/
│           ├── layouts/
│           ├── router/
│           ├── stores/
│           └── views/
├── miniprogram/                     # 微信小程序
│   ├── pages/
│   ├── packageAI/
│   ├── packageVip/
│   ├── api/
│   └── components/
├── docs/
│   └── database/
│       └── blog_db.sql              # 当前数据库脚本
├── deploy/                          # Docker / Nginx / 宝塔部署相关
├── uploads/                         # 上传文件目录
└── README.md
```

## 核心模块说明

### 后端接口模块
`backend/src/main/java/com/blog/controller/web`
- `ArticleController` 文章与详情
- `CommentController` 评论
- `SearchController` 搜索
- `StudyController` 学习中心
- `UserController` 用户认证与用户信息
- `UserExpController` 经验、等级、签到
- `MessageController` 私信
- `NotificationController` 通知
- `VipController` VIP
- `AiChatController` AI 对话

`backend/src/main/java/com/blog/controller/admin`
- `DashboardController` 仪表盘
- `AdminArticleController` 内容管理
- `AdminStudyCategoryController` / `AdminStudyQuestionController` / `AdminStudyCheckController` 学习后台
- `AdminUserController` 用户管理
- `AdminRoleController` / `AdminMenuController` / `AdminConfigController` 系统管理
- `AdminLogController` 操作日志
- `AdminVipController` VIP 管理
- `AdminAiConfigController` / `AdminAiLogoController` AI 管理

### 用户端页面模块
- 公共内容页：`Home`、`Article`、`Category`、`Tag`、`Archive`、`About`
- 用户体系：`Auth`、`User/*`
- 内容创作：`Write`
- 社交消息：`Message`、`Notification`
- 搜索与 AI：`Search`、`AI`
- 学习体系：`Study/*`
- 会员体系：`Vip/*`

### 管理端页面模块
- `Dashboard`
- `Article`、`Category`、`Tag`、`Comment`
- `Study`
- `User`
- `System`
- `Vip`
- `AI`
- `Follow`、`Message`、`Notification`

## 运行环境

### 基础依赖
- JDK 17
- Maven 3.8+
- Node.js 16+
- MySQL 8.0+
- Redis 7.0+

### 建议依赖
- RabbitMQ
  用于异步消息、经验等级联动等扩展能力
- Elasticsearch
  当前配置中默认关闭，启用后可用于搜索增强

## 快速开始

### 1. 初始化数据库

当前仓库内的数据库脚本路径是：

```bash
docs/database/blog_db.sql
```

导入示例：

```bash
mysql -uroot -p123456 < docs/database/blog_db.sql
```

### 2. 配置后端

后端默认启用 `dev` 环境，核心配置文件：

- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-dev.yml`

开发环境默认数据库与 Redis 配置如下：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
  redis:
    host: localhost
    port: 6379
    database: 6
```

如果你要启用完整功能，还需要按实际环境补齐：

- `spring.mail` 邮件配置
- `spring.rabbitmq` RabbitMQ 配置
- `blog.search.elasticsearch` 搜索配置

### 3. 启动后端

```bash
cd backend
mvn clean spring-boot:run
```

后端默认端口：

```text
http://localhost:8080
```

### 4. 启动用户端

```bash
cd frontend/user
npm install
npm run dev
```

访问地址：

```text
http://localhost:5173
```

### 5. 启动管理端

```bash
cd frontend/admin
npm install
npm run dev
```

管理端路由基路径是 `/admin/`，开发访问地址建议使用：

```text
http://localhost:5174/admin/
```

### 6. 微信小程序

```bash
cd miniprogram
npm install
```

然后使用微信开发者工具导入 `miniprogram` 目录，并执行“小程序构建 npm”。

仓库里已经提供了一些小程序说明文档，可直接参考：

- `miniprogram/⚠️必读-构建npm步骤.txt`
- `miniprogram/开发环境配置指南.md`
- `miniprogram/QUICKSTART.md`

## 访问地址

| 服务 | 地址 |
|------|------|
| 用户端 | http://localhost:5173 |
| 管理端 | http://localhost:5174/admin/ |
| 后端 API | http://localhost:8080 |
| Knife4j 文档 | http://localhost:8080/doc.html |
| Swagger UI | http://localhost:8080/swagger-ui.html |

## 部署说明

`deploy/` 目录下已经包含：

- `docker-compose.yml`
- `nginx.conf`
- `nginx-baota.conf`
- `deploy.sh`
- `宝塔Docker部署指南.md`

当前 `docker-compose.yml` 主要覆盖：

- MySQL
- Redis
- 后端服务

前端静态资源部署需要结合 Nginx 或你自己的静态资源托管方案。

### 部署前注意

`deploy/docker-compose.yml` 当前挂载了 `./init.sql`：

```yaml
- ./init.sql:/docker-entrypoint-initdb.d/init.sql:ro
```

但仓库里的数据库脚本实际位于：

```text
docs/database/blog_db.sql
```

所以在正式使用 Docker Compose 前，你需要二选一：

1. 把 `docs/database/blog_db.sql` 复制到 `deploy/init.sql`
2. 或者直接修改 `deploy/docker-compose.yml` 的挂载路径

## 开发说明

### 前端代理

用户端和管理端 Vite 都已配置代理：

- `/api` -> `http://localhost:8080`
- `/uploads` -> `http://localhost:8080`

### 管理端基础路径

管理端 `vite.config.js` 设置了：

```js
base: '/admin/'
```

同时路由使用：

```js
createWebHistory('/admin/')
```

所以本地开发、静态部署和 Nginx 转发时，都需要考虑 `/admin/` 前缀。

### 搜索能力

`application.yml` 中存在 Elasticsearch 配置，但默认：

```yaml
blog:
  search:
    elasticsearch:
      enabled: false
```

这意味着项目当前可以先以普通模式运行，若要启用搜索增强，再补齐 Elasticsearch 服务与配置。

## 当前文档基于的真实代码信息

本 README 已按以下实际文件核对后更新：

- `backend/pom.xml`
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-dev.yml`
- `frontend/user/package.json`
- `frontend/admin/package.json`
- `frontend/user/src/router/index.js`
- `frontend/admin/src/router/index.js`
- `backend/src/main/java/com/blog/controller/admin`
- `backend/src/main/java/com/blog/controller/web`
- `docs/database/blog_db.sql`
- `deploy/docker-compose.yml`
- `miniprogram/package.json`

## License

MIT
