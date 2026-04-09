package com.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_exp_record")
public class UserExpRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String bizType;

    private String bizId;

    private Integer expChange;

    private Integer beforeLevel;

    private Integer afterLevel;

    private Integer beforeExp;

    private Integer afterExp;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
