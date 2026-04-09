package com.blog.task;

import com.blog.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotSearchCleanupTask {

    private static final ZoneId HOT_SEARCH_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter HOT_SEARCH_KEY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final RedisService redisService;

    @Scheduled(cron = "${blog.search.hot.cleanup-cron:5 0 0 * * ?}", zone = "Asia/Shanghai")
    public void cleanupPreviousDayBoard() {
        LocalDate previousDay = LocalDate.now(HOT_SEARCH_ZONE).minusDays(1);
        String cacheKey = RedisService.CACHE_SEARCH_HOT + previousDay.format(HOT_SEARCH_KEY_FORMATTER);
        Boolean deleted = redisService.delete(cacheKey);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("热搜榜单已在零点清理，key: {}", cacheKey);
        }
    }
}
