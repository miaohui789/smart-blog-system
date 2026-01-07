package com.blog.dto.response;

import lombok.Data;

@Data
public class DashboardVO {
    private Long articleCount;
    private Long commentCount;
    private Long userCount;
    private Long viewCount;
}
