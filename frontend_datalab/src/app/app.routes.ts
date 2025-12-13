import { Routes } from '@angular/router';
import { authGuard } from './shared/auth/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login').then((m) => m.LoginComponent),
  },
  {
    path: 'recover',
    loadComponent: () =>
      import('./features/auth/recover-password.component').then((m) => m.RecoverPasswordComponent),
  },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./features/auth/reset-password.component').then((m) => m.ResetPasswordComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register').then((m) => m.RegisterComponent),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard').then((m) => m.DashboardComponent),
  },
  {
    path: 'lista-participantes',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/participantes/lista-participantes').then((m) => m.ListaParticipantesComponent),
  },
  {
    path: 'reclutamiento',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/reclutamiento/reclutamiento').then(
        (m) => m.ReclutamientoComponent
      ),
  },
  {
    path: 'estadisticas',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/estadisticas/estadisticas').then(
        (m) => m.EstadisticasComponent
      ),
  },
  {
    path: 'exportaciones',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/exportaciones/exportaciones').then(
        (m) => m.ExportacionesComponent
      ),
  },
  {
    path: 'auditoria',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/auditoria/auditoria').then((m) => m.AuditoriaComponent),
  },
  {
    path: 'configuracion',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/configuracion/configuracion').then((m) => m.ConfiguracionComponent),
  },
];
