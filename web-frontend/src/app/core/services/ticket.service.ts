import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import type {
  ListTicketsResponse,
  CreateTicketRequest,
  CreateTicketResponse,
} from '../models/ticket.model';

const TICKETS_API = '/tickets';

export interface ListTicketsParams {
  page?: number;
  size?: number;
}

export interface ListAllTicketsParams extends ListTicketsParams {
  /** Optional status filter: OPEN, IN_PROGRESS, RESOLVED, CLOSED, AWAITING_VALIDATION */
  status?: string;
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

  /**
   * Fetches paginated list of all tickets for the tenant (office/ACCOUNTANT role).
   * Requires authenticated request with ACCOUNTANT role; gateway forwards X-Tenant-Id.
   */
  getAllTickets(params: ListAllTicketsParams = {}): Observable<ListTicketsResponse> {
    const { page = 0, size = 20, status } = params;
    let httpParams = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    if (status) {
      httpParams = httpParams.set('status', status);
    }
    return this.http.get<ListTicketsResponse>(`${TICKETS_API}/all`, { params: httpParams });
  }

  /**
   * Creates a new ticket. Requires authenticated request (JWT); gateway adds X-User-Id and X-Tenant-Id.
   */
  createTicket(request: CreateTicketRequest): Observable<CreateTicketResponse> {
    return this.http.post<CreateTicketResponse>(TICKETS_API, request);
  }
}
