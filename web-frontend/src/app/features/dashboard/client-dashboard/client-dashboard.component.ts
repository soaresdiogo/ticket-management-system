import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { TranslateModule } from '@ngx-translate/core';

import { AuthService } from '../../../core/services/auth.service';
import { StatusTrackerComponent } from './status-tracker/status-tracker.component';

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
    TranslateModule,
    StatusTrackerComponent,
  ],
  templateUrl: './client-dashboard.component.html',
  styleUrl: './client-dashboard.component.scss',
})
export class ClientDashboardComponent {
  private readonly auth = inject(AuthService);
  private readonly openTickets = signal(3);
  private readonly resolvedCount = signal(28);

  readonly notificationCount = signal(3);
  readonly openTicketsCount = computed(() => this.openTickets());

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

  readonly metrics = signal<ClientMetric[]>([
    {
      label: 'Em Aberto',
      value: 3,
      sub: 'Aguardando resolução',
      icon: 'forum',
      iconClass: 'mt-icon red',
    },
    {
      label: 'Em Andamento',
      value: 2,
      sub: 'Sendo resolvidos',
      icon: 'schedule',
      iconClass: 'mt-icon amber',
    },
    {
      label: 'Resolvidos',
      value: 28,
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
  ]);

  readonly recentTickets = signal<ClientTicketSummary[]>([
    {
      id: '#3841',
      title: 'IRPJ — Divergência na base de cálculo',
      description: 'Identificamos uma inconsistência no cálculo do IRPJ referente ao 1º trimestre...',
      statusLabel: 'Aberto',
      statusClass: 'aberto',
      categoryIcon: 'description',
      categoryClass: 'fiscal',
      updatedAt: '4h atrás',
    },
    {
      id: '#3840',
      title: 'Certificado digital expirado — renovação',
      description: 'Nosso certificado A1 vence em 3 dias. Solicito apoio para renovação urgente...',
      statusLabel: 'Em Andamento',
      statusClass: 'andamento',
      categoryIcon: 'computer',
      categoryClass: 'ti',
      updatedAt: 'Ontem 14h',
    },
    {
      id: '#3833',
      title: 'Dúvida sobre classificação contábil',
      description: 'Como classificar a aquisição de um software como ativo imobilizado ou despesa?',
      statusLabel: 'Aguardando',
      statusClass: 'aguardando',
      categoryIcon: 'account_balance',
      categoryClass: 'contabil',
      updatedAt: 'Seg 09h',
    },
    {
      id: '#3820',
      title: 'Rescisão contratual — cálculo de verbas',
      description: 'Auxílio no cálculo das verbas rescisórias para funcionário com 4 anos...',
      statusLabel: 'Resolvido',
      statusClass: 'resolvido',
      categoryIcon: 'people',
      categoryClass: 'rh',
      updatedAt: 'Qui 15h',
    },
  ]);

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
