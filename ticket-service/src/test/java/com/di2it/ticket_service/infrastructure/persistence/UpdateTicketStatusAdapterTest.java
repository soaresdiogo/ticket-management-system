package com.di2it.ticket_service.infrastructure.persistence;

import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.domain.entity.TicketStatusHistory;
import com.di2it.ticket_service.domain.repository.TicketRepository;
import com.di2it.ticket_service.domain.repository.TicketStatusHistoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTicketStatusAdapterTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketStatusHistoryRepository ticketStatusHistoryRepository;

    @InjectMocks
    private UpdateTicketStatusAdapter updateTicketStatusAdapter;

    private Ticket ticket;
    private UUID changedBy;

    @BeforeEach
    void setUp() {
        changedBy = UUID.randomUUID();
        ticket = Ticket.builder()
            .id(UUID.randomUUID())
            .tenantId(UUID.randomUUID())
            .clientId(UUID.randomUUID())
            .title("Title")
            .description("Desc")
            .status("IN_PROGRESS")
            .priority("NORMAL")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("saves ticket and creates status history record")
        void savesTicketAndHistory() {
            when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
            when(ticketStatusHistoryRepository.save(any(TicketStatusHistory.class))).thenAnswer(inv -> inv.getArgument(0));

            Ticket result = updateTicketStatusAdapter.updateStatus(ticket, changedBy, "OPEN");

            assertThat(result).isSameAs(ticket);
            verify(ticketRepository).save(ticket);
            ArgumentCaptor<TicketStatusHistory> historyCaptor = ArgumentCaptor.forClass(TicketStatusHistory.class);
            verify(ticketStatusHistoryRepository).save(historyCaptor.capture());
            TicketStatusHistory history = historyCaptor.getValue();
            assertThat(history.getTicket()).isSameAs(ticket);
            assertThat(history.getChangedBy()).isEqualTo(changedBy);
            assertThat(history.getOldStatus()).isEqualTo("OPEN");
            assertThat(history.getNewStatus()).isEqualTo("IN_PROGRESS");
        }
    }
}
