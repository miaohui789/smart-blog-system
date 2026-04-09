package com.blog.mq.event;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserExpChangedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String bizType;
    private String bizId;
    private Integer expChange;
    private Integer beforeLevel;
    private Integer afterLevel;
    private Integer currentExp;
    private Integer totalExp;
    private LocalDateTime eventTime;
}
