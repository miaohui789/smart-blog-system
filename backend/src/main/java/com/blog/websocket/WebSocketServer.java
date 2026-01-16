package com.blog.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务端 - 用于实时通知用户被踢下线和私信推送
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
    
    /**
     * 存储用户正在浏览的文章ID
     * key: platform_userId, value: articleId
     */
    private static final ConcurrentHashMap<String, Long> userViewingArticle = new ConcurrentHashMap<>();
    
    private static final ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @PostConstruct
    public void init() {
        log.info("========== WebSocketServer 初始化完成 ==========");
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("platform") String platform, @PathParam("userId") Long userId) {
        String key = platform + "_" + userId;
        log.info("========== WebSocket连接请求: {} ==========", key);
        
        // 检查是否已有连接
        Session oldSession = onlineSessions.get(key);
        if (oldSession != null && !oldSession.getId().equals(session.getId())) {
            // 尝试发送心跳检测旧连接是否真的还活着
            try {
                if (oldSession.isOpen()) {
                    // 先尝试发送一个测试消息，如果失败说明连接已断开
                    oldSession.getBasicRemote().sendText("{\"type\":\"ping\"}");
                    // 如果能发送成功，说明旧连接还活着，需要踢掉
                    log.info("{} 在新设备登录，踢掉旧连接", key);
                    oldSession.getBasicRemote().sendText("{\"type\":\"FORCE_LOGOUT\",\"message\":\"账号已在其他设备登录\"}");
                    oldSession.close();
                }
            } catch (IOException e) {
                // 发送失败说明旧连接已经断开，直接移除即可，不需要通知
                log.info("{} 旧连接已失效，直接替换", key);
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
            userViewingArticle.remove(key);
        }
        log.info("{} WebSocket连接关闭，当前在线: {}", key, onlineSessions.size());
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("platform") String platform, @PathParam("userId") Long userId) {
        log.error("{}_{} WebSocket错误: {}", platform, userId, error.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("platform") String platform, @PathParam("userId") Long userId) {
        try {
            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            String type = (String) data.get("type");
            
            if ("ping".equals(type) || "ping".equals(message)) {
                session.getBasicRemote().sendText("{\"type\":\"pong\"}");
            } else if ("view_article".equals(type)) {
                // 用户开始浏览文章
                Object articleIdObj = data.get("articleId");
                if (articleIdObj != null) {
                    Long articleId = Long.valueOf(articleIdObj.toString());
                    String key = platform + "_" + userId;
                    userViewingArticle.put(key, articleId);
                    log.info("用户 {} 正在浏览文章 {}", key, articleId);
                }
            } else if ("leave_article".equals(type)) {
                // 用户离开文章页面
                String key = platform + "_" + userId;
                userViewingArticle.remove(key);
            }
        } catch (Exception e) {
            // 兼容旧的ping消息
            if ("ping".equals(message)) {
                try {
                    session.getBasicRemote().sendText("pong");
                } catch (IOException ex) {
                    log.error("发送心跳响应失败: {}", ex.getMessage());
                }
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
    
    /**
     * 向指定用户发送私信消息
     * @param userId 用户ID
     * @param message 消息对象
     */
    public void sendMessageToUser(Long userId, Object message) {
        String key = "user_" + userId;
        Session session = onlineSessions.get(key);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("type", "message");
                data.put("data", message);
                String json = objectMapper.writeValueAsString(data);
                session.getBasicRemote().sendText(json);
                log.info("向用户 {} 推送私信成功", userId);
            } catch (IOException e) {
                log.error("向用户 {} 推送私信失败: {}", userId, e.getMessage());
            }
        } else {
            log.info("用户 {} 不在线，无法推送私信", userId);
        }
    }
    
    /**
     * 向指定用户发送通知
     * @param userId 用户ID
     * @param type 通知类型
     * @param data 通知数据
     */
    public void sendNotification(Long userId, String type, Object data) {
        String key = "user_" + userId;
        Session session = onlineSessions.get(key);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", type);
                notification.put("data", data);
                String json = objectMapper.writeValueAsString(notification);
                session.getBasicRemote().sendText(json);
            } catch (IOException e) {
                log.error("向用户 {} 发送通知失败: {}", userId, e.getMessage());
            }
        }
    }

    public static int getOnlineCount() {
        return onlineSessions.size();
    }
    
    /**
     * 广播消息到正在浏览指定文章的所有用户
     * @param articleId 文章ID
     * @param type 消息类型
     * @param data 消息数据
     */
    public void broadcastToArticle(Long articleId, String type, Object data) {
        userViewingArticle.forEach((key, viewingArticleId) -> {
            if (articleId.equals(viewingArticleId)) {
                Session session = onlineSessions.get(key);
                if (session != null && session.isOpen()) {
                    try {
                        Map<String, Object> message = new HashMap<>();
                        message.put("type", type);
                        message.put("data", data);
                        String json = objectMapper.writeValueAsString(message);
                        session.getBasicRemote().sendText(json);
                        log.info("广播评论到用户 {}", key);
                    } catch (IOException e) {
                        log.error("广播消息失败: {}", e.getMessage());
                    }
                }
            }
        });
    }
}
