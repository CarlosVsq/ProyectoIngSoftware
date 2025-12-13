import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, map, catchError, of, finalize } from 'rxjs';
import { AuthResponse } from './auth.types';
import { BACKEND_BASE_URL } from '../config/api.config';

interface ApiResponse<T> {
  data: T;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_BASE = BACKEND_BASE_URL;
  private readonly TOKEN_KEY = 'datalab_access_token';
  private readonly USER_KEY = 'datalab_user';

  constructor(private http: HttpClient, private router: Router) { }

  login(correo: string, contrasenia: string): Observable<AuthResponse> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.API_BASE}/auth/login`, { correo, contrasenia })
      .pipe(
        map(res => res.data),
        tap((data) => this.setSession(data))
      );
  }

  register(nombreCompleto: string, correo: string, contrasenia: string, idRol: number): Observable<AuthResponse> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.API_BASE}/auth/register`, { nombreCompleto, correo, contrasenia, idRol })
      .pipe(
        map(res => res.data),
        tap((data) => this.setSession(data))
      );
  }

  /**
   * Cierra sesión notificando al backend (Auditoría) y limpiando localmente
   */
  logout(): void {
    this.http.post(`${this.API_BASE}/auth/logout`, {})
      .pipe(
        catchError(err => {
          console.warn('Error al registrar logout en auditoría:', err);
          return of(null);
        }),
        finalize(() => {
          this.doLocalLogout();
        })
      )
      .subscribe();
  }

  private doLocalLogout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getUser() {
    const raw = localStorage.getItem(this.USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  // CORREGIDO: Ahora obtiene el nombre real de la BD
  getUserName(): string {
    const user = this.getUser();
    return user?.nombreCompleto || 'Usuario';
  }

  getUserRole(): string {
    const user = this.getUser();
    if (!user) return 'Rol no asignado';
    if (typeof user.rol === 'string') return user.rol;
    if (user.rol?.nombre) return user.rol.nombre;
    return 'Rol no asignado';
  }

  // Helpers para roles
  isPI(): boolean {
    const user = this.getUser();
    // Ajusta esto según el nombre exacto que viene de tu BD ("Investigadora Principal")
    const rol = user?.rol?.nombre || (typeof user?.rol === 'string' ? user.rol : '') || '';
    return rol.toLowerCase().includes('principal');
  }

  getUserId(): number | null {
    const user = this.getUser();
    return user?.idUsuario ?? null;
  }

  private setSession(res: AuthResponse) {
    localStorage.setItem(this.TOKEN_KEY, res.accessToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(res.usuario));
  }

  // --- Password Recovery ---
  forgotPassword(email: string): Observable<any> {
    return this.http.post<any>(`${this.API_BASE}/auth/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post<any>(`${this.API_BASE}/auth/reset-password`, { token, newPassword });
  }
}
