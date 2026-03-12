import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import type { ListTicketsResponse } from '../models/ticket.model';

const TICKETS_API = '/tickets';

export interface ListTicketsParams {
  page?: number;
  size?: number;
}

/**
 * Service for ticket API operations.
 * Client uses GET /tickets (gateway forwards X-User-Id from JWT).
 */
@Injectable({ providedIn: 'root' })
export class TicketService {
  private readonly http = inject(HttpClient);

  /**
   * Fetches paginated list of tickets for the current user (client).
   * Requires authenticated request (JWT); gateway adds X-User-Id.
   */
  getMyTickets(params: ListTicketsParams = {}): Observable<ListTicketsResponse> {
    const { page = 0, size = 20 } = params;
    const httpParams = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<ListTicketsResponse>(TICKETS_API, { params: httpParams });
  }
}
