package com.blog.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudyAiScoreVO {

    private Long id;
    private String scoreNo;
    private Long answerId;
    private Long aiConfigId;
    private String modelProvider;
    private String modelName;
    private String promptVersion;
    private BigDecimal fullScore;
    private BigDecimal aiScore;
    private Integer resultLevel;
    private String dimensionScores;
    private String keywordHitJson;
    private String strengthsText;
    private String weaknessesText;
    private String suggestionText;
    private Integer requestTokens;
    private Integer responseTokens;
    private Integer totalTokens;
    private Integer scoreStatus;
    private String errorMessage;
    private LocalDateTime requestTime;
    private LocalDateTime responseTime;
    private Integer durationMs;
}
