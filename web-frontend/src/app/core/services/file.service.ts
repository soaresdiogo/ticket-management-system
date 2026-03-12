import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import type { UploadFileResponse } from '../models/file.model';

const FILES_UPLOAD_API = '/files/upload';

/**
 * Service for file upload operations.
 * Upload requires ticketId and file; gateway forwards X-User-Id, X-Tenant-Id, X-User-Role.
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
    return this.http.post<UploadFileResponse>(FILES_UPLOAD_API, formData);
  }
}
