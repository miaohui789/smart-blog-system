# 博客网站项目

一个现代化的博客网站系统，采用前后端分离架构，包含用户端和管理员端。

## 🎨 设计风格

- 深色主题 + 紫色渐变点缀
- 主题色: `#a855f7` → `#ec4899`
- 背景色: `#0f0f0f`, `#1a1a1a`

## 🛠 技术栈

### 前端
- Vue.js 3.3.4 + Vue Router 4.2.4 + Pinia 2.1.4
- Element Plus 2.4.4 + ECharts 5.4.3
- Vite 4.4.9 + Sass 1.66.1

### 后端
- Java 17 + Spring Boot 2.7.15
- Spring Security + JWT 0.11.5
- MyBatis Plus 3.5.4 + MySQL 8.0 + Redis 7.0

## 📁 项目结构

```
blog-project/
├── frontend/
│   ├── user/          # 用户端
│   └── admin/         # 管理员端
├── backend/           # 后端服务
└── docs/              # 项目文档
    ├── 项目开发文档.md
    └── database/
        └── blog_init.sql
```

## 🚀 快速开始

### 1. 初始化数据库
```bash
mysql -u root -p blog_db < docs/database/blog_init.sql
```

### 2. 启动后端
```bash
cd backend
mvn spring-boot:run
```

### 3. 启动前端
```bash
# 用户端
cd frontend/user && npm install && npm run dev

# 管理端
cd frontend/admin && npm install && npm run dev
```

## 📖 文档

详细开发文档请查看 [docs/项目开发文档.md](docs/项目开发文档.md)

## 👤 默认账号

- 管理员: admin / admin123
- 编辑: editor / admin123
