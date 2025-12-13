// src/app/features/crf/schema.ts
export type CRFFieldType = 'text' | 'number' | 'date' | 'radio' | 'checkbox' | 'textarea' | 'select';

export interface CRFField {
  id: string;
  label: string;
  type: CRFFieldType;
  required?: boolean;
  options?: string[];             // para radio/checkbox/select
  placeholder?: string;
  groupVisibility?: Array<'caso' | 'control'>; // si se requiere filtrar por grupo a nivel campo
  validation?: {
    min?: number;
    max?: number;
    pattern?: string;
    minLength?: number;
    maxLength?: number;
  };
}

export interface CRFSection {
  title: string;
  groupVisibility?: Array<'caso' | 'control'>; // visibilidad a nivel sección
  fields: CRFField[];
}

export interface CRFSchema {
  sections: CRFSection[];
}
