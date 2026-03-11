package com.di2it.file_service.application.port;

import com.di2it.file_service.domain.entity.Attachment;

/**
 * Port for persisting attachment metadata. Keeps use case independent of JPA.
 */
@FunctionalInterface
public interface SaveAttachmentPort {

    /**
     * Saves the attachment entity and returns the persisted instance.
     *
     * @param attachment the attachment to save (id and createdAt may be set by persistence)
     * @return the saved attachment
     */
    Attachment save(Attachment attachment);
}
