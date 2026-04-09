package com.blog.dto.response;

import lombok.Data;

@Data
public class UserExpSummaryVO {

    private Long userId;
    private Integer userLevel;
    private Integer currentExp;
    private Integer totalExp;
    private Integer nextLevelNeedExp;
    private Integer nextLevelRemainExp;
    private Integer expProgress;
}
