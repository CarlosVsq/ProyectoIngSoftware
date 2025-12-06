// src/app/features/crf/default-schema.ts
import { CRFSchema } from './schema';

// Fallback mínimo: solo si /api/variables falla o devuelve vacío.
export const CRF_DEFAULT_SCHEMA: CRFSchema = {
  sections: [],
};
