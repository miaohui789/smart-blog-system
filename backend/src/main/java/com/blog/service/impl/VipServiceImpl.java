package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.request.VipActivateRequest;
import com.blog.dto.request.VipKeyGenerateRequest;
import com.blog.dto.request.VipMemberUpdateRequest;
import com.blog.entity.*;
import com.blog.mapper.*;
import com.blog.service.VipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VipServiceImpl implements VipService {

    private final VipMemberMapper vipMemberMapper;
    private final VipActivationKeyMapper vipActivationKeyMapper;
    private final VipArticleHeatMapper vipArticleHeatMapper;
    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    
    private static final String KEY_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    
    // VIP等级权益配置
    private static final Map<Integer, Map<String, Integer>> VIP_PRIVILEGES = new HashMap<>();
    static {
        // 普通VIP
        Map<String, Integer> level1 = new HashMap<>();
        level1.put("downloadLimit", 5);
        level1.put("heatLimit", 3);
        level1.put("heatValue", 10);
        VIP_PRIVILEGES.put(1, level1);
        
        // 高级VIP
        Map<String, Integer> level2 = new HashMap<>();
        level2.put("downloadLimit", 20);
        level2.put("heatLimit", 10);
        level2.put("heatValue", 30);
        VIP_PRIVILEGES.put(2, level2);
        
        // 超级VIP
        Map<String, Integer> level3 = new HashMap<>();
        level3.put("downloadLimit", -1); // -1表示无限
        level3.put("heatLimit", 30);
        level3.put("heatValue", 100);
        VIP_PRIVILEGES.put(3, level3);
    }

    @Override
    @Transactional
    public VipMember activate(Long userId, VipActivateRequest request) {
        // 查找密钥
        LambdaQueryWrapper<VipActivationKey> keyWrapper = new LambdaQueryWrapper<>();
        keyWrapper.eq(VipActivationKey::getKeyCode, request.getKeyCode());
        VipActivationKey key = vipActivationKeyMapper.selectOne(keyWrapper);
        
        if (key == null) {
            throw new RuntimeException("密钥不存在");
        }
        if (key.getStatus() == 0) {
            throw new RuntimeException("密钥已被使用");
        }
        if (key.getStatus() == 2) {
            throw new RuntimeException("密钥已被禁用");
        }
        if (key.getExpireTime() != null && key.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("密钥已过期");
        }
        
        // 查找或创建VIP会员记录
        LambdaQueryWrapper<VipMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(VipMember::getUserId, userId);
        VipMember member = vipMemberMapper.selectOne(memberWrapper);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime;
        
        if (member == null) {
            // 新VIP
            member = new VipMember();
            member.setUserId(userId);
            member.setLevel(key.getLevel());
            member.setStartTime(now);
            expireTime = now.plusDays(key.getDurationDays());
            member.setExpireTime(expireTime);
            member.setActivationKey(key.getKeyCode());
            member.setStatus(1);
            member.setHeatCountToday(0);
            member.setDownloadCountToday(0);
            vipMemberMapper.insert(member);
        } else {
            // 已是VIP，延长时间
            LocalDateTime baseTime = member.getExpireTime().isAfter(now) ? member.getExpireTime() : now;
            expireTime = baseTime.plusDays(key.getDurationDays());
            member.setExpireTime(expireTime);
            // 如果新密钥等级更高，升级
            if (key.getLevel() > member.getLevel()) {
                member.setLevel(key.getLevel());
            }
            member.setStatus(1);
            vipMemberMapper.updateById(member);
        }
        
        // 更新用户表VIP信息
        User user = new User();
        user.setId(userId);
        user.setIsVip(1);
        user.setVipLevel(member.getLevel());
        user.setVipExpireTime(expireTime);
        userMapper.updateById(user);
        
        // 标记密钥已使用
        key.setStatus(0);
        key.setUsedBy(userId);
        key.setUsedTime(now);
        vipActivationKeyMapper.updateById(key);
        
        return member;
    }

    @Override
    public VipMember getVipInfo(Long userId) {
        LambdaQueryWrapper<VipMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VipMember::getUserId, userId);
        VipMember member = vipMemberMapper.selectOne(wrapper);
        
        if (member != null) {
            // 检查是否过期
            if (member.getExpireTime().isBefore(LocalDateTime.now())) {
                member.setStatus(0);
                vipMemberMapper.updateById(member);
                // 更新用户表
                User user = new User();
                user.setId(userId);
                user.setIsVip(0);
                user.setVipLevel(0);
                userMapper.updateById(user);
            }
            
            // 重置每日次数
            resetDailyCount(member);
        }
        
        return member;
    }

    @Override
    public Map<String, Object> getPrivileges() {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> levels = new ArrayList<>();
        
        Map<String, Object> level1 = new HashMap<>();
        level1.put("level", 1);
        level1.put("name", "普通VIP");
        level1.put("color", "#cd7f32");
        level1.put("downloadLimit", 5);
        level1.put("heatLimit", 3);
        level1.put("heatValue", 10);
        levels.add(level1);
        
        Map<String, Object> level2 = new HashMap<>();
        level2.put("level", 2);
        level2.put("name", "高级VIP");
        level2.put("color", "#c0c0c0");
        level2.put("downloadLimit", 20);
        level2.put("heatLimit", 10);
        level2.put("heatValue", 30);
        levels.add(level2);
        
        Map<String, Object> level3 = new HashMap<>();
        level3.put("level", 3);
        level3.put("name", "超级VIP");
        level3.put("color", "#ffd700");
        level3.put("downloadLimit", -1);
        level3.put("heatLimit", 30);
        level3.put("heatValue", 100);
        levels.add(level3);
        
        result.put("levels", levels);
        return result;
    }

    @Override
    @Transactional
    public void heatArticle(Long userId, Long articleId) {
        VipMember member = getVipInfo(userId);
        if (member == null || member.getStatus() != 1) {
            throw new RuntimeException("您不是VIP会员或VIP已过期");
        }
        
        Map<String, Integer> privileges = VIP_PRIVILEGES.get(member.getLevel());
        int heatLimit = privileges.get("heatLimit");
        int heatValue = privileges.get("heatValue");
        
        // 检查今日次数
        if (member.getHeatCountToday() >= heatLimit) {
            throw new RuntimeException("今日加热次数已用完");
        }
        
        // 检查文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        
        // 记录加热
        VipArticleHeat heat = new VipArticleHeat();
        heat.setArticleId(articleId);
        heat.setUserId(userId);
        heat.setHeatValue(heatValue);
        vipArticleHeatMapper.insert(heat);
        
        // 更新文章热度
        LambdaUpdateWrapper<Article> articleWrapper = new LambdaUpdateWrapper<>();
        articleWrapper.eq(Article::getId, articleId)
                .setSql("heat_count = heat_count + " + heatValue);
        articleMapper.update(null, articleWrapper);
        
        // 更新今日次数
        member.setHeatCountToday(member.getHeatCountToday() + 1);
        vipMemberMapper.updateById(member);
    }

    @Override
    public void checkDownload(Long userId, Long articleId) {
        VipMember member = getVipInfo(userId);
        if (member == null || member.getStatus() != 1) {
            throw new RuntimeException("您不是VIP会员或VIP已过期");
        }
        
        Map<String, Integer> privileges = VIP_PRIVILEGES.get(member.getLevel());
        int downloadLimit = privileges.get("downloadLimit");
        
        // -1表示无限
        if (downloadLimit != -1 && member.getDownloadCountToday() >= downloadLimit) {
            throw new RuntimeException("今日下载次数已用完");
        }
        
        // 更新今日次数
        member.setDownloadCountToday(member.getDownloadCountToday() + 1);
        vipMemberMapper.updateById(member);
    }

    @Override
    public boolean isVip(Long userId) {
        VipMember member = getVipInfo(userId);
        return member != null && member.getStatus() == 1;
    }

    @Override
    public Integer getVipLevel(Long userId) {
        VipMember member = getVipInfo(userId);
        if (member != null && member.getStatus() == 1) {
            return member.getLevel();
        }
        return 0;
    }

    @Override
    public IPage<VipMember> getMemberPage(Integer page, Integer size, String keyword, Integer level, Integer status) {
        Page<VipMember> pageParam = new Page<>(page, size);
        return vipMemberMapper.selectMemberPage(pageParam, keyword, level, status);
    }

    @Override
    @Transactional
    public void updateMember(Long id, VipMemberUpdateRequest request) {
        VipMember member = vipMemberMapper.selectById(id);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        
        member.setLevel(request.getLevel());
        member.setExpireTime(request.getExpireTime());
        member.setStatus(request.getExpireTime().isAfter(LocalDateTime.now()) ? 1 : 0);
        vipMemberMapper.updateById(member);
        
        // 同步更新用户表
        User user = new User();
        user.setId(member.getUserId());
        user.setIsVip(member.getStatus());
        user.setVipLevel(member.getStatus() == 1 ? member.getLevel() : 0);
        user.setVipExpireTime(member.getExpireTime());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void deleteMember(Long id) {
        VipMember member = vipMemberMapper.selectById(id);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        
        vipMemberMapper.deleteById(id);
        
        // 清除用户VIP状态
        User user = new User();
        user.setId(member.getUserId());
        user.setIsVip(0);
        user.setVipLevel(0);
        user.setVipExpireTime(null);
        userMapper.updateById(user);
    }

    @Override
    public IPage<VipActivationKey> getKeyPage(Integer page, Integer size, String keyword, Integer level, Integer status) {
        Page<VipActivationKey> pageParam = new Page<>(page, size);
        return vipActivationKeyMapper.selectKeyPage(pageParam, keyword, level, status);
    }

    @Override
    public List<VipActivationKey> generateKeys(VipKeyGenerateRequest request) {
        List<VipActivationKey> keys = new ArrayList<>();
        LocalDateTime expireTime = request.getExpireDays() != null 
                ? LocalDateTime.now().plusDays(request.getExpireDays()) : null;
        
        for (int i = 0; i < request.getCount(); i++) {
            VipActivationKey key = new VipActivationKey();
            key.setKeyCode(generateKeyCode());
            key.setLevel(request.getLevel());
            key.setDurationDays(request.getDurationDays());
            key.setStatus(1);
            key.setRemark(request.getRemark());
            key.setExpireTime(expireTime);
            vipActivationKeyMapper.insert(key);
            keys.add(key);
        }
        
        return keys;
    }

    @Override
    public void updateKeyStatus(Long id, Integer status) {
        VipActivationKey key = vipActivationKeyMapper.selectById(id);
        if (key == null) {
            throw new RuntimeException("密钥不存在");
        }
        if (key.getStatus() == 0) {
            throw new RuntimeException("已使用的密钥不能修改状态");
        }
        key.setStatus(status);
        vipActivationKeyMapper.updateById(key);
    }

    @Override
    public void deleteKey(Long id) {
        vipActivationKeyMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总会员数
        LambdaQueryWrapper<VipMember> totalWrapper = new LambdaQueryWrapper<>();
        stats.put("totalMembers", vipMemberMapper.selectCount(totalWrapper));
        
        // 有效会员数
        LambdaQueryWrapper<VipMember> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(VipMember::getStatus, 1);
        stats.put("activeMembers", vipMemberMapper.selectCount(activeWrapper));
        
        // 各等级分布
        List<Map<String, Object>> levelDistribution = new ArrayList<>();
        for (int level = 1; level <= 3; level++) {
            LambdaQueryWrapper<VipMember> levelWrapper = new LambdaQueryWrapper<>();
            levelWrapper.eq(VipMember::getLevel, level).eq(VipMember::getStatus, 1);
            Map<String, Object> item = new HashMap<>();
            item.put("level", level);
            item.put("name", getLevelName(level));
            item.put("count", vipMemberMapper.selectCount(levelWrapper));
            levelDistribution.add(item);
        }
        stats.put("levelDistribution", levelDistribution);
        
        // 密钥统计
        LambdaQueryWrapper<VipActivationKey> totalKeyWrapper = new LambdaQueryWrapper<>();
        stats.put("totalKeys", vipActivationKeyMapper.selectCount(totalKeyWrapper));
        
        LambdaQueryWrapper<VipActivationKey> usedKeyWrapper = new LambdaQueryWrapper<>();
        usedKeyWrapper.eq(VipActivationKey::getStatus, 0);
        stats.put("usedKeys", vipActivationKeyMapper.selectCount(usedKeyWrapper));
        
        LambdaQueryWrapper<VipActivationKey> availableKeyWrapper = new LambdaQueryWrapper<>();
        availableKeyWrapper.eq(VipActivationKey::getStatus, 1);
        stats.put("availableKeys", vipActivationKeyMapper.selectCount(availableKeyWrapper));
        
        return stats;
    }
    
    private String generateKeyCode() {
        StringBuilder sb = new StringBuilder("VIP-");
        for (int i = 0; i < 4; i++) {
            if (i > 0) sb.append("-");
            for (int j = 0; j < 4; j++) {
                sb.append(KEY_CHARS.charAt(RANDOM.nextInt(KEY_CHARS.length())));
            }
        }
        return sb.toString();
    }
    
    private String getLevelName(int level) {
        switch (level) {
            case 1: return "普通VIP";
            case 2: return "高级VIP";
            case 3: return "超级VIP";
            default: return "未知";
        }
    }
    
    private void resetDailyCount(VipMember member) {
        LocalDate today = LocalDate.now();
        boolean needUpdate = false;
        
        if (member.getHeatDate() == null || !member.getHeatDate().equals(today)) {
            member.setHeatCountToday(0);
            member.setHeatDate(today);
            needUpdate = true;
        }
        
        if (member.getDownloadDate() == null || !member.getDownloadDate().equals(today)) {
            member.setDownloadCountToday(0);
            member.setDownloadDate(today);
            needUpdate = true;
        }
        
        if (needUpdate) {
            vipMemberMapper.updateById(member);
        }
    }
}
