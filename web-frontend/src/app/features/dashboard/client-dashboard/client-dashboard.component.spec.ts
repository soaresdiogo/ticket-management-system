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
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { AuthService } from '../../../core/services/auth.service';
import { createTranslateServiceMock, MockTranslatePipe } from '../../../core/testing/translate.mock';
import { ClientDashboardComponent } from './client-dashboard.component';
import { StatusTrackerComponent } from './status-tracker/status-tracker.component';

const translateMock = createTranslateServiceMock();

describe('ClientDashboardComponent', () => {
  let component: ClientDashboardComponent;
  let fixture: ComponentFixture<ClientDashboardComponent>;
  let authMock: { profile: () => { email: string } | null; logout: () => void };

  beforeEach(async () => {
    authMock = {
      profile: () => ({ userId: '1', email: 'tech@company.com', role: 'CLIENT', tenantId: null }),
      logout: () => {},
    };
    await TestBed.configureTestingModule({
      imports: [
        ClientDashboardComponent,
        RouterTestingModule,
        NoopAnimationsModule,
        TranslateModule.forChild(),
      ],
      providers: [
        { provide: AuthService, useValue: authMock },
        { provide: TranslateService, useValue: translateMock },
      ],
    });
    TestBed.overrideComponent(StatusTrackerComponent, {
      set: { imports: [MatCardModule, MockTranslatePipe] },
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
          MockTranslatePipe,
          StatusTrackerComponent,
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

  it('should have openTicketsCount', () => {
    expect(component.openTicketsCount()).toBe(3);
  });

  it('should have recent tickets', () => {
    expect(component.recentTickets().length).toBeGreaterThan(0);
  });
});
