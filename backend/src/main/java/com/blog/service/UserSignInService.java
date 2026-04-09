package com.blog.service;

import com.blog.dto.response.UserSignInCalendarVO;
import com.blog.dto.response.UserSignInResultVO;

public interface UserSignInService {

    UserSignInCalendarVO getSignInCalendar(Long userId, Integer year, Integer month);

    UserSignInResultVO signToday(Long userId);
}
