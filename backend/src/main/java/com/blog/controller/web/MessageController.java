package com.blog.controller.web;

import com.blog.common.result.Result;
import com.blog.dto.request.MessageRequest;
import com.blog.dto.response.ConversationVO;
import com.blog.dto.response.MessageVO;
import com.blog.entity.Message;
import com.blog.entity.MessageConversation;
import com.blog.entity.User;
import com.blog.security.SecurityUser;
import com.blog.service.MessageService;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "私信接口")
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @Operation(summary = "发送私信")
    @PostMapping("/send")
    public Result<?> sendMessage(@Validated @RequestBody MessageRequest request) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        if (currentUserId.equals(request.getReceiverId())) {
            return Result.error("不能给自己发私信");
        }
        
        // 检查接收者是否存在
        User receiver = userService.getById(request.getReceiverId());
        if (receiver == null) {
            return Result.error("用户不存在");
        }
        
        // 检查接收者是否被冻结或已注销
        if (receiver.getStatus() != null && receiver.getStatus() == 0) {
            return Result.error("该用户已被冻结，无法发送私信");
        }
        if (receiver.getStatus() != null && receiver.getStatus() == 2) {
            return Result.error("该用户已注销，无法发送私信");
        }
        
        Message message = messageService.sendMessage(
                currentUserId, 
                request.getReceiverId(), 
                request.getContent(),
                request.getType()
        );
        
        return Result.success("发送成功", message);
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/conversations")
    public Result<List<ConversationVO>> getConversations() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        
        List<MessageConversation> conversations = messageService.getConversationList(currentUserId);
        
        if (conversations.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        
        // 收集所有相关用户ID
        Set<Long> userIds = conversations.stream()
                .flatMap(c -> List.of(c.getUser1Id(), c.getUser2Id()).stream())
                .filter(id -> !id.equals(currentUserId))
                .collect(Collectors.toSet());
        
        // 批量查询用户信息
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        // 获取最后一条消息
        Set<Long> messageIds = conversations.stream()
                .filter(c -> c.getLastMessageId() != null)
                .map(MessageConversation::getLastMessageId)
                .collect(Collectors.toSet());
        Map<Long, Message> messageMap = messageIds.isEmpty() ? Map.of() :
                messageService.listByIds(messageIds).stream()
                        .collect(Collectors.toMap(Message::getId, m -> m));
        
        // 转换为VO
        List<ConversationVO> voList = conversations.stream().map(conv -> {
            ConversationVO vo = new ConversationVO();
            vo.setId(conv.getId());
            vo.setLastMessageTime(conv.getLastMessageTime());
            
            // 对方用户
            Long targetUserId = conv.getUser1Id().equals(currentUserId) ? conv.getUser2Id() : conv.getUser1Id();
            User targetUser = userMap.get(targetUserId);
            if (targetUser != null) {
                ConversationVO.UserInfo userInfo = new ConversationVO.UserInfo();
                userInfo.setId(targetUser.getId());
                userInfo.setNickname(targetUser.getNickname());
                userInfo.setAvatar(targetUser.getAvatar());
                userInfo.setVipLevel(targetUser.getVipLevel());
                vo.setTargetUser(userInfo);
            }
            
            // 最后一条消息
            if (conv.getLastMessageId() != null) {
                Message lastMsg = messageMap.get(conv.getLastMessageId());
                if (lastMsg != null) {
                    vo.setLastMessage(lastMsg.getContent());
                }
            }
            
            // 未读数
            if (conv.getUser1Id().equals(currentUserId)) {
                vo.setUnreadCount(conv.getUser1Unread());
            } else {
                vo.setUnreadCount(conv.getUser2Unread());
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        return Result.success(voList);
    }

    @Operation(summary = "获取会话消息列表")
    @GetMapping("/conversations/{conversationId}")
    public Result<List<MessageVO>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        
        List<Message> messages = messageService.getMessageList(conversationId, currentUserId, page, pageSize);
        
        // 收集发送者ID
        Set<Long> senderIds = messages.stream()
                .map(Message::getSenderId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = senderIds.isEmpty() ? Map.of() :
                userService.listByIds(senderIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));
        
        // 转换为VO
        List<MessageVO> voList = messages.stream().map(msg -> {
            MessageVO vo = new MessageVO();
            vo.setId(msg.getId());
            vo.setConversationId(msg.getConversationId());
            vo.setSenderId(msg.getSenderId());
            vo.setReceiverId(msg.getReceiverId());
            vo.setContent(msg.getContent());
            vo.setType(msg.getType());
            vo.setIsRead(msg.getIsRead());
            vo.setCreateTime(msg.getCreateTime());
            vo.setIsSelf(msg.getSenderId().equals(currentUserId));
            
            // 发送者信息
            User sender = userMap.get(msg.getSenderId());
            if (sender != null) {
                MessageVO.UserInfo userInfo = new MessageVO.UserInfo();
                userInfo.setId(sender.getId());
                userInfo.setNickname(sender.getNickname());
                userInfo.setAvatar(sender.getAvatar());
                userInfo.setVipLevel(sender.getVipLevel());
                vo.setSender(userInfo);
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        return Result.success(voList);
    }

    @Operation(summary = "标记会话已读")
    @PostMapping("/conversations/{conversationId}/read")
    public Result<?> markAsRead(@PathVariable Long conversationId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        
        messageService.markAsRead(conversationId, currentUserId);
        return Result.success("已标记为已读");
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.success(0);
        }
        
        return Result.success(messageService.getUnreadCount(currentUserId));
    }

    @Operation(summary = "获取与指定用户的会话")
    @GetMapping("/conversation-with/{userId}")
    public Result<?> getConversationWith(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        
        MessageConversation conversation = messageService.getOrCreateConversation(currentUserId, userId);
        return Result.success(conversation);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            return securityUser.getUser().getId();
        }
        return null;
    }
}
