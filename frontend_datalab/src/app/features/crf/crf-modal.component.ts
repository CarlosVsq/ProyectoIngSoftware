// src/app/features/crf/crf-modal.component.ts
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormArray } from '@angular/forms';
import { CrfService } from './crf.service';
import { CRFSchema, CRFSection, CRFField } from './schema';

@Component({
  selector: 'app-crf-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './crf-modal.component.html',
})
export class CrfModalComponent implements OnInit {
  @Input() open = false;
  @Input() recordId: string | null = null;  // por si editas
  @Output() closed = new EventEmitter<void>();

  schema!: CRFSchema;
  form!: FormGroup;
  selectedGroup: 'caso' | 'control' = 'control';

  constructor(private fb: FormBuilder, private crf: CrfService) {}

  ngOnInit(): void {
    this.schema = this.crf.getSchema();
    this.buildForm();
  }

  private buildForm(): void {
    const controls: any = {};

    // base controls
    controls['grupo'] = this.fb.control(this.selectedGroup, Validators.required);

    // crear controles para cada campo
    this.schema.sections.forEach((s: CRFSection) => {
      s.fields.forEach((f: CRFField) => {
        if (f.type === 'checkbox') {
          controls[f.id] = this.fb.array([]); // array de strings
        } else {
          const v = f.required ? [null, Validators.required] : [null];
          controls[f.id] = this.fb.control(v[0], v[1]);
        }
      });
    });

    this.form = this.fb.group(controls);

    // escuchar cambios de grupo para actualizar visibilidad
    this.form.get('grupo')?.valueChanges.subscribe((val) => {
      this.selectedGroup = val;
    });
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
    this.crf.saveDraft(codigo, this.form.value);
    alert('Borrador guardado');
  }

  guardarFinal(): void {
    const codigo = this.form.get('codigo')?.value || 'SIN_CODIGO';
    if (this.form.invalid) {
      alert('Completa los campos obligatorios');
      return;
    }
    this.crf.saveFinal(codigo, this.form.value);
    alert('CRF guardado');
    this.close();
  }

  close(): void {
    this.open = false;
    this.closed.emit();
  }
}
