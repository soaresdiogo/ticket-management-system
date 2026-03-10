package com.di2it.ticket_service.infrastructure.persistence;

import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.domain.repository.TicketRepository;
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
class ListTicketsByClientAdapterTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private ListTicketsByClientAdapter listTicketsByClientAdapter;

    private UUID clientId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("findByClientId")
    class FindByClientId {

        @Test
        @DisplayName("delegates to repository and returns page of tickets")
        void delegatesToRepositoryAndReturnsPage() {
            Ticket ticket = Ticket.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .clientId(clientId)
                .title("Issue")
                .description("Description")
                .status("OPEN")
                .build();
            Page<Ticket> expectedPage = new PageImpl<>(List.of(ticket), pageable, 1);

            when(ticketRepository.findByClientId(eq(clientId), eq(pageable)))
                .thenReturn(expectedPage);

            Page<Ticket> result = listTicketsByClientAdapter.findByClientId(clientId, pageable);

            assertThat(result).isEqualTo(expectedPage);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getClientId()).isEqualTo(clientId);
            verify(ticketRepository).findByClientId(clientId, pageable);
        }

        @Test
        @DisplayName("returns empty page when repository returns no tickets")
        void returnsEmptyPageWhenNoTickets() {
            Page<Ticket> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(ticketRepository.findByClientId(eq(clientId), eq(pageable)))
                .thenReturn(emptyPage);

            Page<Ticket> result = listTicketsByClientAdapter.findByClientId(clientId, pageable);

            assertThat(result.isEmpty()).isTrue();
            assertThat(result.getContent()).isEmpty();
            verify(ticketRepository).findByClientId(clientId, pageable);
        }
    }
}
