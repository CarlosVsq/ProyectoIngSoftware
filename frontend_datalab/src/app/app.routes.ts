import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login').then((m) => m.LoginComponent),
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard').then((m) => m.DashboardComponent),
  },
  {
    path: 'reclutamiento',
    loadComponent: () =>
      import('./features/reclutamiento/reclutamiento').then(
        (m) => m.ReclutamientoComponent
      ),
  },
  {
    path: 'estadisticas',
    loadComponent: () =>
      import('./features/estadisticas/estadisticas').then(
        (m) => m.EstadisticasComponent
      ),
  },
  { 
    path: 'exportaciones',
    loadComponent: () =>
      import('./features/exportaciones/exportaciones').then(
        (m) => m.ExportacionesComponent
      ),
  },
  {
  path: 'auditoria',
  loadComponent: () =>
    import('./features/auditoria/auditoria').then((m) => m.AuditoriaComponent),
  },
  {
  path: 'configuracion',
  loadComponent: () =>
    import('./features/configuracion/configuracion').then((m) => m.ConfiguracionComponent),
  },
];
