import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { FileService } from './file.service';
import type { UploadFileResponse } from '../models/file.model';

describe('FileService', () => {
  let service: FileService;
  let httpMock: HttpTestingController;

  const mockUploadResponse: UploadFileResponse = {
    id: '550e8400-e29b-41d4-a716-446655440000',
    ticketId: '550e8400-e29b-41d4-a716-446655440001',
    fileName: 'doc.pdf',
    mimeType: 'application/pdf',
    fileSize: 1024,
    createdAt: '2025-03-12T10:00:00Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [FileService],
    });
    service = TestBed.inject(FileService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('uploadFile should POST multipart to /files/upload with ticketId and file', () => {
    const ticketId = '550e8400-e29b-41d4-a716-446655440001';
    const file = new File(['content'], 'doc.pdf', { type: 'application/pdf' });

    service.uploadFile(ticketId, file).subscribe((res) => {
      expect(res).toEqual(mockUploadResponse);
      expect(res.fileName).toBe('doc.pdf');
    });

    const req = httpMock.expectOne(
      (r) => r.url === '/files/upload' && r.method === 'POST'
    );
    expect(req.request.body instanceof FormData).toBe(true);
    expect(req.request.body.get('ticketId')).toBe(ticketId);
    const sentFile = req.request.body.get('file') as File;
    expect(sentFile).toBeInstanceOf(File);
    expect(sentFile.name).toBe(file.name);
    req.flush(mockUploadResponse);
  });
});
