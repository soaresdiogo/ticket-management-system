import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { DatePipe } from '@angular/common';

import { OfficeTicketMapperService } from './office-ticket-mapper.service';
import { TicketStatusService } from '../../../../core/services/ticket-status.service';
import type { TicketListItem } from '../../../../core/models/ticket.model';

describe('OfficeTicketMapperService', () => {
  let service: OfficeTicketMapperService;
  const getDisplayMock = vi.fn();

  const mockTicket: TicketListItem = {
    id: '550e8400-e29b-41d4-a716-446655440000',
    tenantId: '550e8400-e29b-41d4-a716-446655440001',
    clientId: '550e8400-e29b-41d4-a716-446655440002',
    title: 'IRPJ — Divergência',
    description: 'Description',
    status: 'OPEN',
    priority: 'HIGH',
    category: 'Fiscal',
    createdAt: '2025-03-12T10:00:00Z',
  };

  beforeEach(() => {
    getDisplayMock.mockReturnValue({
      labelKey: 'client.ticketStatus.open',
      cssClass: 'status-open',
    });

    TestBed.configureTestingModule({
      providers: [
        OfficeTicketMapperService,
        DatePipe,
        { provide: TicketStatusService, useValue: { getDisplay: getDisplayMock } },
      ],
    });
    service = TestBed.inject(OfficeTicketMapperService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should map ticket to office row with status from TicketStatusService', () => {
    const row = service.toRow(mockTicket);

    expect(getDisplayMock).toHaveBeenCalledWith('OPEN');
    expect(row.rawId).toBe(mockTicket.id);
    expect(row.id).toMatch(/^#/);
    expect(row.subject).toBe('IRPJ — Divergência');
    expect(row.statusValue).toBe('OPEN');
    expect(row.sub).toContain('Fiscal');
    expect(row.client).toBeTruthy();
    expect(row.statusLabelKey).toBe('client.ticketStatus.open');
    expect(row.statusCssClass).toBe('status-open');
    expect(row.assignee).toBe('—');
    expect(row.sla).toBe('—');
    expect(row.priority).toBe('high');
  });

  it('should normalize HIGH priority to high', () => {
    getDisplayMock.mockReturnValue({ labelKey: 'x', cssClass: 'y' });
    const row = service.toRow({ ...mockTicket, priority: 'HIGH' });
    expect(row.priority).toBe('high');
  });

  it('should normalize LOW priority to low', () => {
    getDisplayMock.mockReturnValue({ labelKey: 'x', cssClass: 'y' });
    const row = service.toRow({ ...mockTicket, priority: 'LOW' });
    expect(row.priority).toBe('low');
  });

  it('should default priority to medium when missing', () => {
    getDisplayMock.mockReturnValue({ labelKey: 'x', cssClass: 'y' });
    const row = service.toRow({ ...mockTicket, priority: '' });
    expect(row.priority).toBe('medium');
  });
});
