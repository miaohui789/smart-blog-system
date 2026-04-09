package com.blog.mq.consumer;

import com.blog.common.constant.RabbitMqConstants;
import com.blog.mq.event.UserExpGrantEvent;
import com.blog.service.UserExpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserExpGrantConsumer {

    private final UserExpService userExpService;

    @RabbitListener(queues = RabbitMqConstants.QUEUE_EXP_GRANT)
    public void consume(UserExpGrantEvent event) {
        if (event == null) {
            return;
        }
        try {
            userExpService.grantExp(event.getUserId(), event.getBizType(), event.getBizId(), event.getExp(), event.getRemark());
        } catch (Exception e) {
            log.error("处理经验发放事件失败 userId={} bizType={} bizId={}", event.getUserId(), event.getBizType(), event.getBizId(), e);
            throw e;
        }
    }
}
