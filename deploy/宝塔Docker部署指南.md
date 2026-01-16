# 博客系统 - 宝塔 Docker 部署指南

> 适用于 4核CPU + 4G内存 服务器

## 一、服务器准备

### 1.1 安装宝塔面板

```bash
# CentOS
yum install -y wget && wget -O install.sh https://download.bt.cn/install/install_6.0.sh && sh install.sh ed8484bec

# Ubuntu/Debian
wget -O install.sh https://download.bt.cn/install/install-ubuntu_6.0.sh && sudo bash install.sh ed8484bec
```

### 1.2 宝塔面板安装 Docker

1. 登录宝塔面板
2. 进入 **软件商店**
3. 搜索 **Docker管理器**，点击安装
4. 等待安装完成

### 1.3 创建项目目录

```bash
mkdir -p /www/wwwroot/blog/{backend,frontend/user,frontend/admin,uploads,logs,mysql,redis}
chmod -R 755 /www/wwwroot/blog
```

---

## 二、部署 MySQL（Docker）

### 2.1 宝塔面板创建 MySQL 容器

1. 进入 **Docker** → **容器** → **创建容器**
2. 配置如下：

| 配置项 | 值 |
|--------|-----|
| 镜像 | mysql:8.0 |
| 容器名称 | blog-mysql |
| 端口映射 | 3306:3306 |
| 内存限制 | 1024MB |
| CPU限制 | 1核 |

3. 环境变量：
```
MYSQL_ROOT_PASSWORD=123456
MYSQL_DATABASE=blog_db
TZ=Asia/Shanghai
```

4. 目录映射：
```
/www/wwwroot/blog/mysql:/var/lib/mysql
```

5. 命令参数：
```
--innodb-buffer-pool-size=512M --max-connections=100 --character-set-server=utf8mb4
```

### 2.2 或使用命令行创建

```bash
docker run -d \
  --name blog-mysql \
  --restart always \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=blog_db \
  -e TZ=Asia/Shanghai \
  -v /www/wwwroot/blog/mysql:/var/lib/mysql \
  --memory=1024m \
  --cpus=1 \
  mysql:8.0 \
  --innodb-buffer-pool-size=512M \
  --max-connections=100 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

### 2.3 初始化数据库

```bash
# 进入MySQL容器
docker exec -it blog-mysql mysql -uroot -p123456

# 执行SQL（复制 docs/database/blog_init.sql 内容）
# 执行SQL（复制 docs/database/vip_extension.sql 内容）
```

或者使用文件导入：
```bash
# 复制SQL文件到容器
docker cp docs/database/blog_init.sql blog-mysql:/tmp/
docker cp docs/database/vip_extension.sql blog-mysql:/tmp/

# 执行导入
docker exec -it blog-mysql mysql -uroot -p123456 blog_db -e "source /tmp/blog_init.sql"
docker exec -it blog-mysql mysql -uroot -p123456 blog_db -e "source /tmp/vip_extension.sql"
```

---

## 三、部署 Redis（Docker）

### 3.1 宝塔面板创建 Redis 容器

1. 进入 **Docker** → **容器** → **创建容器**
2. 配置如下：

| 配置项 | 值 |
|--------|-----|
| 镜像 | redis:7-alpine |
| 容器名称 | blog-redis |
| 端口映射 | 6379:6379 |
| 内存限制 | 256MB |
| CPU限制 | 0.5核 |

3. 目录映射：
```
/www/wwwroot/blog/redis:/data
```

4. 命令参数：
```
redis-server --maxmemory 200mb --maxmemory-policy allkeys-lru --appendonly yes
```

### 3.2 或使用命令行创建

```bash
docker run -d \
  --name blog-redis \
  --restart always \
  -p 6379:6379 \
  -v /www/wwwroot/blog/redis:/data \
  --memory=256m \
  --cpus=0.5 \
  redis:7-alpine \
  redis-server --maxmemory 200mb --maxmemory-policy allkeys-lru --appendonly yes
```

---

## 四、部署后端（Docker）

### 4.1 本地构建 JAR 包

在本地开发机器上执行：

```bash
cd backend
mvn clean package -DskipTests
```

生成文件：`backend/target/blog-backend-1.0.0.jar`

### 4.2 创建 Dockerfile

在 `backend/` 目录下创建 `Dockerfile`：

```dockerfile
FROM openjdk:17-jdk-slim

