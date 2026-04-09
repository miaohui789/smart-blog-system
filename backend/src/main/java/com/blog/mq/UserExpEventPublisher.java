package com.blog.mq;

import com.blog.common.constant.RabbitMqConstants;
import com.blog.mq.event.UserExpChangedEvent;
import com.blog.mq.event.UserExpGrantEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserExpEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishGrant(UserExpGrantEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMqConstants.EXCHANGE_USER_EXP, RabbitMqConstants.ROUTING_KEY_EXP_GRANT, event);
        } catch (Exception e) {
            log.error("发送经验发放事件失败 userId={} bizType={} bizId={}", event.getUserId(), event.getBizType(), event.getBizId(), e);
        }
    }

    public void publishChanged(UserExpChangedEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMqConstants.EXCHANGE_USER_EXP, RabbitMqConstants.ROUTING_KEY_EXP_CHANGED, event);
        } catch (Exception e) {
            log.error("发送经验变更事件失败 userId={} bizType={} bizId={}", event.getUserId(), event.getBizType(), event.getBizId(), e);
        }
    }
}
