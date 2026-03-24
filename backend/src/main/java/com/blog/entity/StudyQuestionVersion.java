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
@TableName("study_question_version")
public class StudyQuestionVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private Integer versionNo;

    private String title;

    private String questionStem;

    private String standardAnswer;

    private String answerSummary;

    private String keywords;

    private Integer difficulty;

    private BigDecimal scoreFullMark;

    private BigDecimal scorePassMark;

    private String scoreRubricJson;

    private Integer changeType;

    private String changeReason;

    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
