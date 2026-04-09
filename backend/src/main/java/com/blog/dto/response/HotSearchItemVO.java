package com.blog.dto.response;

import lombok.Data;

@Data
public class HotSearchItemVO {
    private Integer rank;
    private String keyword;
    private Long score;
}
