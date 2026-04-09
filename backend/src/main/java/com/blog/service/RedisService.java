package com.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis服务 - 支持Redis不可用时的降级处理
 */
@Slf4j
@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private boolean redisAvailable = false;

    // 缓存Key前缀常量
    public static final String CACHE_PREFIX = "blog:";
    public static final String CACHE_ARTICLE = CACHE_PREFIX + "article:";
    public static final String CACHE_ARTICLE_LIST = CACHE_PREFIX + "article:list:";
    public static final String CACHE_HOT_ARTICLES = CACHE_PREFIX + "hot:articles";
    public static final String CACHE_RECOMMEND_ARTICLES = CACHE_PREFIX + "recommend:articles";
    public static final String CACHE_CATEGORY = CACHE_PREFIX + "category:";
    public static final String CACHE_CATEGORY_LIST = CACHE_PREFIX + "category:list";
    public static final String CACHE_TAG = CACHE_PREFIX + "tag:";
    public static final String CACHE_TAG_LIST = CACHE_PREFIX + "tag:list";
    public static final String CACHE_USER = CACHE_PREFIX + "user:";
    public static final String CACHE_SITE_CONFIG = CACHE_PREFIX + "site:config";
    public static final String CACHE_ARCHIVE = CACHE_PREFIX + "archive";
    public static final String CACHE_STATS = CACHE_PREFIX + "stats";
    public static final String CACHE_VIEW_COUNT = CACHE_PREFIX + "view:";
    
    // 新功能缓存Key
    public static final String CACHE_NOTIFICATION_UNREAD = CACHE_PREFIX + "notification:unread:";
    public static final String CACHE_MESSAGE_UNREAD = CACHE_PREFIX + "message:unread:";
    public static final String CACHE_MESSAGE_CONVERSATION = CACHE_PREFIX + "message:conv:";
    public static final String CACHE_WEATHER = CACHE_PREFIX + "weather:";
    public static final String CACHE_USER_FOLLOW = CACHE_PREFIX + "user:follow:";
    public static final String CACHE_USER_FANS = CACHE_PREFIX + "user:fans:";
    public static final String CACHE_SEARCH_HOT = CACHE_PREFIX + "search:hot:";
    public static final String CACHE_STUDY_CATEGORY_TREE = CACHE_PREFIX + "study:category:tree:";
    public static final String CACHE_STUDY_CATEGORY_TREE_ENABLED = CACHE_STUDY_CATEGORY_TREE + "enabled";
    public static final String CACHE_STUDY_OVERVIEW = CACHE_PREFIX + "study:overview:";
    public static final String CACHE_STUDY_OVERVIEW_VERSION = CACHE_PREFIX + "study:overview:version";
    public static final String CACHE_STUDY_CHECK_STATS = CACHE_PREFIX + "study:check:stats:";

    // 缓存过期时间（分钟）
    public static final long EXPIRE_SHORT = 5;      // 5分钟
    public static final long EXPIRE_MEDIUM = 30;    // 30分钟
    public static final long EXPIRE_LONG = 60;      // 1小时
    public static final long EXPIRE_DAY = 1440;     // 1天
    public static final long EXPIRE_WEEK = 10080;   // 7天 - 用于图片和头像缓存
    public static final long EXPIRE_MONTH = 43200;  // 30天 - 用于静态资源

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

    // ==================== 基础操作 ====================

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

    /**
     * 设置缓存（分钟为单位）
     */
    public void setWithMinutes(String key, Object value, long minutes) {
        set(key, value, minutes, TimeUnit.MINUTES);
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

    /**
     * 获取缓存并转换类型
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) return null;
        if (clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * 获取List类型缓存
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key) {
        Object value = get(key);
        if (value instanceof List) {
            return (List<T>) value;
        }
        return null;
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

    /**
     * 批量删除缓存
     */
    public Long deleteByPattern(String pattern) {
        if (!redisAvailable) return 0L;
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                return redisTemplate.delete(keys);
            }
            return 0L;
        } catch (Exception e) {
            handleRedisError(e);
            return 0L;
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

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        if (!redisAvailable) return false;
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            handleRedisError(e);
            return false;
        }
    }

    /**
     * 获取过期时间
     */
    public Long getExpire(String key) {
        if (!redisAvailable) return -1L;
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            handleRedisError(e);
            return -1L;
        }
    }

    // ==================== 计数器操作 ====================

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

    public Long decrement(String key) {
        if (!redisAvailable) return 0L;
        try {
            return redisTemplate.opsForValue().decrement(key);
        } catch (Exception e) {
            handleRedisError(e);
            return 0L;
        }
    }

    // ==================== Hash操作 ====================

    public void hSet(String key, String hashKey, Object value) {
        if (!redisAvailable) return;
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    public Object hGet(String key, String hashKey) {
        if (!redisAvailable) return null;
        try {
            return redisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e) {
            handleRedisError(e);
            return null;
        }
    }

    public Map<Object, Object> hGetAll(String key) {
        if (!redisAvailable) return new HashMap<>();
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            handleRedisError(e);
            return new HashMap<>();
        }
    }

    public void hSetAll(String key, Map<String, Object> map) {
        if (!redisAvailable) return;
        try {
            redisTemplate.opsForHash().putAll(key, map);
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    public Long hIncrement(String key, String hashKey, long delta) {
        if (!redisAvailable) return 0L;
        try {
            return redisTemplate.opsForHash().increment(key, hashKey, delta);
        } catch (Exception e) {
            handleRedisError(e);
            return 0L;
        }
    }

    // ==================== Set操作 ====================

    public Long sAdd(String key, Object... values) {
        if (!redisAvailable) return 0L;
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            handleRedisError(e);
            return 0L;
        }
    }

    public Boolean sIsMember(String key, Object value) {
        if (!redisAvailable) return false;
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            handleRedisError(e);
            return false;
        }
    }

    public Long sRemove(String key, Object... values) {
        if (!redisAvailable) return 0L;
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            handleRedisError(e);
            return 0L;
        }
    }

    public Set<Object> sMembers(String key) {
        if (!redisAvailable) return new HashSet<>();
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            handleRedisError(e);
            return new HashSet<>();
        }
    }

    // ==================== ZSet操作（排行榜） ====================

    public Boolean zAdd(String key, Object value, double score) {
        if (!redisAvailable) return false;
        try {
            return redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            handleRedisError(e);
            return false;
        }
    }

    public Double zIncrementScore(String key, Object value, double delta) {
        if (!redisAvailable) return 0.0;
        try {
            return redisTemplate.opsForZSet().incrementScore(key, value, delta);
        } catch (Exception e) {
            handleRedisError(e);
            return 0.0;
        }
    }

    public Set<Object> zReverseRange(String key, long start, long end) {
        if (!redisAvailable) return new LinkedHashSet<>();
        try {
            return redisTemplate.opsForZSet().reverseRange(key, start, end);
        } catch (Exception e) {
            handleRedisError(e);
            return new LinkedHashSet<>();
        }
    }

    public Set<ZSetOperations.TypedTuple<Object>> zReverseRangeWithScores(String key, long start, long end) {
        if (!redisAvailable) return new LinkedHashSet<>();
        try {
            return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        } catch (Exception e) {
            handleRedisError(e);
            return new LinkedHashSet<>();
        }
    }

    public Double zScore(String key, Object value) {
        if (!redisAvailable) return null;
        try {
            return redisTemplate.opsForZSet().score(key, value);
        } catch (Exception e) {
            handleRedisError(e);
            return null;
        }
    }

    // ==================== 缓存清理 ====================

    /**
     * 清除文章相关缓存
     */
    public void clearArticleCache(Long articleId) {
        if (!redisAvailable) return;
        try {
            // 清除单篇文章缓存
            delete(CACHE_ARTICLE + articleId);
            // 清除文章列表缓存
            deleteByPattern(CACHE_ARTICLE_LIST + "*");
            // 清除热门文章缓存
            delete(CACHE_HOT_ARTICLES);
            // 清除推荐文章缓存
            delete(CACHE_RECOMMEND_ARTICLES);
            // 清除归档缓存
            delete(CACHE_ARCHIVE);
            // 清除统计缓存
            delete(CACHE_STATS);
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    /**
     * 清除分类相关缓存
     */
    public void clearCategoryCache() {
        if (!redisAvailable) return;
        try {
            deleteByPattern(CACHE_CATEGORY + "*");
            delete(CACHE_CATEGORY_LIST);
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    /**
     * 清除标签相关缓存
     */
    public void clearTagCache() {
        if (!redisAvailable) return;
        try {
            deleteByPattern(CACHE_TAG + "*");
            delete(CACHE_TAG_LIST);
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    /**
     * 清除用户相关缓存
     */
    public void clearUserCache(Long userId) {
        if (!redisAvailable) return;
        try {
            delete(CACHE_USER + userId);
            delete(CACHE_NOTIFICATION_UNREAD + userId);
            delete(CACHE_MESSAGE_UNREAD + userId);
            deleteByPattern(CACHE_USER_FOLLOW + userId + ":*");
            deleteByPattern(CACHE_USER_FANS + userId + ":*");
        } catch (Exception e) {
            handleRedisError(e);
        }
    }
    
    /**
     * 清除通知未读数缓存
     */
    public void clearNotificationCache(Long userId) {
        if (!redisAvailable) return;
        try {
            delete(CACHE_NOTIFICATION_UNREAD + userId);
        } catch (Exception e) {
            handleRedisError(e);
        }
    }
    
    /**
     * 清除私信未读数缓存
     */
    public void clearMessageCache(Long userId) {
        if (!redisAvailable) return;
        try {
            delete(CACHE_MESSAGE_UNREAD + userId);
            delete(CACHE_MESSAGE_CONVERSATION + userId);
        } catch (Exception e) {
            handleRedisError(e);
        }
    }
    
    /**
     * 增加私信未读数（原子操作）
     */
    public Long incrementMessageUnread(Long userId) {
        if (!redisAvailable) return 0L;
        try {
            String key = CACHE_MESSAGE_UNREAD + userId;
            Long result = redisTemplate.opsForValue().increment(key);
            // 设置过期时间，防止数据不一致时长期存在
            expire(key, EXPIRE_MEDIUM, TimeUnit.MINUTES);
            return result;
        } catch (Exception e) {
            handleRedisError(e);
            return 0L;
        }
    }
    
    /**
     * 减少私信未读数（原子操作）
     */
    public Long decrementMessageUnread(Long userId, int count) {
        if (!redisAvailable) return 0L;
        try {
            String key = CACHE_MESSAGE_UNREAD + userId;
            Long result = redisTemplate.opsForValue().decrement(key, count);
            // 防止负数
            if (result != null && result < 0) {
                set(key, 0);
                return 0L;
            }
            return result;
        } catch (Exception e) {
            handleRedisError(e);
            return 0L;
        }
    }
    
    /**
     * 清除天气缓存
     */
    public void clearWeatherCache(String city) {
        if (!redisAvailable) return;
        try {
            delete(CACHE_WEATHER + city);
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    /**
     * 清除学习分类树缓存
     */
    public void clearStudyCategoryCache() {
        if (!redisAvailable) return;
        try {
            delete(CACHE_STUDY_CATEGORY_TREE_ENABLED);
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    /**
     * 清除用户学习概览缓存
     */
    public void clearStudyOverviewCache(Long userId) {
        if (!redisAvailable || userId == null) return;
        try {
            delete(buildStudyOverviewCacheKey(userId));
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    /**
     * 清除全部学习概览缓存
     */
    public void clearAllStudyOverviewCache() {
        if (!redisAvailable) return;
        try {
            Long version = increment(CACHE_STUDY_OVERVIEW_VERSION);
            if (version != null && version > 0) {
                expire(CACHE_STUDY_OVERVIEW_VERSION, EXPIRE_MONTH, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    /**
     * 清除用户抽查统计缓存
     */
    public void clearStudyCheckStatsCache(Long userId) {
        if (!redisAvailable || userId == null) return;
        try {
            delete(buildStudyCheckStatsCacheKey(userId));
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    /**
     * 清除用户学习模块缓存
     */
    public void clearStudyUserCache(Long userId) {
        if (!redisAvailable || userId == null) return;
        try {
            clearStudyOverviewCache(userId);
            clearStudyCheckStatsCache(userId);
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    /**
     * 构建学习概览缓存Key，使用版本号避免全量模糊删除
     */
    public String buildStudyOverviewCacheKey(Long userId) {
        return CACHE_STUDY_OVERVIEW + getCacheVersion(CACHE_STUDY_OVERVIEW_VERSION) + ":" + userId;
    }

    /**
     * 构建抽查统计缓存Key
     */
    public String buildStudyCheckStatsCacheKey(Long userId) {
        return CACHE_STUDY_CHECK_STATS + userId;
    }

    /**
     * 在事务提交后执行缓存操作，避免旧数据回填缓存
     */
    public void runAfterCommit(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runnable.run();
                }
            });
            return;
        }
        runnable.run();
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        if (!redisAvailable) return;
        try {
            deleteByPattern(CACHE_PREFIX + "*");
            log.info("已清除所有缓存");
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    /**
     * 应用启动时清除全部用户/管理员 Token 及 Token 黑名单
     * 防止重启后旧 token 残留导致单设备互踢逻辑错乱
     */
    public void clearAllTokensOnStartup() {
        if (!redisAvailable) return;
        try {
            long userCount   = deleteByPattern("blog:user:token:*");
            long adminCount  = deleteByPattern("blog:admin:token:*");
            long blackCount  = deleteByPattern("blog:token:blacklist:*");
            log.info("服务启动 - 已清除 Token: 用户端={}, 管理端={}, 黑名单={}", userCount, adminCount, blackCount);
        } catch (Exception e) {
            handleRedisError(e);
        }
    }

    private void handleRedisError(Exception e) {
        log.warn("Redis操作失败: {}", e.getMessage());
        // 不再禁用Redis，只记录错误，下次操作继续尝试
    }

    private long getCacheVersion(String key) {
        Object value = get(key);
        if (value instanceof Number) {
            return Math.max(1L, ((Number) value).longValue());
        }
        if (value != null) {
            try {
                return Math.max(1L, Long.parseLong(value.toString()));
            } catch (NumberFormatException ignored) {
                return 1L;
            }
        }
        return 1L;
    }
}
