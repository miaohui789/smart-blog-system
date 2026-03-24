package com.blog.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudyCheckTaskVO {

    private Long id;
    private String taskNo;
    private String taskName;
    private Integer taskSource;
    private Integer checkMode;
    private Long categoryId;
    private String categoryName;
    private String filterJson;
    private Integer questionCount;
    private Integer answeredCount;
    private Integer scoredCount;
    private Integer needAiScore;
    private Integer allowSelfAssessment;
    private Integer showStandardAnswer;
    private Integer scoringMode;
    private BigDecimal fullScore;
    private BigDecimal selfScore;
    private BigDecimal aiScore;
    private BigDecimal finalScore;
    private Integer rememberCount;
    private Integer fuzzyCount;
    private Integer forgetCount;
    private Integer excellentCount;
    private Integer passCount;
    private Integer failedCount;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer durationSeconds;
    private List<StudyCheckTaskItemVO> items;
}
