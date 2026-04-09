package com.blog.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserExpRecordVO {

    private Long id;
    private String bizType;
    private String bizId;
    private Integer expChange;
    private Integer beforeLevel;
    private Integer afterLevel;
    private Integer beforeExp;
    private Integer afterExp;
    private String remark;
    private LocalDateTime createTime;
}
