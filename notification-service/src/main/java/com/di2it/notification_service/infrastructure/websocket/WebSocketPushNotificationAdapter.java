package com.di2it.notification_service.infrastructure.websocket;

import com.di2it.notification_service.application.port.PushNotificationPort;
import com.di2it.notification_service.domain.entity.Notification;
import com.di2it.notification_service.web.dto.NotificationPayload;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Pushes notifications to WebSocket subscribers on /topic/notifications.
 */
@Component
public class WebSocketPushNotificationAdapter implements PushNotificationPort {

    public static final String TOPIC_NOTIFICATIONS = "/topic/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketPushNotificationAdapter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void push(Notification notification) {
        NotificationPayload payload = NotificationPayload.from(notification);
        messagingTemplate.convertAndSend(TOPIC_NOTIFICATIONS, payload);
    }
}
