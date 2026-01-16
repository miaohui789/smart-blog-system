package com.blog.common.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    DISABLED(0, "禁用"),
    NORMAL(1, "正常"),
    CANCELLED(2, "已注销");

    private final Integer code;
    private final String desc;

    UserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
