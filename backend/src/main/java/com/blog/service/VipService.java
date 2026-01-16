package com.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.dto.request.VipActivateRequest;
import com.blog.dto.request.VipKeyGenerateRequest;
import com.blog.dto.request.VipMemberUpdateRequest;
import com.blog.entity.VipActivationKey;
import com.blog.entity.VipMember;

import java.util.List;
import java.util.Map;

public interface VipService {
    
    // ========== 用户端接口 ==========
    
    /**
     * 激活VIP
     */
    VipMember activate(Long userId, VipActivateRequest request);
    
    /**
     * 获取用户VIP信息
     */
    VipMember getVipInfo(Long userId);
    
    /**
     * 获取VIP权益说明
     */
    Map<String, Object> getPrivileges();
    
    /**
     * 文章加热
     */
    void heatArticle(Long userId, Long articleId);
    
    /**
     * 下载文章（检查权限并记录）
     */
    void checkDownload(Long userId, Long articleId);
    
    /**
     * 检查用户是否是VIP
     */
    boolean isVip(Long userId);
    
    /**
     * 获取用户VIP等级
     */
    Integer getVipLevel(Long userId);
    
    // ========== 管理端接口 ==========
    
    /**
     * 分页查询会员列表
     */
    IPage<VipMember> getMemberPage(Integer page, Integer size, String keyword, Integer level, Integer status);
    
    /**
     * 更新会员信息
     */
    void updateMember(Long id, VipMemberUpdateRequest request);
    
    /**
     * 删除会员
     */
    void deleteMember(Long id);
    
    /**
     * 分页查询密钥列表
     */
    IPage<VipActivationKey> getKeyPage(Integer page, Integer size, String keyword, Integer level, Integer status);
    
    /**
     * 批量生成密钥
     */
    List<VipActivationKey> generateKeys(VipKeyGenerateRequest request);
    
    /**
     * 更新密钥状态
     */
    void updateKeyStatus(Long id, Integer status);
    
    /**
     * 删除密钥
     */
    void deleteKey(Long id);
    
    /**
     * 获取VIP统计数据
     */
    Map<String, Object> getStatistics();
}
