package com.blog.mq.consumer;

import com.blog.common.constant.ExpConstants;
import com.blog.common.constant.RabbitMqConstants;
import com.blog.mq.event.UserExpChangedEvent;
import com.blog.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserExpChangedRankConsumer {

    private final RedisService redisService;

    @RabbitListener(queues = RabbitMqConstants.QUEUE_EXP_RANK)
    public void consume(UserExpChangedEvent event) {
        if (event == null || event.getUserId() == null || event.getExpChange() == null || event.getExpChange() <= 0) {
            return;
        }
        redisService.zIncrementScore(ExpConstants.REDIS_EXP_RANK_TOTAL, String.valueOf(event.getUserId()), event.getExpChange());
    }
}
