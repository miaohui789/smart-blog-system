package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Message;
import com.blog.entity.MessageConversation;
import com.blog.entity.User;
import com.blog.mapper.MessageConversationMapper;
import com.blog.mapper.MessageMapper;
import com.blog.service.MessageService;
import com.blog.service.RedisService;
import com.blog.service.UserFollowService;
import com.blog.service.UserService;
import com.blog.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final MessageConversationMapper conversationMapper;
    private final WebSocketServer webSocketServer;
    private final RedisService redisService;
    private final UserFollowService userFollowService;
    private final UserService userService;

    @Override
    public String checkCanSendMessage(Long senderId, Long receiverId) {
        // 不能给自己发消息
        if (senderId.equals(receiverId)) {
            return "不能给自己发送消息";
        }
        
        // 检查是否互相关注或发送者关注了接收者
        boolean senderFollowsReceiver = userFollowService.isFollowed(senderId, receiverId);
        boolean receiverFollowsSender = userFollowService.isFollowed(receiverId, senderId);
        
        // 如果发送者关注了接收者，或者接收者关注了发送者，可以无限发消息
        if (senderFollowsReceiver || receiverFollowsSender) {
            return null;
        }
        
        // 陌生人：检查是否已经发过消息
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getSenderId, senderId)
               .eq(Message::getReceiverId, receiverId);
        long count = count(wrapper);
        
        if (count > 0) {
            return "对方未关注你，只能发送一条消息。关注对方后可继续发送";
        }
        
        return null;
    }

    @Override
    @Transactional
    public Message sendMessage(Long senderId, Long receiverId, String content, Integer type) {
        // 获取或创建会话
        MessageConversation conversation = getOrCreateConversation(senderId, receiverId);
        
        // 创建消息
        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType(type != null ? type : 1);
        message.setIsRead(0);
        save(message);
        
        // 更新会话
        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageTime(LocalDateTime.now());
        
        // 更新未读数（数据库）
        if (senderId < receiverId) {
            conversation.setUser2Unread(conversation.getUser2Unread() + 1);
        } else {
            conversation.setUser1Unread(conversation.getUser1Unread() + 1);
        }
        conversationMapper.updateById(conversation);
        
        // 更新Redis未读数（原子操作，保证一致性）
        redisService.incrementMessageUnread(receiverId);
        // 清除接收者的会话列表缓存（因为有新消息）
        redisService.delete(RedisService.CACHE_MESSAGE_CONVERSATION + receiverId);
        
        // 通过WebSocket推送消息给接收者
        log.info("准备推送消息给接收者 userId={}, messageId={}", receiverId, message.getId());
        
        // 构建包含发送者信息的推送数据
        Map<String, Object> pushData = new HashMap<>();
        pushData.put("id", message.getId());
        pushData.put("conversationId", message.getConversationId());
        pushData.put("senderId", message.getSenderId());
        pushData.put("receiverId", message.getReceiverId());
        pushData.put("content", message.getContent());
        pushData.put("type", message.getType());
        pushData.put("createTime", message.getCreateTime());
        
        // 获取发送者信息
        User sender = userService.getById(senderId);
        if (sender != null) {
            Map<String, Object> senderInfo = new HashMap<>();
            senderInfo.put("id", sender.getId());
            senderInfo.put("nickname", sender.getNickname());
            senderInfo.put("avatar", sender.getAvatar());
            senderInfo.put("userLevel", sender.getUserLevel());
            senderInfo.put("vipLevel", sender.getVipLevel());
            pushData.put("sender", senderInfo);
        }
        
        webSocketServer.sendMessageToUser(receiverId, pushData);
        
        return message;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MessageConversation> getConversationList(Long userId) {
        // 尝试从缓存获取
        String cacheKey = RedisService.CACHE_MESSAGE_CONVERSATION + userId;
        List<MessageConversation> cached = redisService.getList(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        
        // 从数据库查询
        LambdaQueryWrapper<MessageConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageConversation::getUser1Id, userId)
               .or()
               .eq(MessageConversation::getUser2Id, userId);
        wrapper.orderByDesc(MessageConversation::getLastMessageTime);
        List<MessageConversation> list = conversationMapper.selectList(wrapper);
        
        // 缓存30分钟
        if (!list.isEmpty()) {
            redisService.setWithMinutes(cacheKey, list, RedisService.EXPIRE_MEDIUM);
        }
        
        return list;
    }

    @Override
    public List<Message> getMessageList(Long conversationId, Long userId, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
               .and(w -> w
                   // 发送者查看：不显示自己删除的
                   .apply("(sender_id = {0} AND (sender_deleted IS NULL OR sender_deleted = 0))", userId)
                   .or()
                   // 接收者查看：不显示自己删除的
                   .apply("(receiver_id = {0} AND (receiver_deleted IS NULL OR receiver_deleted = 0))", userId)
               )
               .orderByDesc(Message::getCreateTime);
        
        Page<Message> pageResult = page(new Page<>(page, pageSize), wrapper);
        return pageResult.getRecords();
    }

    @Override
    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        // 先获取会话，计算要减少的未读数
        MessageConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return;
        }
        
        int unreadToDecrease = 0;
        if (userId.equals(conversation.getUser1Id())) {
            unreadToDecrease = conversation.getUser1Unread() != null ? conversation.getUser1Unread() : 0;
            conversation.setUser1Unread(0);
        } else {
            unreadToDecrease = conversation.getUser2Unread() != null ? conversation.getUser2Unread() : 0;
            conversation.setUser2Unread(0);
        }
        
        // 更新消息为已读
        LambdaUpdateWrapper<Message> messageWrapper = new LambdaUpdateWrapper<>();
        messageWrapper.eq(Message::getConversationId, conversationId)
                      .eq(Message::getReceiverId, userId)
                      .eq(Message::getIsRead, 0)
                      .set(Message::getIsRead, 1);
        update(messageWrapper);
        
        // 更新会话未读数
        conversationMapper.updateById(conversation);
        
        // 更新Redis未读数（原子减少）
        if (unreadToDecrease > 0) {
            redisService.decrementMessageUnread(userId, unreadToDecrease);
        }
    }

    @Override
    public int getUnreadCount(Long userId) {
        // 先从缓存获取
        String cacheKey = RedisService.CACHE_MESSAGE_UNREAD + userId;
        Object cached = redisService.get(cacheKey);
        if (cached instanceof Integer) {
            return (Integer) cached;
        }
        if (cached instanceof Number) {
            return ((Number) cached).intValue();
        }
        
        // 缓存不存在，从数据库计算并写入缓存
        List<MessageConversation> conversations = getConversationList(userId);
        int total = 0;
        for (MessageConversation conv : conversations) {
            if (userId.equals(conv.getUser1Id())) {
                total += conv.getUser1Unread() != null ? conv.getUser1Unread() : 0;
            } else {
                total += conv.getUser2Unread() != null ? conv.getUser2Unread() : 0;
            }
        }
        
        // 缓存30分钟（与会话列表缓存时间一致）
        redisService.setWithMinutes(cacheKey, total, RedisService.EXPIRE_MEDIUM);
        
        return total;
    }

    @Override
    public MessageConversation getOrCreateConversation(Long userId1, Long userId2) {
        // 确保 user1Id < user2Id
        Long smallerId = Math.min(userId1, userId2);
        Long largerId = Math.max(userId1, userId2);
        
        // 查找现有会话
        LambdaQueryWrapper<MessageConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageConversation::getUser1Id, smallerId)
               .eq(MessageConversation::getUser2Id, largerId);
        MessageConversation conversation = conversationMapper.selectOne(wrapper);
        
        // 不存在则创建
        if (conversation == null) {
            conversation = new MessageConversation();
            conversation.setUser1Id(smallerId);
            conversation.setUser2Id(largerId);
            conversation.setUser1Unread(0);
            conversation.setUser2Unread(0);
            conversationMapper.insert(conversation);
            
            // 清除双方的会话列表缓存
            redisService.delete(RedisService.CACHE_MESSAGE_CONVERSATION + userId1);
            redisService.delete(RedisService.CACHE_MESSAGE_CONVERSATION + userId2);
        }
        
        return conversation;
    }

    @Override
    @Transactional
    public boolean deleteMessage(Long messageId, Long userId) {
        Message message = getById(messageId);
        if (message == null) {
            return false;
        }
        
        // 只能删除自己发送或接收的消息
        if (!message.getSenderId().equals(userId) && !message.getReceiverId().equals(userId)) {
            return false;
        }
        
        // 逻辑删除：标记当前用户的删除状态
        if (message.getSenderId().equals(userId)) {
            // 发送者删除
            message.setSenderDeleted(1);
        } else {
            // 接收者删除
            message.setReceiverDeleted(1);
        }
        
        boolean success = updateById(message);
        
        // 如果双方都删除了，物理删除消息
        if (success && message.getSenderDeleted() != null && message.getSenderDeleted() == 1 
                && message.getReceiverDeleted() != null && message.getReceiverDeleted() == 1) {
            removeById(messageId);
        }
        
        return success;
    }

    @Override
    @Transactional
    public boolean withdrawMessage(Long messageId, Long userId) {
        Message message = getById(messageId);
        if (message == null) {
            return false;
        }
        
        // 只能撤回自己发送的消息
        if (!message.getSenderId().equals(userId)) {
            return false;
        }
        
        // 检查是否在5分钟内
        LocalDateTime createTime = message.getCreateTime();
        if (createTime == null || createTime.plusMinutes(5).isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // 更新消息状态为已撤回
        message.setIsWithdrawn(1);
        message.setContent("[消息已撤回]");
        boolean success = updateById(message);
        
        // 通过WebSocket通知对方消息已撤回
        if (success) {
            webSocketServer.sendMessageWithdrawNotice(message.getReceiverId(), messageId);
        }
        
        return success;
    }

    @Override
    @Transactional
    public boolean deleteConversation(Long conversationId, Long userId) {
        MessageConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return false;
        }
        
        // 只能删除自己参与的会话
        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            return false;
        }
        
        // 获取当前用户在该会话的未读数
        int unreadCount = 0;
        if (userId.equals(conversation.getUser1Id())) {
            unreadCount = conversation.getUser1Unread() != null ? conversation.getUser1Unread() : 0;
        } else {
            unreadCount = conversation.getUser2Unread() != null ? conversation.getUser2Unread() : 0;
        }
        
        // 删除会话中的所有消息
        LambdaQueryWrapper<Message> messageWrapper = new LambdaQueryWrapper<>();
        messageWrapper.eq(Message::getConversationId, conversationId);
        remove(messageWrapper);
        
        // 删除会话
        conversationMapper.deleteById(conversationId);
        
        // 更新Redis：减少未读数，清除会话列表缓存
        if (unreadCount > 0) {
            redisService.decrementMessageUnread(userId, unreadCount);
        }
        redisService.delete(RedisService.CACHE_MESSAGE_CONVERSATION + userId);
        
        return true;
    }
}
