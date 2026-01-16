FROM openjdk:17-jdk-slim

LABEL maintainer="your-email@example.com"

WORKDIR /app

# 复制JAR包
COPY target/blog-backend-1.0.0.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploads

# 暴露端口
EXPOSE 8080

# JVM优化参数 - 针对2G内存服务器
ENV JAVA_OPTS="-Xms128m -Xmx256m \
  -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -Djava.security.egd=file:/dev/./urandom"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=prod"]