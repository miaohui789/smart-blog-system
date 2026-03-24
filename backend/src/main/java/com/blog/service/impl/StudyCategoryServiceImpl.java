package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.exception.BusinessException;
import com.blog.config.StudyCacheProperties;
import com.blog.dto.request.StudyCategorySaveRequest;
import com.blog.dto.response.StudyCategoryVO;
import com.blog.entity.StudyCategory;
import com.blog.entity.StudyQuestion;
import com.blog.mapper.StudyCategoryMapper;
import com.blog.mapper.StudyQuestionMapper;
import com.blog.service.RedisService;
import com.blog.service.StudyCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudyCategoryServiceImpl extends ServiceImpl<StudyCategoryMapper, StudyCategory> implements StudyCategoryService {

    private final StudyQuestionMapper studyQuestionMapper;
    private final RedisService redisService;
    private final StudyCacheProperties studyCacheProperties;

    public StudyCategoryServiceImpl(StudyQuestionMapper studyQuestionMapper,
                                    RedisService redisService,
                                    StudyCacheProperties studyCacheProperties) {
        this.studyQuestionMapper = studyQuestionMapper;
        this.redisService = redisService;
        this.studyCacheProperties = studyCacheProperties;
    }

    @Override
    public List<StudyCategoryVO> getCategoryTree(boolean includeDisabled) {
        if (!includeDisabled) {
            List<StudyCategoryVO> cachedTree = redisService.getList(RedisService.CACHE_STUDY_CATEGORY_TREE_ENABLED);
            if (cachedTree != null) {
                return cachedTree;
            }
        }
        LambdaQueryWrapper<StudyCategory> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(StudyCategory::getStatus, 1);
        }
        wrapper.orderByDesc(StudyCategory::getSortOrder).orderByAsc(StudyCategory::getId);
        List<StudyCategory> categories = list(wrapper);
        Map<Long, StudyCategoryVO> voMap = new LinkedHashMap<>();
        List<StudyCategoryVO> roots = new ArrayList<>();
        for (StudyCategory category : categories) {
            StudyCategoryVO vo = new StudyCategoryVO();
            BeanUtils.copyProperties(category, vo);
            voMap.put(category.getId(), vo);
        }
        for (StudyCategory category : categories) {
            StudyCategoryVO current = voMap.get(category.getId());
            if (category.getParentId() == null || category.getParentId() == 0 || !voMap.containsKey(category.getParentId())) {
                roots.add(current);
            } else {
                voMap.get(category.getParentId()).getChildren().add(current);
            }
        }
        roots.sort(Comparator.comparing(StudyCategoryVO::getSortOrder, Comparator.nullsLast(Integer::compareTo)).reversed()
                .thenComparing(StudyCategoryVO::getId));
        if (!includeDisabled) {
            redisService.setWithMinutes(RedisService.CACHE_STUDY_CATEGORY_TREE_ENABLED, roots, studyCacheProperties.getCategoryTreeMinutes());
        }
        return roots;
    }

    @Override
    @Transactional
    public StudyCategory saveCategory(StudyCategorySaveRequest request, Long operatorId) {
        checkCategoryCode(null, request.getCategoryCode());
        StudyCategory parent = null;
        if (request.getParentId() != null && request.getParentId() > 0) {
            parent = getById(request.getParentId());
            if (parent == null) {
                throw new BusinessException("父级分类不存在");
            }
        }
        StudyCategory category = new StudyCategory();
        BeanUtils.copyProperties(request, category);
        category.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        category.setCategoryLevel(request.getCategoryLevel() != null ? request.getCategoryLevel() : (parent == null ? 1 : parent.getCategoryLevel() + 1));
        category.setQuestionCount(0);
        category.setCreateBy(operatorId);
        category.setUpdateBy(operatorId);
        save(category);
        category.setCategoryPath(buildCategoryPath(parent, category.getId()));
        updateById(category);
        clearStudyCategoryCacheAfterCommit();
        return category;
    }

    @Override
    @Transactional
    public StudyCategory updateCategory(Long id, StudyCategorySaveRequest request, Long operatorId) {
        StudyCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        checkCategoryCode(id, request.getCategoryCode());
        StudyCategory parent = null;
        if (request.getParentId() != null && request.getParentId() > 0) {
            if (id.equals(request.getParentId())) {
                throw new BusinessException("父级分类不能选择自己");
            }
            parent = getById(request.getParentId());
            if (parent == null) {
                throw new BusinessException("父级分类不存在");
            }
        }
        category.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        category.setCategoryName(request.getCategoryName());
        category.setCategoryCode(request.getCategoryCode());
        category.setCategoryLevel(request.getCategoryLevel() != null ? request.getCategoryLevel() : (parent == null ? 1 : parent.getCategoryLevel() + 1));
        category.setCategoryPath(buildCategoryPath(parent, category.getId()));
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());
        category.setStatus(request.getStatus());
        category.setUpdateBy(operatorId);
        updateById(category);
        clearStudyCategoryCacheAfterCommit();
        return category;
    }

    @Override
    public void updateQuestionCount(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        Long count = studyQuestionMapper.selectCount(new LambdaQueryWrapper<StudyQuestion>()
                .eq(StudyQuestion::getCategoryId, categoryId)
                .eq(StudyQuestion::getStatus, 1)
                .eq(StudyQuestion::getReviewStatus, 2));
        StudyCategory category = new StudyCategory();
        category.setId(categoryId);
        category.setQuestionCount(count == null ? 0 : count.intValue());
        updateById(category);
        clearStudyCategoryCacheAfterCommit();
    }

    private void checkCategoryCode(Long id, String categoryCode) {
        if (categoryCode == null || categoryCode.trim().isEmpty()) {
            return;
        }
        LambdaQueryWrapper<StudyCategory> wrapper = new LambdaQueryWrapper<StudyCategory>()
                .eq(StudyCategory::getCategoryCode, categoryCode.trim());
        if (id != null) {
            wrapper.ne(StudyCategory::getId, id);
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("分类编码已存在");
        }
    }

    private String buildCategoryPath(StudyCategory parent, Long id) {
        if (parent == null) {
            return "/" + id;
        }
        String prefix = parent.getCategoryPath() == null ? "/" + parent.getId() : parent.getCategoryPath();
        return prefix + "/" + id;
    }

    private void clearStudyCategoryCacheAfterCommit() {
        redisService.runAfterCommit(redisService::clearStudyCategoryCache);
    }
}
