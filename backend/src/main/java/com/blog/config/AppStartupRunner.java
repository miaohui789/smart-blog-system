package com.blog.config;

import com.blog.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动后执行的初始化任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppStartupRunner implements ApplicationRunner {

    private final RedisService redisService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("==== 应用启动初始化开始 ====");
        // 每次后端重启清除所有 Token，防止旧 Token 残留与新实例产生单设备互踢冲突
        redisService.clearAllTokensOnStartup();
        log.info("==== 应用启动初始化完成 ====");
    }
}
