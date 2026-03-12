import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { TicketService } from './ticket.service';
import type {
  ListTicketsResponse,
  CreateTicketRequest,
  CreateTicketResponse,
} from '../models/ticket.model';

describe('TicketService', () => {
  let service: TicketService;
  let httpMock: HttpTestingController;

  const mockResponse: ListTicketsResponse = {
    content: [
      {
        id: '550e8400-e29b-41d4-a716-446655440000',
        tenantId: '550e8400-e29b-41d4-a716-446655440001',
        clientId: '550e8400-e29b-41d4-a716-446655440002',
        title: 'Test ticket',
        description: 'Description',
        status: 'OPEN',
        priority: 'NORMAL',
        category: 'TAX',
        createdAt: '2025-03-12T10:00:00Z',
      },
    ],
    totalElements: 1,
    totalPages: 1,
    number: 0,
    size: 20,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TicketService],
    });
    service = TestBed.inject(TicketService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getMyTickets should call GET /tickets with default page and size', () => {
    service.getMyTickets().subscribe((res) => {
      expect(res).toEqual(mockResponse);
      expect(res.content).toHaveLength(1);
      expect(res.content[0].status).toBe('OPEN');
    });

    const req = httpMock.expectOne((r) => r.url === '/tickets' && r.method === 'GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('20');
    req.flush(mockResponse);
  });

  it('getMyTickets should pass custom page and size', () => {
    service.getMyTickets({ page: 1, size: 10 }).subscribe();

    const req = httpMock.expectOne((r) => r.url === '/tickets' && r.method === 'GET');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('10');
    req.flush(mockResponse);
  });

  it('createTicket should POST to /tickets with request body', () => {
    const createRequest: CreateTicketRequest = {
      title: 'New ticket',
      description: 'Description',
      priority: 'NORMAL',
      category: 'Billing',
    };
    const createResponse: CreateTicketResponse = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      tenantId: '550e8400-e29b-41d4-a716-446655440001',
      clientId: '550e8400-e29b-41d4-a716-446655440002',
      title: createRequest.title,
      description: createRequest.description,
      status: 'OPEN',
      priority: createRequest.priority ?? 'NORMAL',
      category: createRequest.category ?? null,
      createdAt: '2025-03-12T10:00:00Z',
    };

    service.createTicket(createRequest).subscribe((res) => {
      expect(res).toEqual(createResponse);
      expect(res.id).toBe(createResponse.id);
    });

    const req = httpMock.expectOne((r) => r.url === '/tickets' && r.method === 'POST');
    expect(req.request.body).toEqual(createRequest);
    req.flush(createResponse);
  });
});
