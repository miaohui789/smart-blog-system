package com.blog.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminAiScoreTestVO {

    private Long configId;
    private String configDisplayName;
    private String provider;
    private String modelName;
    private String promptVersion;
    private String resolvedSystemPrompt;
    private String questionTitle;
    private BigDecimal fullScore;
    private BigDecimal passScore;
    private BigDecimal aiScore;
    private Boolean passed;
    private Integer resultLevel;
    private String resultLevelLabel;
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
    private Integer durationMs;
    private String rawResponse;
}
