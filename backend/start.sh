#!/bin/bash
# 博客系统启动脚本 - 针对2核CPU + 2G内存优化

APP_NAME="blog-backend"
JAR_FILE="target/blog-backend-1.0.0.jar"
LOG_DIR="/data/blog/logs"
PID_FILE="/data/blog/blog.pid"

# JVM参数优化 - 2G内存配置
# 堆内存: 最大512M，初始256M
# 元空间: 最大128M
# 使用G1垃圾收集器，适合低延迟场景
JVM_OPTS="-Xms256m -Xmx512m \
  -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -XX:+ParallelRefProcEnabled \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=${LOG_DIR}/heapdump.hprof \
  -Djava.security.egd=file:/dev/./urandom \
  -Dfile.encoding=UTF-8"

# Spring配置
SPRING_OPTS="--spring.profiles.active=prod"

# 创建日志目录
mkdir -p ${LOG_DIR}

start() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            echo "${APP_NAME} is already running (PID: $PID)"
            return 1
        fi
    fi
    
    echo "Starting ${APP_NAME}..."
    nohup java ${JVM_OPTS} -jar ${JAR_FILE} ${SPRING_OPTS} > ${LOG_DIR}/startup.log 2>&1 &
    echo $! > ${PID_FILE}
    echo "${APP_NAME} started (PID: $!)"
}

stop() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            echo "Stopping ${APP_NAME} (PID: $PID)..."
            kill $PID
            sleep 3
            if ps -p $PID > /dev/null 2>&1; then
                kill -9 $PID
            fi
            rm -f ${PID_FILE}
            echo "${APP_NAME} stopped"
        else
            echo "${APP_NAME} is not running"
            rm -f ${PID_FILE}
        fi
    else
        echo "${APP_NAME} is not running"
    fi
}

restart() {
    stop
    sleep 2
    start
}

status() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            echo "${APP_NAME} is running (PID: $PID)"
            return 0
        fi
    fi
    echo "${APP_NAME} is not running"
    return 1
}

case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
