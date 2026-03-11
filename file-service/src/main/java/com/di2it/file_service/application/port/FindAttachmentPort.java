package com.di2it.file_service.application.port;

import com.di2it.file_service.domain.entity.Attachment;

import java.util.Optional;
import java.util.UUID;

/**
 * Port for loading a single attachment by id and tenant (for download / authorization).
 */
@FunctionalInterface
public interface FindAttachmentPort {

    /**
     * Finds an attachment by id and tenant id, only if not soft-deleted.
     *
     * @param id       attachment id
     * @param tenantId tenant id (from JWT/header)
     * @return optional attachment, empty if not found or wrong tenant
     */
    Optional<Attachment> findByIdAndTenantId(UUID id, UUID tenantId);
}
