package com.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("study_ai_score_record")
public class StudyAiScoreRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String scoreNo;

    private Long answerId;

    private Long taskId;

    private Long taskItemId;

    private Long questionId;

    private Long userId;

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

    private String rawResponse;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
