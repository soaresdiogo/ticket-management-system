import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { AuthService } from '../services/auth.service';
import { dashboardRedirectGuard } from './dashboard-redirect.guard';

describe('dashboardRedirectGuard', () => {
  let authMock: { isAuthenticated: () => boolean; profile: () => { role: string } | null };
  let router: Router;

  beforeEach(() => {
    authMock = {
      isAuthenticated: () => true,
      profile: () => ({ userId: '1', email: 'u@x.com', role: 'CLIENT', tenantId: null }),
    };
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authMock },
        {
          provide: Router,
          useValue: {
            createUrlTree: (commands: unknown[]) => ({ redirect: true, commands }) as unknown as ReturnType<Router['createUrlTree']>,
          },
        },
      ],
    });
    router = TestBed.inject(Router);
  });

  it('should redirect CLIENT to dashboard/client', () => {
    authMock.profile = () => ({ userId: '1', email: 'u@x.com', role: 'CLIENT', tenantId: null });
    const tree = TestBed.runInInjectionContext(() => dashboardRedirectGuard(null!, null!)) as unknown as { commands: unknown[] };
    expect(tree).toBeDefined();
    expect(tree.commands).toEqual(['/dashboard', 'client']);
  });

  it('should redirect ACCOUNTANT to dashboard/office', () => {
    authMock.profile = () => ({ userId: '1', email: 'u@x.com', role: 'ACCOUNTANT', tenantId: 't1' });
    const tree = TestBed.runInInjectionContext(() => dashboardRedirectGuard(null!, null!)) as unknown as { commands: unknown[] };
    expect(tree).toBeDefined();
    expect(tree.commands).toEqual(['/dashboard', 'office']);
  });

  it('should redirect unauthenticated user to login', () => {
    authMock.isAuthenticated = () => false;
    const tree = TestBed.runInInjectionContext(() => dashboardRedirectGuard(null!, null!)) as unknown as { commands: unknown[] };
    expect(tree).toBeDefined();
    expect(tree.commands).toEqual(['/login']);
  });

  it('should redirect to dashboard/client when profile role is not ACCOUNTANT', () => {
    authMock.profile = () => ({ userId: '1', email: 'u@x.com', role: 'USER', tenantId: null });
    const tree = TestBed.runInInjectionContext(() => dashboardRedirectGuard(null!, null!)) as unknown as { commands: unknown[] };
    expect(tree.commands).toEqual(['/dashboard', 'client']);
  });
});
