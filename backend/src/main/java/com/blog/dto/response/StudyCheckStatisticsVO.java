package com.blog.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StudyCheckStatisticsVO {

    private Long totalTaskCount;
    private Long completedTaskCount;
    private Long totalQuestionCount;
    private Long answeredQuestionCount;
    private Long favoriteQuestionCount;
    private Long wrongQuestionCount;
    private Long masteredQuestionCount;
    private Long reviewingQuestionCount;
    private BigDecimal avgScore;
    private BigDecimal bestScore;
    private BigDecimal latestScore;
}
