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
