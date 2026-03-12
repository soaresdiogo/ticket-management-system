package com.di2it.ticket_service.application.usecase;

import com.di2it.ticket_service.application.port.ListAllTicketsPort;
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
class ListAllTicketsUseCaseTest {

    @Mock
    private ListAllTicketsPort listAllTicketsPort;

    @InjectMocks
    private ListAllTicketsUseCase listAllTicketsUseCase;

    private UUID tenantId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        pageable = PageRequest.of(0, 20);
    }

    @Nested
    @DisplayName("listByTenant")
    class ListByTenant {

        @Test
        @DisplayName("delegates to port and returns page of tickets")
        void delegatesToPortAndReturnsPage() {
            Ticket ticket = Ticket.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .clientId(UUID.randomUUID())
                .title("Issue")
                .description("Description")
                .status("OPEN")
                .build();
            Page<Ticket> expectedPage = new PageImpl<>(List.of(ticket), pageable, 1);

            when(listAllTicketsPort.findByTenantId(eq(tenantId), eq(pageable)))
                .thenReturn(expectedPage);

            Page<Ticket> result = listAllTicketsUseCase.listByTenant(tenantId, pageable, null);

            assertThat(result).isEqualTo(expectedPage);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTenantId()).isEqualTo(tenantId);
            verify(listAllTicketsPort).findByTenantId(tenantId, pageable);
        }

        @Test
        @DisplayName("returns empty page when port returns no tickets")
        void returnsEmptyPageWhenNoTickets() {
            Page<Ticket> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(listAllTicketsPort.findByTenantId(eq(tenantId), eq(pageable)))
                .thenReturn(emptyPage);

            Page<Ticket> result = listAllTicketsUseCase.listByTenant(tenantId, pageable, null);

            assertThat(result.isEmpty()).isTrue();
            assertThat(result.getContent()).isEmpty();
            verify(listAllTicketsPort).findByTenantId(tenantId, pageable);
        }

        @Test
        @DisplayName("when status is provided delegates to findByTenantIdAndStatus")
        void whenStatusProvidedDelegatesToFindByTenantIdAndStatus() {
            Ticket ticket = Ticket.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .clientId(UUID.randomUUID())
                .title("Issue")
                .description("Description")
                .status("OPEN")
                .build();
            Page<Ticket> expectedPage = new PageImpl<>(List.of(ticket), pageable, 1);

            when(listAllTicketsPort.findByTenantIdAndStatus(eq(tenantId), eq("OPEN"), eq(pageable)))
                .thenReturn(expectedPage);

            Page<Ticket> result = listAllTicketsUseCase.listByTenant(tenantId, pageable, "OPEN");

            assertThat(result).isEqualTo(expectedPage);
            assertThat(result.getContent()).hasSize(1);
            verify(listAllTicketsPort).findByTenantIdAndStatus(tenantId, "OPEN", pageable);
        }
    }
}
