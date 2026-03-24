package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.dto.response.StudyDashboardFailedRecordVO;
import com.blog.entity.StudyCheckTaskItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudyCheckTaskItemMapper extends BaseMapper<StudyCheckTaskItem> {

    StudyCheckTaskItem selectByIdForUpdate(@Param("id") Long id);

    List<StudyDashboardFailedRecordVO> selectDashboardFailedRecords(@Param("userId") Long userId,
                                                                    @Param("limit") Integer limit);
}
