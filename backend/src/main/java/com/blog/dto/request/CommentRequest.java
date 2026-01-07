package com.blog.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CommentRequest {
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    private Long parentId;

    private Long replyUserId;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
