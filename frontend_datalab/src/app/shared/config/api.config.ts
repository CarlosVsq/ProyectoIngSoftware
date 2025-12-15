// Punto único para configurar la URL base del backend / API.
// Orden de prioridad:
// 1) Variable de entorno NG_APP_BACKEND_BASE (inyectada en build).
// 2) Host actual con puerto 8038 (ej. cuando frontend y backend corren juntos).
// 3) Fallback a localhost:8038.
const envBackendBase =
  (import.meta as any)?.env?.NG_APP_BACKEND_BASE as string | undefined;

const fallbackHost =
  typeof window !== 'undefined' && window.location?.hostname
    ? `http://${window.location.hostname}:8038`
    : 'http://localhost:8038';

export const BACKEND_BASE_URL =
  (envBackendBase && envBackendBase.trim()) || fallbackHost;
export const API_BASE_URL = `${BACKEND_BASE_URL}/api`;
