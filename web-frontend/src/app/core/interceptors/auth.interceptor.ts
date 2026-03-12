import type { HttpErrorResponse } from '@angular/common/http';
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

const AUTH_PATHS = ['/auth/login', '/auth/verify-mfa', '/auth/refresh', '/auth/logout'];

function isAuthRequest(url: string): boolean {
  return AUTH_PATHS.some((path) => url.includes(path));
}

/**
 * Adds Bearer token to outgoing requests when available (skips auth endpoints).
 * On 401, attempts one token refresh (using HttpOnly cookie) and retries the request.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getAccessToken();
  const shouldAttachToken = token && !isAuthRequest(req.url);

  const reqWithAuth = shouldAttachToken
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(reqWithAuth).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status !== 401 || isAuthRequest(req.url)) return throwError(() => err);

      return auth.refreshSession().pipe(
        switchMap((refreshRes) => {
          const newToken = refreshRes ? auth.getAccessToken() : null;
          if (!newToken) return throwError(() => err);
          return next(
            req.clone({ setHeaders: { Authorization: `Bearer ${newToken}` } })
          );
        }),
        catchError(() => throwError(() => err))
      );
    })
  );
};
