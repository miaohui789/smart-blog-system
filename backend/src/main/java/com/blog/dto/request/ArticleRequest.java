package com.blog.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ArticleRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private List<Long> tagIds;

    private String cover;

    private String summary;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Integer status;
}
