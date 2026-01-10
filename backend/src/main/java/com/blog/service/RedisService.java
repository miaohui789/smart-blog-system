package com.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Redis服务 - 支持Redis不可用时的降级处理
 */
@Slf4j
@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private boolean redisAvailable = false;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        checkRedisConnection();
    }

    /**
     * 检查Redis连接是否可用
     */
    public void checkRedisConnection() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            redisAvailable = true;
            log.info("Redis连接成功，缓存功能已启用");
        } catch (Exception e) {
            redisAvailable = false;
            log.warn("Redis连接失败，缓存功能已禁用，项目将以无缓存模式运行: {}", e.getMessage());
        }
    }

    /**
     * 判断Redis是否可用
     */
    public boolean isAvailable() {
        return redisAvailable;
    }

    public void set(String key, Object value) {
        if (!redisAvailable) return;
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        if (!redisAvailable) return;
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    public Object get(String key) {
        if (!redisAvailable) return null;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            handleRedisError(e);
            return null;
        }
    }

    public Boolean delete(String key) {
        if (!redisAvailable) return false;
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            handleRedisError(e);
            return false;
        }
    }

    public Boolean hasKey(String key) {
        if (!redisAvailable) return false;
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            handleRedisError(e);
            return false;
        }
    }

    public Long increment(String key) {
        if (!redisAvailable) return 0L;
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            handleRedisError(e);
            return 0L;
        }
    }

    public Long increment(String key, long delta) {
        if (!redisAvailable) return 0L;
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            handleRedisError(e);
            return 0L;
        }
    }

    private void handleRedisError(Exception e) {
        log.warn("Redis操作失败: {}", e.getMessage());
        redisAvailable = false;
    }
}
