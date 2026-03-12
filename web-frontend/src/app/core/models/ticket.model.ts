/**
 * API response models for the ticket service.
 * Aligned with backend DTOs (TicketListItemResponse, ListTicketsResponse).
 */

/** Single ticket item from GET /tickets list (ISO date strings from JSON). */
export interface TicketListItem {
  id: string;
  tenantId: string;
  clientId: string;
  title: string;
  description: string;
  status: string;
  priority: string;
  category: string | null;
  createdAt: string;
}

/** Paginated response from GET /tickets. */
export interface ListTicketsResponse {
  content: TicketListItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

/** Known status values from the backend (for display mapping). */
export type TicketStatusValue =
  | 'OPEN'
  | 'IN_PROGRESS'
  | 'RESOLVED'
  | 'CLOSED'
  | 'AWAITING_VALIDATION';

/** Display info for a ticket status (label key and CSS class). */
export interface TicketStatusDisplay {
  labelKey: string;
  cssClass: string;
}

/** Request body for POST /tickets (create ticket). */
export interface CreateTicketRequest {
  title: string;
  description: string;
  priority?: string;
  category?: string | null;
}

/** Response from POST /tickets. */
export interface CreateTicketResponse {
  id: string;
  tenantId: string;
  clientId: string;
  title: string;
  description: string;
  status: string;
  priority: string;
  category: string | null;
  createdAt: string;
}
