import { TestBed } from '@angular/core/testing';
import {
  HttpRequest,
  HttpResponse,
  HttpErrorResponse,
  HttpHandlerFn,
} from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';

describe('authInterceptor', () => {
  let mockAuth: {
    getAccessToken: () => string | null;
    refreshSession: () => ReturnType<AuthService['refreshSession']>;
  };
  let nextCalls: HttpRequest<unknown>[];
  let nextReturn: ReturnType<HttpHandlerFn>;
  let nextReturnSecond: ReturnType<HttpHandlerFn> | null = null;

  beforeEach(() => {
    mockAuth = {
      getAccessToken: () => null,
      refreshSession: () => of(null),
    };
    nextCalls = [];
    nextReturn = of(new HttpResponse({ body: 'ok' }));
    nextReturnSecond = null;
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: mockAuth }],
    });
  });

  function nextHandler(req: HttpRequest<unknown>): ReturnType<HttpHandlerFn> {
    nextCalls.push(req);
    if (nextCalls.length === 2 && nextReturnSecond !== null) return nextReturnSecond;
    return nextReturn;
  }

  it('should not add Authorization header when no token', () => {
    const req = new HttpRequest('GET', '/api/tickets');
    TestBed.runInInjectionContext(() => {
      authInterceptor(req, nextHandler).subscribe();
    });
    expect(nextCalls).toHaveLength(1);
    expect(nextCalls[0]).toBe(req);
  });

  it('should add Bearer token when token is available and URL is not auth', () => {
    mockAuth.getAccessToken = () => 'token.here';
    const req = new HttpRequest('GET', '/api/tickets');
    TestBed.runInInjectionContext(() => {
      authInterceptor(req, nextHandler).subscribe();
    });
    expect(nextCalls).toHaveLength(1);
    expect(nextCalls[0].headers.get('Authorization')).toBe('Bearer token.here');
  });

  it('should not add Authorization for /auth/refresh', () => {
    mockAuth.getAccessToken = () => 'token.here';
    const req = new HttpRequest('POST', '/auth/refresh', {});
    TestBed.runInInjectionContext(() => {
      authInterceptor(req, nextHandler).subscribe();
    });
    expect(nextCalls).toHaveLength(1);
    expect(nextCalls[0]).toBe(req);
  });

  it('should not add Authorization for /auth/login', () => {
    mockAuth.getAccessToken = () => 'token.here';
    const req = new HttpRequest('POST', '/auth/login', { email: 'a@b.com', password: 'p' });
    TestBed.runInInjectionContext(() => {
      authInterceptor(req, nextHandler).subscribe();
    });
    expect(nextCalls).toHaveLength(1);
    expect(nextCalls[0]).toBe(req);
  });

  it('on 401 should try refresh and retry with new token', async () => {
    let getAccessTokenCallCount = 0;
    mockAuth.getAccessToken = () => (getAccessTokenCallCount++ === 0 ? null : 'new.token');
    mockAuth.refreshSession = () =>
      of({
        accessToken: 'new.token',
        tokenType: 'Bearer',
        expiresIn: 900,
        refreshTokenSet: true,
      });
    const req = new HttpRequest('GET', '/api/tickets');
    nextReturn = throwError(() => new HttpErrorResponse({ status: 401, url: '/api/tickets' }));
    nextReturnSecond = of(new HttpResponse({ body: 'ok' }));
    await new Promise<void>((resolve, reject) => {
      TestBed.runInInjectionContext(() => {
        authInterceptor(req, nextHandler).subscribe({
          next: () => {
            expect(nextCalls).toHaveLength(2);
            expect(nextCalls[1].headers.get('Authorization')).toBe('Bearer new.token');
            resolve();
          },
          error: () => reject(new Error('expected success after refresh')),
        });
      });
    });
  });
});
