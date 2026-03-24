package com.blog.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudyQuestionListVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private Integer questionNo;
    private String questionCode;
    private String title;
    private String answerSummary;
    private String keywords;
    private Integer difficulty;
    private Integer estimatedMinutes;
    private BigDecimal scoreFullMark;
    private BigDecimal scorePassMark;
    private Integer aiScoreEnabled;
    private Integer selfAssessmentEnabled;
    private Integer viewCount;
    private Integer studyCount;
    private Integer checkCount;
    private Integer studyStatus;
    private Integer masteryLevel;
    private Integer reviewPriority;
    private Integer isFavorite;
    private Integer isWrongQuestion;
    private BigDecimal lastScore;
    private LocalDateTime lastStudyTime;
    private LocalDateTime nextReviewTime;
}
