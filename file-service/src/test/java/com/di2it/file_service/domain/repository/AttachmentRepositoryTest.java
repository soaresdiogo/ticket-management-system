package com.di2it.file_service.domain.repository;

import com.di2it.file_service.domain.entity.Attachment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AttachmentRepositoryTest {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByTicketIdAndDeletedAtIsNullOrderByCreatedAtDesc returns only non-deleted attachments")
    void findByTicketIdExcludesDeleted() {
        UUID tenantId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();

        Attachment active = Attachment.builder()
            .tenantId(tenantId)
            .ticketId(ticketId)
            .uploadedBy(uploaderId)
            .uploaderRole("CLIENT")
            .fileName("doc.pdf")
            .minioKey("tenant/ticket/doc.pdf")
            .mimeType("application/pdf")
            .fileSize(1024L)
            .visibleTo("ALL")
            .createdAt(LocalDateTime.now())
            .build();
        Attachment deleted = Attachment.builder()
            .tenantId(tenantId)
            .ticketId(ticketId)
            .uploadedBy(uploaderId)
            .uploaderRole("CLIENT")
            .fileName("old.pdf")
            .minioKey("tenant/ticket/old.pdf")
            .mimeType("application/pdf")
            .fileSize(512L)
            .visibleTo("ALL")
            .deletedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();

        entityManager.persist(active);
        entityManager.persist(deleted);
        entityManager.flush();
        entityManager.clear();

        List<Attachment> result = attachmentRepository.findByTicketIdAndDeletedAtIsNullOrderByCreatedAtDesc(ticketId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFileName()).isEqualTo("doc.pdf");
    }

    @Test
    @DisplayName("findByIdAndTenantIdAndDeletedAtIsNull returns empty when attachment is deleted")
    void findByIdAndTenantIdExcludesDeleted() {
        UUID tenantId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();
        Attachment attachment = Attachment.builder()
            .tenantId(tenantId)
            .ticketId(ticketId)
            .uploadedBy(uploaderId)
            .uploaderRole("CLIENT")
            .fileName("doc.pdf")
            .minioKey("key")
            .mimeType("application/pdf")
            .fileSize(1024L)
            .visibleTo("ALL")
            .deletedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();
        entityManager.persist(attachment);
        entityManager.flush();
        UUID id = attachment.getId();

        Optional<Attachment> result = attachmentRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByIdAndTenantId returns true when attachment exists for tenant")
    void existsByIdAndTenantId() {
        UUID tenantId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        Attachment attachment = Attachment.builder()
            .tenantId(tenantId)
            .ticketId(ticketId)
            .uploadedBy(UUID.randomUUID())
            .uploaderRole("CLIENT")
            .fileName("f.pdf")
            .minioKey("k")
            .mimeType("application/pdf")
            .fileSize(100L)
            .visibleTo("ALL")
            .createdAt(LocalDateTime.now())
            .build();
        entityManager.persist(attachment);
        entityManager.flush();

        boolean exists = attachmentRepository.existsByIdAndTenantId(attachment.getId(), tenantId);

        assertThat(exists).isTrue();
    }
}
