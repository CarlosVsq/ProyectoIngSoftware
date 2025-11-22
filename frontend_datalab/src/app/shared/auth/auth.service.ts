import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthResponse } from './auth.types';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_BASE = 'http://localhost:8080';
  private readonly TOKEN_KEY = 'datalab_access_token';
  private readonly USER_KEY = 'datalab_user';

  constructor(private http: HttpClient) {}

  login(correo: string, contrasenia: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_BASE}/auth/login`, { correo, contrasenia })
      .pipe(tap((res) => this.setSession(res)));
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
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

  getUserName(): string {
    const user = this.getUser();
    return user?.nombreCompleto || 'Usuario';
  }

  // Helpers para roles (usado por reclutamiento.html)
  isPI(): boolean {
    const user = this.getUser();
    const rol = user?.rol?.toLowerCase() || '';
    return rol.includes('investigadora principal') || rol.includes('principal');
  }

  getUserId(): number | null {
    const user = this.getUser();
    return user?.idUsuario ?? null;
  }

  private setSession(res: AuthResponse) {
    localStorage.setItem(this.TOKEN_KEY, res.accessToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(res.usuario));
  }
}
