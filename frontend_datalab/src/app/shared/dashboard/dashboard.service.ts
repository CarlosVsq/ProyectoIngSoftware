import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export interface DashboardResumen {
  total: number;
  casos: number;
  controles: number;
  completas: number;
  incompletas: number;
  noCompletable: number;
  meta: number;
  serieReclutamiento: Array<{ label: string; valor: number }>;
  porSexo: { masculino: number; femenino: number; otros: number };
  porEdad: Record<string, number>;
}

interface ApiResponse<T> {
  data: T;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly API_BASE = `${API_BASE_URL}/dashboard`;

  constructor(private http: HttpClient) {}

  getResumen(): Observable<ApiResponse<DashboardResumen>> {
    return this.http.get<ApiResponse<DashboardResumen>>(`${this.API_BASE}/resumen`);
  }
}
