package com.di2it.notification_service.application.usecase;

import com.di2it.notification_service.application.port.FindNotificationByIdPort;
import com.di2it.notification_service.application.port.PersistNotificationPort;
import com.di2it.notification_service.domain.entity.Notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkNotificationReadUseCaseTest {

    @Mock
    private FindNotificationByIdPort findNotificationByIdPort;

    @Mock
    private PersistNotificationPort persistNotificationPort;

    @InjectMocks
    private MarkNotificationReadUseCase useCase;

    private UUID notificationId;
    private UUID userId;
    private Notification notification;

    @BeforeEach
    void setUp() {
        notificationId = UUID.randomUUID();
        userId = UUID.randomUUID();
        notification = Notification.builder()
            .id(notificationId)
            .tenantId(UUID.randomUUID())
            .userId(userId)
            .type("TICKET_STATUS_CHANGED")
            .title("Ticket updated")
            .message("Updated to Processing")
            .referenceId(UUID.randomUUID())
            .read(false)
            .createdAt(Instant.now())
            .build();
    }

    @Test
    @DisplayName("marks as read and returns notification when found and owned by user")
    void markRead_returnsUpdatedWhenFoundAndOwned() {
        when(findNotificationByIdPort.findById(notificationId)).thenReturn(Optional.of(notification));
        when(persistNotificationPort.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Notification> result = useCase.markRead(notificationId, userId);

        assertThat(result).isPresent();
        assertThat(result.get().isRead()).isTrue();
        assertThat(result.get().getReadAt()).isNotNull();
        verify(persistNotificationPort).save(any(Notification.class));
    }

    @Test
    @DisplayName("returns empty when notification not found")
    void markRead_returnsEmptyWhenNotFound() {
        when(findNotificationByIdPort.findById(notificationId)).thenReturn(Optional.empty());

        Optional<Notification> result = useCase.markRead(notificationId, userId);

        assertThat(result).isEmpty();
        verify(persistNotificationPort, never()).save(any());
    }

    @Test
    @DisplayName("returns empty when notification owned by different user")
    void markRead_returnsEmptyWhenNotOwned() {
        when(findNotificationByIdPort.findById(notificationId)).thenReturn(Optional.of(notification));
        UUID otherUser = UUID.randomUUID();

        Optional<Notification> result = useCase.markRead(notificationId, otherUser);

        assertThat(result).isEmpty();
        verify(persistNotificationPort, never()).save(any());
    }
}
