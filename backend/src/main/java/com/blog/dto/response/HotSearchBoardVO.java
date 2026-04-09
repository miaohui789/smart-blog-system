package com.blog.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class HotSearchBoardVO {
    private String date;
    private String timezone;
    private String period;
    private Long expiresInSeconds;
    private List<HotSearchItemVO> list;
}
