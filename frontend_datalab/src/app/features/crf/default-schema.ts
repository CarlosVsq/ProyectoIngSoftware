// src/app/features/crf/default-schema.ts
import { CRFSchema } from './schema';

// Fallback mínimo para que el sistema sea funcional si no hay BD
export const CRF_DEFAULT_SCHEMA: CRFSchema = {
  sections: [
    {
      title: 'Datos Demográficos',
      fields: [
        { id: 'EDAD', label: 'Edad', type: 'number', required: true, validation: { min: 18, max: 100 } },
        { id: 'SEXO', label: 'Sexo Biológico', type: 'radio', required: true, options: ['Masculino', 'Femenino', 'Otro'] },
        { id: 'ESCOLARIDAD', label: 'Nivel de Escolaridad', type: 'select', required: false, options: ['Primaria', 'Secundaria', 'Universitaria', 'Posgrado'] }
      ]
    },
    {
      title: 'Antecedentes Médicos',
      fields: [
        { id: 'ANT_FAMILIARES', label: 'Antecedentes Familiares', type: 'textarea', required: false },
        { id: 'ALERGIAS', label: 'Alergias Conocidas', type: 'checkbox', required: false, options: ['Penicilina', 'Polen', 'Ninguna'] },
        { id: 'TABAQUISMO', label: '¿Fuma actualmente?', type: 'radio', required: true, options: ['Sí', 'No'] }
      ]
    },
    {
      title: 'Evaluación Clínica',
      fields: [
        { id: 'PESO', label: 'Peso (kg)', type: 'number', required: true },
        { id: 'TALLA', label: 'Talla (cm)', type: 'number', required: true },
        { id: 'IMC', label: 'IMC Calculado', type: 'text', required: false, placeholder: 'Automático o manual' }
      ]
    }
  ]
};
