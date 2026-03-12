import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Redirects /dashboard to the role-appropriate dashboard.
 * CLIENT -> dashboard/client, ACCOUNTANT (office) -> dashboard/office.
 * Other roles default to dashboard/client.
 */
export const dashboardRedirectGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }
  const profile = auth.profile();
  const target =
    profile?.role === 'ACCOUNTANT'
      ? ['/dashboard', 'office']
      : ['/dashboard', 'client'];
  return router.createUrlTree(target);
};
