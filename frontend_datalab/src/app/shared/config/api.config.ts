// Punto único para configurar la URL base del backend / API.
// Se puede inyectar en build con la env NG_APP_BACKEND_BASE (Angular 20+ con esbuild).
const envBackendBase =
  (import.meta as any)?.env?.NG_APP_BACKEND_BASE as string | undefined;
export const BACKEND_BASE_URL = envBackendBase && envBackendBase.length
  ? envBackendBase
  : 'http://pacheco.chillan.ubiobio.cl:8038';
export const API_BASE_URL = `${BACKEND_BASE_URL}/api`;
