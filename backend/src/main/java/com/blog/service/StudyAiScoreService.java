package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.request.AdminAiScoreTestRequest;
import com.blog.dto.response.AdminAiScoreTestVO;
import com.blog.dto.response.StudyAiScoreVO;
import com.blog.entity.StudyAiScoreRecord;
import com.blog.entity.StudyAnswerRecord;
import com.blog.entity.StudyQuestion;

public interface StudyAiScoreService extends IService<StudyAiScoreRecord> {

    StudyAiScoreRecord scoreAnswer(StudyQuestion question, StudyAnswerRecord answerRecord, Long taskId, Long taskItemId, Long userId);

    StudyAiScoreVO getScoreDetail(Long scoreRecordId);

    AdminAiScoreTestVO testScore(AdminAiScoreTestRequest request);
}
