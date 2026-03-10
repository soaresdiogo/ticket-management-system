package com.di2it.ticket_service.application.usecase;

import com.di2it.ticket_service.application.port.ListTicketsByClientPort;
import com.di2it.ticket_service.domain.entity.Ticket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListTicketsUseCaseTest {

    @Mock
    private ListTicketsByClientPort listTicketsByClientPort;

    @InjectMocks
    private ListTicketsUseCase listTicketsUseCase;

    private UUID clientId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        pageable = PageRequest.of(0, 20);
    }

    @Nested
    @DisplayName("listByClient")
    class ListByClient {

        @Test
        @DisplayName("delegates to port and returns page of tickets")
        void delegatesToPortAndReturnsPage() {
            Ticket ticket = Ticket.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .clientId(clientId)
                .title("Issue")
                .description("Description")
                .status("OPEN")
                .build();
            Page<Ticket> expectedPage = new PageImpl<>(List.of(ticket), pageable, 1);

            when(listTicketsByClientPort.findByClientId(eq(clientId), eq(pageable)))
                .thenReturn(expectedPage);

            Page<Ticket> result = listTicketsUseCase.listByClient(clientId, pageable);

            assertThat(result).isEqualTo(expectedPage);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getClientId()).isEqualTo(clientId);
            verify(listTicketsByClientPort).findByClientId(clientId, pageable);
        }

        @Test
        @DisplayName("returns empty page when port returns no tickets")
        void returnsEmptyPageWhenNoTickets() {
            Page<Ticket> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(listTicketsByClientPort.findByClientId(eq(clientId), eq(pageable)))
                .thenReturn(emptyPage);

            Page<Ticket> result = listTicketsUseCase.listByClient(clientId, pageable);

            assertThat(result.isEmpty()).isTrue();
            assertThat(result.getContent()).isEmpty();
            verify(listTicketsByClientPort).findByClientId(clientId, pageable);
        }
    }
}
