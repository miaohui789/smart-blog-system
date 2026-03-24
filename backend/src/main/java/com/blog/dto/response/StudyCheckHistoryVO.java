package com.blog.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudyCheckHistoryVO {

    private Long id;
    private String taskNo;
    private Long userId;
    private String userNickname;
    private String taskName;
    private Integer checkMode;
    private Long categoryId;
    private String categoryName;
    private Integer questionCount;
    private Integer answeredCount;
    private Integer scoredCount;
    private Integer rememberCount;
    private Integer fuzzyCount;
    private Integer forgetCount;
    private Integer excellentCount;
    private Integer passCount;
    private Integer failedCount;
    private BigDecimal selfScore;
    private BigDecimal aiScore;
    private BigDecimal finalScore;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer durationSeconds;
}
