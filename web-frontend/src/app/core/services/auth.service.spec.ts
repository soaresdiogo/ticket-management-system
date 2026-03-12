import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let routerNavigateSpy: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    const router = TestBed.inject(Router);
    routerNavigateSpy = vi.fn().mockResolvedValue(true);
    vi.spyOn(router, 'navigate').mockImplementation(routerNavigateSpy as typeof router.navigate);
  });

  afterEach(() => httpMock.verify());

  describe('initial state', () => {
    it('should not be authenticated when no token is set', () => {
      expect(service.isAuthenticated()).toBe(false);
      expect(service.getAccessToken()).toBeNull();
    });
  });

  describe('login', () => {
    it('should POST to /auth/login with credentials', () => {
      service.login({ email: 'u@example.com', password: 'pwd' }).subscribe();
      const req = httpMock.expectOne('/auth/login');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ email: 'u@example.com', password: 'pwd' });
      req.flush({ message: 'MFA sent' });
    });
  });

  describe('verifyMfa', () => {
    it('should POST with credentials and set access token in memory on success', () => {
      service
        .verifyMfa({ email: 'u@example.com', code: '123456', includeRefreshToken: true })
        .subscribe((res) => {
          expect(res.accessToken).toBe('jwt.here');
          expect(res.expiresIn).toBe(900);
        });
      const req = httpMock.expectOne('/auth/verify-mfa');
      expect(req.request.method).toBe('POST');
      expect(req.request.withCredentials).toBe(true);
      req.flush({
        accessToken: 'jwt.here',
        tokenType: 'Bearer',
        expiresIn: 900,
        refreshTokenSet: true,
      });
      expect(service.isAuthenticated()).toBe(true);
      expect(service.getAccessToken()).toBe('jwt.here');
    });
  });

  describe('refreshSession', () => {
    it('should POST to /auth/refresh with credentials and set token on success', () => {
      service.refreshSession().subscribe((res) => {
        expect(res).not.toBeNull();
        expect(res!.accessToken).toBe('new.jwt');
      });
      const req = httpMock.expectOne('/auth/refresh');
      expect(req.request.method).toBe('POST');
      expect(req.request.withCredentials).toBe(true);
      req.flush({
        accessToken: 'new.jwt',
        tokenType: 'Bearer',
        expiresIn: 900,
        refreshTokenSet: true,
      });
      expect(service.getAccessToken()).toBe('new.jwt');
    });

    it('should return null when refresh fails', () => {
      service.refreshSession().subscribe((res) => expect(res).toBeNull());
      const req = httpMock.expectOne('/auth/refresh');
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('logout', () => {
    it('should POST to /auth/logout and clear tokens and navigate to login', async () => {
      service.logout();
      const req = httpMock.expectOne('/auth/logout');
      expect(req.request.method).toBe('POST');
      expect(req.request.withCredentials).toBe(true);
      req.flush(null);
      await new Promise((r) => setTimeout(r, 0));
      expect(routerNavigateSpy).toHaveBeenCalledWith(['/login']);
    });

    it('should clear and navigate even when logout request fails', async () => {
      service.logout();
      const req = httpMock.expectOne('/auth/logout');
      req.flush('error', { status: 500, statusText: 'Error' });
      await new Promise((r) => setTimeout(r, 0));
      expect(routerNavigateSpy).toHaveBeenCalledWith(['/login']);
    });
  });
});