LABEL maintainer="your-email@example.com"

WORKDIR /app

# 复制JAR包
COPY target/blog-backend-1.0.0.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploads

# 暴露端口
EXPOSE 8080

# JVM优化参数 - 针对4G内存服务器
ENV JAVA_OPTS="-Xms256m -Xmx512m \
  -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -Djava.security.egd=file:/dev/./urandom"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=prod"]
```

### 4.3 创建生产环境配置

确保 `backend/src/main/resources/application-prod.yml` 内容如下：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://blog-mysql:3306/blog_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: ${DB_PASSWORD:123456}
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      connection-timeout: 20000

  redis:
    host: blog-redis
    port: 6379
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 4
        min-idle: 2

jwt:
  secret: ${JWT_SECRET:your-secret-key-at-least-256-bits-long-for-security}
  expiration: 604800000

file:
  upload-path: /app/uploads

logging:
  level:
    root: WARN
    com.blog: INFO
```

### 4.4 上传文件到服务器

使用宝塔面板的 **文件** 功能，或使用 scp：

```bash
# 上传JAR包和Dockerfile
scp backend/target/blog-backend-1.0.0.jar root@你的服务器IP:/www/wwwroot/blog/backend/
scp backend/Dockerfile root@你的服务器IP:/www/wwwroot/blog/backend/
```

### 4.5 构建并运行后端容器

```bash
# 进入目录
cd /www/wwwroot/blog/backend

# 构建镜像
docker build -t blog-backend:1.0 .

# 创建Docker网络（让容器互通）
docker network create blog-network

# 将MySQL和Redis加入网络
docker network connect blog-network blog-mysql
docker network connect blog-network blog-redis

# 运行后端容器
docker run -d --name blog-backend --restart always --network blog-network -p 8080:8080 -v /www/wwwroot/blog/uploads:/app/uploads -v /www/wwwroot/blog/logs:/app/logs -e DB_PASSWORD=123456 -e JWT_SECRET=myblog2026secretkey123456789abcdefghijklmnop --memory=768m --cpus=1.5 blog-backend:1.0
```

### 4.6 验证后端运行

```bash
# 查看日志
docker logs -f blog-backend

# 测试接口
curl http://localhost:8080/api/articles
```

---

## 五、部署前端

### 5.1 本地构建前端

```bash
# 用户端
cd frontend/user
npm install
npm run build

# 管理端
cd frontend/admin
npm install
npm run build
```

### 5.2 上传前端文件

```bash
# 上传用户端
scp -r frontend/user/dist/* root@你的服务器IP:/www/wwwroot/blog/frontend/user/

# 上传管理端
scp -r frontend/admin/dist/* root@你的服务器IP:/www/wwwroot/blog/frontend/admin/
```

或使用宝塔面板的 **文件** 功能上传。

---

## 六、配置 Nginx（宝塔面板）

> 💡 **没有域名？** 可以直接使用服务器 IP 访问，参考下方 **方案一**

---

### 方案一：使用 IP 访问（无域名）

通过不同端口区分用户端和管理端：
- 用户端：`http://你的服务器IP:80`
- 管理端：`http://你的服务器IP:8081`

#### 6.1 创建用户端站点

1. 进入 **网站** → **添加站点**
2. 配置：
   - 域名：填写你的服务器 IP（如 `123.456.789.0`）
   - 根目录：`/www/wwwroot/blog/frontend/user`
   - PHP版本：纯静态

3. 点击站点 **设置** → **配置文件**，修改为：

```nginx
server {
    listen 80;
    server_name 你的服务器IP;
    root /www/wwwroot/blog/frontend/user;
    index index.html;

    # Gzip压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    gzip_min_length 1024;

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # Vue Router History模式
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_connect_timeout 10s;
        proxy_read_timeout 30s;
    }

    # 上传文件
    location /uploads {
        alias /www/wwwroot/blog/uploads;
        expires 30d;
    }

    # 禁止访问隐藏文件
    location ~ /\. {
        deny all;
    }
}
```

#### 6.2 创建管理端站点

1. 进入 **网站** → **添加站点**
2. 配置：
   - 域名：填写 `你的服务器IP:8081`（如 `123.456.789.0:8081`）
   - 根目录：`/www/wwwroot/blog/frontend/admin`
   - PHP版本：纯静态

