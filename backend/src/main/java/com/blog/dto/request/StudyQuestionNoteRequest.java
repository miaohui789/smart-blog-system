package com.blog.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class StudyQuestionNoteRequest {

    @NotNull(message = "笔记类型不能为空")
    private Integer noteType;

    @NotBlank(message = "笔记内容不能为空")
    @Size(max = 20000, message = "笔记内容不能超过20000字符")
    private String content;

    private Integer isPinned;
}
