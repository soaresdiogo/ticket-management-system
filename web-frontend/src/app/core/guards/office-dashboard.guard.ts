import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, Observable, of } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Single guard for office dashboard: ensures authenticated (refresh if needed) then checks role.
 * Runs auth and role check in one observable chain so the role check always runs after refresh
 * completes, avoiding the second guard running with stale state before refresh resolves.
 */
export const officeDashboardGuard: CanActivateFn = (): Observable<boolean | ReturnType<Router['createUrlTree']>> => {
  const auth = inject(AuthService);
  const router = inject(Router);

  function resolveRoute(): ReturnType<Router['createUrlTree']> | true {
    if (!auth.isAuthenticated()) return router.createUrlTree(['/login']);
    if (auth.isOfficeUser()) return true;
    return router.createUrlTree(['/dashboard']);
  }

  if (auth.isAuthenticated()) {
    return of(resolveRoute());
  }
  return auth.refreshSession().pipe(
    map((res) => (res ? resolveRoute() : router.createUrlTree(['/login'])))
  );
};
