package com.di2it.file_service.web;

import com.di2it.file_service.application.usecase.UploadFileCommand;
import com.di2it.file_service.application.usecase.UploadFileUseCase;
import com.di2it.file_service.domain.entity.Attachment;
import com.di2it.file_service.web.dto.UploadFileResponse;
import com.di2it.file_service.web.mapper.UploadFileResponseMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * REST controller for file upload. Expects X-User-Id, X-Tenant-Id, X-User-Role from the API Gateway.
 */
@Tag(name = "Files", description = "Upload and manage ticket attachments (MinIO)")
@RestController
@RequestMapping("/files")
public class FileController {

    private static final String API_RESPONSE_UNAUTHORIZED = "401";
    private static final String SECURITY_SCHEME_BEARER_JWT = "bearer-jwt";

    private final UploadFileUseCase uploadFileUseCase;

    public FileController(UploadFileUseCase uploadFileUseCase) {
        this.uploadFileUseCase = uploadFileUseCase;
    }

    /**
     * Upload a file for a ticket. File is stored in MinIO and metadata in Postgres; publishes ticket.document.uploaded.
     */
    @Operation(
        summary = "Upload file",
        description = "Multipart upload to MinIO, saves metadata in Postgres, publishes Kafka event "
            + "ticket.document.uploaded. Requires X-User-Id, X-Tenant-Id, X-User-Role."
    )
    @ApiResponse(responseCode = "201", description = "File uploaded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid file (size, type) or missing ticketId/file")
    @ApiResponse(responseCode = API_RESPONSE_UNAUTHORIZED, description = "Missing or invalid user context")
    @SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadFileResponse> upload(
        @RequestHeader(WebConstants.HEADER_USER_ID) UUID uploadedBy,
        @RequestHeader(WebConstants.HEADER_TENANT_ID) UUID tenantId,
        @RequestHeader(WebConstants.HEADER_USER_ROLE) String uploaderRole,
        @Parameter(description = "Ticket ID this file belongs to", required = true)
        @RequestParam UUID ticketId,
        @Parameter(description = "File to upload", required = true)
        @RequestParam("file") MultipartFile file,
        @Parameter(description = "Visibility: ALL, CLIENT_ONLY, etc. Default ALL")
        @RequestParam(required = false) String visibleTo
    ) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        UploadFileCommand command = UploadFileCommand.builder()
            .ticketId(ticketId)
            .tenantId(tenantId)
            .uploadedBy(uploadedBy)
            .uploaderRole(uploaderRole != null ? uploaderRole : "USER")
            .fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file")
            .contentType(file.getContentType())
            .fileSize(file.getSize())
            .inputStream(file.getInputStream())
            .visibleTo(visibleTo)
            .build();

        Attachment attachment = uploadFileUseCase.upload(command);
        UploadFileResponse response = UploadFileResponseMapper.toResponse(attachment);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
