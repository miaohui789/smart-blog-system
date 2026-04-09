package com.blog.mq;

import com.blog.mq.event.UserExpGrantEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserExpAsyncService {

    private final UserExpEventPublisher userExpEventPublisher;

    public void publishGrant(Long userId, String bizType, String bizId, Integer exp, String remark, String source) {
        if (userId == null || exp == null || exp <= 0 || bizType == null || bizId == null) {
            return;
        }
        UserExpGrantEvent event = new UserExpGrantEvent();
        event.setUserId(userId);
        event.setBizType(bizType);
        event.setBizId(bizId);
        event.setExp(exp);
        event.setRemark(remark);
        event.setSource(source);
        event.setEventTime(LocalDateTime.now());
        userExpEventPublisher.publishGrant(event);
    }
}
