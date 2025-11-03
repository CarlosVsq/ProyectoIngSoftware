// src/app/features/crf/default-schema.ts
import { CRFSchema } from './schema';

export const CRF_DEFAULT_SCHEMA: CRFSchema = {
  sections: [
    {
      title: '1. Identificación del participante',
      fields: [
        { id: 'nombre', label: 'Nombre completo', type: 'text', required: true },
        { id: 'codigo', label: 'Código del participante', type: 'text', required: true },
        { id: 'fecha_inclusion', label: 'Fecha de inclusión', type: 'date', required: true }
      ]
    },
    {
      title: '2. Datos sociodemográficos',
      fields: [
        { id: 'edad', label: 'Edad (años)', type: 'number', required: true },
        { id: 'sexo', label: 'Sexo', type: 'radio', options: ['Masculino', 'Femenino'] },
        { id: 'nacionalidad', label: 'Nacionalidad', type: 'text' },
        { id: 'direccion', label: 'Dirección', type: 'text' },
        { id: 'zona', label: 'Zona', type: 'radio', options: ['Urbana', 'Rural'] },
        { id: 'anios_residencia', label: 'Años viviendo en la residencia actual', type: 'radio', options: ['<5', '5–10', '>10'] },
        { id: 'nivel_educacional', label: 'Nivel educacional', type: 'checkbox', options: ['Básico', 'Medio', 'Superior'] },
        { id: 'ocupacion', label: 'Ocupación actual', type: 'text' }
      ]
    },
    {
      title: '3. Clínica (solo Caso)',
      groupVisibility: ['caso'],
      fields: [
        { id: 'diag_histologico', label: 'Diagnóstico histológico de adenocarcinoma gástrico', type: 'radio', options: ['Sí','No'] },
        { id: 'fecha_diagnostico', label: 'Fecha de diagnóstico', type: 'date' },
        { id: 'cirugia_previa', label: 'Cirugía gástrica previa', type: 'radio', options: ['Sí','No'] }
      ]
    },
    {
      title: '4. Variables antropométricas',
      fields: [
        { id: 'peso', label: 'Peso (kg)', type: 'number' },
        { id: 'estatura', label: 'Estatura (m)', type: 'number' }
      ]
    },
    {
      title: '5. Tabaquismo',
      fields: [
        { id: 'tabaquismo_estado', label: 'Estado de tabaquismo', type: 'radio', options: ['Nunca fumó', 'Exfumador', 'Fumador actual'] },
        { id: 'tabaquismo_edad_inicio', label: 'Edad de inicio', type: 'radio', options: ['<18', '18–25', '>25'] },
        { id: 'tabaquismo_promedio', label: 'Cantidad promedio fumada', type: 'radio', options: ['1–9','10–19','≥20'] }
      ]
    },
    {
      title: '6. Consumo de alcohol',
      fields: [
        { id: 'alcohol_estado', label: 'Estado de consumo', type: 'radio', options: ['Nunca','Exconsumidor','Consumidor actual'] },
        { id: 'alcohol_frecuencia', label: 'Frecuencia', type: 'radio', options: ['Ocasional','Regular (1–3/sem)','Frecuente (≥4/sem)'] },
        { id: 'alcohol_tipica', label: 'Cantidad típica por ocasión', type: 'radio', options: ['1–2','3–4','≥5'] }
      ]
    },
    {
      title: '7. Factores dietarios y ambientales',
      fields: [
        { id: 'carne_procesada', label: 'Carne procesada / cecinas', type: 'radio', options: ['<1/sem','1–2/sem','≥3/sem'] },
        { id: 'salados', label: 'Alimentos muy salados', type: 'radio', options: ['Sí','No'] },
        { id: 'frutas_verduras', label: 'Porciones de frutas/verduras por día', type: 'radio', options: ['≥5','3–4','≤2'] },
        { id: 'frituras', label: 'Consumo de frituras (≥3/sem)', type: 'radio', options: ['Sí','No'] },
        { id: 'agua_principal', label: 'Fuente principal de agua en el hogar', type: 'radio', options: ['Red pública','Pozo','Camión aljibe','Otra'] }
      ]
    },
    {
      title: '8. Infección por Helicobacter pylori',
      fields: [
        { id: 'hp_prueba', label: 'Prueba realizada', type: 'radio', options: ['Test de aliento','Antígeno','Endoscopía/biopsia'] },
        { id: 'hp_resultado', label: 'Resultado', type: 'radio', options: ['Positivo','Negativo'] },
        { id: 'hp_tiempo', label: '¿Hace cuánto se realizó el test?', type: 'radio', options: ['<1 año','1–5 años','>5 años'] }
      ]
    },
    {
      title: '9. Muestras biológicas y genéticas',
      fields: [
        { id: 'muestra_fecha', label: 'Fecha de toma de sangre', type: 'date' },
        { id: 'tlr9_rs5743836', label: 'TLR9 rs5743836', type: 'radio', options: ['TT','TC','CC'] },
        { id: 'tlr9_rs187084', label: 'TLR9 rs187084', type: 'radio', options: ['TT','TC','CC'] },
        { id: 'mir146a_rs2910164', label: 'miR-146a rs2910164', type: 'radio', options: ['GG','GC','CC'] }
      ]
    },
    {
      title: '10. Histopatología (solo casos)',
      groupVisibility: ['caso'],
      fields: [
        { id: 'tipo_histologico', label: 'Tipo histológico', type: 'radio', options: ['Intestinal','Difuso','Mixto','Otro'] },
        { id: 'localizacion_tumoral', label: 'Localización tumoral', type: 'radio', options: ['Cardias','Cuerpo','Antro','Difuso'] },
        { id: 'tnm', label: 'Estadio clínico (TNM)', type: 'text' }
      ]
    }
  ]
};
