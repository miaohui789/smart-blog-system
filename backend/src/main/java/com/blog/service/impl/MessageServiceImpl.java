package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Message;
import com.blog.entity.MessageConversation;
import com.blog.mapper.MessageConversationMapper;
import com.blog.mapper.MessageMapper;
import com.blog.service.MessageService;
import com.blog.service.RedisService;
import com.blog.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final MessageConversationMapper conversationMapper;
    private final WebSocketServer webSocketServer;
    private final RedisService redisService;

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
        
        // 更新未读数
        if (senderId < receiverId) {
            conversation.setUser2Unread(conversation.getUser2Unread() + 1);
        } else {
            conversation.setUser1Unread(conversation.getUser1Unread() + 1);
        }
        conversationMapper.updateById(conversation);
        
        // 清除接收者的未读数缓存
        redisService.clearMessageCache(receiverId);
        
        // 通过WebSocket推送消息给接收者
        webSocketServer.sendMessageToUser(receiverId, message);
        
        return message;
    }

    @Override
    public List<MessageConversation> getConversationList(Long userId) {
        LambdaQueryWrapper<MessageConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageConversation::getUser1Id, userId)
               .or()
               .eq(MessageConversation::getUser2Id, userId);
        wrapper.orderByDesc(MessageConversation::getLastMessageTime);
        return conversationMapper.selectList(wrapper);
    }

    @Override
    public List<Message> getMessageList(Long conversationId, Long userId, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
               .orderByDesc(Message::getCreateTime);
        
        Page<Message> pageResult = page(new Page<>(page, pageSize), wrapper);
        return pageResult.getRecords();
    }

    @Override
    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        // 更新消息为已读
        LambdaUpdateWrapper<Message> messageWrapper = new LambdaUpdateWrapper<>();
        messageWrapper.eq(Message::getConversationId, conversationId)
                      .eq(Message::getReceiverId, userId)
                      .eq(Message::getIsRead, 0)
                      .set(Message::getIsRead, 1);
        update(messageWrapper);
        
        // 更新会话未读数
        MessageConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null) {
            if (userId.equals(conversation.getUser1Id())) {
                conversation.setUser1Unread(0);
            } else {
                conversation.setUser2Unread(0);
            }
            conversationMapper.updateById(conversation);
        }
        
        // 清除缓存
        redisService.clearMessageCache(userId);
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
        
        // 查询所有相关会话的未读数总和
        List<MessageConversation> conversations = getConversationList(userId);
        int total = 0;
        for (MessageConversation conv : conversations) {
            if (userId.equals(conv.getUser1Id())) {
                total += conv.getUser1Unread() != null ? conv.getUser1Unread() : 0;
            } else {
                total += conv.getUser2Unread() != null ? conv.getUser2Unread() : 0;
            }
        }
        
        // 缓存5分钟
        redisService.setWithMinutes(cacheKey, total, RedisService.EXPIRE_SHORT);
        
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
        }
        
        return conversation;
    }
}
