import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog } from '@angular/material/dialog';
import { DatePipe } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';
import { TicketService } from '../../../core/services/ticket.service';
import { createTranslateServiceMock, MockTranslatePipe } from '../../../core/testing/translate.mock';
import { ClientDashboardComponent } from './client-dashboard.component';
import { StatusTrackerComponent } from './status-tracker/status-tracker.component';
import { ClientTicketRowComponent } from './client-ticket-list/client-ticket-row.component';
import type { ListTicketsResponse } from '../../../core/models/ticket.model';

const translateMock = createTranslateServiceMock();

const mockTicketsResponse: ListTicketsResponse = {
  content: [
    {
      id: '550e8400-e29b-41d4-a716-446655440000',
      tenantId: '550e8400-e29b-41d4-a716-446655440001',
      clientId: '550e8400-e29b-41d4-a716-446655440002',
      title: 'Test ticket',
      description: 'Description',
      status: 'OPEN',
      priority: 'NORMAL',
      category: 'TAX',
      createdAt: '2025-03-12T10:00:00Z',
    },
  ],
  totalElements: 1,
  totalPages: 1,
  number: 0,
  size: 20,
};

describe('ClientDashboardComponent', () => {
  let component: ClientDashboardComponent;
  let fixture: ComponentFixture<ClientDashboardComponent>;
  let authMock: { profile: () => { email: string } | null; logout: () => void };
  let ticketServiceMock: { getMyTickets: () => ReturnType<TicketService['getMyTickets']> };
  let dialogMock: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    authMock = {
      profile: () => ({ userId: '1', email: 'tech@company.com', role: 'CLIENT', tenantId: null }),
      logout: () => {},
    };
    ticketServiceMock = {
      getMyTickets: () => of(mockTicketsResponse),
    };
    dialogMock = {
      open: vi.fn().mockReturnValue({ afterClosed: () => of(null) }),
    };
    await TestBed.configureTestingModule({
      imports: [
        ClientDashboardComponent,
        RouterTestingModule,
        NoopAnimationsModule,
        TranslateModule.forChild(),
      ],
      providers: [
        DatePipe,
        { provide: AuthService, useValue: authMock },
        { provide: TranslateService, useValue: translateMock },
        { provide: TicketService, useValue: ticketServiceMock },
        { provide: MatDialog, useValue: dialogMock },
      ],
    });
    TestBed.overrideComponent(StatusTrackerComponent, {
      set: { imports: [MatCardModule, MockTranslatePipe] },
    });
    TestBed.overrideComponent(ClientTicketRowComponent, {
      set: {
        imports: [MatListModule, MatChipsModule, MatIconModule, MockTranslatePipe],
      },
    });
    TestBed.overrideComponent(ClientDashboardComponent, {
      set: {
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
          DatePipe,
          MockTranslatePipe,
          StatusTrackerComponent,
          ClientTicketRowComponent,
        ],
      },
    });
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(ClientDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display displayName from profile email', () => {
    expect(component.displayName()).toBe('Tech');
  });

  it('should show initials from displayName', () => {
    expect(component.initials()).toBe('T');
  });

  it('should call auth.logout when logout is invoked', () => {
    const spy = vi.spyOn(authMock, 'logout');
    component.logout();
    expect(spy).toHaveBeenCalled();
  });

  it('should have metrics defined', () => {
    expect(component.metrics().length).toBe(4);
    expect(component.metrics()[0].label).toBe('Em Aberto');
  });

  it('should load tickets on init and set openTicketsCount from response', async () => {
    await Promise.resolve();
    fixture.detectChanges();
    expect(component.openTicketsCount()).toBe(1);
    expect(component.ticketListDisplay().length).toBe(1);
    expect(component.ticketListDisplay()[0].title).toBe('Test ticket');
  });

  it('should call retryLoadTickets and reload from TicketService', () => {
    const getMyTicketsSpy = vi.spyOn(ticketServiceMock, 'getMyTickets').mockReturnValue(of(mockTicketsResponse));
    component.retryLoadTickets();
    expect(getMyTicketsSpy).toHaveBeenCalled();
  });

  it('openNewTicket should open CreateTicketDialog and refresh tickets when dialog closes with result', () => {
    const getMyTicketsSpy = vi.spyOn(ticketServiceMock, 'getMyTickets').mockReturnValue(of(mockTicketsResponse));
    dialogMock.open.mockReturnValue({ afterClosed: () => of({ id: 'new-ticket' }) });

    component.openNewTicket();

    expect(dialogMock.open).toHaveBeenCalled();
    expect(getMyTicketsSpy).toHaveBeenCalledTimes(1);
  });

  it('openNewTicket should not refresh when dialog closes with null', () => {
    dialogMock.open.mockReturnValue({ afterClosed: () => of(null) });
    const getMyTicketsSpy = vi.spyOn(ticketServiceMock, 'getMyTickets');

    component.openNewTicket();

    expect(dialogMock.open).toHaveBeenCalled();
    expect(getMyTicketsSpy).toHaveBeenCalledTimes(0);
  });
});
