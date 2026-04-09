package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.constant.ExpConstants;
import com.blog.common.result.PageResult;
import com.blog.dto.response.UserExpRecordVO;
import com.blog.dto.response.UserExpSummaryVO;
import com.blog.entity.User;
import com.blog.entity.UserExpRecord;
import com.blog.entity.UserLevelConfig;
import com.blog.mapper.UserExpRecordMapper;
import com.blog.mapper.UserLevelConfigMapper;
import com.blog.mapper.UserMapper;
import com.blog.mq.UserExpEventPublisher;
import com.blog.mq.event.UserExpChangedEvent;
import com.blog.service.RedisService;
import com.blog.service.SearchService;
import com.blog.service.UserExpService;
import com.blog.util.UserLevelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserExpServiceImpl implements UserExpService {

    private final UserMapper userMapper;
    private final UserExpRecordMapper userExpRecordMapper;
    private final UserLevelConfigMapper userLevelConfigMapper;
    private final UserExpEventPublisher userExpEventPublisher;
    private final RedisService redisService;
    private final SearchService searchService;

    @Override
    @Transactional
    public boolean grantExp(Long userId, String bizType, String bizId, Integer exp, String remark) {
        if (userId == null || exp == null || exp <= 0 || bizType == null || bizId == null) {
            return false;
        }
        int grantExp = resolveGrantExp(userId, bizType, exp);
        if (grantExp <= 0) {
            return false;
        }
        User user = userMapper.selectByIdForUpdate(userId);
        if (user == null) {
            return false;
        }
        int beforeLevel = UserLevelUtils.normalize(user.getUserLevel());
        int beforeExp = normalizeNonNegative(user.getCurrentExp());
        int totalExp = normalizeNonNegative(user.getTotalExp());
        int afterLevel = beforeLevel;
        int afterExp = beforeExp + grantExp;
        List<UserLevelConfig> configs = loadLevelConfigs();
        while (afterLevel < ExpConstants.MAX_LEVEL) {
            int needExp = needExpForLevel(configs, afterLevel);
            if (needExp <= 0 || afterExp < needExp) {
                break;
            }
            afterExp -= needExp;
            afterLevel++;
        }
        if (afterLevel >= ExpConstants.MAX_LEVEL) {
            afterLevel = ExpConstants.MAX_LEVEL;
        }
        UserExpRecord record = new UserExpRecord();
        record.setUserId(userId);
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setExpChange(grantExp);
        record.setBeforeLevel(beforeLevel);
        record.setAfterLevel(afterLevel);
        record.setBeforeExp(beforeExp);
        record.setAfterExp(afterExp);
        record.setRemark(remark);
        try {
            userExpRecordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            return false;
        }

        User update = new User();
        update.setId(userId);
        update.setUserLevel(afterLevel);
        update.setCurrentExp(afterExp);
        update.setTotalExp(totalExp + grantExp);
        update.setExpUpdateTime(LocalDateTime.now());
        userMapper.updateById(update);

        int finalAfterLevel = afterLevel;
        int finalAfterExp = afterExp;
        int finalTotalExp = totalExp + grantExp;
        redisService.runAfterCommit(() -> {
            UserExpChangedEvent changedEvent = new UserExpChangedEvent();
            changedEvent.setUserId(userId);
            changedEvent.setBizType(bizType);
            changedEvent.setBizId(bizId);
            changedEvent.setExpChange(grantExp);
            changedEvent.setBeforeLevel(beforeLevel);
            changedEvent.setAfterLevel(finalAfterLevel);
            changedEvent.setCurrentExp(finalAfterExp);
            changedEvent.setTotalExp(finalTotalExp);
            changedEvent.setEventTime(LocalDateTime.now());
            userExpEventPublisher.publishChanged(changedEvent);
            searchService.syncUser(userId);
            searchService.syncArticlesByUserId(userId);
        });
        return true;
    }

    @Override
    public UserExpSummaryVO getUserExpSummary(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            UserExpSummaryVO empty = new UserExpSummaryVO();
            empty.setUserId(userId);
            empty.setUserLevel(UserLevelUtils.MIN_LEVEL);
            empty.setCurrentExp(0);
            empty.setTotalExp(0);
            empty.setNextLevelNeedExp(0);
            empty.setNextLevelRemainExp(0);
            empty.setExpProgress(0);
            return empty;
        }
        int level = UserLevelUtils.normalize(user.getUserLevel());
        int currentExp = normalizeNonNegative(user.getCurrentExp());
        int totalExp = normalizeNonNegative(user.getTotalExp());
        List<UserLevelConfig> configs = loadLevelConfigs();
        int needExp = level >= ExpConstants.MAX_LEVEL ? 0 : needExpForLevel(configs, level);
        int remainExp = needExp > 0 ? Math.max(needExp - currentExp, 0) : 0;
        int progress = needExp > 0 ? Math.min((int) Math.round(currentExp * 100.0 / needExp), 100) : 100;

        UserExpSummaryVO vo = new UserExpSummaryVO();
        vo.setUserId(userId);
        vo.setUserLevel(level);
        vo.setCurrentExp(currentExp);
        vo.setTotalExp(totalExp);
        vo.setNextLevelNeedExp(needExp);
        vo.setNextLevelRemainExp(remainExp);
        vo.setExpProgress(progress);
        return vo;
    }

    @Override
    public PageResult<UserExpRecordVO> getUserExpRecords(Long userId, Integer page, Integer pageSize) {
        Page<UserExpRecord> pageResult = userExpRecordMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<UserExpRecord>()
                        .eq(UserExpRecord::getUserId, userId)
                        .orderByDesc(UserExpRecord::getCreateTime)
                        .orderByDesc(UserExpRecord::getId)
        );
        List<UserExpRecordVO> list = pageResult.getRecords().stream().map(record -> {
            UserExpRecordVO vo = new UserExpRecordVO();
            BeanUtils.copyProperties(record, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(list, pageResult.getTotal(), page, pageSize);
    }

    private int resolveGrantExp(Long userId, String bizType, Integer exp) {
        Integer dailyCap = ExpConstants.BIZ_DAILY_CAP.get(bizType);
        if (dailyCap == null || dailyCap <= 0) {
            return exp;
        }
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        QueryWrapper<UserExpRecord> wrapper = new QueryWrapper<>();
        wrapper.select("COALESCE(SUM(exp_change), 0) AS totalExp")
                .eq("user_id", userId)
                .eq("biz_type", bizType)
                .ge("create_time", start)
                .lt("create_time", end);
        List<Map<String, Object>> rows = userExpRecordMapper.selectMaps(
                wrapper
        );
        int todayExp = 0;
        if (!rows.isEmpty()) {
            Object value = rows.get(0).get("totalExp");
            if (value instanceof Number) {
                todayExp = ((Number) value).intValue();
            } else if (value != null) {
                todayExp = Integer.parseInt(value.toString());
            }
        }
        if (todayExp >= dailyCap) {
            return 0;
        }
        return Math.min(exp, dailyCap - todayExp);
    }

    private int needExpForLevel(List<UserLevelConfig> configs, int level) {
        return configs.stream()
                .filter(item -> Objects.equals(item.getLevel(), level))
                .map(UserLevelConfig::getNeedExp)
                .findFirst()
                .orElse(100);
    }

    private List<UserLevelConfig> loadLevelConfigs() {
        List<UserLevelConfig> configs = userLevelConfigMapper.selectList(
                new LambdaQueryWrapper<UserLevelConfig>()
                        .eq(UserLevelConfig::getStatus, 1)
                        .orderByAsc(UserLevelConfig::getLevel)
        );
        if (configs == null || configs.isEmpty()) {
            return Collections.emptyList();
        }
        configs.sort(Comparator.comparing(UserLevelConfig::getLevel));
        return configs;
    }

    private int normalizeNonNegative(Integer value) {
        return value == null || value < 0 ? 0 : value;
    }
}
