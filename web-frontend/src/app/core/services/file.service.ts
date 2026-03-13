import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import type {
  UploadFileResponse,
  FileDownloadUrlResponse,
  FileListItem,
} from '../models/file.model';

const FILES_API = '/files';

/**
 * Service for file upload, list, and download operations.
 * Gateway forwards X-User-Id, X-Tenant-Id, X-User-Role.
 */
@Injectable({ providedIn: 'root' })
export class FileService {
  private readonly http = inject(HttpClient);

  /**
   * Uploads a file for a ticket. Uses multipart/form-data with ticketId and file.
   */
  uploadFile(ticketId: string, file: File): Observable<UploadFileResponse> {
    const formData = new FormData();
    formData.set('ticketId', ticketId);
    formData.set('file', file, file.name);
    return this.http.post<UploadFileResponse>(`${FILES_API}/upload`, formData);
  }

  /**
   * Returns a presigned URL for downloading the attachment. Opens in new tab or triggers download.
   */
  getDownloadUrl(attachmentId: string): Observable<FileDownloadUrlResponse> {
    return this.http.get<FileDownloadUrlResponse>(`${FILES_API}/${attachmentId}/download`);
  }

  /**
   * Lists attachments for a ticket. Scoped by tenant (from gateway).
   */
  listByTicket(ticketId: string): Observable<FileListItem[]> {
    return this.http
      .get<FileListItem[]>(`${FILES_API}/ticket/${ticketId}`)
      .pipe(map((list) => list ?? []));
  }

  /**
   * Fetches presigned URL and opens it in a new tab (or triggers download via link).
   */
  downloadAttachment(attachmentId: string): void {
    this.getDownloadUrl(attachmentId).subscribe({
      next: (res) => {
        window.open(res.url, '_blank', 'noopener,noreferrer');
      },
    });
  }
}
