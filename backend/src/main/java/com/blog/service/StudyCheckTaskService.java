package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.common.result.PageResult;
import com.blog.dto.request.StudyAnswerSubmitRequest;
import com.blog.dto.request.StudyTaskCreateRequest;
import com.blog.dto.response.StudyCheckHistoryVO;
import com.blog.dto.response.StudyCheckStatisticsVO;
import com.blog.dto.response.StudyCheckTaskItemVO;
import com.blog.dto.response.StudyCheckTaskVO;
import com.blog.entity.StudyCheckTask;

public interface StudyCheckTaskService extends IService<StudyCheckTask> {

    StudyCheckTaskVO createTask(Long userId, StudyTaskCreateRequest request);

    StudyCheckTaskVO getTaskDetail(Long userId, Long taskId, boolean adminView);

    void markViewAnswer(Long userId, Long taskId, Long taskItemId);

    StudyCheckTaskItemVO submitAnswer(Long userId, Long taskId, Long taskItemId, StudyAnswerSubmitRequest request);

    StudyCheckTaskVO finishTask(Long userId, Long taskId);

    PageResult<StudyCheckHistoryVO> getHistory(Long userId, Integer page, Integer pageSize, Integer status);

    StudyCheckStatisticsVO getStatistics(Long userId);

    PageResult<StudyCheckHistoryVO> getAdminTaskPage(Integer page, Integer pageSize, Long userId, Long categoryId, Integer status);

    int cleanupExpiredHistory();
}
