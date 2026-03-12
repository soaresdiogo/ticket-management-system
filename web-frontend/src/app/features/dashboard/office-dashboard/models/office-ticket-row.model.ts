/**
 * Display model for a single row in the office dashboard ticket table.
 * Mapped from API TicketListItem; assignee/SLA are placeholders until backend supports them.
 */
export interface OfficeTicketRow {
  id: string;
  subject: string;
  sub: string;
  client: string;
  statusLabelKey: string;
  statusCssClass: string;
  assignee: string;
  assigneeInitials: string;
  sla: string;
  slaPercent: number;
  slaColor: string;
  priority: 'high' | 'medium' | 'low';
}
