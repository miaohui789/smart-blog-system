package com.blog.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudyQuestionDetailVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private Integer questionNo;
    private String questionCode;
    private Integer questionType;
    private String title;
    private String questionStem;
    private String standardAnswer;
    private String answerSummary;
    private String keywords;
    private Integer difficulty;
    private Integer estimatedMinutes;
    private Integer answerWordCount;
    private BigDecimal scoreFullMark;
    private BigDecimal scorePassMark;
    private Integer aiScoreEnabled;
    private Integer selfAssessmentEnabled;
    private Integer reviewStatus;
    private Integer viewCount;
    private Integer studyCount;
    private Integer checkCount;
    private Integer studyStatus;
    private Integer masteryLevel;
    private Integer reviewPriority;
    private Integer isFavorite;
    private Integer isWrongQuestion;
    private Integer noteCount;
    private BigDecimal lastScore;
    private BigDecimal bestScore;
    private BigDecimal avgScore;
    private LocalDateTime lastStudyTime;
    private LocalDateTime lastCheckTime;
    private LocalDateTime nextReviewTime;
    private List<StudyNoteVO> notes;
}
