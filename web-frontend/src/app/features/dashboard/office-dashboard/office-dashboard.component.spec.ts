import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginatorModule } from '@angular/material/paginator';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';
import { TicketService } from '../../../core/services/ticket.service';
import { OfficeTicketMapperService } from './services/office-ticket-mapper.service';
import { createTranslateServiceMock, MockTranslatePipe } from '../../../core/testing/translate.mock';
import { OfficeDashboardComponent } from './office-dashboard.component';
import type { ListTicketsResponse } from '../../../core/models/ticket.model';
import type { OfficeTicketRow } from './models/office-ticket-row.model';

const translateMock = createTranslateServiceMock();

const mockTicketsResponse: ListTicketsResponse = {
  content: [
    {
      id: '550e8400-e29b-41d4-a716-446655440000',
      tenantId: '550e8400-e29b-41d4-a716-446655440001',
      clientId: '550e8400-e29b-41d4-a716-446655440002',
      title: 'IRPJ — Divergência',
      description: 'Description',
      status: 'OPEN',
      priority: 'HIGH',
      category: 'Fiscal',
      createdAt: '2025-03-12T10:00:00Z',
    },
  ],
  totalElements: 1,
  totalPages: 1,
  number: 0,
  size: 10,
};

const mockRow: OfficeTicketRow = {
  id: '#550E8400',
  subject: 'IRPJ — Divergência',
  sub: 'Fiscal · 3/12/25',
  client: '550e8400…',
  statusLabelKey: 'client.ticketStatus.open',
  statusCssClass: 'status-open',
  assignee: '—',
  assigneeInitials: '—',
  sla: '—',
  slaPercent: 0,
  slaColor: 'teal',
  priority: 'high',
};

describe('OfficeDashboardComponent', () => {
  let component: OfficeDashboardComponent;
  let fixture: ComponentFixture<OfficeDashboardComponent>;
  let authMock: { profile: () => { email: string } | null; logout: () => void };
  let ticketServiceMock: { getAllTickets: () => ReturnType<TicketService['getAllTickets']> };
  let mapperMock: { toRow: (t: unknown) => OfficeTicketRow };

  beforeEach(async () => {
    authMock = {
      profile: () => ({ userId: '1', email: 'marina.alves@contaboard.com', role: 'ACCOUNTANT', tenantId: 't1' }),
      logout: () => {},
    };
    ticketServiceMock = {
      getAllTickets: vi.fn(() => of(mockTicketsResponse)),
    };
    mapperMock = {
      toRow: () => mockRow,
    };

    await TestBed.configureTestingModule({
      imports: [
        OfficeDashboardComponent,
        RouterTestingModule,
        NoopAnimationsModule,
        TranslateModule.forChild(),
      ],
      providers: [
        { provide: AuthService, useValue: authMock },
        { provide: TranslateService, useValue: translateMock },
        { provide: TicketService, useValue: ticketServiceMock },
        { provide: OfficeTicketMapperService, useValue: mapperMock },
      ],
    });
    TestBed.overrideComponent(OfficeDashboardComponent, {
      set: {
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
          MatProgressSpinnerModule,
          MatPaginatorModule,
          MockTranslatePipe,
        ],
      },
    });
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(OfficeDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display displayName from profile email', () => {
    expect(component.displayName()).toBe('Marina Alves');
  });

  it('should show initials from displayName', () => {
    expect(component.initials()).toBe('MA');
  });

  it('should call auth.logout when logout is invoked', () => {
    const spy = vi.spyOn(authMock, 'logout');
    component.logout();
    expect(spy).toHaveBeenCalled();
  });

  it('should have metrics defined', () => {
    expect(component.metrics().length).toBe(4);
    expect(component.metrics()[0].label).toBe('Chamados Abertos');
  });

  it('should load tickets on init and map to ticketRows', async () => {
    await Promise.resolve();
    fixture.detectChanges();
    expect(ticketServiceMock.getAllTickets).toHaveBeenCalled();
    expect(component.ticketRows().length).toBe(1);
    expect(component.ticketRows()[0].subject).toBe(mockRow.subject);
    expect(component.totalElements()).toBe(1);
  });

  it('should have status filter options', () => {
    expect(component.statusFilters.length).toBeGreaterThan(0);
    expect(component.activeStatusFilter()).toBe('');
  });

  it('setStatusFilter should update filter and reload tickets', () => {
    const getAllSpy = vi.spyOn(ticketServiceMock, 'getAllTickets').mockReturnValue(of(mockTicketsResponse));
    component.setStatusFilter('OPEN');
    expect(component.activeStatusFilter()).toBe('OPEN');
    expect(getAllSpy).toHaveBeenCalledWith(expect.objectContaining({ status: 'OPEN', page: 0 }));
  });

  it('retryLoadTickets should call getAllTickets', () => {
    const getAllSpy = vi.spyOn(ticketServiceMock, 'getAllTickets').mockReturnValue(of(mockTicketsResponse));
    component.retryLoadTickets();
    expect(getAllSpy).toHaveBeenCalled();
  });

  it('onPageChange should update page and reload', () => {
    const getAllSpy = vi.spyOn(ticketServiceMock, 'getAllTickets').mockReturnValue(of(mockTicketsResponse));
    component.onPageChange({ pageIndex: 1, pageSize: 20, length: 50 });
    expect(component.pageIndex()).toBe(1);
    expect(component.pageSize()).toBe(20);
    expect(getAllSpy).toHaveBeenCalledWith(
      expect.objectContaining({ page: 1, size: 20 })
    );
  });

  it('should toggle sidebar', () => {
    expect(component.sidebarOpen()).toBe(false);
    component.toggleSidebar();
    expect(component.sidebarOpen()).toBe(true);
    component.closeSidebar();
    expect(component.sidebarOpen()).toBe(false);
  });
});
