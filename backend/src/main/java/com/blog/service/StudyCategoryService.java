package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.request.StudyCategorySaveRequest;
import com.blog.dto.response.StudyCategoryVO;
import com.blog.entity.StudyCategory;

import java.util.List;

public interface StudyCategoryService extends IService<StudyCategory> {

    List<StudyCategoryVO> getCategoryTree(boolean includeDisabled);

    StudyCategory saveCategory(StudyCategorySaveRequest request, Long operatorId);

    StudyCategory updateCategory(Long id, StudyCategorySaveRequest request, Long operatorId);

    void updateQuestionCount(Long categoryId);
}
