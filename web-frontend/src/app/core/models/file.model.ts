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
