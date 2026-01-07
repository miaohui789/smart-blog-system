package com.blog.dto.response;

import lombok.Data;

@Data
public class TagVO {
    private Long id;
    private String name;
    private String color;
    private Integer articleCount;
}
