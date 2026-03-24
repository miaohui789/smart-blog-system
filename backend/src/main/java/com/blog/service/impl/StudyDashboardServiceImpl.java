package com.blog.service.impl;

import com.blog.dto.response.StudyDashboardVO;
import com.blog.mapper.StudyCheckTaskItemMapper;
import com.blog.mapper.StudyUserQuestionProgressMapper;
import com.blog.service.StudyCheckTaskService;
import com.blog.service.StudyDashboardService;
import com.blog.service.StudyQuestionService;
import org.springframework.stereotype.Service;

@Service
public class StudyDashboardServiceImpl implements StudyDashboardService {

    private static final int FAVORITE_LIMIT = 6;
    private static final int REVIEW_LIMIT = 6;
    private static final int FAILED_LIMIT = 6;
    private static final int RECENT_HISTORY_LIMIT = 10;

    private final StudyQuestionService studyQuestionService;
    private final StudyCheckTaskService studyCheckTaskService;
    private final StudyUserQuestionProgressMapper progressMapper;
    private final StudyCheckTaskItemMapper taskItemMapper;

    public StudyDashboardServiceImpl(StudyQuestionService studyQuestionService,
                                     StudyCheckTaskService studyCheckTaskService,
                                     StudyUserQuestionProgressMapper progressMapper,
                                     StudyCheckTaskItemMapper taskItemMapper) {
        this.studyQuestionService = studyQuestionService;
        this.studyCheckTaskService = studyCheckTaskService;
        this.progressMapper = progressMapper;
        this.taskItemMapper = taskItemMapper;
    }

    @Override
    public StudyDashboardVO getUserDashboard(Long userId) {
        StudyDashboardVO vo = new StudyDashboardVO();
        vo.setOverview(studyQuestionService.getOverview(userId));
        vo.setStatistics(studyCheckTaskService.getStatistics(userId));
        vo.setFavoriteQuestions(progressMapper.selectDashboardFavoriteQuestions(userId, FAVORITE_LIMIT));
        vo.setReviewQuestions(progressMapper.selectDashboardReviewQuestions(userId, REVIEW_LIMIT));
        vo.setFailedRecords(taskItemMapper.selectDashboardFailedRecords(userId, FAILED_LIMIT));
        vo.setRecentCheckHistory(studyCheckTaskService.getHistory(userId, 1, RECENT_HISTORY_LIMIT, null).getList());
        return vo;
    }
}
