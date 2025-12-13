// src/app/features/crf/crf-modal.component.ts
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormArray, AbstractControl, ValidatorFn, FormsModule } from '@angular/forms';
import { CrfService } from './crf.service';
import { CRFSchema, CRFSection, CRFField } from './schema';
import { Subscription } from 'rxjs';
import { ComentarioService } from '../../shared/comentarios/comentario.service';

@Component({
  selector: 'app-crf-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './crf-modal.component.html',
})
export class CrfModalComponent implements OnInit, OnDestroy, OnChanges {
  @Input() open = false;
  @Input() recordId: string | null = null;  // identificador local de borrador
  @Input() preloadData: any = null;
  @Input() participantId: number | null = null;
  @Input() fresh = false; // cuando se inicia nueva encuesta, limpiar formulario
  @Output() closed = new EventEmitter<void>();

  schema!: CRFSchema;
  form!: FormGroup;
  selectedGroup: 'caso' | 'control' = 'control';
  lastAutoSaveAt = '';
  missingRequiredLabels: string[] = [];
  private autoSaveHandle: ReturnType<typeof setInterval> | null = null;
  isSubmitting = false;
  private schemaSub?: Subscription;

  // Justification Modal
  showJustificationModal = false;
  justificationText = '';

  constructor(private fb: FormBuilder, private crf: CrfService, private comentarioService: ComentarioService) { }

