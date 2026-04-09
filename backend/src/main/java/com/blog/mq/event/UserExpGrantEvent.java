package com.blog.mq.event;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserExpGrantEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String bizType;
    private String bizId;
    private Integer exp;
    private String remark;
    private String source;
    private LocalDateTime eventTime;
}
