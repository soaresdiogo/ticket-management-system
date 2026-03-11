package com.di2it.file_service.domain.repository;

import com.di2it.file_service.domain.entity.Attachment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    List<Attachment> findByTicketIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID ticketId);

    List<Attachment> findByTicketIdAndTenantIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID ticketId, UUID tenantId);

    Optional<Attachment> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
