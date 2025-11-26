// src/app/features/crf/default-schema.ts
import { CRFSchema } from './schema';

export const CRF_DEFAULT_SCHEMA: CRFSchema = {
  sections: [
    {
      title: '1. Identificacion del participante',
      fields: [
        { id: 'nombre', label: 'Nombre completo', type: 'text', required: true },
        { id: 'telefono', label: 'Telefono de contacto', type: 'text', required: true, validation: { pattern: '^[0-9+\\s-]{8,20}$' } },
        { id: 'fecha_inclusion', label: 'Fecha de inclusion', type: 'date', required: true }
      ]
    },
    {
      title: '2. Datos sociodemograficos',
      fields: [
        { id: 'edad', label: 'Edad (años)', type: 'number', required: true, validation: { min: 0, max: 120 } },
        { id: 'sexo', label: 'Sexo', type: 'radio', options: ['Masculino', 'Femenino'] },
        { id: 'nacionalidad', label: 'Nacionalidad', type: 'text' },
        { id: 'direccion', label: 'Direccion', type: 'text' },
        { id: 'zona', label: 'Zona', type: 'radio', options: ['Urbana', 'Rural'] },
        { id: 'anios_residencia', label: 'Años viviendo en la residencia actual', type: 'radio', options: ['<5', '5-10', '>10'] },
        { id: 'nivel_educacional', label: 'Nivel educacional', type: 'checkbox', options: ['Basico', 'Medio', 'Superior'] },
        { id: 'ocupacion', label: 'Ocupacion actual', type: 'text' }
      ]
    },
    {
      title: '3. Clinica (solo Caso)',
      groupVisibility: ['caso'],
      fields: [
        { id: 'diag_histologico', label: 'Diagnostico histologico de adenocarcinoma gastrico', type: 'radio', options: ['Si','No'] },
        { id: 'fecha_diagnostico', label: 'Fecha de diagnostico', type: 'date' },
        { id: 'cirugia_previa', label: 'Cirugia gastrica previa', type: 'radio', options: ['Si','No'] }
      ]
    },
    {
      title: '4. Variables antropometricas',
      fields: [
        { id: 'peso', label: 'Peso (kg)', type: 'number', validation: { min: 0, max: 500 } },
        { id: 'estatura', label: 'Estatura (m)', type: 'number', validation: { min: 0.5, max: 2.5 } }
      ]
    },
    {
      title: '5. Tabaquismo',
      fields: [
        { id: 'tabaquismo_estado', label: 'Estado de tabaquismo', type: 'radio', options: ['Nunca fumo', 'Exfumador', 'Fumador actual'] },
        { id: 'tabaquismo_edad_inicio', label: 'Edad de inicio', type: 'radio', options: ['<18', '18-25', '>25'] },
        { id: 'tabaquismo_promedio', label: 'Cantidad promedio fumada', type: 'radio', options: ['1-9','10-19','>=20'] }
      ]
    },
    {
      title: '6. Consumo de alcohol',
      fields: [
        { id: 'alcohol_estado', label: 'Estado de consumo', type: 'radio', options: ['Nunca','Exconsumidor','Consumidor actual'] },
        { id: 'alcohol_frecuencia', label: 'Frecuencia', type: 'radio', options: ['Ocasional','Regular (1-3/sem)','Frecuente (>=4/sem)'] },
        { id: 'alcohol_tipica', label: 'Cantidad tipica por ocasion', type: 'radio', options: ['1-2','3-4','>=5'] }
      ]
    },
    {
      title: '7. Factores dietarios y ambientales',
      fields: [
        { id: 'carne_procesada', label: 'Carne procesada / cecinas', type: 'radio', options: ['<1/sem','1-2/sem','>=3/sem'] },
        { id: 'salados', label: 'Alimentos muy salados', type: 'radio', options: ['Si','No'] },
        { id: 'frutas_verduras', label: 'Porciones de frutas/verduras por dia', type: 'radio', options: ['>=5','3-4','<=2'] },
        { id: 'frituras', label: 'Consumo de frituras (>=3/sem)', type: 'radio', options: ['Si','No'] },
        { id: 'agua_principal', label: 'Fuente principal de agua en el hogar', type: 'radio', options: ['Red publica','Pozo','Camion aljibe','Otra'] }
      ]
    },
    {
      title: '8. Infeccion por Helicobacter pylori',
      fields: [
        { id: 'hp_prueba', label: 'Prueba realizada', type: 'radio', options: ['Test de aliento','Antigeno','Endoscopia/biopsia'] },
        { id: 'hp_resultado', label: 'Resultado', type: 'radio', options: ['Positivo','Negativo'] },
        { id: 'hp_tiempo', label: 'Hace cuanto se realizo el test?', type: 'radio', options: ['<1 ano','1-5 anos','>5 anos'] }
      ]
    },
    {
      title: '9. Muestras biologicas y geneticas',
      fields: [
        { id: 'muestra_fecha', label: 'Fecha de toma de sangre', type: 'date' },
        { id: 'tlr9_rs5743836', label: 'TLR9 rs5743836', type: 'radio', options: ['TT','TC','CC'] },
        { id: 'tlr9_rs187084', label: 'TLR9 rs187084', type: 'radio', options: ['TT','TC','CC'] },
        { id: 'mir146a_rs2910164', label: 'miR-146a rs2910164', type: 'radio', options: ['GG','GC','CC'] }
      ]
    },
    {
      title: '10. Histopatologia (solo casos)',
      groupVisibility: ['caso'],
      fields: [
        { id: 'tipo_histologico', label: 'Tipo histologico', type: 'radio', options: ['Intestinal','Difuso','Mixto','Otro'] },
        { id: 'localizacion_tumoral', label: 'Localizacion tumoral', type: 'radio', options: ['Cardias','Cuerpo','Antro','Difuso'] },
        { id: 'tnm', label: 'Estadio clinico (TNM)', type: 'text' }
      ]
    }
  ]
};
