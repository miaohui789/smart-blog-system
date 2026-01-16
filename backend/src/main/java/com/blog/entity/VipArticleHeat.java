package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("vip_article_heat")
public class VipArticleHeat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private Long userId;
    private Integer heatValue;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
