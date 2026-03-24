package com.blog.dto.request;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class StudyQuestionSaveRequest {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private Integer questionNo;

    @Size(max = 64, message = "题目编码不能超过64字符")
    private String questionCode;

    private Integer questionType;

    @NotBlank(message = "题目标题不能为空")
    @Size(max = 300, message = "题目标题不能超过300字符")
    private String title;

    private String questionStem;

    @NotBlank(message = "标准答案不能为空")
    private String standardAnswer;

    @Size(max = 1000, message = "答案摘要不能超过1000字符")
    private String answerSummary;

    @Size(max = 1000, message = "关键字不能超过1000字符")
    private String keywords;

    private Integer difficulty;

    private Integer estimatedMinutes;

    @DecimalMin(value = "0", message = "满分不能小于0")
    private BigDecimal scoreFullMark;

    @DecimalMin(value = "0", message = "及格分不能小于0")
    private BigDecimal scorePassMark;

    private Integer aiScoreEnabled;

    private Integer selfAssessmentEnabled;

    private String aiScorePromptVersion;

    private String scoreRubricJson;

    private Integer reviewStatus;

    private Integer status;

    private String sourceFileName;

    private String sourceSection;
}
