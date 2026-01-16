package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.entity.Message;
import com.blog.entity.MessageConversation;
import com.blog.entity.User;
import com.blog.service.MessageService;
import com.blog.service.UserService;
import com.blog.mapper.MessageConversationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "私信管理")
@RestController
@RequestMapping("/api/admin/messages")
@RequiredArgsConstructor
public class AdminMessageController {

    private final MessageService messageService;
    private final MessageConversationMapper conversationMapper;
    private final UserService userService;

    @Operation(summary = "私信列表")
    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword) {
        
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.and(w -> w.eq(Message::getSenderId, userId)
                    .or().eq(Message::getReceiverId, userId));
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Message::getContent, keyword);
        }
        
        wrapper.orderByDesc(Message::getCreateTime);
        
        Page<Message> pageResult = messageService.page(new Page<>(page, pageSize), wrapper);
        
        // 获取用户信息
        Set<Long> userIds = new HashSet<>();
        pageResult.getRecords().forEach(msg -> {
            userIds.add(msg.getSenderId());
            userIds.add(msg.getReceiverId());
        });
        
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userService.listByIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }
        
        List<Map<String, Object>> list = pageResult.getRecords().stream().map(msg -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("conversationId", msg.getConversationId());
            map.put("senderId", msg.getSenderId());
            map.put("receiverId", msg.getReceiverId());
            map.put("content", msg.getContent());
            map.put("type", msg.getType());
            map.put("isRead", msg.getIsRead());
            map.put("createTime", msg.getCreateTime());
            
            User sender = userMap.get(msg.getSenderId());
            if (sender != null) {
                Map<String, Object> senderInfo = new HashMap<>();
                senderInfo.put("id", sender.getId());
                senderInfo.put("nickname", sender.getNickname());
                senderInfo.put("avatar", sender.getAvatar());
                map.put("sender", senderInfo);
            }
            
            User receiver = userMap.get(msg.getReceiverId());
            if (receiver != null) {
                Map<String, Object> receiverInfo = new HashMap<>();
                receiverInfo.put("id", receiver.getId());
                receiverInfo.put("nickname", receiver.getNickname());
                receiverInfo.put("avatar", receiver.getAvatar());
                map.put("receiver", receiverInfo);
            }
            
            return map;
        }).collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", pageResult.getTotal());
        return Result.success(result);
    }

    @Operation(summary = "会话列表")
    @GetMapping("/conversations")
    public Result<?> conversations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long userId) {
        
        LambdaQueryWrapper<MessageConversation> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.and(w -> w.eq(MessageConversation::getUser1Id, userId)
                    .or().eq(MessageConversation::getUser2Id, userId));
        }
        
        wrapper.orderByDesc(MessageConversation::getLastMessageTime);
        
        Page<MessageConversation> pageResult = conversationMapper.selectPage(
            new Page<>(page, pageSize), wrapper);
        
        // 获取用户信息
        Set<Long> userIds = new HashSet<>();
        Set<Long> lastMessageIds = new HashSet<>();
        pageResult.getRecords().forEach(conv -> {
            userIds.add(conv.getUser1Id());
            userIds.add(conv.getUser2Id());
            if (conv.getLastMessageId() != null) {
                lastMessageIds.add(conv.getLastMessageId());
            }
        });
        
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userService.listByIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }
        
        // 获取最后一条消息内容
        Map<Long, Message> lastMessageMap = new HashMap<>();
        if (!lastMessageIds.isEmpty()) {
            messageService.listByIds(lastMessageIds).forEach(m -> lastMessageMap.put(m.getId(), m));
        }
        
        List<Map<String, Object>> list = pageResult.getRecords().stream().map(conv -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", conv.getId());
            map.put("lastMessageTime", conv.getLastMessageTime());
            
            // 获取最后消息内容
            Message lastMsg = lastMessageMap.get(conv.getLastMessageId());
            map.put("lastMessage", lastMsg != null ? lastMsg.getContent() : "");
            
            // 计算消息数量
            LambdaQueryWrapper<Message> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(Message::getConversationId, conv.getId());
            map.put("messageCount", messageService.count(countWrapper));
            
            User user1 = userMap.get(conv.getUser1Id());
            User user2 = userMap.get(conv.getUser2Id());
            
            if (user1 != null) {
                Map<String, Object> u1 = new HashMap<>();
                u1.put("id", user1.getId());
                u1.put("nickname", user1.getNickname());
                u1.put("avatar", user1.getAvatar());
                map.put("user1", u1);
            }
            
            if (user2 != null) {
                Map<String, Object> u2 = new HashMap<>();
                u2.put("id", user2.getId());
                u2.put("nickname", user2.getNickname());
                u2.put("avatar", user2.getAvatar());
                map.put("user2", u2);
            }
            
            return map;
        }).collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", pageResult.getTotal());
        return Result.success(result);
    }

    @Operation(summary = "删除私信")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        messageService.removeById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除私信")
    @DeleteMapping("/batch")
    public Result<?> batchDelete(@RequestBody List<Long> ids) {
        messageService.removeByIds(ids);
        return Result.success("删除成功");
    }

    @Operation(summary = "私信统计")
    @GetMapping("/stats")
    public Result<?> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalMessages", messageService.count());
        
        LambdaQueryWrapper<Message> unreadWrapper = new LambdaQueryWrapper<>();
        unreadWrapper.eq(Message::getIsRead, 0);
        data.put("unreadMessages", messageService.count(unreadWrapper));
        
        data.put("totalConversations", conversationMapper.selectCount(null));
        
        return Result.success(data);
    }
}