  ngOnInit(): void {
    this.schemaSub = this.crf.getSchema().subscribe((schema) => {
      this.schema = schema;
      this.buildForm();
      this.startAutoSave();
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.form) return;
    if (changes['fresh'] && this.fresh && this.open) {
      this.clearDraft(this.getDraftKey());
      this.resetForm();
    }
    if (changes['preloadData'] && this.preloadData && this.open) {
      this.hydrateForm(this.preloadData);
    }
    if (changes['open'] && this.open && this.preloadData) {
      this.hydrateForm(this.preloadData);
    }
  }

  ngOnDestroy(): void {
    if (this.autoSaveHandle !== null) {
      clearInterval(this.autoSaveHandle);
      this.autoSaveHandle = null;
    }
    if (this.schemaSub) {
      this.schemaSub.unsubscribe();
    }
  }

  private buildForm(): void {
    const controls: Record<string, AbstractControl> = {};

    // Base controls
    controls['grupo'] = this.fb.control(this.selectedGroup, Validators.required);
    // Hidden calculated fields
    controls['imc'] = this.fb.control(null);

    // crear controles para cada campo
    if (this.schema && this.schema.sections) {
      this.schema.sections.forEach((s: CRFSection) => {
        s.fields.forEach((f: CRFField) => {
          if (f.id === 'codigo') { return; } // codigo lo asigna backend
          if (f.type === 'checkbox') {
            const validators = f.required ? [Validators.required, Validators.minLength(1)] : [];
            controls[f.id] = this.fb.array([], validators);
          } else {
            controls[f.id] = this.fb.control(null, this.buildValidatorsForField(f));
          }
        });
      });
    }

    this.form = this.fb.group(controls);

    // escuchar cambios de grupo para actualizar visibilidad
    this.form.get('grupo')?.valueChanges.subscribe((val) => {
      this.selectedGroup = val;
    });

    // precarga de borrador o datos existentes
    // precarga de borrador o datos existentes
    const key = this.getDraftKey();
    const isEditing = this.participantId !== null;
    let loadedFromDraft = false;

    if (!isEditing && key) {
      const draft = this.crf.load(key);
      if (draft) {
        this.form.patchValue(draft);
        loadedFromDraft = true;
      }
    }

    if (!loadedFromDraft && this.preloadData) {
      this.hydrateForm(this.preloadData);
    }

    this.setupImcCalculation();
  }

  private hydrateForm(data: any): void {
    if (!this.schema || !this.form) return;

    const patch: any = {};

    this.schema.sections.forEach(section => {
      section.fields.forEach(field => {
        const val = data[field.id];
        if (val === undefined || val === null) return;

        if (field.type === 'checkbox') {
          const arr = this.form.get(field.id) as FormArray;
          if (arr) {
            arr.clear();
            let selected: string[] = [];
            if (Array.isArray(val)) {
              selected = val;
            } else if (typeof val === 'string') {
              selected = val.split(',').map(s => s.trim()).filter(s => s);
            }
            selected.forEach(opt => arr.push(this.fb.control(opt)));
          }
        } else {
          patch[field.id] = val;
        }
      });
    });

    // Patch core fields
    const coreFields = ['nombre_completo', 'telefono', 'direccion', 'grupo', 'fecha_inclusion'];
    coreFields.forEach(k => {
      if (data[k]) patch[k] = data[k];
    });

    this.form.patchValue(patch);
    console.log('Form hydrated', this.form.value);
  }

  // Helper for template
  isFieldInvalid(fieldId: string): boolean {
    const control = this.form?.get(fieldId);
    if (!control) return false;
    // Show error if touched or if form was submitted
    return control.invalid && (control.touched || this.isSubmitting);
  }

  private setupImcCalculation(): void {
    if (!this.schema || !this.schema.sections || !this.form) return;

    let pesoId = '';
    let tallaId = '';
    let imcId = '';

    for (const section of this.schema.sections) {
      for (const field of section.fields) {
        const id = field.id.toUpperCase();
        const label = (field.label || '').toUpperCase();

        if (!pesoId && (id === 'PESO' || id === 'WEIGHT' || id === 'KG' || label.includes('PESO') || label.includes('WEIGHT'))) {
          pesoId = field.id;
        }
        if (!tallaId && (id === 'TALLA' || id === 'ESTATURA' || id === 'ALTURA' || id === 'HEIGHT' || label.includes('TALLA') || label.includes('ESTATURA') || label.includes('ALTURA') || label.includes('HEIGHT'))) {
          tallaId = field.id;
        }
        if (!imcId && (id === 'IMC' || id === 'BMI' || label.includes('IMC') || label.includes('BMI'))) {
          imcId = field.id;
        }
      }
    }

    const pesoControl = this.form.get(pesoId);
    const tallaControl = this.form.get(tallaId);
    const imcControl = this.form.get(imcId);

    if (!pesoControl || !tallaControl || !imcControl) return;
    imcControl.disable();

    const calculate = () => {
      const pesoStr = (pesoControl.value || '').toString().replace(',', '.');
      const tallaStr = (tallaControl.value || '').toString().replace(',', '.');

      console.log('Calculating IMC...', { pesoId, tallaId, pesoStr, tallaStr });
      const peso = parseFloat(pesoStr);
      let talla = parseFloat(tallaStr);

      if (isNaN(peso) || isNaN(talla) || talla <= 0) {
        console.log('Invalid values for IMC', { peso, talla });
        imcControl.setValue(null);
        return;
      }

      const isCm = talla > 3; // assume centimeters if value is greater than typical meters
      if (isCm) talla = talla / 100;

      const imc = peso / (talla * talla);
      console.log('calculated IMC:', imc);
      imcControl.setValue(imc.toFixed(2));
    };

    pesoControl.valueChanges.subscribe(calculate);
    tallaControl.valueChanges.subscribe(calculate);
    calculate();
  }

  private buildValidatorsForField(field: CRFField): ValidatorFn[] {
    const validators: ValidatorFn[] = [];
    if (field.required) {
      validators.push(Validators.required);
    }
    if (field.type === 'number') {
      if (field.validation?.min !== undefined) validators.push(Validators.min(field.validation.min));
      if (field.validation?.max !== undefined) validators.push(Validators.max(field.validation.max));
    }
    if (field.validation?.pattern) {
      validators.push(Validators.pattern(field.validation.pattern));
    }
    if (field.validation?.minLength !== undefined) {
      validators.push(Validators.minLength(field.validation.minLength));
    }
    if (field.validation?.maxLength !== undefined) {
      validators.push(Validators.maxLength(field.validation.maxLength));
    }
    return validators;
  }

  // Helpers
  showSection(section: CRFSection): boolean {
    // Hide section if no fields are visible
    if (!section.fields.some(f => this.showField(f))) return false;

    if (!section.groupVisibility || section.groupVisibility.length === 0) return true;
    return section.groupVisibility.includes(this.selectedGroup);
  }

  showField(field: CRFField): boolean {
    // Hide IMC from UI as it is auto-calculated
    if (field.id.toUpperCase() === 'IMC' || field.label?.toUpperCase().includes('IMC')) return false;

    if (!field.groupVisibility || field.groupVisibility.length === 0) return true;
    return field.groupVisibility.includes(this.selectedGroup);
  }

  // Checkbox handler
  onCheckboxChange(evt: Event, fieldId: string, option: string): void {
    const arr = this.form.get(fieldId) as FormArray;
    const input = evt.target as HTMLInputElement;
    if (input.checked) {
      if (!arr.value.includes(option)) arr.push(this.fb.control(option));
    } else {
      const idx = arr.value.indexOf(option);
      if (idx >= 0) arr.removeAt(idx);
    }
  }

  // Guardados
  // Guardados
  guardarBorrador(): void {
    // Abrir modal de justificación
    this.showJustificationModal = true;
    this.justificationText = '';
  }

  cerrarJustificationModal(): void {
    this.showJustificationModal = false;
    this.justificationText = '';
  }

  confirmarGuardarBorrador(): void {
    this.showJustificationModal = false;
    this.saveToBackend(false);
  }

  guardarFinal(): void {
    const key = this.getDraftKey();

    // Check strict completeness (all fields)
    const missing = this.getAllMissingFields();
    if (missing.length > 0) {
      console.warn('Faltan campos para finalizar', missing);
      this.missingRequiredLabels = missing;
      this.crf.saveDraft(key, { ...this.form.value, estado: 'borrador' });
      alert(`No se puede finalizar. La ficha debe estar 100% completa.\nFaltan campos: ${missing.join(', ')}`);

      // Marcar controles vacíos como touched para feedback visual si tienen validadores, 
      // o invalidar manualmente si queremos forzar el rojo.
      this.markMissingAsTouched();
      return;
    }

    this.saveToBackend(true);
  }

  private saveToBackend(isFinal: boolean): void {
    if (this.isSubmitting) return;
    this.isSubmitting = true;
    const key = this.getDraftKey();

    const payload = {
      nombreCompleto: this.form.get('nombre_completo')?.value || '',
      telefono: this.form.get('telefono')?.value || '',
      direccion: this.form.get('direccion')?.value || '',
      grupo: (this.form.get('grupo')?.value || 'CONTROL').toString().toUpperCase()
    };

    // Validar datos mínimos para crear participante si no existe
    if (!this.participantId) {
      if (!payload.nombreCompleto) {
        alert('Debes ingresar al menos el Nombre Completo para guardar.');
        this.isSubmitting = false;
        return;
      }
    }

    const respuestas = this.buildRespuestasMap();

    const guardarRespuestas = (idParticipante: number) => {
      this.crf.guardarRespuestas(idParticipante, respuestas, {
        nombre: payload.nombreCompleto,
        telefono: payload.telefono,
        direccion: payload.direccion,
        grupo: payload.grupo
      }).subscribe({
        next: () => {
          if (isFinal) {
            this.crf.saveFinalLocal(key, { ...this.form.getRawValue(), estado: 'completo', idParticipante });
            alert('CRF Finalizado y Guardado exitosamente.');
            this.close();
          } else {
            // Actualizar estado local
            this.participantId = idParticipante;
            this.crf.saveDraft(key, { ...this.form.getRawValue(), estado: 'borrador', idParticipante });

            // Guardar justificación si existe
            if (this.justificationText.trim()) {
              this.comentarioService.agregarComentario(idParticipante, this.justificationText).subscribe({
                error: (e) => console.error('Error guardando justificación', e)
              });
            }

            alert('Guardado como Incompleto en el sistema con justificación.');
            this.lastAutoSaveAt = 'Guardado en sistema: ' + new Date().toLocaleTimeString();
            this.justificationText = '';
          }
          this.isSubmitting = false;
        },
        error: (err) => {
          const msg = err?.error?.message || 'No se pudieron guardar las respuestas.';
          alert(msg);
          this.isSubmitting = false;
        }
      });
    };

    // Si ya existe participante, solo guardamos respuestas
    if (this.participantId) {
      guardarRespuestas(this.participantId);
      return;
    }

    // Crear participante si no existe
    this.crf.crearParticipante(payload).subscribe({
      next: (res) => guardarRespuestas(res.idParticipante),
      error: (err) => {
        const msg = err?.error?.message || 'No se pudo crear el participante. Verifica los datos básicos.';
        alert(msg);
        this.isSubmitting = false;
      }
    });
  }

  private startAutoSave(): void {
    if (this.autoSaveHandle !== null) {
      clearInterval(this.autoSaveHandle);
    }

    this.autoSaveHandle = setInterval(() => {
      const key = this.getDraftKey();
      if (this.form) {
        this.crf.saveDraft(key, { ...this.form.getRawValue(), estado: 'autosave' });
        this.lastAutoSaveAt = new Date().toLocaleTimeString();
      }
    }, 15000); // 15 segundos
  }

  // Helper para validar completitud absoluta
  private getAllMissingFields(): string[] {
    const missing: string[] = [];

    // Controles base
    const baseRequired: Array<{ id: string; label: string }> = [
      { id: 'grupo', label: 'Grupo' },
      { id: 'nombre_completo', label: 'Nombre Completo' }
    ];
    baseRequired.forEach(({ id, label }) => {
      const control = this.form.get(id);
      if (!control || !control.value) {
        missing.push(label);
      }
    });

    if (this.schema && this.schema.sections) {
      this.schema.sections.forEach(section => {
        // Skip hidden sections
        if (!this.showSection(section)) return;

        section.fields.forEach(field => {
          // Skip hidden fields
          if (!this.showField(field)) return;

          const control = this.form.get(field.id);
          // Check value emptiness regardless of Validators.required
          if (control) {
            const val = control.value;
            const isArray = control instanceof FormArray;

            let isEmpty = false;
            if (isArray) {
              isEmpty = (val || []).length === 0;
            } else {
              isEmpty = (val === null || val === undefined || val === '');
            }

            if (isEmpty) {
              missing.push(field.label);
              // Optional: set explicit error for visual feedback
              if (!control.errors) {
                control.setErrors({ 'required': true });
              }
            }
          }
        });
      });
    }
    return missing;
  }

  private markMissingAsTouched() {
    Object.keys(this.form.controls).forEach(key => {
      this.form.get(key)?.markAsTouched();
    });
  }

  // Renamed/Replaced old getMissingRequiredFields
  private getMissingRequiredFields(): string[] {
    return this.getAllMissingFields();
  }


  private buildRespuestasMap(): Record<string, string> {
    const map: Record<string, string> = {};
    if (this.schema && this.schema.sections) {
      this.schema.sections.forEach(section => {
        section.fields.forEach(field => {
          // omite campos de control que no queremos mandar como variable
          if (field.id === 'grupo') return;
          const control = this.form.get(field.id);
          if (!control) return;
          const value = control.value;
          if (field.type === 'checkbox' && Array.isArray(value)) {
            if (value.length > 0) map[field.id] = value.join(',');
          } else if (value !== null && value !== undefined && value !== '') {
            map[field.id] = value.toString();
          }
        });
      });
    }

    // Include hidden/calculated fields manually
    const imcVal = this.form.get('imc')?.value;
    if (imcVal) map['imc'] = imcVal.toString();

    return map;
  }

  private getDraftKey(): string {
    if (this.recordId) return this.recordId;
    if (this.participantId) return `CRF_PART_${this.participantId}`;
    return 'CRF_BORRADOR_LOCAL';
  }

  private resetForm(): void {
    if (!this.form) return;
    const defaultGroup = 'control';
    this.form.reset({ grupo: defaultGroup });
    this.selectedGroup = defaultGroup as 'control' | 'caso';
  }

  private clearDraft(key: string): void {
    localStorage.removeItem(`crf_${key}`);
  }

  close(): void {
    if (this.autoSaveHandle !== null) {
      clearInterval(this.autoSaveHandle);
      this.autoSaveHandle = null;
    }
    this.open = false;
    this.closed.emit();
  }
}
