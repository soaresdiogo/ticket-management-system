import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatMenuModule } from '@angular/material/menu';
import { TranslateModule } from '@ngx-translate/core';

import { AuthService } from '../../../core/services/auth.service';

export interface OfficeMetric {
  label: string;
  value: number;
  delta: string;
  deltaUp: boolean;
  icon: string;
  theme: 'amber' | 'red' | 'green' | 'blue';
}

export interface OfficeTicketRow {
  id: string;
  subject: string;
  sub: string;
  client: string;
  status: string;
  statusClass: string;
  assignee: string;
  assigneeInitials: string;
  sla: string;
  slaPercent: number;
  slaColor: string;
  priority: 'high' | 'medium' | 'low';
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
    TranslateModule,
  ],
  templateUrl: './office-dashboard.component.html',
  styleUrl: './office-dashboard.component.scss',
})
export class OfficeDashboardComponent {
  private readonly auth = inject(AuthService);

  readonly sidebarOpen = signal(false);

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

  readonly ticketsDataSource = new MatTableDataSource<OfficeTicketRow>([
    {
      id: '#3841',
      subject: 'IRPJ — Divergência na base de cálculo',
      sub: 'Aberto há 4h · Fiscal',
      client: 'Tech Solutions Ltda',
      status: 'Aberto',
      statusClass: 'aberto',
      assignee: 'Lucas R.',
      assigneeInitials: 'LR',
      sla: '2h restantes',
      slaPercent: 80,
      slaColor: 'red',
      priority: 'high',
    },
    {
      id: '#3840',
      subject: 'Certificado digital expirado',
      sub: 'Aberto há 6h · TI',
      client: 'Construtora Nova Era',
      status: 'Em Andamento',
      statusClass: 'andamento',
      assignee: 'Julia M.',
      assigneeInitials: 'JM',
      sla: '5h restantes',
      slaPercent: 55,
      slaColor: 'amber',
      priority: 'high',
    },
    {
      id: '#3838',
      subject: 'Configuração do sistema ERP',
      sub: 'Aberto há 1d · Sistemas',
      client: 'Farmácias BemViver',
      status: 'Aguardando',
      statusClass: 'aguardando',
      assignee: 'Rafael S.',
      assigneeInitials: 'RS',
      sla: '1d 3h restantes',
      slaPercent: 30,
      slaColor: 'teal',
      priority: 'medium',
    },
    {
      id: '#3835',
      subject: 'Dúvida sobre MEI — Faturamento',
      sub: 'Aberto há 2d · Consultoria',
      client: 'Studio Art Criativo',
      status: 'Resolvido',
      statusClass: 'resolvido',
      assignee: 'Marina A.',
      assigneeInitials: 'MA',
      sla: 'Concluído ✓',
      slaPercent: 100,
      slaColor: 'green',
      priority: 'low',
    },
    {
      id: '#3833',
      subject: 'Regularização SPED Fiscal',
      sub: 'Aberto há 3d · Fiscal',
      client: 'Metalúrgica São Jorge',
      status: 'Em Andamento',
      statusClass: 'andamento',
      assignee: 'Lucas R.',
      assigneeInitials: 'LR',
      sla: '2d restantes',
      slaPercent: 20,
      slaColor: 'green',
      priority: 'medium',
    },
  ]);

  readonly displayedColumns = ['id', 'subject', 'client', 'status', 'assignee', 'sla'];

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
