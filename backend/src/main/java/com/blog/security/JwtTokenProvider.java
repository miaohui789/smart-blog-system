package com.blog.security;

import com.blog.service.RedisService;
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

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        
        // 将 Token 存入 Redis，用于追踪用户登录状态
        redisService.set(USER_TOKEN_PREFIX + userId, token, expiration, TimeUnit.MILLISECONDS);
        
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

    public boolean validateToken(String token) {
        try {
            // 检查 Token 是否在黑名单中
            if (Boolean.TRUE.equals(redisService.hasKey(TOKEN_BLACKLIST_PREFIX + token))) {
                return false;
            }
            
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
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
            
            // 计算 Token 剩余有效时间
            long remainingTime = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (remainingTime > 0) {
                // 将 Token 加入黑名单，过期时间与 Token 剩余时间一致
                redisService.set(TOKEN_BLACKLIST_PREFIX + token, "1", remainingTime, TimeUnit.MILLISECONDS);
            }
            
            // 删除用户的 Token 记录
            Long userId = Long.parseLong(claims.getSubject());
            redisService.delete(USER_TOKEN_PREFIX + userId);
        } catch (Exception e) {
            // Token 解析失败，忽略
        }
    }
}
