# 博客系统后端

基于 Spring Boot 2.7 + MyBatis Plus + MySQL + Redis 构建的博客系统后端。

## 技术栈

- Java 17
- Spring Boot 2.7.15
- Spring Security
- MyBatis Plus 3.5.4
- MySQL 8.0
- Redis 7.0
- JWT 0.11.5
- MinIO 8.5.7
- Knife4j 4.3.0

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.8+
- MySQL 8.0
- Redis 7.0

### 2. 数据库初始化

```bash
mysql -u root -p
CREATE DATABASE blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
mysql -u root -p blog_db < docs/database/blog_init.sql
```

### 3. 修改配置

编辑 `src/main/resources/application-dev.yml`，配置数据库和Redis连接信息。

### 4. 启动项目

```bash
mvn spring-boot:run
```

### 5. 访问

- API文档: http://localhost:8080/doc.html
- 默认管理员: admin / admin123

## 项目结构

```
src/main/java/com/blog/
├── BlogApplication.java      # 启动类
├── common/                   # 公共模块
├── config/                   # 配置类
├── controller/               # 控制器
├── entity/                   # 实体类
├── mapper/                   # 数据访问层
├── service/                  # 服务层
├── security/                 # 安全模块
├── dto/                      # 数据传输对象
└── utils/                    # 工具类
```
