package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.dto.response.StudyQuestionListVO;
import com.blog.entity.StudyUserQuestionProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudyUserQuestionProgressMapper extends BaseMapper<StudyUserQuestionProgress> {

    List<StudyQuestionListVO> selectDashboardFavoriteQuestions(@Param("userId") Long userId,
                                                               @Param("limit") Integer limit);

    List<StudyQuestionListVO> selectDashboardReviewQuestions(@Param("userId") Long userId,
                                                             @Param("limit") Integer limit);
}
