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
class SaveTicketAdapterTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketStatusHistoryRepository ticketStatusHistoryRepository;

    @InjectMocks
    private SaveTicketAdapter saveTicketAdapter;

    private Ticket ticket;
    private UUID changedBy;

    @BeforeEach
    void setUp() {
        changedBy = UUID.randomUUID();
        ticket = Ticket.builder()
            .tenantId(UUID.randomUUID())
            .clientId(changedBy)
            .title("Title")
            .description("Description")
            .status("OPEN")
            .priority("NORMAL")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("saves ticket then creates initial status history")
        void savesTicketAndStatusHistory() {
            Ticket savedTicket = Ticket.builder()
                .id(UUID.randomUUID())
                .tenantId(ticket.getTenantId())
                .clientId(ticket.getClientId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status("OPEN")
                .priority(ticket.getPriority())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
            when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
            when(ticketStatusHistoryRepository.save(any(TicketStatusHistory.class))).thenAnswer(inv -> inv.getArgument(0));

            Ticket result = saveTicketAdapter.save(ticket, changedBy);

            assertThat(result).isSameAs(savedTicket);
            verify(ticketRepository).save(ticket);
            ArgumentCaptor<TicketStatusHistory> historyCaptor = ArgumentCaptor.forClass(TicketStatusHistory.class);
            verify(ticketStatusHistoryRepository).save(historyCaptor.capture());
            TicketStatusHistory history = historyCaptor.getValue();
            assertThat(history.getTicket()).isSameAs(savedTicket);
            assertThat(history.getChangedBy()).isEqualTo(changedBy);
            assertThat(history.getOldStatus()).isEqualTo("NONE");
            assertThat(history.getNewStatus()).isEqualTo("OPEN");
        }
    }
}
