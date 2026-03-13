import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Redirects /dashboard to the role-appropriate dashboard.
 * USER (office) -> dashboard/office. CLIENT and other roles -> dashboard/client.
 */
export const dashboardRedirectGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }
  const isOfficeRole = auth.isOfficeUser();
  const target = isOfficeRole ? ['/dashboard', 'office'] : ['/dashboard', 'client'];
  return router.createUrlTree(target);
};
