package com.blog.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StudyCategoryVO {

    private Long id;
    private Long parentId;
    private String categoryName;
    private String categoryCode;
    private Integer categoryLevel;
    private String categoryPath;
    private String sourceFileName;
    private String description;
    private Integer questionCount;
    private Integer sortOrder;
    private Integer status;
    private List<StudyCategoryVO> children = new ArrayList<>();
}
