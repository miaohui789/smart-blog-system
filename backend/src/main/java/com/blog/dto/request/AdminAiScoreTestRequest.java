package com.blog.dto.request;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class AdminAiScoreTestRequest {

    @NotNull(message = "请选择要测试的模型配置")
    private Long configId;

    @NotBlank(message = "题目标题不能为空")
    @Size(max = 255, message = "题目标题不能超过255字符")
    private String questionTitle;

    @Size(max = 20000, message = "题目内容不能超过20000字符")
    private String questionStem;

    @NotBlank(message = "标准答案不能为空")
    @Size(max = 50000, message = "标准答案不能超过50000字符")
    private String standardAnswer;

    @NotBlank(message = "候选人答案不能为空")
    @Size(max = 50000, message = "候选人答案不能超过50000字符")
    private String candidateAnswer;

    @DecimalMin(value = "1", message = "题目满分至少为1分")
    @DecimalMax(value = "1000", message = "题目满分不能超过1000分")
    private BigDecimal scoreFullMark;

    @DecimalMin(value = "0", message = "及格分不能小于0分")
    @DecimalMax(value = "1000", message = "及格分不能超过1000分")
    private BigDecimal scorePassMark;

    @Size(max = 5000, message = "评分规则不能超过5000字符")
    private String scoreRubricJson;

    @Min(value = 1, message = "自评结果不合法")
    @Max(value = 3, message = "自评结果不合法")
    private Integer candidateSelfAssessment;

    @DecimalMin(value = "0", message = "自评分不能小于0")
    @DecimalMax(value = "1000", message = "自评分不能超过1000")
    private BigDecimal candidateSelfScore;

    @Size(max = 4000, message = "评分提示词覆盖内容不能超过4000字符")
    private String promptOverride;
}
