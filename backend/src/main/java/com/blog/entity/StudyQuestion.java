package com.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("study_question")
public class StudyQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    private Integer questionNo;

    private String questionCode;

    private Integer questionType;

    private String title;

    private String questionStem;

    private String standardAnswer;

    private String answerSummary;

    private String keywords;

    private Integer difficulty;

    private Integer sourceType;

    private String sourceFileName;

    private String sourceSection;

    private String sourceRefNo;

    private Long importBatchId;

    private Integer estimatedMinutes;

    private Integer answerWordCount;

    private BigDecimal scoreFullMark;

    private BigDecimal scorePassMark;

    private Integer aiScoreEnabled;

    private Integer selfAssessmentEnabled;

    private String aiScorePromptVersion;

    private String scoreRubricJson;

    private Integer reviewStatus;

    private LocalDateTime publishTime;

    private Integer versionNo;

    private Integer viewCount;

    private Integer studyCount;

    private Integer checkCount;

    private Integer status;

    @TableLogic
    private Integer isDeleted;

    private Long createBy;

    private Long updateBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
