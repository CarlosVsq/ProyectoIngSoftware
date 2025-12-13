// src/app/features/crf/crf-modal.component.ts
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormArray, AbstractControl, ValidatorFn } from '@angular/forms';
import { CrfService } from './crf.service';
import { CRFSchema, CRFSection, CRFField } from './schema';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-crf-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
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
  private autoSaveHandle?: ReturnType<typeof setInterval>;
  isSubmitting = false;
  private schemaSub?: Subscription;

  constructor(private fb: FormBuilder, private crf: CrfService) { }

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
    if (this.autoSaveHandle) {
      clearInterval(this.autoSaveHandle);
    }
    if (this.schemaSub) {
      this.schemaSub.unsubscribe();
    }
  }

  private buildForm(): void {
    const controls: Record<string, AbstractControl> = {};

    // base controls
    controls['grupo'] = this.fb.control(this.selectedGroup, Validators.required);

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
    const key = this.getDraftKey();
    const isEditing = this.participantId !== null;

    if (!isEditing && key) {
      const draft = this.crf.load(key);
      if (draft) {
        this.form.patchValue(draft);
        return;
      }
    }

    if (this.preloadData) {
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
    if (!this.schema || !this.schema.sections) return;

    let pesoId = '';
    let tallaId = '';
    let imcId = '';

    // Search schema for fields matching Weight/Height/BMI patterns
    for (const section of this.schema.sections) {
      for (const field of section.fields) {
        const id = field.id.toUpperCase();
        const label = (field.label || '').toUpperCase();

        // Detect Weight
        if (!pesoId && (id === 'PESO' || id === 'WEIGHT' || id === 'KG' || label.includes('PESO') || label.includes('WEIGHT'))) {
          pesoId = field.id;
        }
        // Detect Height
        if (!tallaId && (id === 'TALLA' || id === 'ESTATURA' || id === 'ALTURA' || id === 'HEIGHT' || label.includes('TALLA') || label.includes('ESTATURA') || label.includes('ALTURA'))) {
          tallaId = field.id;
        }
        // Detect BMI
        if (!imcId && (id === 'IMC' || id === 'BMI' || label.includes('IMC') || label.includes('BMI'))) {
          imcId = field.id;
        }
      }
    }

    console.log('Auto-BMI Config:', { pesoId, tallaId, imcId });

    if (pesoId && tallaId && imcId) {
      const pesoControl = this.form.get(pesoId);
      const tallaControl = this.form.get(tallaId);
      const imcControl = this.form.get(imcId);

      if (pesoControl && tallaControl && imcControl) {
        // Disable IMC to prevent manual edit
        imcControl.disable();

        const calculate = () => {
          const peso = parseFloat(pesoControl.value);
          let talla = parseFloat(tallaControl.value);

          if (!isNaN(peso) && !isNaN(talla) && talla > 0 && peso > 0) {
            // Heuristic: If height is likely in cm (e.g. > 3), convert to meters
            if (talla > 3) {
              talla = talla / 100;
            }

            const imc = peso / (talla * talla);
            imcControl.setValue(imc.toFixed(2));
          } else {
            imcControl.setValue('');
          }
        };

        // Trigger initial calculation
        calculate();

        pesoControl.valueChanges.subscribe(calculate);
        tallaControl.valueChanges.subscribe(calculate);
      }
    }
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
  guardarBorrador(): void {
    const key = this.getDraftKey();
    this.crf.saveDraft(key, { ...this.form.value, estado: 'borrador' });
    this.lastAutoSaveAt = 'Borrador guardado manualmente';
    alert('Borrador guardado');
  }

  guardarFinal(): void {
    console.warn('Form invalid', this.form.value, this.form);
    console.warn('Requeridos faltantes', this.missingRequiredLabels);

    if (this.isSubmitting) return;
    this.isSubmitting = true;
    const key = this.getDraftKey();
    if (this.form.invalid) {
      this.missingRequiredLabels = this.getMissingRequiredFields();
      this.crf.saveDraft(key, { ...this.form.value, estado: 'borrador' });
      alert(`Guardado como borrador. Faltan campos: ${this.missingRequiredLabels.join(', ')}`);
      this.isSubmitting = false;
      return;
    }

    const payload = {
      nombreCompleto: this.form.get('nombre_completo')?.value || '',
      telefono: this.form.get('telefono')?.value || '',
      direccion: this.form.get('direccion')?.value || '',
      grupo: (this.form.get('grupo')?.value || 'CONTROL').toString().toUpperCase()
    };

    const respuestas = this.buildRespuestasMap();

    const guardarRespuestas = (idParticipante: number) => {
      this.crf.guardarRespuestas(idParticipante, respuestas, {
        nombre: payload.nombreCompleto,
        telefono: payload.telefono,
        direccion: payload.direccion,
        grupo: payload.grupo
      }).subscribe({
        next: () => {
          this.crf.saveFinalLocal(key, { ...this.form.value, estado: 'completo', idParticipante });
          alert('CRF guardado');
          this.isSubmitting = false;
          this.close();
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
        const msg = err?.error?.message || 'No se pudo crear el participante. Verifica los datos.';
        alert(msg);
        this.isSubmitting = false;
      }
    });
  }

  private startAutoSave(): void {
    this.autoSaveHandle = setInterval(() => {
      const key = this.getDraftKey();
      if (this.form) {
        this.crf.saveDraft(key, { ...this.form.value, estado: 'autosave' });
        this.lastAutoSaveAt = new Date().toLocaleTimeString();
      }
    }, 15000); // 15 segundos
  }

  private getMissingRequiredFields(): string[] {
    const missing: string[] = [];

    // Controles base (no vienen en el schema)
    const baseRequired: Array<{ id: string; label: string }> = [
      { id: 'grupo', label: 'Grupo' },
    ];
    baseRequired.forEach(({ id, label }) => {
      const control = this.form.get(id);
      if (control && control.invalid) {
        missing.push(label);
      }
    });

    // Controles dinámicos del schema
    if (this.schema && this.schema.sections) {
      this.schema.sections.forEach(section => {
        section.fields.forEach(field => {
          const control = this.form.get(field.id);
          if (field.required && control) {
            const isArray = control instanceof FormArray;
            const isMissing = isArray
              ? (control.value || []).length === 0
              : control.invalid;
            if (isMissing) {
              missing.push(field.label);
            }
          }
        });
      });
    }
    return missing;
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
    if (this.autoSaveHandle) {
      clearInterval(this.autoSaveHandle);
    }
    this.open = false;
    this.closed.emit();
  }
}
