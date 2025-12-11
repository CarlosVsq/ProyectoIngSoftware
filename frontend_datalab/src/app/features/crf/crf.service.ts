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
