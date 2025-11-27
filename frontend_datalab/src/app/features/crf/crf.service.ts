// src/app/features/crf/crf.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CRF_DEFAULT_SCHEMA } from './default-schema';
import { CRFSchema } from './schema';
import { AuthService } from '../../shared/auth/auth.service';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CrfService {
  private readonly API_BASE = 'http://localhost:8080/api';

  constructor(private http: HttpClient, private auth: AuthService) {}

  getSchema(): CRFSchema {
    return CRF_DEFAULT_SCHEMA;
  }

  saveDraft(codigo: string, data: any) {
    localStorage.setItem(`crf_${codigo}`, JSON.stringify({ ...data, estado: 'borrador' }));
  }

  saveFinalLocal(codigo: string, data: any) {
    localStorage.setItem(`crf_${codigo}`, JSON.stringify({ ...data, estado: 'completo' }));
  }

  load(codigo: string): any | null {
    const raw = localStorage.getItem(`crf_${codigo}`);
    return raw ? JSON.parse(raw) : null;
  }

  crearParticipante(payload: { nombreCompleto: string; telefono: string; direccion: string; grupo: string; }): Observable<any> {
    const usuarioReclutadorId = this.auth.getUserId();
    if (!usuarioReclutadorId) {
      throw new Error('No hay usuario autenticado para asignar como reclutador');
    }

    return this.http.post(`${this.API_BASE}/participantes`, {
      ...payload,
      grupo: payload.grupo.toUpperCase(),
      usuarioReclutadorId
    });
  }

  guardarRespuestas(participanteId: number, respuestas: Record<string, string>): Observable<void> {
    const usuarioEditorId = this.auth.getUserId();
    if (!usuarioEditorId) {
      throw new Error('No hay usuario autenticado para registrar respuestas');
    }
    return this.http.post<void>(`${this.API_BASE}/participantes/${participanteId}/respuestas`, {
      usuarioEditorId,
      respuestasMap: respuestas
    });
  }

  listarCrfs(limit = 20): Observable<{ data: any[] }> {
    return this.http.get<{ data: any[] }>(`${this.API_BASE}/participantes/resumen?limit=${limit}`);
  }

  eliminarCrf(participanteId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_BASE}/participantes/${participanteId}`);
  }
}
