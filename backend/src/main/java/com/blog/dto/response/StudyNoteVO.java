package com.blog.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudyNoteVO {

    private Long id;
    private Integer noteType;
    private String content;
    private Integer isPinned;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
