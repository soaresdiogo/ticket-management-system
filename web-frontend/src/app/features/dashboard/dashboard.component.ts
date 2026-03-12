import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [MatButtonModule],
  template: `
    <div class="dashboard-placeholder">
      <h1>Welcome</h1>
      <p>You are signed in.</p>
      <button mat-flat-button color="primary" (click)="auth.logout()">
        Sign out
      </button>
    </div>
  `,
  styles: [
    `
      .dashboard-placeholder {
        min-height: 100vh;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 16px;
        padding: 24px;
        background: var(--bg);
      }
      .dashboard-placeholder h1 {
        font-size: 24px;
        color: var(--text-primary);
        margin: 0;
      }
      .dashboard-placeholder p {
        color: var(--text-secondary);
        margin: 0;
      }
    `,
  ],
})
export class DashboardComponent {
  constructor(protected readonly auth: AuthService) {}
}
