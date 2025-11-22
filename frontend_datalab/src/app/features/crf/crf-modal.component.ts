// src/app/features/crf/crf-modal.component.ts
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormArray, AbstractControl, ValidatorFn } from '@angular/forms';
import { CrfService } from './crf.service';
import { CRFSchema, CRFSection, CRFField } from './schema';

@Component({
  selector: 'app-crf-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './crf-modal.component.html',
})
export class CrfModalComponent implements OnInit, OnDestroy {
  @Input() open = false;
  @Input() recordId: string | null = null;  // por si editas
  @Output() closed = new EventEmitter<void>();

  schema!: CRFSchema;
  form!: FormGroup;
  selectedGroup: 'caso' | 'control' = 'control';
  lastAutoSaveAt = '';
  missingRequiredLabels: string[] = [];
  private autoSaveHandle?: ReturnType<typeof setInterval>;
  isSubmitting = false;

  constructor(private fb: FormBuilder, private crf: CrfService) {}

  ngOnInit(): void {
    this.schema = this.crf.getSchema();
    this.buildForm();
    this.startAutoSave();
  }

  ngOnDestroy(): void {
    if (this.autoSaveHandle) {
      clearInterval(this.autoSaveHandle);
    }
  }

  private buildForm(): void {
    const controls: Record<string, AbstractControl> = {};

    // base controls
    controls['grupo'] = this.fb.control(this.selectedGroup, Validators.required);

    // crear controles para cada campo
    this.schema.sections.forEach((s: CRFSection) => {
      s.fields.forEach((f: CRFField) => {
        if (f.type === 'checkbox') {
          controls[f.id] = this.fb.array([]); // array de strings
        } else {
          controls[f.id] = this.fb.control(null, this.buildValidatorsForField(f));
        }
      });
    });

    this.form = this.fb.group(controls);

    // escuchar cambios de grupo para actualizar visibilidad
    this.form.get('grupo')?.valueChanges.subscribe((val) => {
      this.selectedGroup = val;
    });

    // si hay recordId intenta precargar borrador
    if (this.recordId) {
      const draft = this.crf.load(this.recordId);
      if (draft) {
        this.form.patchValue(draft);
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
    if (!section.groupVisibility || section.groupVisibility.length === 0) return true;
    return section.groupVisibility.includes(this.selectedGroup);
  }

  showField(field: CRFField): boolean {
    if (!field.groupVisibility || field.groupVisibility.length === 0) return true;
    return field.groupVisibility.includes(this.selectedGroup);
  }

  // Checkbox handler (evita el error del template)
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
    const codigo = this.form.get('codigo')?.value || 'SIN_CODIGO';
    this.crf.saveDraft(codigo, { ...this.form.value, estado: 'borrador' });
    this.lastAutoSaveAt = 'Borrador guardado manualmente';
    alert('Borrador guardado');
  }

  guardarFinal(): void {
    if (this.isSubmitting) return;
    this.isSubmitting = true;
    const codigo = this.form.get('codigo')?.value || 'SIN_CODIGO';
    if (this.form.invalid) {
      this.missingRequiredLabels = this.getMissingRequiredFields();
      alert(`Completa los campos obligatorios: ${this.missingRequiredLabels.join(', ')}`);
      this.isSubmitting = false;
      return;
    }
    const payload = {
      nombreCompleto: this.form.get('nombre')?.value || '',
      telefono: this.form.get('telefono')?.value || '',
      direccion: this.form.get('direccion')?.value || '',
      grupo: this.form.get('grupo')?.value || 'CONTROL'
    };

    this.crf.crearParticipante(payload).subscribe({
      next: (res) => {
<<<<<<< Updated upstream
        this.crf.saveFinalLocal(codigo, { ...this.form.value, estado: 'completo', idParticipante: res.idParticipante });
        alert('CRF guardado y participante creado');
        this.isSubmitting = false;
        this.close();
=======
        const respuestas = this.buildRespuestasMap();
        this.crf.guardarRespuestas(res.idParticipante, respuestas).subscribe({
          next: () => {
            this.crf.saveFinalLocal(codigo, { ...this.form.value, estado: 'completo', idParticipante: res.idParticipante });
            alert('CRF guardado y participante creado');
            this.isSubmitting = false;
            this.close();
          },
          error: (err) => {
            const msg = err?.error?.message || 'No se pudieron guardar las respuestas.';
            alert(msg);
            this.isSubmitting = false;
          }
        });
>>>>>>> Stashed changes
      },
      error: (err) => {
        const msg = err?.error?.message || 'No se pudo crear el participante. Verifica los datos.';
        alert(msg);
        this.isSubmitting = false;
      }
    });
  }

  private startAutoSave(): void {
    this.autoSaveHandle = setInterval(() => {
      const codigo = this.form.get('codigo')?.value || 'SIN_CODIGO';
      this.crf.saveDraft(codigo, { ...this.form.value, estado: 'autosave' });
      this.lastAutoSaveAt = new Date().toLocaleTimeString();
    }, 15000); // 15 segundos
  }

  private getMissingRequiredFields(): string[] {
    const missing: string[] = [];
    this.schema.sections.forEach(section => {
      section.fields.forEach(field => {
        const control = this.form.get(field.id);
        if (field.required && control && control.invalid) {
          missing.push(field.label);
        }
      });
    });
    return missing;
<<<<<<< Updated upstream
=======
  }

  private buildRespuestasMap(): Record<string, string> {
    const map: Record<string, string> = {};
    this.schema.sections.forEach(section => {
      section.fields.forEach(field => {
        // omite campos de control que no queremos mandar como variable
        if (field.id === 'codigo' || field.id === 'grupo') return;
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
    return map;
>>>>>>> Stashed changes
  }

  close(): void {
    if (this.autoSaveHandle) {
      clearInterval(this.autoSaveHandle);
    }
    this.open = false;
    this.closed.emit();
  }
}
