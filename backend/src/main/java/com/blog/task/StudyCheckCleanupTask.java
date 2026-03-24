package com.blog.task;

import com.blog.service.StudyCheckTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyCheckCleanupTask {

    private final StudyCheckTaskService studyCheckTaskService;

    @Scheduled(cron = "${blog.study.check.cleanup-cron:0 20 3 * * ?}")
    public void cleanupExpiredHistory() {
        int deletedCount = studyCheckTaskService.cleanupExpiredHistory();
        if (deletedCount > 0) {
            log.info("学习抽查历史清理完成，删除任务数: {}", deletedCount);
        }
    }
}
