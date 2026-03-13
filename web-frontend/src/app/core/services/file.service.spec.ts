import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { FileService } from './file.service';
import type {
  UploadFileResponse,
  FileDownloadUrlResponse,
  FileListItem,
} from '../models/file.model';

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

  it('getDownloadUrl should GET /files/{id}/download', () => {
    const attachmentId = '550e8400-e29b-41d4-a716-446655440000';
    const mockDownload: FileDownloadUrlResponse = {
      url: 'https://minio.example.com/presigned',
      expiresInSeconds: 900,
      fileName: 'doc.pdf',
    };

    service.getDownloadUrl(attachmentId).subscribe((res) => {
      expect(res).toEqual(mockDownload);
      expect(res.fileName).toBe('doc.pdf');
    });

    const req = httpMock.expectOne(
      (r) => r.url === `/files/${attachmentId}/download` && r.method === 'GET'
    );
    req.flush(mockDownload);
  });

  it('listByTicket should GET /files/ticket/{ticketId}', () => {
    const ticketId = '550e8400-e29b-41d4-a716-446655440001';
    const mockList: FileListItem[] = [
      {
        id: '550e8400-e29b-41d4-a716-446655440000',
        ticketId,
        fileName: 'doc.pdf',
        mimeType: 'application/pdf',
        fileSize: 1024,
        createdAt: '2025-03-12T10:00:00Z',
      },
    ];

    service.listByTicket(ticketId).subscribe((res) => {
      expect(res).toEqual(mockList);
      expect(res).toHaveLength(1);
    });

    const req = httpMock.expectOne(
      (r) => r.url === `/files/ticket/${ticketId}` && r.method === 'GET'
    );
    req.flush(mockList);
  });

  it('listByTicket should return empty array when response is null', () => {
    const ticketId = '550e8400-e29b-41d4-a716-446655440001';

    service.listByTicket(ticketId).subscribe((res) => {
      expect(res).toEqual([]);
    });

    const req = httpMock.expectOne(
      (r) => r.url === `/files/ticket/${ticketId}` && r.method === 'GET'
    );
    req.flush(null);
  });

  it('downloadAttachment should call getDownloadUrl and open window', () => {
    const attachmentId = '550e8400-e29b-41d4-a716-446655440000';
    const mockDownload: FileDownloadUrlResponse = {
      url: 'https://minio.example.com/presigned',
      expiresInSeconds: 900,
      fileName: 'doc.pdf',
    };
    const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null);

    service.downloadAttachment(attachmentId);

    const req = httpMock.expectOne(
      (r) => r.url === `/files/${attachmentId}/download` && r.method === 'GET'
    );
    req.flush(mockDownload);

    expect(openSpy).toHaveBeenCalledWith(mockDownload.url, '_blank', 'noopener,noreferrer');
    openSpy.mockRestore();
  });
});
