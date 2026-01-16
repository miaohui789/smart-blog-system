package com.blog.security;

import com.blog.service.RedisService;
import com.blog.websocket.WebSocketServer;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Key key;
    
    private final RedisService redisService;
    
    private static final String TOKEN_BLACKLIST_PREFIX = "blog:token:blacklist:";
    private static final String USER_TOKEN_PREFIX = "blog:user:token:";
    private static final String ADMIN_TOKEN_PREFIX = "blog:admin:token:";

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成用户端Token
     */
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, false);
    }

    /**
     * 生成管理端Token
     */
    public String generateAdminToken(Long userId, String username) {
        return generateToken(userId, username, true);
    }

    /**
     * 生成Token
     * @param isAdmin 是否是管理端
     */
    private String generateToken(Long userId, String username, boolean isAdmin) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("isAdmin", isAdmin)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        
        String tokenPrefix = isAdmin ? ADMIN_TOKEN_PREFIX : USER_TOKEN_PREFIX;
        String wsPrefix = isAdmin ? "admin_" : "user_";
        
        boolean shouldForceLogout = false;
        
        if (redisService.isAvailable()) {
            // 获取该端之前的Token
            Object oldToken = redisService.get(tokenPrefix + userId);
            if (oldToken != null) {
                String oldTokenStr = oldToken.toString();
                try {
                    Claims oldClaims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(oldTokenStr)
                            .getBody();
                    long remainingTime = oldClaims.getExpiration().getTime() - System.currentTimeMillis();
                    if (remainingTime > 0) {
                        // 旧Token还有效，需要踢掉旧设备
                        shouldForceLogout = true;
                        redisService.set(TOKEN_BLACKLIST_PREFIX + oldTokenStr, "1", remainingTime, TimeUnit.MILLISECONDS);
                    }
                } catch (Exception e) {
                    // 旧Token已失效，不需要踢人
                }
            }
            // 存储新Token
            redisService.set(tokenPrefix + userId, token, expiration, TimeUnit.MILLISECONDS);
        }
        
        // 只有当旧Token还有效时，才通过WebSocket通知旧设备下线
        if (shouldForceLogout) {
            WebSocketServer.forceLogout(wsPrefix + userId);
        }
        
        return token;
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            
            if (redisService.isAvailable()) {
                // 检查Token是否在黑名单中
                if (Boolean.TRUE.equals(redisService.hasKey(TOKEN_BLACKLIST_PREFIX + token))) {
                    return false;
                }
                
                // 单设备登录校验
                Long userId = Long.parseLong(claims.getSubject());
                Boolean isAdmin = claims.get("isAdmin", Boolean.class);
                String tokenPrefix = Boolean.TRUE.equals(isAdmin) ? ADMIN_TOKEN_PREFIX : USER_TOKEN_PREFIX;
                
                Object currentToken = redisService.get(tokenPrefix + userId);
                if (currentToken != null && !token.equals(currentToken.toString())) {
                    return false;
                }
            }
            
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 将 Token 加入黑名单（用于登出）
     */
    public void invalidateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            if (redisService.isAvailable()) {
                long remainingTime = claims.getExpiration().getTime() - System.currentTimeMillis();
                if (remainingTime > 0) {
                    redisService.set(TOKEN_BLACKLIST_PREFIX + token, "1", remainingTime, TimeUnit.MILLISECONDS);
                }
                Long userId = Long.parseLong(claims.getSubject());
                Boolean isAdmin = claims.get("isAdmin", Boolean.class);
                String tokenPrefix = Boolean.TRUE.equals(isAdmin) ? ADMIN_TOKEN_PREFIX : USER_TOKEN_PREFIX;
                redisService.delete(tokenPrefix + userId);
            }
        } catch (Exception e) {
            // Token 解析失败，忽略
        }
    }
}
