package com.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("study_import_batch")
public class StudyImportBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchNo;

    private Integer sourceType;

    private String sourceName;

    private String sourcePath;

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private Integer duplicateCount;

    private Integer batchStatus;

    private String errorMessage;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long operatorId;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
