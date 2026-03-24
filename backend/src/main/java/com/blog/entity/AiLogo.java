package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_logo")
public class AiLogo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 服务商标识（deepseek, openai, zhipu, siliconflow, moonshot, dashscope, qianfan 等）
     */
    private String provider;

    /**
     * Logo名称/备注
     */
    private String name;

    /**
     * Logo类型：url-网络地址, upload-上传图片
     */
    private String logoType;

    /**
     * Logo地址（网络URL或上传路径）
     */
    private String logoUrl;

    /**
     * 排序（越小越靠前）
     */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
