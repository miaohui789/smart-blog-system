package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.common.result.PageResult;
import com.blog.dto.request.StudyQuestionNoteRequest;
import com.blog.dto.request.StudyQuestionSaveRequest;
import com.blog.dto.request.StudyQuestionStatusRequest;
import com.blog.dto.response.StudyNoteVO;
import com.blog.dto.response.StudyProgressOverviewVO;
import com.blog.dto.response.StudyQuestionDetailVO;
import com.blog.dto.response.StudyQuestionListVO;
import com.blog.entity.StudyQuestion;

import java.util.List;

public interface StudyQuestionService extends IService<StudyQuestion> {

    PageResult<StudyQuestionListVO> getQuestionPage(Long userId, Integer page, Integer pageSize,
                                                    Long categoryId, String keyword, Integer difficulty,
                                                    Integer studyStatus, Integer isFavorite,
                                                    String studyCountSort);

    StudyQuestionDetailVO getQuestionDetail(Long userId, Long questionId);

    StudyQuestionDetailVO getRandomQuestion(Long userId, Long categoryId, Integer difficulty,
                                            Boolean preferPendingReview, Boolean preferLowStudyCount,
                                            String excludeIds);

    void recordStudy(Long userId, Long questionId);

    void toggleFavorite(Long userId, Long questionId, Integer favorite);

    StudyNoteVO saveNote(Long userId, Long questionId, StudyQuestionNoteRequest request);

    List<StudyNoteVO> listNotes(Long userId, Long questionId);

    void updateProgressStatus(Long userId, Long questionId, StudyQuestionStatusRequest request);

    StudyProgressOverviewVO getOverview(Long userId);

    StudyQuestion saveQuestion(StudyQuestionSaveRequest request, Long operatorId);

    StudyQuestion updateQuestion(Long id, StudyQuestionSaveRequest request, Long operatorId);

    void deleteQuestion(Long id, Long operatorId);
}
