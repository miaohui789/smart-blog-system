package com.blog.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务端 - 用于实时通知用户被踢下线
 * 路径格式: /ws/{platform}/{userId}
 * platform: user 或 admin
 */
@Slf4j
@Component
@ServerEndpoint("/ws/{platform}/{userId}")
public class WebSocketServer {

    /**
     * 存储所有在线连接
     * key: platform_userId (如 user_1, admin_1)
     */
    private static final ConcurrentHashMap<String, Session> onlineSessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("platform") String platform, @PathParam("userId") Long userId) {
        String key = platform + "_" + userId;
        
        // 检查是否已有连接，踢掉旧连接
        Session oldSession = onlineSessions.get(key);
        if (oldSession != null && oldSession.isOpen() && !oldSession.getId().equals(session.getId())) {
            try {
                log.info("{} 在新设备登录，踢掉旧连接", key);
                oldSession.getBasicRemote().sendText("{\"type\":\"FORCE_LOGOUT\",\"message\":\"账号已在其他设备登录\"}");
                oldSession.close();
            } catch (IOException e) {
                log.error("关闭旧WebSocket连接失败: {}", e.getMessage());
            }
        }
        
        onlineSessions.put(key, session);
        log.info("{} WebSocket连接成功，当前在线: {}", key, onlineSessions.size());
    }

    @OnClose
    public void onClose(Session session, @PathParam("platform") String platform, @PathParam("userId") Long userId) {
        String key = platform + "_" + userId;
        Session stored = onlineSessions.get(key);
        if (stored != null && stored.getId().equals(session.getId())) {
            onlineSessions.remove(key);
        }
        log.info("{} WebSocket连接关闭，当前在线: {}", key, onlineSessions.size());
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("platform") String platform, @PathParam("userId") Long userId) {
        log.error("{}_{} WebSocket错误: {}", platform, userId, error.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if ("ping".equals(message)) {
            try {
                session.getBasicRemote().sendText("pong");
            } catch (IOException e) {
                log.error("发送心跳响应失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 强制下线
     * @param key 格式: platform_userId (如 user_1, admin_1)
     */
    public static void forceLogout(String key) {
        Session session = onlineSessions.get(key);
        if (session != null && session.isOpen()) {
            try {
                log.info("强制 {} 下线", key);
                session.getBasicRemote().sendText("{\"type\":\"FORCE_LOGOUT\",\"message\":\"账号已在其他设备登录\"}");
                session.close();
                onlineSessions.remove(key);
            } catch (IOException e) {
                log.error("强制 {} 下线失败: {}", key, e.getMessage());
            }
        }
    }

    public static int getOnlineCount() {
        return onlineSessions.size();
    }
}
