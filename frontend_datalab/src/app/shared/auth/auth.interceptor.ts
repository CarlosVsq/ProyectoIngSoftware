import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // 1. OBTENER EL TOKEN DIRECTAMENTE
  // Usamos localStorage directamente para evitar la Dependencia Circular:
  // (AuthService -> HttpClient -> Interceptor -> AuthService)
  // Asegúrate de que la clave coincida con la de tu AuthService ('datalab_access_token')
  const token = localStorage.getItem('datalab_access_token');

  // 2. CLONAR Y PEGAR EL TOKEN
  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(cloned);
  }

  // 3. SI NO HAY TOKEN, PASAR LA PETICIÓN ORIGINAL
  return next(req);
};