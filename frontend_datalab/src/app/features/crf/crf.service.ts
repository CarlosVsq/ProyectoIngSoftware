// src/app/features/crf/crf.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CRF_DEFAULT_SCHEMA } from './default-schema';
import { CRFSchema, CRFFieldType, CRFSection } from './schema';
import { AuthService } from '../../shared/auth/auth.service';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class CrfService {
  private readonly API_BASE = 'http://localhost:8080/api';
  private cachedSchema?: CRFSchema;

  constructor(private http: HttpClient, private auth: AuthService) { }

  getSchema(): Observable<CRFSchema> {
    if (this.cachedSchema) {
      return of(this.cachedSchema);
    }

    return this.http.get<{ data?: VariableRow[]; variables?: VariableRow[]; }>(
      `${this.API_BASE}/variables`
    ).pipe(
      map(res => this.mapToSchema(res.data || res.variables || (res as any))),
      tap(schema => {
        console.log('CRF schema cargado', schema);
        const requeridos = schema.sections.flatMap(s => s.fields.filter(f => f.required).map(f => `${s.title} -> ${f.id}`));
        console.log('Campos requeridos', requeridos);
        this.cachedSchema = schema;
      }),
      catchError(() => {
        this.cachedSchema = CRF_DEFAULT_SCHEMA;
        return of(CRF_DEFAULT_SCHEMA);
      })
    );
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

  crearVariable(variable: any): Observable<any> {
    return this.http.post(`${this.API_BASE}/variables`, variable);
  }

  // Returns raw variables without schema filtering
  listarTodasLasVariables(): Observable<VariableRow[]> {
    return this.http.get<{ data: VariableRow[] }>(`${this.API_BASE}/variables`).pipe(
      map(res => res.data || [])
    );
  }

  deleteVariable(codigo: string): Observable<void> {
    return this.http.delete<void>(`${this.API_BASE}/variables/${codigo}`);
  }

  guardarRespuestas(participanteId: number, respuestas: Record<string, string>, extra?: { nombre?: string, telefono?: string, direccion?: string, grupo?: string }): Observable<void> {
    const usuarioEditorId = this.auth.getUserId();
    if (!usuarioEditorId) {
      throw new Error('No hay usuario autenticado para registrar respuestas');
    }
    return this.http.post<void>(`${this.API_BASE}/participantes/${participanteId}/respuestas`, {
      usuarioEditorId,
      respuestasMap: respuestas,
      nombreCompleto: extra?.nombre,
      telefono: extra?.telefono,
      direccion: extra?.direccion,
      grupo: extra?.grupo
    });
  }

  listarCrfs(limit = 20): Observable<{ data: any[] }> {
    return this.http.get<{ data: any[] }>(`${this.API_BASE}/participantes/resumen?limit=${limit}`);
  }

  eliminarCrf(participanteId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_BASE}/participantes/${participanteId}`);
  }

  // Convierte filas de la tabla Variable al CRFSchema esperado por el front.
  private mapToSchema(rows: VariableRow[] | any): CRFSchema {
    if (!Array.isArray(rows) || rows.length === 0) {
      return CRF_DEFAULT_SCHEMA;
    }

    const sections = new Map<string, CRFSection>();
    const sectionOrder = new Map<string, number>();

    const sorted = [...rows].sort((a, b) => {
      const seccionA = (a?.seccion || a?.seccionVariable || '').toString();
      const seccionB = (b?.seccion || b?.seccionVariable || '').toString();
      if (seccionA !== seccionB) {
        return seccionA.localeCompare(seccionB);
      }
      return (a?.orden_enunciado ?? a?.ordenEnunciado ?? 0) - (b?.orden_enunciado ?? b?.ordenEnunciado ?? 0);
    });

    sorted.forEach((row) => {
      const codigo = row?.codigo_variable || row?.codigoVariable;
      if (!codigo) return;

      const title = row.seccion || row.seccionVariable || 'Generales';
      if (!sections.has(title)) {
        sections.set(title, { title, fields: [] });
      }
      const section = sections.get(title)!;

      
}

interface VariableRow {
  id_variable: number;
  enunciado: string;
  codigo_variable: string;
  tipo_dato: string;
  opciones?: string | null;
  aplica_a?: string | null;
  seccion?: string | null;
  orden_enunciado?: number | null;
  es_obligatoria?: number | boolean;
  regla_validacion?: string | null;
}
