package com.blog.service;

import com.blog.dto.response.StudyDashboardVO;

public interface StudyDashboardService {

    StudyDashboardVO getUserDashboard(Long userId);
}
