import { Injectable, inject } from '@angular/core';
import { DatePipe } from '@angular/common';

import type { TicketListItem } from '../../../../core/models/ticket.model';
import { TicketStatusService } from '../../../../core/services/ticket-status.service';
import type { OfficeTicketRow } from '../models/office-ticket-row.model';

/**
 * Maps API ticket items to office dashboard row display model.
 * Single responsibility: transformation only; no HTTP or global state.
 */
@Injectable({ providedIn: 'root' })
export class OfficeTicketMapperService {
  private readonly ticketStatusService = inject(TicketStatusService);
  private readonly datePipe = inject(DatePipe);

  toRow(item: TicketListItem): OfficeTicketRow {
    const display = this.ticketStatusService.getDisplay(item.status);
    const createdAtFormatted = item.createdAt
      ? this.datePipe.transform(item.createdAt, 'short', undefined, 'en') ?? ''
      : '';
    const sub = [item.category, createdAtFormatted].filter(Boolean).join(' · ') || '—';
    const priority = this.normalizePriority(item.priority);
    return {
      rawId: item.id,
      id: this.formatId(item.id),
      subject: item.title,
      sub,
      client: this.formatClientId(item.clientId),
      statusLabelKey: display.labelKey,
      statusCssClass: display.cssClass,
      statusValue: item.status ?? '',
      assignee: '—',
      assigneeInitials: '—',
      sla: '—',
      slaPercent: 0,
      slaColor: 'teal',
      priority,
    };
  }

  private formatId(id: string): string {
    if (!id) return '—';
    const short = id.replace(/-/g, '').slice(0, 8).toUpperCase();
    return `#${short}`;
  }

  private formatClientId(clientId: string): string {
    if (!clientId) return '—';
    return clientId.slice(0, 8) + '…';
  }

  private normalizePriority(priority: string | null | undefined): 'high' | 'medium' | 'low' {
    const p = (priority ?? '').trim().toUpperCase();
    if (p === 'URGENT' || p === 'HIGH') return 'high';
    if (p === 'LOW') return 'low';
    return 'medium';
  }
}
