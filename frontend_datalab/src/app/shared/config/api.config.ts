// Punto único para configurar la URL base del backend / API.
// Orden de prioridad:
// 1) Variable de entorno NG_APP_BACKEND_BASE (inyectada en build).
// 2) Host actual con puerto 8038 (ej. cuando frontend y backend corren juntos).
// 3) Fallback a localhost:8038.
const envBackendBase =
  (import.meta as any)?.env?.NG_APP_BACKEND_BASE as string | undefined;

const hostname =
  typeof window !== 'undefined' && window.location?.hostname
    ? window.location.hostname
    : 'localhost';

const fallbackHost = `http://${hostname}:8038`;
const resolvedEnv = envBackendBase?.trim();

// Evita usar un valor de build que apunte a localhost cuando el frontend se sirve desde otro host.
const shouldIgnoreEnvLocalhost =
  resolvedEnv &&
  resolvedEnv.includes('localhost') &&
  hostname !== 'localhost' &&
  hostname !== '127.0.0.1';

export const BACKEND_BASE_URL =
  !resolvedEnv || shouldIgnoreEnvLocalhost ? fallbackHost : resolvedEnv;
export const API_BASE_URL = `${BACKEND_BASE_URL}/api`;
