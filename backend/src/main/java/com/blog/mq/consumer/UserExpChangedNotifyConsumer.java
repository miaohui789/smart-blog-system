package com.blog.mq.consumer;

import cn.hutool.json.JSONUtil;
import com.blog.common.constant.RabbitMqConstants;
import com.blog.mq.event.UserExpChangedEvent;
import com.blog.service.NotificationService;
import com.blog.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserExpChangedNotifyConsumer {

    private final NotificationService notificationService;
    private final WebSocketServer webSocketServer;

    @RabbitListener(queues = RabbitMqConstants.QUEUE_EXP_NOTIFY)
    public void consume(UserExpChangedEvent event) {
        if (event == null) {
            return;
        }
        Map<String, Object> extra = new HashMap<>();
        extra.put("beforeLevel", event.getBeforeLevel());
        extra.put("afterLevel", event.getAfterLevel());
        extra.put("expChange", event.getExpChange());
        extra.put("currentExp", event.getCurrentExp());
        extra.put("totalExp", event.getTotalExp());
        extra.put("bizType", event.getBizType());
        extra.put("bizId", event.getBizId());
        try {
            webSocketServer.sendNotification(event.getUserId(), "EXP_CHANGED", extra);
        } catch (Exception e) {
            log.error("推送经验变更WebSocket消息失败", e);
        }
        if (event.getAfterLevel() != null && event.getBeforeLevel() != null && event.getAfterLevel() > event.getBeforeLevel()) {
            notificationService.createNotification(
                    event.getUserId(),
                    "LEVEL_UP",
                    "等级提升",
                    "恭喜升级到 Lv." + event.getAfterLevel(),
                    null,
                    "user",
                    event.getUserId(),
                    JSONUtil.toJsonStr(extra)
            );
            try {
                webSocketServer.sendNotification(event.getUserId(), "EXP_LEVEL_UP", extra);
            } catch (Exception e) {
                log.error("推送升级WebSocket消息失败", e);
            }
        }
    }
}
