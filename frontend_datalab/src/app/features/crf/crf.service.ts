// src/app/features/crf/crf.service.ts
import { Injectable } from '@angular/core';
import { CRF_DEFAULT_SCHEMA } from './default-schema';
import { CRFSchema } from './schema';

@Injectable({ providedIn: 'root' })
export class CrfService {
  getSchema(): CRFSchema {
    return CRF_DEFAULT_SCHEMA;
  }

  saveDraft(codigo: string, data: any) {
    localStorage.setItem(`crf_${codigo}`, JSON.stringify({ ...data, estado: 'borrador' }));
  }

  saveFinal(codigo: string, data: any) {
    localStorage.setItem(`crf_${codigo}`, JSON.stringify({ ...data, estado: 'completo' }));
  }

  load(codigo: string): any | null {
    const raw = localStorage.getItem(`crf_${codigo}`);
    return raw ? JSON.parse(raw) : null;
  }
}
