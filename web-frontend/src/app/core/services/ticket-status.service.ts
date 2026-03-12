import { Injectable } from '@angular/core';

import type { TicketStatusDisplay, TicketStatusValue } from '../models/ticket.model';

/** Maps backend status values to display label keys and CSS classes. Single responsibility. */
const STATUS_MAP: Record<string, TicketStatusDisplay> = {
  OPEN: { labelKey: 'client.ticketStatus.open', cssClass: 'status-open' },
  IN_PROGRESS: { labelKey: 'client.ticketStatus.inProgress', cssClass: 'status-in-progress' },
  RESOLVED: { labelKey: 'client.ticketStatus.resolved', cssClass: 'status-resolved' },
  CLOSED: { labelKey: 'client.ticketStatus.closed', cssClass: 'status-closed' },
  AWAITING_VALIDATION: {
    labelKey: 'client.ticketStatus.awaitingValidation',
    cssClass: 'status-awaiting',
  },
};

@Injectable({ providedIn: 'root' })
export class TicketStatusService {
  /**
   * Returns display info for a given status code.
   * Unknown statuses get a generic display.
   */
  getDisplay(status: string | null | undefined): TicketStatusDisplay {
    if (!status) {
      return { labelKey: 'client.ticketStatus.unknown', cssClass: 'status-unknown' };
    }
    const normalized = status.trim().toUpperCase();
    return (
      STATUS_MAP[normalized] ?? {
        labelKey: 'client.ticketStatus.unknown',
        cssClass: 'status-unknown',
      }
    );
  }

  /** Returns whether the status is considered "open" (not resolved/closed). */
  isOpenStatus(status: string | null | undefined): boolean {
    const upper = (status ?? '').trim().toUpperCase();
    return upper === 'OPEN' || upper === 'IN_PROGRESS' || upper === 'AWAITING_VALIDATION';
  }
}
