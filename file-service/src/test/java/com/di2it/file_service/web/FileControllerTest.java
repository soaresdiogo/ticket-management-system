package com.di2it.file_service.web;

import com.di2it.file_service.application.usecase.GetFileDownloadUrlUseCase;
import com.di2it.file_service.application.usecase.ListFilesByTicketUseCase;
import com.di2it.file_service.application.usecase.UploadFileUseCase;
import com.di2it.file_service.config.SecurityConfig;
import com.di2it.file_service.domain.entity.Attachment;
import com.di2it.file_service.domain.exception.AttachmentNotFoundException;
import com.di2it.file_service.domain.exception.InvalidFileUploadException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UploadFileUseCase uploadFileUseCase;

    @MockitoBean
    private GetFileDownloadUrlUseCase getFileDownloadUrlUseCase;

    @MockitoBean
    private ListFilesByTicketUseCase listFilesByTicketUseCase;

    @Test
    @DisplayName("POST /files/upload returns 201 and attachment metadata when upload succeeds")
    void upload_returns201AndBody() throws Exception {
        UUID attachmentId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Attachment attachment = Attachment.builder()
            .id(attachmentId)
            .ticketId(ticketId)
            .tenantId(tenantId)
            .uploadedBy(userId)
            .uploaderRole("USER")
            .fileName("doc.pdf")
            .minioKey("key")
            .mimeType("application/pdf")
            .fileSize(100L)
            .visibleTo("ALL")
            .createdAt(LocalDateTime.now())
            .build();
        when(uploadFileUseCase.upload(any())).thenReturn(attachment);

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "doc.pdf",
            "application/pdf",
            new byte[100]
        );

        mockMvc.perform(
            multipart("/files/upload")
                .file(file)
                .param("ticketId", ticketId.toString())
                .header(WebConstants.HEADER_USER_ID, userId.toString())
                .header(WebConstants.HEADER_TENANT_ID, tenantId.toString())
                .header(WebConstants.HEADER_USER_ROLE, "USER")
        )
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(attachmentId.toString()))
            .andExpect(jsonPath("$.ticketId").value(ticketId.toString()))
            .andExpect(jsonPath("$.fileName").value("doc.pdf"))
            .andExpect(jsonPath("$.mimeType").value("application/pdf"))
            .andExpect(jsonPath("$.fileSize").value(100));

        verify(uploadFileUseCase).upload(any());
    }

    @Test
    @DisplayName("POST /files/upload returns 400 when file is empty")
    void upload_returns400WhenFileEmpty() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.pdf",
            "application/pdf",
            new byte[0]
        );

        mockMvc.perform(
            multipart("/files/upload")
                .file(file)
                .param("ticketId", UUID.randomUUID().toString())
                .header(WebConstants.HEADER_USER_ID, UUID.randomUUID().toString())
                .header(WebConstants.HEADER_TENANT_ID, UUID.randomUUID().toString())
                .header(WebConstants.HEADER_USER_ROLE, "USER")
        )
            .andExpect(status().isBadRequest());

        verify(uploadFileUseCase, never()).upload(any());
    }

    @Test
    @DisplayName("POST /files/upload returns 400 when use case throws InvalidFileUploadException")
    void upload_returns400WhenValidationFails() throws Exception {
        when(uploadFileUseCase.upload(any())).thenThrow(
            new InvalidFileUploadException("File size exceeds maximum")
        );

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "big.pdf",
            "application/pdf",
            new byte[200]
        );

        mockMvc.perform(
            multipart("/files/upload")
                .file(file)
                .param("ticketId", UUID.randomUUID().toString())
                .header(WebConstants.HEADER_USER_ID, UUID.randomUUID().toString())
                .header(WebConstants.HEADER_TENANT_ID, UUID.randomUUID().toString())
                .header(WebConstants.HEADER_USER_ROLE, "USER")
        )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("File size exceeds maximum"));
    }

    @Test
    @DisplayName("GET /files/{id}/download returns 200 and presigned URL when found")
    void getDownloadUrl_returns200AndBody() throws Exception {
        UUID attachmentId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GetFileDownloadUrlUseCase.FileDownloadResult result =
            new GetFileDownloadUrlUseCase.FileDownloadResult("https://minio.example.com/presigned", 900, "doc.pdf");
        when(getFileDownloadUrlUseCase.getDownloadUrl(eq(attachmentId), eq(tenantId), eq(userId), any()))
            .thenReturn(result);

        mockMvc.perform(
            get("/files/{id}/download", attachmentId)
                .header(WebConstants.HEADER_USER_ID, userId.toString())
                .header(WebConstants.HEADER_TENANT_ID, tenantId.toString())
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.url").value("https://minio.example.com/presigned"))
            .andExpect(jsonPath("$.expiresInSeconds").value(900))
            .andExpect(jsonPath("$.fileName").value("doc.pdf"));

        verify(getFileDownloadUrlUseCase).getDownloadUrl(eq(attachmentId), eq(tenantId), eq(userId), any());
    }

    @Test
    @DisplayName("GET /files/{id}/download returns 404 when attachment not found")
    void getDownloadUrl_returns404WhenNotFound() throws Exception {
        UUID attachmentId = UUID.randomUUID();
        when(getFileDownloadUrlUseCase.getDownloadUrl(any(), any(), any(), any()))
            .thenThrow(new AttachmentNotFoundException("Attachment not found: " + attachmentId));

        mockMvc.perform(
            get("/files/{id}/download", attachmentId)
                .header(WebConstants.HEADER_USER_ID, UUID.randomUUID().toString())
                .header(WebConstants.HEADER_TENANT_ID, UUID.randomUUID().toString())
        )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Attachment not found: " + attachmentId));
    }

    @Test
    @DisplayName("GET /files/ticket/{ticketId} returns 200 and list of files")
    void listByTicket_returns200AndList() throws Exception {
        UUID ticketId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Attachment a = Attachment.builder()
            .id(UUID.randomUUID())
            .ticketId(ticketId)
            .tenantId(tenantId)
            .fileName("a.pdf")
            .mimeType("application/pdf")
            .fileSize(100L)
            .createdAt(LocalDateTime.now())
            .build();
        when(listFilesByTicketUseCase.listByTicket(ticketId, tenantId)).thenReturn(List.of(a));

        mockMvc.perform(
            get("/files/ticket/{ticketId}", ticketId)
                .header(WebConstants.HEADER_TENANT_ID, tenantId.toString())
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].fileName").value("a.pdf"))
            .andExpect(jsonPath("$[0].mimeType").value("application/pdf"));

        verify(listFilesByTicketUseCase).listByTicket(ticketId, tenantId);
    }
}
