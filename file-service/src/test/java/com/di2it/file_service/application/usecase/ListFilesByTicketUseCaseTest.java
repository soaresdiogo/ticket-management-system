package com.di2it.file_service.application.usecase;

import com.di2it.file_service.application.port.ListAttachmentsByTicketPort;
import com.di2it.file_service.domain.entity.Attachment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListFilesByTicketUseCaseTest {

    @Mock
    private ListAttachmentsByTicketPort listAttachmentsByTicketPort;

    private ListFilesByTicketUseCase useCase;

    private UUID ticketId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        useCase = new ListFilesByTicketUseCase(listAttachmentsByTicketPort);
        ticketId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
    }

    @Test
    @DisplayName("listByTicket returns attachments from port")
    void listByTicket() {
        Attachment a1 = Attachment.builder()
            .id(UUID.randomUUID())
            .ticketId(ticketId)
            .tenantId(tenantId)
            .fileName("a.pdf")
            .mimeType("application/pdf")
            .fileSize(100L)
            .createdAt(LocalDateTime.now())
            .build();
        List<Attachment> attachments = List.of(a1);
        when(listAttachmentsByTicketPort.findByTicketIdAndTenantId(ticketId, tenantId)).thenReturn(attachments);

        List<Attachment> result = useCase.listByTicket(ticketId, tenantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFileName()).isEqualTo("a.pdf");
        verify(listAttachmentsByTicketPort).findByTicketIdAndTenantId(ticketId, tenantId);
    }

    @Test
    @DisplayName("listByTicket returns empty list when no attachments")
    void listByTicketEmpty() {
        when(listAttachmentsByTicketPort.findByTicketIdAndTenantId(ticketId, tenantId)).thenReturn(List.of());

        List<Attachment> result = useCase.listByTicket(ticketId, tenantId);

        assertThat(result).isEmpty();
    }
}
