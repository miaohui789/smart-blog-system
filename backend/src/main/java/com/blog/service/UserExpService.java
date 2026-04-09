package com.blog.service;

import com.blog.common.result.PageResult;
import com.blog.dto.response.UserExpRecordVO;
import com.blog.dto.response.UserExpSummaryVO;

public interface UserExpService {

    boolean grantExp(Long userId, String bizType, String bizId, Integer exp, String remark);

    UserExpSummaryVO getUserExpSummary(Long userId);

    PageResult<UserExpRecordVO> getUserExpRecords(Long userId, Integer page, Integer pageSize);
}
