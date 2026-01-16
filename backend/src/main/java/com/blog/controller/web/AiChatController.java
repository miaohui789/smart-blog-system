package com.blog.controller.web;

import com.blog.common.result.Result;
import com.blog.entity.AiConfig;
import com.blog.entity.AiConversation;
import com.blog.entity.AiMessage;
import com.blog.security.SecurityUser;
import com.blog.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "AI聊天")
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @Operation(summary = "获取AI状态")
    @GetMapping("/status")
    public Result<?> getStatus(@AuthenticationPrincipal SecurityUser user) {
        AiConfig config = aiChatService.getConfig();
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", config != null && config.getEnabled() == 1);
        status.put("model", config != null ? config.getModel() : null);
        
        if (user != null) {
            Long userId = user.getUser().getId();
            int todayUsage = aiChatService.getTodayUsage(userId);
            int dailyLimit = config != null ? config.getDailyLimit() : 0;
            status.put("todayUsage", todayUsage);
            status.put("dailyLimit", dailyLimit);
            status.put("remaining", Math.max(0, dailyLimit - todayUsage));
        }
        
        return Result.success(status);
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/conversations")
    public Result<?> getConversations(@AuthenticationPrincipal SecurityUser user) {
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        List<AiConversation> conversations = aiChatService.getConversations(user.getUser().getId());
        return Result.success(conversations);
    }

    @Operation(summary = "创建新会话")
    @PostMapping("/conversations")
    public Result<?> createConversation(@AuthenticationPrincipal SecurityUser user,
                                        @RequestBody(required = false) Map<String, String> body) {
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        String title = body != null ? body.get("title") : null;
        AiConversation conversation = aiChatService.createConversation(user.getUser().getId(), title);
        return Result.success(conversation);
    }

    @Operation(summary = "获取会话消息")
    @GetMapping("/conversations/{id}/messages")
    public Result<?> getMessages(@AuthenticationPrincipal SecurityUser user,
                                 @PathVariable Long id) {
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        List<AiMessage> messages = aiChatService.getMessages(id, user.getUser().getId());
        return Result.success(messages);
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/conversations/{id}")
    public Result<?> deleteConversation(@AuthenticationPrincipal SecurityUser user,
                                        @PathVariable Long id) {
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        aiChatService.deleteConversation(id, user.getUser().getId());
        return Result.success();
    }

    @Operation(summary = "更新会话标题")
    @PutMapping("/conversations/{id}/title")
    public Result<?> updateConversationTitle(@AuthenticationPrincipal SecurityUser user,
                                             @PathVariable Long id,
                                             @RequestBody Map<String, String> body) {
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        String title = body.get("title");
        if (title == null || title.trim().isEmpty()) {
            return Result.error(400, "标题不能为空");
        }
        aiChatService.updateConversationTitle(id, user.getUser().getId(), title.trim());
        return Result.success();
    }

    @Operation(summary = "发送消息（SSE流式）")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@AuthenticationPrincipal SecurityUser user,
                           @RequestBody Map<String, Object> body) {
        SseEmitter emitter = new SseEmitter(120000L); // 2分钟超时
        
        if (user == null) {
            try {
                emitter.send(SseEmitter.event().name("error").data("请先登录"));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }
        
        Long conversationId = Long.valueOf(body.get("conversationId").toString());
        String message = (String) body.get("message");
        
        if (message == null || message.trim().isEmpty()) {
            try {
                emitter.send(SseEmitter.event().name("error").data("消息不能为空"));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }
        
        Long userId = user.getUser().getId();
        aiChatService.chat(userId, conversationId, message.trim(), new AiChatService.AiStreamCallback() {
            @Override
            public void onStart() {
                try {
                    emitter.send(SseEmitter.event().name("start").data(""));
                } catch (IOException e) {
                    log.error("SSE发送start失败", e);
                }
            }
            
            @Override
            public void onToken(String token) {
                try {
                    // 转义换行符，防止 SSE 解析问题
                    String escapedToken = token.replace("\n", "\\n").replace("\t", "\\t");
                    emitter.send(SseEmitter.event().name("token").data(escapedToken));
                } catch (IOException e) {
                    log.error("SSE发送token失败", e);
                }
            }
            
            @Override
            public void onComplete(String fullResponse) {
                try {
                    emitter.send(SseEmitter.event().name("done").data(""));
                    emitter.complete();
                } catch (IOException e) {
                    log.error("SSE发送complete失败", e);
                }
            }
            
            @Override
            public void onError(String error) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(error));
                    emitter.complete();
                } catch (IOException e) {
                    log.error("SSE发送error失败", e);
                    emitter.completeWithError(e);
                }
            }
        });
        
        return emitter;
    }
}
