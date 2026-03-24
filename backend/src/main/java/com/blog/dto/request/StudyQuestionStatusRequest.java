package com.blog.dto.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class StudyQuestionStatusRequest {

    @NotNull(message = "学习状态不能为空")
    private Integer studyStatus;

    @NotNull(message = "掌握度不能为空")
    @Min(value = 0, message = "掌握度不能小于0")
    @Max(value = 5, message = "掌握度不能大于5")
    private Integer masteryLevel;

    @Min(value = 1, message = "复习优先级不能小于1")
    @Max(value = 5, message = "复习优先级不能大于5")
    private Integer reviewPriority;
}
