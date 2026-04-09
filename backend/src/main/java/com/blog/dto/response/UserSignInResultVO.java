package com.blog.dto.response;

import lombok.Data;

@Data
public class UserSignInResultVO {

    private String signedDate;
    private Integer baseExp;
    private Integer streakBonusExp;
    private Integer totalExp;
    private Integer continuousDays;
}
