package com.blog.dto.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class StudyAnswerSubmitRequest {

    @Size(max = 50000, message = "答案内容过长")
    private String answerContent;

    @NotNull(message = "自评结果不能为空")
    private Integer selfAssessmentResult;

    @Min(value = 0, message = "自评分不能小于0")
    @Max(value = 100, message = "自评分不能大于100")
    private BigDecimal selfScore;

    @Size(max = 1000, message = "自评备注不能超过1000字符")
    private String selfComment;

    private Integer standardAnswerViewed;

    private Integer triggerAiScore;

    @Min(value = 0, message = "答题耗时不能小于0")
    private Integer answerDurationSeconds;
}
