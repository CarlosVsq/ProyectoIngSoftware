// src/app/features/crf/crf.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CRF_DEFAULT_SCHEMA } from './default-schema';
import { CRFSchema, CRFFieldType, CRFSection } from './schema';
import { AuthService } from '../../shared/auth/auth.service';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { API_BASE_URL } from '../../shared/config/api.config';

@Injectable({ providedIn: 'root' })
export class CrfService {
  private readonly API_BASE = API_BASE_URL;
  private cachedSchema?: CRFSchema;

  clearCache() {
    this.cachedSchema = undefined;
  }

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

  actualizarObligatoria(codigo: string, esObligatoria: boolean): Observable<any> {
    return this.http.patch(`${this.API_BASE}/variables/${codigo}/obligatoria`, { esObligatoria });
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

      // Filter out auto-generated or unwanted fields
      if (['CODIGO_PARTICIPANTE', 'CODIGO', 'IMC'].includes(codigo.toUpperCase())) return;

      const type = this.mapTipoDato(row.tipo_dato || row.tipoDato);
      let options = this.parseOptions(row.opciones);
      const groupVisibility = this.mapAplica(row.aplica_a || row.aplicaA);
      const validation = this.parseValidation(row.regla_validacion || row.reglaValidacion);
      const orden = this.toNumber(row.orden_enunciado ?? row.ordenEnunciado, 0);

      // Force 'Otro' for SEXO if not present
      if (codigo.toUpperCase() === 'SEXO' && options) {
        if (!options.some(o => o.toLowerCase() === 'otro' || o.toLowerCase() === 'otros')) {
          options.push('Otro');
        }
      }

      section.fields.push({
        id: codigo,
        label: row.enunciado || codigo,
        type,
        required: this.toBoolean(row.es_obligatoria ?? row.esObligatoria),
        options,
        groupVisibility,
        validation
      });

      // Guarda el orden más bajo por sección para ordenar las secciones
      if (!sectionOrder.has(title) || (sectionOrder.get(title) ?? Number.MAX_SAFE_INTEGER) > orden) {
        sectionOrder.set(title, orden);
      }
    });

    // Inject Standard Fields if they are missing
    const standardFields = [
      { id: 'nombre_completo', label: 'Nombre Completo', section: 'Identificacion del participante' },
      { id: 'telefono', label: 'Telefono', section: 'Identificacion del participante' },
      { id: 'direccion', label: 'Direccion', section: 'Identificacion del participante' },
      { id: 'fecha_inclusion', label: 'Fecha de inclusion', type: 'date', section: 'Identificacion del participante' }
    ];

    standardFields.forEach(std => {
      let exists = false;
      for (const s of sections.values()) {
        if (s.fields.some(f => f.id.toLowerCase() === std.id.toLowerCase())) { exists = true; break; }
      }
      if (!exists) {
        if (!sections.has(std.section)) {
          sections.set(std.section, { title: std.section, fields: [] });
          sectionOrder.set(std.section, -1);
        }
        sections.get(std.section)!.fields.push({
          id: std.id,
          label: std.label,
          type: (std.type as CRFFieldType) || 'text',
          required: true,
          groupVisibility: undefined
        });
      }
    });

    const schemaSections = Array.from(sections.values()).sort((a, b) => {
      const orderA = sectionOrder.get(a.title) ?? 0;
      const orderB = sectionOrder.get(b.title) ?? 0;
      if (orderA !== orderB) return orderA - orderB;
      return a.title.localeCompare(b.title);
    });
    return schemaSections.length ? { sections: schemaSections } : CRF_DEFAULT_SCHEMA;
  }

  private mapTipoDato(tipo: string | null | undefined): CRFFieldType {
    const normalized = (tipo || '').toLowerCase();
    if (normalized.includes('numero')) return 'number';
    if (normalized.includes('fecha')) return 'date';
    if (normalized.includes('seleccionunica')) return 'radio';
    if (normalized.includes('seleccionmultiple') || normalized.includes('checkbox')) return 'checkbox';
    if (normalized.includes('textarea') || normalized.includes('texto_largo')) return 'textarea';
    if (normalized.includes('select')) return 'select';
    return 'text';
  }

  private parseOptions(raw: string | null | undefined): string[] | undefined {
    if (!raw) return undefined;
    return raw.split(',')
      .map(o => o.trim())
      .filter(o => o.length > 0);
  }

  private mapAplica(aplica: string | null | undefined): Array<'caso' | 'control'> | undefined {
    const normalized = (aplica || 'Ambos').toLowerCase();
    if (normalized.includes('caso')) return ['caso'];
    if (normalized.includes('control')) return ['control'];
    return undefined; // ambos
  }

  private parseValidation(raw: string | null | undefined) {
    if (!raw) return undefined;
    try {
      const parsed = JSON.parse(raw);
      return {
        min: parsed.min ?? parsed.minValue,
        max: parsed.max ?? parsed.maxValue,
        pattern: parsed.pattern,
        minLength: parsed.minLength,
        maxLength: parsed.maxLength,
      };
    } catch {
      // Si no es JSON legible, omite validaciones.
      return undefined;
    }
  }

  private toBoolean(val: any): boolean {
    if (val === true || val === 1) return true;
    if (typeof val === 'string') {
      const normalized = val.trim().toLowerCase();
      if (['1', 'true', 't', 'yes', 'y', 'si', 's'].includes(normalized)) return true;
    }
    return false;
  }

  private toNumber(val: any, fallback: number): number {
    const n = Number(val);
    return Number.isFinite(n) ? n : fallback;
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
