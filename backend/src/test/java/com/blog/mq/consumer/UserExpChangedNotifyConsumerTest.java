package com.blog.mq.consumer;

import com.blog.mq.event.UserExpChangedEvent;
import com.blog.service.NotificationService;
import com.blog.websocket.WebSocketServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserExpChangedNotifyConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private WebSocketServer webSocketServer;

    @InjectMocks
    private UserExpChangedNotifyConsumer consumer;

    @Captor
    private ArgumentCaptor<Object> payloadCaptor;

    @Captor
    private ArgumentCaptor<String> extraJsonCaptor;

    @Test
    void shouldPushExpChangedWhenLevelUnchanged() {
        UserExpChangedEvent event = buildEvent(1, 1);

        consumer.consume(event);

        verify(webSocketServer).sendNotification(eq(1L), eq("EXP_CHANGED"), payloadCaptor.capture());
        verify(webSocketServer, never()).sendNotification(eq(1L), eq("EXP_LEVEL_UP"), org.mockito.ArgumentMatchers.any());
        verify(notificationService, never()).createNotification(anyLong(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.any(), anyString(), anyLong(), anyString());

        Map<?, ?> payload = assertInstanceOf(Map.class, payloadCaptor.getValue());
        assertEquals(1, payload.get("beforeLevel"));
        assertEquals(1, payload.get("afterLevel"));
        assertEquals(5, payload.get("expChange"));
        assertEquals(25, payload.get("currentExp"));
        assertEquals("SIGN_IN", payload.get("bizType"));
        assertEquals("today", payload.get("bizId"));
    }

    @Test
    void shouldPushExpChangedAndLevelUpWhenLevelIncreased() {
        UserExpChangedEvent event = buildEvent(1, 2);

        consumer.consume(event);

        InOrder inOrder = inOrder(webSocketServer);
        inOrder.verify(webSocketServer).sendNotification(eq(1L), eq("EXP_CHANGED"), org.mockito.ArgumentMatchers.any());
        inOrder.verify(webSocketServer).sendNotification(eq(1L), eq("EXP_LEVEL_UP"), org.mockito.ArgumentMatchers.any());

        verify(notificationService).createNotification(
                eq(1L),
                eq("LEVEL_UP"),
                eq("等级提升"),
                eq("恭喜升级到 Lv.2"),
                isNull(),
                eq("user"),
                eq(1L),
                extraJsonCaptor.capture()
        );
        assertTrue(extraJsonCaptor.getValue().contains("\"afterLevel\":2"));
        assertTrue(extraJsonCaptor.getValue().contains("\"bizType\":\"SIGN_IN\""));
    }

    private UserExpChangedEvent buildEvent(int beforeLevel, int afterLevel) {
        UserExpChangedEvent event = new UserExpChangedEvent();
        event.setUserId(1L);
        event.setBizType("SIGN_IN");
        event.setBizId("today");
        event.setExpChange(5);
        event.setBeforeLevel(beforeLevel);
        event.setAfterLevel(afterLevel);
        event.setCurrentExp(25);
        event.setTotalExp(100);
        return event;
    }
}
