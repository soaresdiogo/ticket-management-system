import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { dashboardRedirectGuard } from './core/guards/dashboard-redirect.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent) },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard-redirect.component').then((m) => m.DashboardRedirectComponent),
    canActivate: [authGuard, dashboardRedirectGuard],
  },
  {
    path: 'dashboard/client',
    loadComponent: () =>
      import('./features/dashboard/client-dashboard/client-dashboard.component').then((m) => m.ClientDashboardComponent),
    canActivate: [authGuard],
  },
  {
    path: 'dashboard/office',
    loadComponent: () =>
      import('./features/dashboard/office-dashboard/office-dashboard.component').then((m) => m.OfficeDashboardComponent),
    canActivate: [authGuard],
  },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: 'dashboard' },
];