3. 点击站点 **设置** → **配置文件**，修改为：

```nginx
server {
    listen 8081;
    server_name 你的服务器IP;
    root /www/wwwroot/blog/frontend/admin;
    index index.html;

    # Gzip压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 7d;
        add_header Cache-Control "public, immutable";
    }

    # Vue Router History模式
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 上传文件
    location /uploads {
        alias /www/wwwroot/blog/uploads;
        expires 30d;
    }
}
```

#### 6.3 开放端口

在宝塔面板 **安全** 中放行端口：
- 80（用户端）
- 8081（管理端）
- 8080（后端API，可选，建议仅内网访问）

如果是阿里云/腾讯云服务器，还需要在云控制台的 **安全组** 中放行这些端口。

#### 6.4 访问地址

- 用户端：`http://你的服务器IP`
- 管理端：`http://你的服务器IP:8081`

---

### 方案二：使用域名访问

如果你有域名，可以使用子域名区分用户端和管理端。

#### 6.1 创建用户端站点

1. 进入 **网站** → **添加站点**
2. 配置：
   - 域名：`blog.你的域名.com`
   - 根目录：`/www/wwwroot/blog/frontend/user`
   - PHP版本：纯静态

3. 点击站点 **设置** → **配置文件**，修改为：

```nginx
server {
    listen 80;
    server_name blog.你的域名.com;
    root /www/wwwroot/blog/frontend/user;
    index index.html;

    # Gzip压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    gzip_min_length 1024;

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # Vue Router History模式
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_connect_timeout 10s;
        proxy_read_timeout 30s;
    }

    # 上传文件
    location /uploads {
        alias /www/wwwroot/blog/uploads;
        expires 30d;
    }

    # 禁止访问隐藏文件
    location ~ /\. {
        deny all;
    }
}
```

#### 6.2 创建管理端站点

1. 进入 **网站** → **添加站点**
2. 配置：
   - 域名：`admin.你的域名.com`
   - 根目录：`/www/wwwroot/blog/frontend/admin`
   - PHP版本：纯静态

3. 点击站点 **设置** → **配置文件**，修改为：

```nginx
server {
    listen 80;
    server_name admin.你的域名.com;
    root /www/wwwroot/blog/frontend/admin;
    index index.html;

    # Gzip压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 7d;
        add_header Cache-Control "public, immutable";
    }

    # Vue Router History模式
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 上传文件
    location /uploads {
        alias /www/wwwroot/blog/uploads;
        expires 30d;
    }
}
```

#### 6.3 配置 SSL（可选但推荐）

1. 点击站点 **设置** → **SSL**
2. 选择 **Let's Encrypt** 免费证书
3. 点击申请，等待完成
4. 开启 **强制HTTPS**

---

## 七、内存分配总览

| 服务 | 内存限制 | CPU限制 | 说明 |
|------|----------|---------|------|
| MySQL | 1024MB | 1核 | 数据库 |
| Redis | 256MB | 0.5核 | 缓存 |
| 后端 (JVM) | 768MB | 1.5核 | Spring Boot |
| Nginx | ~100MB | - | 反向代理 |
| 系统 | ~1.8GB | 1核 | 系统预留 |
| **总计** | **~4GB** | **4核** | |

---

## 八、常用运维命令

### 8.1 查看容器状态

```bash
# 查看所有容器
docker ps -a

# 查看容器资源使用
docker stats

# 查看容器日志
docker logs -f --tail 100 blog-backend
docker logs -f --tail 100 blog-mysql
docker logs -f --tail 100 blog-redis
```

### 8.2 重启服务

```bash
# 重启后端
docker restart blog-backend

# 重启所有服务
docker restart blog-mysql blog-redis blog-backend
```

### 8.3 更新后端（一键命令）

```bash
docker stop blog-backend && docker rm blog-backend && docker rmi blog-backend:1.0 && cd /www/wwwroot/blog/backend && docker build -t blog-backend:1.0 . && docker run -d --name blog-backend --restart always --network blog-network -p 8080:8080 -v /www/wwwroot/blog/uploads:/app/uploads -v /www/wwwroot/blog/logs:/app/logs -e DB_PASSWORD=123456 -e JWT_SECRET=myblog2026secretkey123456789abcdefghijklmnop --memory=768m --cpus=1.5 blog-backend:1.0
```

