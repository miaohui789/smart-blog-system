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
@TableName("study_check_task_item")
public class StudyCheckTaskItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

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

    private Long lastAnswerId;

    private Long lastAiScoreRecordId;

    private Integer answerDurationSeconds;

    private LocalDateTime submitTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
