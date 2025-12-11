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