### 8.4 更新前端

```bash
# 直接覆盖文件即可
# 用户端
rm -rf /www/wwwroot/blog/frontend/user/*
# 上传新的dist文件

# 管理端
rm -rf /www/wwwroot/blog/frontend/admin/*
# 上传新的dist文件
```

### 8.5 数据库备份

```bash
# 备份
docker exec blog-mysql mysqldump -uroot -p123456 blog_db > /www/wwwroot/blog/backup/blog_$(date +%Y%m%d).sql

# 恢复
docker exec -i blog-mysql mysql -uroot -p123456 blog_db < /www/wwwroot/blog/backup/blog_20240101.sql
```

---

## 九、故障排查

### 9.1 后端无法连接数据库

```bash
# 检查MySQL容器是否运行
docker ps | grep mysql

# 检查网络
docker network inspect blog-network

# 进入后端容器测试连接
docker exec -it blog-backend sh
ping blog-mysql
```

### 9.2 内存不足

```bash
# 查看内存使用
free -h
docker stats --no-stream

# 减少JVM内存（修改Dockerfile后重新构建）
# 或者直接修改运行参数
docker stop blog-backend
docker rm blog-backend
# 重新运行时减少 --memory 参数
```

### 9.3 Redis连接失败

```bash
# 检查Redis容器
docker logs blog-redis

# 测试连接
docker exec -it blog-redis redis-cli ping
```

---

## 十、一键部署脚本

将以下脚本保存为 `/www/wwwroot/blog/deploy.sh`：

```bash
#!/bin/bash

# 博客系统Docker一键部署脚本（4核4G配置）

set -e

echo "=== 博客系统部署开始 ==="

# 配置
DB_PASSWORD="123456"
JWT_SECRET="myblog2026secretkey123456789abcdefghijklmnop"

# 创建网络
docker network create blog-network 2>/dev/null || true

# 启动MySQL
echo "启动MySQL..."
docker run -d \
  --name blog-mysql \
  --restart always \
  --network blog-network \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=$DB_PASSWORD \
  -e MYSQL_DATABASE=blog_db \
  -e TZ=Asia/Shanghai \
  -v /www/wwwroot/blog/mysql:/var/lib/mysql \
  --memory=1024m \
  --cpus=1 \
  mysql:8.0 \
  --innodb-buffer-pool-size=512M --max-connections=100 --character-set-server=utf8mb4

# 等待MySQL启动
echo "等待MySQL启动..."
sleep 30

# 启动Redis
echo "启动Redis..."
docker run -d \
  --name blog-redis \
  --restart always \
  --network blog-network \
  -p 6379:6379 \
  -v /www/wwwroot/blog/redis:/data \
  --memory=256m \
  --cpus=0.5 \
  redis:7-alpine \
  redis-server --maxmemory 200mb --maxmemory-policy allkeys-lru --appendonly yes

# 构建后端镜像
echo "构建后端镜像..."
cd /www/wwwroot/blog/backend
docker build -t blog-backend:1.0 .

# 启动后端
echo "启动后端..."
docker run -d \
  --name blog-backend \
  --restart always \
  --network blog-network \
  -p 8080:8080 \
  -v /www/wwwroot/blog/uploads:/app/uploads \
  -v /www/wwwroot/blog/logs:/app/logs \
  -e DB_PASSWORD=$DB_PASSWORD \
  -e JWT_SECRET=$JWT_SECRET \
  --memory=768m \
  --cpus=1.5 \
  blog-backend:1.0

# 将后端加入网络
docker network connect blog-network blog-backend 2>/dev/null || true

echo "=== 部署完成 ==="
echo "请配置Nginx并导入数据库"
docker ps
```

运行：
```bash
chmod +x /www/wwwroot/blog/deploy.sh
/www/wwwroot/blog/deploy.sh
```

---

## 完成！

部署完成后：

**使用 IP 访问（无域名）：**
- 用户端访问：`http://你的服务器IP`
- 管理端访问：`http://你的服务器IP:8081`

**使用域名访问：**
- 用户端访问：`http://blog.你的域名.com`
- 管理端访问：`http://admin.你的域名.com`

**默认管理员账号：**`admin` / `123456`（请及时修改密码）
