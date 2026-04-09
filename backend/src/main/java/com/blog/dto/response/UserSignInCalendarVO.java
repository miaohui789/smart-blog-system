package com.blog.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class UserSignInCalendarVO {

    private Integer year;
    private Integer month;
    private String today;
    private Boolean todaySigned;
    private Integer continuousDays;
    private Integer monthSignedCount;
    private List<String> signedDates;
}
