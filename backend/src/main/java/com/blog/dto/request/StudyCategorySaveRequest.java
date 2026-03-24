package com.blog.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class StudyCategorySaveRequest {

    private Long parentId;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称不能超过100字符")
    private String categoryName;

    @Size(max = 100, message = "分类编码不能超过100字符")
    private String categoryCode;

    private Integer categoryLevel;

    @Size(max = 500, message = "分类描述不能超过500字符")
    private String description;

    private Integer sortOrder;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
