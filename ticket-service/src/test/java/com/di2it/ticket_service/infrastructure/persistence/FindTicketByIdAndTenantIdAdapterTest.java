package com.di2it.ticket_service.infrastructure.persistence;

import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.domain.repository.TicketRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindTicketByIdAndTenantIdAdapterTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private FindTicketByIdAndTenantIdAdapter adapter;

    @Test
    @DisplayName("findByIdAndTenantId delegates to repository and returns optional")
    void findByIdAndTenantIdDelegatesToRepository() {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Ticket ticket = Ticket.builder()
            .id(id)
            .tenantId(tenantId)
            .clientId(UUID.randomUUID())
            .title("Title")
            .description("Desc")
            .status("OPEN")
            .build();
        when(ticketRepository.findByIdAndTenantId(id, tenantId)).thenReturn(Optional.of(ticket));

        Optional<Ticket> result = adapter.findByIdAndTenantId(id, tenantId);

        assertThat(result).containsSame(ticket);
        verify(ticketRepository).findByIdAndTenantId(id, tenantId);
    }

    @Test
    @DisplayName("findByIdAndTenantId returns empty when repository returns empty")
    void findByIdAndTenantIdReturnsEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(ticketRepository.findByIdAndTenantId(id, tenantId)).thenReturn(Optional.empty());

        Optional<Ticket> result = adapter.findByIdAndTenantId(id, tenantId);

        assertThat(result).isEmpty();
        verify(ticketRepository).findByIdAndTenantId(id, tenantId);
    }
}
