package com.blog.dto.response;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class StudyDashboardVO {

    private StudyProgressOverviewVO overview;
    private StudyCheckStatisticsVO statistics;
    private List<StudyQuestionListVO> favoriteQuestions = Collections.emptyList();
    private List<StudyQuestionListVO> reviewQuestions = Collections.emptyList();
    private List<StudyDashboardFailedRecordVO> failedRecords = Collections.emptyList();
    private List<StudyCheckHistoryVO> recentCheckHistory = Collections.emptyList();
}
