package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.constant.ExpConstants;
import com.blog.common.exception.BusinessException;
import com.blog.dto.response.UserSignInCalendarVO;
import com.blog.dto.response.UserSignInResultVO;
import com.blog.entity.UserExpRecord;
import com.blog.mapper.UserExpRecordMapper;
import com.blog.service.UserExpService;
import com.blog.service.UserSignInService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSignInServiceImpl implements UserSignInService {

    private static final int STREAK_LOOKBACK_DAYS = 365;

    private final UserExpRecordMapper userExpRecordMapper;
    private final UserExpService userExpService;

    @Override
    public UserSignInCalendarVO getSignInCalendar(Long userId, Integer year, Integer month) {
        LocalDate monthDate = resolveMonthDate(year, month);
        LocalDate monthStart = monthDate.withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1);
        List<String> signedDates = listMonthlySignedDates(userId, monthStart, monthEnd);

        UserSignInCalendarVO vo = new UserSignInCalendarVO();
        vo.setYear(monthStart.getYear());
        vo.setMonth(monthStart.getMonthValue());
        vo.setToday(LocalDate.now().toString());
        vo.setTodaySigned(isSignedOn(userId, LocalDate.now()));
        vo.setContinuousDays(calculateActiveStreak(userId));
        vo.setMonthSignedCount(signedDates.size());
        vo.setSignedDates(signedDates);
        return vo;
    }

    @Override
    @Transactional
    public UserSignInResultVO signToday(Long userId) {
        LocalDate today = LocalDate.now();
        if (isSignedOn(userId, today)) {
            throw new BusinessException("今天已经签到过了");
        }

        Set<LocalDate> recentSignedDates = loadRecentSignedDateSet(userId);
        int continuousDays = countBackward(recentSignedDates, today.minusDays(1)) + 1;
        boolean signGranted = userExpService.grantExp(
                userId,
                ExpConstants.BIZ_SIGN_DAILY,
                today.toString(),
                ExpConstants.EXP_SIGN_DAILY,
                "每日签到奖励"
        );
        if (!signGranted) {
            if (isSignedOn(userId, today)) {
                throw new BusinessException("今天已经签到过了");
            }
            throw new BusinessException("签到失败，请稍后重试");
        }

        int streakBonusExp = resolveStreakBonus(continuousDays);
        if (streakBonusExp > 0) {
            boolean streakGranted = userExpService.grantExp(
                    userId,
                    ExpConstants.BIZ_SIGN_STREAK_BONUS,
                    today + "#" + continuousDays,
                    streakBonusExp,
                    "连续签到奖励"
            );
            if (!streakGranted) {
                throw new BusinessException("签到失败，请稍后重试");
            }
        }

        UserSignInResultVO vo = new UserSignInResultVO();
        vo.setSignedDate(today.toString());
        vo.setBaseExp(ExpConstants.EXP_SIGN_DAILY);
        vo.setStreakBonusExp(streakBonusExp);
        vo.setTotalExp(ExpConstants.EXP_SIGN_DAILY + streakBonusExp);
        vo.setContinuousDays(continuousDays);
        return vo;
    }

    private LocalDate resolveMonthDate(Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        if (year == null || month == null) {
            return now.withDayOfMonth(1);
        }
        if (month < 1 || month > 12) {
            throw new BusinessException("月份参数不正确");
        }
        try {
            return LocalDate.of(year, month, 1);
        } catch (RuntimeException e) {
            throw new BusinessException("日期参数不正确");
        }
    }

    private List<String> listMonthlySignedDates(Long userId, LocalDate monthStart, LocalDate monthEnd) {
        List<UserExpRecord> records = userExpRecordMapper.selectList(
                new LambdaQueryWrapper<UserExpRecord>()
                        .eq(UserExpRecord::getUserId, userId)
                        .eq(UserExpRecord::getBizType, ExpConstants.BIZ_SIGN_DAILY)
                        .ge(UserExpRecord::getCreateTime, monthStart.atStartOfDay())
                        .lt(UserExpRecord::getCreateTime, monthEnd.atStartOfDay())
                        .orderByAsc(UserExpRecord::getCreateTime)
                        .orderByAsc(UserExpRecord::getId)
        );
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream()
                .map(UserExpRecord::getBizId)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isSignedOn(Long userId, LocalDate date) {
        Long count = userExpRecordMapper.selectCount(
                new LambdaQueryWrapper<UserExpRecord>()
                        .eq(UserExpRecord::getUserId, userId)
                        .eq(UserExpRecord::getBizType, ExpConstants.BIZ_SIGN_DAILY)
                        .eq(UserExpRecord::getBizId, date.toString())
        );
        return count != null && count > 0;
    }

    private int calculateActiveStreak(Long userId) {
        Set<LocalDate> signedDates = loadRecentSignedDateSet(userId);
        LocalDate today = LocalDate.now();
        if (signedDates.contains(today)) {
            return countBackward(signedDates, today);
        }
        LocalDate yesterday = today.minusDays(1);
        if (signedDates.contains(yesterday)) {
            return countBackward(signedDates, yesterday);
        }
        return 0;
    }

    private Set<LocalDate> loadRecentSignedDateSet(Long userId) {
        Page<UserExpRecord> page = userExpRecordMapper.selectPage(
                new Page<>(1, STREAK_LOOKBACK_DAYS),
                new LambdaQueryWrapper<UserExpRecord>()
                        .eq(UserExpRecord::getUserId, userId)
                        .eq(UserExpRecord::getBizType, ExpConstants.BIZ_SIGN_DAILY)
                        .orderByDesc(UserExpRecord::getCreateTime)
                        .orderByDesc(UserExpRecord::getId)
        );
        if (page == null || page.getRecords() == null || page.getRecords().isEmpty()) {
            return Collections.emptySet();
        }
        return page.getRecords().stream()
                .map(UserExpRecord::getBizId)
                .map(LocalDate::parse)
                .collect(Collectors.toSet());
    }

    private int countBackward(Set<LocalDate> signedDates, LocalDate anchorDate) {
        if (anchorDate == null || signedDates == null || signedDates.isEmpty()) {
            return 0;
        }
        int streak = 0;
        LocalDate cursor = anchorDate;
        while (signedDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private int resolveStreakBonus(int continuousDays) {
        return ExpConstants.SIGN_STREAK_REWARD.entrySet().stream()
                .filter(entry -> continuousDays >= entry.getKey())
                .sorted((left, right) -> Integer.compare(right.getKey(), left.getKey()))
                .map(entry -> entry.getValue())
                .findFirst()
                .orElse(0);
    }
}
