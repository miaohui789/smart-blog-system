package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.StudyAnswerRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StudyAnswerRecordMapper extends BaseMapper<StudyAnswerRecord> {

    Integer selectMaxAnswerRound(@Param("taskItemId") Long taskItemId);

    StudyAnswerRecord selectLatestByTaskItemId(@Param("taskItemId") Long taskItemId);
}
