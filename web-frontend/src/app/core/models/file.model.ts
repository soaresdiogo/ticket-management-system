/**
 * API response model for file upload (POST /files/upload).
 */
export interface UploadFileResponse {
  id: string;
  ticketId: string;
  fileName: string;
  mimeType: string;
  fileSize: number;
  createdAt: string;
}

/** Response from GET /files/{id}/download (presigned URL). */
export interface FileDownloadUrlResponse {
  url: string;
  expiresInSeconds: number;
  fileName: string;
}

/** Item from GET /files/ticket/{ticketId} list. */
export interface FileListItem {
  id: string;
  ticketId: string;
  fileName: string;
  mimeType: string;
  fileSize: number;
  createdAt: string;
}
