import { Component, computed, inject, signal } from '@angular/core';
import type { OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';

import { AuthService } from '../../../core/services/auth.service';
import { TicketService } from '../../../core/services/ticket.service';
import { TicketStatusService } from '../../../core/services/ticket-status.service';
import type { TicketListItem } from '../../../core/models/ticket.model';
import { StatusTrackerComponent } from './status-tracker/status-tracker.component';
import {
  ClientTicketRowComponent,
  type ClientTicketRowDisplay,
} from './client-ticket-list/client-ticket-row.component';

export interface ClientMetric {
  label: string;
  value: string | number;
  sub: string;
  icon: string;
  iconClass: string;
}

export interface ClientTicketSummary {
  id: string;
  title: string;
  description: string;
  statusLabel: string;
  statusClass: string;
  categoryIcon: string;
  categoryClass: string;
  updatedAt: string;
}

export interface ClientDocumentSummary {
  name: string;
  meta: string;
}

export interface ClientNotification {
  id: string;
  title: string;
  time: string;
  read: boolean;
  icon: string;
}

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatListModule,
    MatChipsModule,
    MatMenuModule,
    MatBadgeModule,
    MatProgressSpinnerModule,
    TranslateModule,
    DatePipe,
    StatusTrackerComponent,
    ClientTicketRowComponent,
  ],
  templateUrl: './client-dashboard.component.html',
  styleUrl: './client-dashboard.component.scss',
})
export class ClientDashboardComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly ticketService = inject(TicketService);
  private readonly ticketStatusService = inject(TicketStatusService);
  private readonly datePipe = inject(DatePipe);

  private readonly ticketsContent = signal<TicketListItem[]>([]);
  private readonly ticketsTotalElements = signal(0);
  readonly ticketsLoading = signal(true);
  readonly ticketsError = signal(false);
  private readonly openTickets = signal(0);

  readonly notificationCount = signal(3);
  readonly openTicketsCount = computed(() => this.openTickets());

  /** Tickets mapped for display (status label key, CSS class, formatted date). */
  readonly ticketListDisplay = computed<ClientTicketRowDisplay[]>(() => {
    const content = this.ticketsContent();
    return content.map((t) => this.toRowDisplay(t));
  });

  ngOnInit(): void {
    this.loadTickets();
  }

  private loadTickets(): void {
    this.ticketsLoading.set(true);
    this.ticketsError.set(false);
    this.ticketService.getMyTickets({ page: 0, size: 20 }).subscribe({
      next: (res) => {
        this.ticketsContent.set(res.content);
        this.ticketsTotalElements.set(res.totalElements);
        this.openTickets.set(
          res.content.filter((t) => this.ticketStatusService.isOpenStatus(t.status)).length
        );
        this.ticketsLoading.set(false);
      },
      error: () => {
        this.ticketsError.set(true);
        this.ticketsLoading.set(false);
      },
    });
  }

  private toRowDisplay(t: TicketListItem): ClientTicketRowDisplay {
    const display = this.ticketStatusService.getDisplay(t.status);
    const formatted = t.createdAt
      ? this.datePipe.transform(t.createdAt, 'short', undefined, 'en')
      : '';
    return {
      id: t.id,
      title: t.title,
      description: t.description,
      statusLabelKey: display.labelKey,
      statusCssClass: display.cssClass,
      createdAtFormatted: formatted ?? '',
    };
  }

  retryLoadTickets(): void {
    this.loadTickets();
  }

  readonly displayName = computed(() => {
    const p = this.auth.profile();
    if (!p?.email) return 'Cliente';
    const name = p.email.split('@')[0];
    return name.charAt(0).toUpperCase() + name.slice(1);
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

  readonly metrics = computed<ClientMetric[]>(() => {
    const open = this.openTicketsCount();
    return [
      {
        label: 'Em Aberto',
        value: open,
        sub: 'Aguardando resolução',
        icon: 'forum',
        iconClass: 'mt-icon red',
      },
      {
        label: 'Em Andamento',
        value: '-',
        sub: 'Sendo resolvidos',
        icon: 'schedule',
        iconClass: 'mt-icon amber',
      },
      {
        label: 'Resolvidos',
        value: '-',
        sub: 'Total este ano',
        icon: 'check_circle',
        iconClass: 'mt-icon green',
      },
      {
        label: 'Satisfação',
        value: '4.8/5',
        sub: 'Avaliação média',
        icon: 'sentiment_satisfied',
        iconClass: 'mt-icon blue',
      },
    ];
  });

  readonly recentDocuments = signal<ClientDocumentSummary[]>([
    { name: 'Balanço Patrimonial — Maio 2025', meta: 'PDF · 1.2 MB · Enviado hoje' },
    { name: 'DRE — 1º Trimestre 2025', meta: 'XLSX · 384 KB · Enviado ontem' },
    { name: 'Guia de Obrigações Fiscais 2025', meta: 'DOCX · 890 KB · Enviado há 5 dias' },
  ]);

  readonly notifications = signal<ClientNotification[]>([
    {
      id: '1',
      title: 'Prazo de SLA — Seu chamado #3841 tem prazo em 2 horas',
      time: 'há 12 minutos',
      read: false,
      icon: 'warning',
    },
    {
      id: '2',
      title: 'Documento disponível — Balanço Patrimonial de Maio foi enviado',
      time: 'há 1 hora',
      read: false,
      icon: 'info',
    },
    {
      id: '3',
      title: 'Atualização — Lucas R. adicionou comentário no chamado #3840',
      time: 'há 3 horas',
      read: true,
      icon: 'chat',
    },
  ]);

  logout(): void {
    this.auth.logout();
  }

  openNewTicket(): void {
    // TODO: open create-ticket dialog/modal
  }
}
