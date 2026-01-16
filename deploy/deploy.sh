#!/bin/bash
# 博客系统一键部署脚本
# 适用于2核CPU + 2G内存服务器

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
echo_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
echo_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 配置变量
DEPLOY_DIR="/data/blog"
BACKEND_JAR="blog-backend-1.0.0.jar"

# 创建目录结构
create_dirs() {
    echo_info "创建目录结构..."
    mkdir -p ${DEPLOY_DIR}/{frontend/user,frontend/admin,uploads,logs}
}

# 部署后端
deploy_backend() {
    echo_info "部署后端服务..."
    
    # 停止旧服务
    if [ -f "${DEPLOY_DIR}/blog.pid" ]; then
        PID=$(cat ${DEPLOY_DIR}/blog.pid)
        if ps -p $PID > /dev/null 2>&1; then
            echo_info "停止旧服务 (PID: $PID)..."
            kill $PID
            sleep 3
        fi
    fi
    
    # 复制JAR包
    cp backend/target/${BACKEND_JAR} ${DEPLOY_DIR}/
    cp backend/start.sh ${DEPLOY_DIR}/
    chmod +x ${DEPLOY_DIR}/start.sh
    
    # 启动服务
    cd ${DEPLOY_DIR}
    ./start.sh start
    
    echo_info "后端服务已启动"
}

# 部署前端
deploy_frontend() {
    echo_info "部署前端..."
    
    # 用户端
    if [ -d "frontend/user/dist" ]; then
        rm -rf ${DEPLOY_DIR}/frontend/user/*
        cp -r frontend/user/dist/* ${DEPLOY_DIR}/frontend/user/
        echo_info "用户端部署完成"
    else
        echo_warn "用户端未构建，请先执行: cd frontend/user && npm run build"
    fi
    
    # 管理端
    if [ -d "frontend/admin/dist" ]; then
        rm -rf ${DEPLOY_DIR}/frontend/admin/*
        cp -r frontend/admin/dist/* ${DEPLOY_DIR}/frontend/admin/
        echo_info "管理端部署完成"
    else
        echo_warn "管理端未构建，请先执行: cd frontend/admin && npm run build"
    fi
}

# 配置Nginx
setup_nginx() {
    echo_info "配置Nginx..."
    
    if [ -f "/etc/nginx/nginx.conf" ]; then
        cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.bak
    fi
    
    # 复制配置（需要手动修改域名）
    cp deploy/nginx.conf /etc/nginx/nginx.conf
    
    # 测试配置
    nginx -t
    
    # 重载配置
    systemctl reload nginx || nginx -s reload
    
    echo_info "Nginx配置完成"
}

# 显示帮助
show_help() {
    echo "博客系统部署脚本"
    echo ""
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  all       - 完整部署（后端+前端）"
    echo "  backend   - 仅部署后端"
    echo "  frontend  - 仅部署前端"
    echo "  nginx     - 配置Nginx"
    echo "  help      - 显示帮助"
    echo ""
    echo "示例:"
    echo "  $0 all      # 完整部署"
    echo "  $0 backend  # 仅更新后端"
}

# 主函数
main() {
    case "$1" in
        all)
            create_dirs
            deploy_backend
            deploy_frontend
            echo_info "部署完成！"
            ;;
        backend)
            create_dirs
            deploy_backend
            ;;
        frontend)
            create_dirs
            deploy_frontend
            ;;
        nginx)
            setup_nginx
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            show_help
            exit 1
            ;;
    esac
}

main "$@"
