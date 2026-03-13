import { Component, computed, inject, signal } from '@angular/core';
import type { OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { TranslateModule } from '@ngx-translate/core';

import { AuthService } from '../../../core/services/auth.service';
import { TicketService } from '../../../core/services/ticket.service';
import { NotificationWebSocketService } from '../../../core/services/notification-websocket.service';
import { OfficeTicketMapperService } from './services/office-ticket-mapper.service';
import type { OfficeTicketRow } from './models/office-ticket-row.model';
import {
  type OfficeTicketStatusFilter,
  OFFICE_TICKET_STATUS_FILTERS,
  OFFICE_TICKET_STATUS_OPTIONS,
} from './models/office-ticket-filter.model';
import {
  TicketAttachmentsDialogComponent,
  type TicketAttachmentsDialogData,
} from '../ticket-attachments-dialog/ticket-attachments-dialog.component';

export interface OfficeMetric {
  label: string;
  value: number;
  delta: string;
  deltaUp: boolean;
  icon: string;
  theme: 'amber' | 'red' | 'green' | 'blue';
}

export interface OfficeClientSummary {
  name: string;
  count: string;
  dotColor: string;
  badge: string;
  badgeClass: string;
}

export interface OfficeActivity {
  text: string;
  time: string;
  dotColor: string;
}

const DEFAULT_PAGE_SIZE = 10;
const PAGE_SIZE_OPTIONS = [5, 10, 20, 50];

@Component({
  selector: 'app-office-dashboard',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatMenuModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatPaginatorModule,
    TranslateModule,
  ],
  templateUrl: './office-dashboard.component.html',
  styleUrl: './office-dashboard.component.scss',
})
export class OfficeDashboardComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly ticketService = inject(TicketService);
  private readonly officeTicketMapper = inject(OfficeTicketMapperService);
  private readonly notificationWs = inject(NotificationWebSocketService);
  private readonly dialog = inject(MatDialog);

  readonly sidebarOpen = signal(false);
  readonly ticketsLoading = signal(false);
  readonly ticketsError = signal(false);
  private readonly ticketsContent = signal<OfficeTicketRow[]>([]);
  private readonly ticketsTotalElements = signal(0);
  private readonly currentPage = signal(0);
  readonly pageSize = signal(DEFAULT_PAGE_SIZE);
  private readonly statusFilter = signal<OfficeTicketStatusFilter>('');

  readonly ticketRows = computed(() => this.ticketsContent());
  readonly totalElements = computed(() => this.ticketsTotalElements());
  readonly pageIndex = computed(() => this.currentPage());
  readonly pageSizeOptions = PAGE_SIZE_OPTIONS;
  readonly statusFilters = OFFICE_TICKET_STATUS_FILTERS;
  readonly statusOptions = OFFICE_TICKET_STATUS_OPTIONS;
  readonly activeStatusFilter = computed(() => this.statusFilter());
  readonly displayedColumns = ['id', 'subject', 'client', 'status', 'assignee', 'sla', 'actions'];
  private readonly statusChangePending = signal<Set<string>>(new Set());

  readonly hasTickets = computed(() => this.ticketRows().length > 0);
  readonly isEmpty = computed(() => !this.ticketsLoading() && !this.ticketsError() && this.ticketRows().length === 0);

  ngOnInit(): void {
    this.loadTickets();
    this.notificationWs.connect();
    this.notificationWs.notifications$.subscribe(() => this.loadTickets());
  }

  loadTickets(): void {
    this.ticketsLoading.set(true);
    this.ticketsError.set(false);
    const status = this.statusFilter() || undefined;
    this.ticketService
      .getAllTickets({
        page: this.currentPage(),
        size: this.pageSize(),
        status,
      })
      .subscribe({
        next: (res) => {
          const rows = res.content.map((t) => this.officeTicketMapper.toRow(t));
          this.ticketsContent.set(rows);
          this.ticketsTotalElements.set(res.totalElements);
          this.ticketsLoading.set(false);
        },
        error: () => {
          this.ticketsError.set(true);
          this.ticketsLoading.set(false);
        },
      });
  }

  setStatusFilter(value: OfficeTicketStatusFilter): void {
    this.statusFilter.set(value);
    this.currentPage.set(0);
    this.loadTickets();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadTickets();
  }

  retryLoadTickets(): void {
    this.loadTickets();
  }

  isStatusChangePending(rawId: string): boolean {
    return this.statusChangePending().has(rawId);
  }

  changeTicketStatus(row: OfficeTicketRow, newStatus: string): void {
    if (row.statusValue === newStatus) return;
    this.statusChangePending.update((set) => new Set(set).add(row.rawId));
    this.ticketService.changeTicketStatus(row.rawId, newStatus).subscribe({
      next: () => {
        this.statusChangePending.update((set) => {
          const next = new Set(set);
          next.delete(row.rawId);
          return next;
        });
        this.loadTickets();
      },
      error: () => {
        this.statusChangePending.update((set) => {
          const next = new Set(set);
          next.delete(row.rawId);
          return next;
        });
      },
    });
  }

  openAttachmentsDialog(row: OfficeTicketRow): void {
    const data: TicketAttachmentsDialogData = {
      ticketId: row.rawId,
      ticketDisplayId: row.id,
    };
    this.dialog.open(TicketAttachmentsDialogComponent, { data, width: '420px' });
  }

  toggleSidebar(): void {
    this.sidebarOpen.update((v) => !v);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

  readonly displayName = computed(() => {
    const p = this.auth.profile();
    if (!p?.email) return 'Usuário';
    const name = p.email.split('@')[0];
    return name.split(/[._-]/).map((s: string) => s.charAt(0).toUpperCase() + s.slice(1)).join(' ');
  });

  readonly initials = computed(() => {
    const name = this.displayName();
    return name
      .split(/\s+/)
      .map((s: string) => s[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  });

  readonly metrics = signal<OfficeMetric[]>([
    { label: 'Chamados Abertos', value: 47, delta: '↑ 8 vs semana anterior', deltaUp: true, icon: 'forum', theme: 'amber' },
    { label: 'SLA em Risco', value: 5, delta: '↑ 2 precisam atenção urgente', deltaUp: false, icon: 'warning', theme: 'red' },
    { label: 'Resolvidos Hoje', value: 12, delta: '↑ 4 acima da média diária', deltaUp: true, icon: 'check_circle', theme: 'green' },
    { label: 'Clientes Ativos', value: 34, delta: '+2 novos este mês', deltaUp: true, icon: 'people', theme: 'blue' },
  ]);

  readonly clientsSummary = signal<OfficeClientSummary[]>([
    { name: 'Tech Solutions Ltda', count: '3 abertos · 1 urgente', dotColor: 'red', badge: 'ALTO', badgeClass: 'high' },
    { name: 'Construtora Nova Era', count: '2 abertos · Em andamento', dotColor: 'amber', badge: 'MED', badgeClass: 'med' },
    { name: 'Farmácias BemViver', count: '1 aberto · Aguardando', dotColor: 'teal', badge: 'LOW', badgeClass: 'low' },
    { name: 'Metalúrgica São Jorge', count: '1 aberto · Em andamento', dotColor: 'green', badge: 'LOW', badgeClass: 'low' },
  ]);

  readonly recentActivity = signal<OfficeActivity[]>([
    { text: 'Lucas R. abriu chamado urgente sobre IRPJ da Tech Solutions', time: 'há 4 minutos', dotColor: 'red' },
    { text: 'Marina A. resolveu chamado #3835 — Studio Art', time: 'há 23 minutos', dotColor: 'green' },
    { text: 'Julia M. atualizou o status do chamado #3840', time: 'há 41 minutos', dotColor: 'amber' },
    { text: 'Rafael S. adicionou comentário no chamado #3838', time: 'há 1h 12min', dotColor: 'blue' },
  ]);

  logout(): void {
    this.auth.logout();
  }

  openNewTicket(): void {
    // TODO: open create-ticket modal
  }
}
