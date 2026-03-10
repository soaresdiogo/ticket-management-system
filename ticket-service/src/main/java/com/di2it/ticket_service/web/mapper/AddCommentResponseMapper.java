package com.di2it.ticket_service.web.mapper;

import com.di2it.ticket_service.domain.entity.TicketComment;
import com.di2it.ticket_service.web.dto.AddCommentResponse;

/**
 * Maps TicketComment entity to AddCommentResponse for POST /tickets/{id}/comments.
 */
public final class AddCommentResponseMapper {

    private AddCommentResponseMapper() {
    }

    public static AddCommentResponse toResponse(TicketComment comment) {
        return AddCommentResponse.builder()
            .id(comment.getId())
            .content(comment.getContent())
            .authorId(comment.getAuthorId())
            .authorRole(comment.getAuthorRole())
            .internal(comment.isInternal())
            .createdAt(comment.getCreatedAt())
            .build();
    }
}
