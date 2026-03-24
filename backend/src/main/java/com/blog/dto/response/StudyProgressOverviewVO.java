package com.blog.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StudyProgressOverviewVO {

    private Long totalQuestions;
    private Long studiedQuestions;
    private Long masteredQuestions;
    private Long reviewingQuestions;
    private Long favoriteQuestions;
    private Long wrongQuestions;
    private Long checkTaskCount;
    private Long completedCheckTaskCount;
    private BigDecimal avgScore;
}
