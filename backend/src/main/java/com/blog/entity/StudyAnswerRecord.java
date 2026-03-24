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
@TableName("study_answer_record")
public class StudyAnswerRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String answerNo;

    private Long taskId;

    private Long taskItemId;

    private Long userId;

    private Long questionId;

    private Integer answerRound;

    private String answerContent;

    private Integer charCount;

    private Integer wordCount;

    private String answerLanguage;

    private Integer answerSource;

    private Integer answerStatus;

    private Integer selfAssessmentResult;

    private BigDecimal selfScore;

    private String selfComment;

    private Integer standardAnswerViewed;

    private LocalDateTime standardAnswerViewTime;

    private Integer aiScoreStatus;

    private BigDecimal finalScore;

    private Integer isBestAnswer;

    private LocalDateTime submittedTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
