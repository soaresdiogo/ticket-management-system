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
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { AuthService } from '../../../core/services/auth.service';
import { createTranslateServiceMock, MockTranslatePipe } from '../../../core/testing/translate.mock';
import { OfficeDashboardComponent } from './office-dashboard.component';

const translateMock = createTranslateServiceMock();

describe('OfficeDashboardComponent', () => {
  let component: OfficeDashboardComponent;
  let fixture: ComponentFixture<OfficeDashboardComponent>;
  let authMock: { profile: () => { email: string } | null; logout: () => void };

  beforeEach(async () => {
    authMock = {
      profile: () => ({ userId: '1', email: 'marina.alves@contaboard.com', role: 'ACCOUNTANT', tenantId: 't1' }),
      logout: () => {},
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

  it('should have tickets data source', () => {
    expect(component.ticketsDataSource.data.length).toBeGreaterThan(0);
  });

  it('should toggle sidebar', () => {
    expect(component.sidebarOpen()).toBe(false);
    component.toggleSidebar();
    expect(component.sidebarOpen()).toBe(true);
    component.closeSidebar();
    expect(component.sidebarOpen()).toBe(false);
  });
});
