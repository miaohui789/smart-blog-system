# 博客系统部署指南

## 服务器要求
- CPU: 2核
- 内存: 2GB
- 系统: CentOS 7+ / Ubuntu 18.04+
- JDK: 17+
- Node.js: 18+

## 内存分配建议

| 服务 | 内存 |
|------|------|
| JVM (Spring Boot) | 256-512MB |
| MySQL | 256-512MB |
| Redis | 128MB |
| Nginx | 50MB |
| 系统预留 | 500MB+ |

## 快速部署

### 1. 构建项目

```bash
# 后端
cd backend
mvn clean package -DskipTests

# 前端用户端
cd frontend/user
npm install
npm run build

# 前端管理端
cd frontend/admin
npm install
npm run build
```

### 2. 部署

```bash
# 完整部署
chmod +x deploy/deploy.sh
./deploy/deploy.sh all
```

### 3. 配置

修改 `/etc/nginx/nginx.conf` 中的域名配置。

## JVM 启动参数说明

```bash
-Xms256m              # 初始堆内存
-Xmx512m              # 最大堆内存
-XX:MetaspaceSize=64m # 初始元空间
-XX:MaxMetaspaceSize=128m # 最大元空间
-XX:+UseG1GC          # 使用G1垃圾收集器
-XX:MaxGCPauseMillis=100 # 最大GC停顿时间
```

## 性能优化要点

### 后端
- 数据库连接池: 2-5个连接
- Redis连接池: 2-4个连接
- Tomcat线程: 5-50个
- 开启响应压缩

### 前端
- 代码分包 (Vue/Element/Echarts分离)
- Gzip压缩
- 静态资源缓存 (30天)
- 移除console.log

### Nginx
- 开启Gzip
- 静态资源缓存
- 连接复用 (keepalive)

### MySQL
- InnoDB缓冲池: 256MB
- 连接数限制: 100
- 开启慢查询日志

### Redis
- 内存限制: 128MB
- LRU淘汰策略
- 关闭AOF持久化

## 监控命令

```bash
# 查看Java进程内存
jstat -gc <pid>

# 查看系统内存
free -h

# 查看CPU使用
top -p <pid>

# 查看Nginx状态
nginx -t
systemctl status nginx

# 查看MySQL状态
mysqladmin status
```

## 常见问题

### 内存不足
1. 减小JVM堆内存 (-Xmx)
2. 减少数据库连接池大小
3. 关闭不必要的服务

### 响应慢
1. 检查Redis是否正常
2. 检查慢查询日志
3. 检查网络延迟
