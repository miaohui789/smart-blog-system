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
@TableName("study_check_task")
public class StudyCheckTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskNo;

    private Long userId;

    private String taskName;

    private Integer taskSource;

    private Integer checkMode;

    private Long categoryId;

    private String filterJson;

    private Integer questionCount;

    private Integer answeredCount;

    private Integer scoredCount;

    private Integer needAiScore;

    private Integer allowSelfAssessment;

    private Integer showStandardAnswer;

    private Integer scoringMode;

    private String scoreRuleJson;

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

    private LocalDateTime expireTime;

    private Integer durationSeconds;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
