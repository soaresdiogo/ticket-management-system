/**
 * Status filter for office ticket list.
 * Empty string means "All" (no status filter).
 */
export type OfficeTicketStatusFilter = '' | 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'AWAITING_VALIDATION';

export const OFFICE_TICKET_STATUS_FILTERS: { value: OfficeTicketStatusFilter; labelKey: string }[] = [
  { value: '', labelKey: 'office.all' },
  { value: 'OPEN', labelKey: 'office.open' },
  { value: 'IN_PROGRESS', labelKey: 'office.inProgress' },
  { value: 'RESOLVED', labelKey: 'office.resolved' },
];

/** All status values that can be set when changing a ticket status (for dropdown). */
export const OFFICE_TICKET_STATUS_OPTIONS: { value: TicketStatusChangeValue; labelKey: string }[] = [
  { value: 'OPEN', labelKey: 'office.open' },
  { value: 'IN_PROGRESS', labelKey: 'office.inProgress' },
  { value: 'RESOLVED', labelKey: 'office.resolved' },
  { value: 'CLOSED', labelKey: 'office.closed' },
  { value: 'AWAITING_VALIDATION', labelKey: 'office.awaitingValidation' },
];

export type TicketStatusChangeValue =
  | 'OPEN'
  | 'IN_PROGRESS'
  | 'RESOLVED'
  | 'CLOSED'
  | 'AWAITING_VALIDATION';
