package com.di2it.file_service.web;

import com.di2it.file_service.application.usecase.GetFileDownloadUrlUseCase;
import com.di2it.file_service.application.usecase.ListFilesByTicketUseCase;
import com.di2it.file_service.application.usecase.UploadFileCommand;
import com.di2it.file_service.application.usecase.UploadFileUseCase;
import com.di2it.file_service.domain.entity.Attachment;
import com.di2it.file_service.web.dto.FileDownloadUrlResponse;
import com.di2it.file_service.web.dto.FileListItemResponse;
import com.di2it.file_service.web.dto.UploadFileResponse;
import com.di2it.file_service.web.mapper.FileDownloadUrlResponseMapper;
import com.di2it.file_service.web.mapper.FileListItemResponseMapper;
import com.di2it.file_service.web.mapper.UploadFileResponseMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
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
    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    private final UploadFileUseCase uploadFileUseCase;
    private final GetFileDownloadUrlUseCase getFileDownloadUrlUseCase;
    private final ListFilesByTicketUseCase listFilesByTicketUseCase;

    public FileController(
        UploadFileUseCase uploadFileUseCase,
        GetFileDownloadUrlUseCase getFileDownloadUrlUseCase,
        ListFilesByTicketUseCase listFilesByTicketUseCase
    ) {
        this.uploadFileUseCase = uploadFileUseCase;
        this.getFileDownloadUrlUseCase = getFileDownloadUrlUseCase;
        this.listFilesByTicketUseCase = listFilesByTicketUseCase;
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

    /**
     * Returns a presigned MinIO URL to download the file (e.g. 15 min TTL). Logs the download for audit.
     */
    @Operation(
        summary = "Get download URL",
        description = "Returns a presigned URL for the attachment (limited TTL, e.g. 15 min). "
            + "Requires X-User-Id, X-Tenant-Id. Attachment must belong to the tenant."
    )
    @ApiResponse(responseCode = "200", description = "Presigned URL returned")
    @ApiResponse(responseCode = "404", description = "Attachment not found or not in tenant")
    @ApiResponse(responseCode = API_RESPONSE_UNAUTHORIZED, description = "Missing or invalid user context")
    @SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
    @GetMapping("/{id}/download")
    public ResponseEntity<FileDownloadUrlResponse> getDownloadUrl(
        @Parameter(description = "Attachment ID") @PathVariable UUID id,
        @RequestHeader(WebConstants.HEADER_USER_ID) UUID userId,
        @RequestHeader(WebConstants.HEADER_TENANT_ID) UUID tenantId,
        HttpServletRequest request
    ) {
        Optional<String> ipAddress = Optional.ofNullable(request.getHeader(HEADER_X_FORWARDED_FOR))
            .filter(s -> !s.isBlank())
            .or(() -> Optional.ofNullable(request.getRemoteAddr()));
        GetFileDownloadUrlUseCase.FileDownloadResult result =
            getFileDownloadUrlUseCase.getDownloadUrl(id, tenantId, userId, ipAddress);
        FileDownloadUrlResponse response = FileDownloadUrlResponseMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all files (attachments) for a ticket, scoped by tenant.
     */
    @Operation(
        summary = "List files by ticket",
        description = "Returns attachments for the given ticket. Requires X-Tenant-Id. Results scoped by tenant."
    )
    @ApiResponse(responseCode = "200", description = "List of attachments")
    @ApiResponse(responseCode = API_RESPONSE_UNAUTHORIZED, description = "Missing or invalid user context")
    @SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<FileListItemResponse>> listByTicket(
        @Parameter(description = "Ticket ID") @PathVariable UUID ticketId,
        @RequestHeader(WebConstants.HEADER_TENANT_ID) UUID tenantId
    ) {
        List<Attachment> attachments = listFilesByTicketUseCase.listByTicket(ticketId, tenantId);
        List<FileListItemResponse> response = FileListItemResponseMapper.toResponseList(attachments);
        return ResponseEntity.ok(response);
    }
}
