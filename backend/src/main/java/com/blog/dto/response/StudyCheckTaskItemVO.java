package com.blog.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudyCheckTaskItemVO {

    private Long id;
    private Long questionId;
    private Integer questionVersionNo;
    private Integer sortOrder;
    private String questionTitleSnapshot;
    private String questionStemSnapshot;
    private String standardAnswerSnapshot;
    private BigDecimal scoreFullMark;
    private BigDecimal scorePassMark;
    private Integer status;
    private Integer answerCount;
    private Integer viewAnswerFlag;
    private LocalDateTime viewAnswerTime;
    private Integer selfAssessmentResult;
    private BigDecimal selfScore;
    private BigDecimal aiScore;
    private BigDecimal finalScore;
    private Integer answerDurationSeconds;
    private LocalDateTime submitTime;
    private Long lastAnswerId;
    private String answerContent;
    private String selfComment;
    private StudyAiScoreVO aiScoreDetail;
}
