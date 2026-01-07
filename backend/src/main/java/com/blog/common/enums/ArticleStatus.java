package com.blog.common.enums;

import lombok.Getter;

@Getter
public enum ArticleStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    OFFLINE(2, "已下架");

    private final Integer code;
    private final String desc;

    ArticleStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
