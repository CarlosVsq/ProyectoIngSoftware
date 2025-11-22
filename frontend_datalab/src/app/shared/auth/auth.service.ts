import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
<<<<<<< Updated upstream
import { Observable, tap } from 'rxjs';
import { AuthResponse } from './auth.types';
=======
import { Observable, tap, map } from 'rxjs';
import { AuthResponse } from './auth.types';

interface ApiResponse<T> {
  data: T;
  message?: string;
}
>>>>>>> Stashed changes

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_BASE = 'http://localhost:8080';
  private readonly TOKEN_KEY = 'datalab_access_token';
  private readonly USER_KEY = 'datalab_user';

  constructor(private http: HttpClient) {}

  login(correo: string, contrasenia: string): Observable<AuthResponse> {
<<<<<<< Updated upstream
    return this.http.post<AuthResponse>(`${this.API_BASE}/auth/login`, { correo, contrasenia })
      .pipe(tap((res) => this.setSession(res)));
=======
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
>>>>>>> Stashed changes
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
