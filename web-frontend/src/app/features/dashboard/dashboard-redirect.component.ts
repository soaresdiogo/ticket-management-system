import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

/**
 * Placeholder for /dashboard route. The dashboardRedirectGuard redirects
 * to /dashboard/client or /dashboard/office before this component loads.
 * Fallback redirect in case guard is bypassed.
 */
@Component({
  selector: 'app-dashboard-redirect',
  standalone: true,
  template: `<div class="redirect">Redirecionando...</div>`,
  styles: [
    `
      .redirect {
        padding: 2rem;
        text-align: center;
        color: var(--text-secondary, #4a6fa5);
      }
    `,
  ],
})
export class DashboardRedirectComponent {
  constructor(
    private readonly auth: AuthService,
    private readonly router: Router
  ) {
    const isOfficeRole = this.auth.isOfficeUser();
    this.router.navigate(isOfficeRole ? ['/dashboard', 'office'] : ['/dashboard', 'client']);
  }
}
