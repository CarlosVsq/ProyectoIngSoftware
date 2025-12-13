import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

const TOKEN_KEY = 'datalab_access_token';
const USER_KEY = 'datalab_user';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const zone = inject(NgZone);

  // 1. OBTENER EL TOKEN DIRECTAMENTE
  // Usamos localStorage directamente para evitar la Dependencia Circular:
  // (AuthService -> HttpClient -> Interceptor -> AuthService)
  const token = localStorage.getItem(TOKEN_KEY);

  // 2. CLONAR Y PEGAR EL TOKEN
  const request = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  // 3. Pasar la petición y capturar errores de autenticación/usuario inexistente
  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      const message = (error?.error?.message || '').toString();
      const userNotFound = /usuario no encontrado/i.test(message);
      const unauthorized = error.status === 401;
      const forbidden = error.status === 403;
      const networkError = error.status === 0 && !!token; // backend caído con sesión previa

      if (userNotFound || unauthorized || forbidden || networkError) {
        // Limpiar sesión inválida y forzar re-login
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        zone.run(() => router.navigate(['/login'], { replaceUrl: true }));
      }

      return throwError(() => error);
    })
  );
};
