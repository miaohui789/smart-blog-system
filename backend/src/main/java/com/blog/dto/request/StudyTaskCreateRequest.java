package com.blog.dto.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class StudyTaskCreateRequest {

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @NotNull(message = "抽查模式不能为空")
    private Integer checkMode;

    private Long categoryId;

    @NotNull(message = "题目数量不能为空")
    @Min(value = 1, message = "题目数量最少1题")
    @Max(value = 50, message = "题目数量最多50题")
    private Integer questionCount;

    private Integer difficulty;

    private Integer needAiScore;

    private Integer allowSelfAssessment;

    private Integer showStandardAnswer;

    private Integer scoringMode;

    private Integer onlyWrongQuestions;

    private Integer onlyFavorites;
}
