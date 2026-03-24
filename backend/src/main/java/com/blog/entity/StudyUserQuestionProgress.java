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
@TableName("study_user_question_progress")
public class StudyUserQuestionProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    private Integer studyStatus;

    private Integer masteryLevel;

    private Integer reviewPriority;

    private Integer studyCount;

    private Integer checkCount;

    private Integer rememberCount;

    private Integer fuzzyCount;

    private Integer forgetCount;

    private Integer wrongCount;

    private Integer lastResult;

    private BigDecimal lastScore;

    private BigDecimal bestScore;

    private BigDecimal avgScore;

    private Long lastAnswerId;

    private Long lastAiScoreRecordId;

    private Integer isFavorite;

    private Integer isWrongQuestion;

    private Integer noteCount;

    private LocalDateTime firstStudyTime;

    private LocalDateTime lastStudyTime;

    private LocalDateTime lastCheckTime;

    private LocalDateTime nextReviewTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
