// src/app/features/reclutamiento/variables-modal.component.ts
import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CrfService } from '../crf/crf.service';

@Component({
  selector: 'app-variables-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  styleUrls: ['./variables-modal.scss'], // Add this
  template: `
    <div *ngIf="open" class="fixed inset-0 bg-black/40 flex justify-center items-center z-50 transition-opacity duration-300">
      <div class="bg-white rounded-xl shadow-xl w-11/12 max-w-6xl overflow-hidden max-h-[90vh] flex flex-col animate-modal-enter">
        <div class="px-6 py-4 border-b flex items-center justify-between bg-gray-50">
          <h2 class="text-lg font-semibold text-gray-800">Personalizar Formulario (Variables)</h2>
          <button class="text-gray-500 hover:text-red-600" (click)="close()">✕</button>
        </div>

        <!-- TABS -->
        <div class="flex border-b">
            <button class="px-6 py-3 text-sm font-medium border-b-2 transition-colors"
                [class.border-blue-600]="activeTab === 'crear'"
                [class.text-blue-600]="activeTab === 'crear'"
                [class.border-transparent]="activeTab !== 'crear'"
                [class.text-gray-500]="activeTab !== 'crear'"
                (click)="activeTab = 'crear'">
                Crear Variable
            </button>
            <button class="px-6 py-3 text-sm font-medium border-b-2 transition-colors"
                [class.border-blue-600]="activeTab === 'lista'"
                [class.text-blue-600]="activeTab === 'lista'"
                [class.border-transparent]="activeTab !== 'lista'"
                [class.text-gray-500]="activeTab !== 'lista'"
                (click)="loadVariables(); activeTab = 'lista'">
                Administrar Variables
            </button>
        </div>

        <div class="p-6 overflow-y-auto flex-1">
          
          <!-- TAB CREAR -->
          <div *ngIf="activeTab === 'crear'">
              <div class="bg-blue-50 p-4 rounded-lg border border-blue-100 mb-6">
                <h3 class="font-semibold text-blue-800 mb-3">Agregar Nueva Variable</h3>
                <form [formGroup]="form" (ngSubmit)="crear()" class="space-y-4">
                  <div class="grid grid-cols-2 gap-4">
                    <div>
                      <label class="block text-xs font-medium text-gray-700 mb-1">Código (ID único)</label>
                      <input class="w-full border rounded px-3 py-2 text-sm" formControlName="codigoVariable" placeholder="EJ: PREGUNTA_01" (input)="onCodeInput($event)" />
                      <p *ngIf="form.get('codigoVariable')?.invalid && form.get('codigoVariable')?.touched" class="text-xs text-red-500 mt-1">
                        Solo mayúsculas, números y guiones bajos (sin espacios).
                      </p>
                    </div>
                    <div>
                    <label class="block text-xs font-medium text-gray-700 mb-1">Sección</label>
                    <input class="w-full border rounded px-3 py-2 text-sm" formControlName="seccion" placeholder="Generales" />
                    </div>
                  </div>

                  <div>
                    <label class="block text-xs font-medium text-gray-700 mb-1">Enunciado (Pregunta)</label>
                    <input class="w-full border rounded px-3 py-2 text-sm" formControlName="enunciado" placeholder="¿Cuál es su edad?" />
                  </div>

                  <div class="grid grid-cols-2 gap-4">
                    <div>
                      <label class="block text-xs font-medium text-gray-700 mb-1">Tipo de Dato</label>
                      <select class="w-full border rounded px-3 py-2 text-sm" formControlName="tipoDato">
                        <option value="Texto">Texto</option>
                        <option value="Numero">Número</option>
                        <option value="Fecha">Fecha</option>
                        <option value="SeleccionUnica">Selección Única (Radio)</option>
                        <option value="SeleccionMultiple">Selección Múltiple (Checkbox)</option>
                        <option value="Textarea">Texto Largo</option>
                      </select>
                    </div>
                    <div>
                      <label class="block text-xs font-medium text-gray-700 mb-1">Aplica a</label>
                      <select class="w-full border rounded px-3 py-2 text-sm" formControlName="aplicaA">
                        <option value="Ambos">Ambos</option>
                        <option value="Caso">Caso</option>
                        <option value="Control">Control</option>
                      </select>
                    </div>
                  </div>
                  
                  <div *ngIf="showOptions">
                    <label class="block text-xs font-medium text-gray-700 mb-1">Opciones (separadas por coma)</label>
                    <input class="w-full border rounded px-3 py-2 text-sm" formControlName="opciones" placeholder="Opción A, Opción B, Opción C" />
                  </div>

                  <div class="flex items-center gap-2">
                    <input type="checkbox" formControlName="esObligatoria" id="obl" />
                    <label for="obl" class="text-sm text-gray-700">Es obligatoria</label>
                  </div>

                  <div class="flex justify-end">
                    <button type="submit" [disabled]="form.invalid || isSubmitting" class="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-50">
                      {{ isSubmitting ? 'Guardando...' : 'Agregar Variable' }}
                    </button>
                  </div>
                </form>
              </div>
          </div>

          <!-- TAB LISTA -->
          <div *ngIf="activeTab === 'lista'">
              <div class="flex justify-between items-center mb-4">
                 <h3 class="font-semibold text-gray-800">Variables Existentes</h3>
                 <button (click)="loadVariables()" class="text-blue-600 text-sm hover:underline">Actualizar lista</button>
              </div>
              <div class="bg-white border rounded-lg overflow-hidden">
                <table class="w-full text-sm text-left">
                  <thead class="bg-gray-100 text-gray-600 font-medium">
                    <tr>
                      <th class="px-4 py-2">Código</th>
                      <th class="px-4 py-2">Enunciado</th>
                      <th class="px-4 py-2">Tipo</th>
                      <th class="px-4 py-2">Sección</th>
                      <th class="px-4 py-2 text-center">Obligatoria</th>
                      <th class="px-4 py-2">Acción</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y">
                    <tr *ngFor="let v of variables" class="hover:bg-blue-50 transition-colors duration-200">
                      <td class="px-4 py-2 font-mono text-xs">{{ v.codigo_variable || v.codigoVariable }}</td>
                      <td class="px-4 py-2">{{ v.enunciado }}</td>
                      <td class="px-4 py-2">{{ v.tipo_dato || v.tipoDato }}</td>
                      <td class="px-4 py-2 text-gray-500">{{ v.seccion }}</td>
                      <td class="px-4 py-2 text-center">
                         <div class="relative inline-block w-10 mr-2 align-middle select-none transition duration-200 ease-in">
                            <input type="checkbox" [checked]="v.es_obligatoria || v.esObligatoria" (change)="toggleObligatoria(v, $event)" 
                                   class="toggle-checkbox absolute block w-6 h-6 rounded-full bg-white border-4 appearance-none cursor-pointer border-gray-300"/>
                            <label class="toggle-label block overflow-hidden h-6 rounded-full bg-gray-300 cursor-pointer"></label>
                        </div>
                      </td>
                      <td class="px-4 py-2">
                          <button (click)="eliminar(v)" class="text-red-500 hover:underline text-xs font-bold">ELIMINAR</button>
                      </td>
                    </tr>
                    <tr *ngIf="variables.length === 0">
                      <td colspan="5" class="px-4 py-4 text-center text-gray-500">No hay variables cargadas.</td>
                    </tr>
                  </tbody>
                </table>
              </div>
          </div>

        </div>
      </div>
    </div>
  `
})
export class VariablesModalComponent implements OnInit {
  @Input() open = false;
  @Output() closed = new EventEmitter<void>();

  form!: FormGroup;
  variables: any[] = [];
  isSubmitting = false;
  activeTab: 'crear' | 'lista' = 'crear';

  constructor(private fb: FormBuilder, private crfService: CrfService) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      codigoVariable: ['', [Validators.required, Validators.pattern(/^[A-Z0-9_]+$/)]],
      enunciado: ['', Validators.required],
      tipoDato: ['Texto', Validators.required],
      opciones: [''],
      seccion: ['Generales', Validators.required],
      aplicaA: ['Ambos', Validators.required],
      esObligatoria: [false],
      ordenEnunciado: [0]
    });

    this.loadVariables();
  }

  get showOptions(): boolean {
    const type = this.form.get('tipoDato')?.value;
    return type === 'SeleccionUnica' || type === 'SeleccionMultiple';
  }

  loadVariables() {
    this.crfService.listarTodasLasVariables().subscribe({
      next: (data: any[]) => {
        this.variables = data.map((v: any) => ({
          codigo_variable: v.codigo_variable || v.codigoVariable,
          enunciado: v.enunciado,
          tipo_dato: v.tipo_dato || v.tipoDato,
          seccion: v.seccion || v.seccionVariable || 'Generales',
          es_obligatoria: v.es_obligatoria ?? v.esObligatoria
        }));
      },
      error: () => {
        // Fallback to schema if raw fails
        this.crfService.getSchema().subscribe(schema => {
          const flat: any[] = [];
          schema.sections.forEach(s => s.fields.forEach(f => {
            flat.push({
              codigoVariable: f.id,
              enunciado: f.label,
              tipoDato: f.type,
              seccion: s.title,
              es_obligatoria: f.required
            });
          }));
          this.variables = flat;
        });
      }
    });
  }

  crear() {
    if (this.form.invalid) {
      alert('Por favor complete todos los campos obligatorios (Código, Enunciado, Tipo de Dato).');
      return;
    }
    this.isSubmitting = true;
    const val = this.form.value;
    // Ensure ordenEnunciado is a valid number
    val.ordenEnunciado = (this.variables.length || 0) + 1;

    console.log('Enviando variable:', val);

    this.crfService.crearVariable(val).subscribe({
      next: (res) => {
        alert('Variable creada exitosamente');
        this.crfService.clearCache(); // Clear cache
        this.form.reset({
          codigoVariable: '',
          enunciado: '',
          tipoDato: 'Texto',
          seccion: 'Generales',
          aplicaA: 'Ambos',
          esObligatoria: false
        });
        this.isSubmitting = false;
        // Force reload
        this.loadVariables();
        // Switch to list tab to show result
        this.activeTab = 'lista';
      },
      error: (err) => {
        console.error('Error creando variable:', err);
        const msg = err?.error?.message || err?.message || 'Error desconocido del servidor';
        alert('Error al crear variable: ' + msg);
        this.isSubmitting = false;
      }
    });
  }

  eliminar(v: any) {
    const codigo = v.codigo_variable || v.codigoVariable;
    if (!confirm('¿Eliminar variable ' + codigo + '?')) return;

    this.crfService.deleteVariable(codigo).subscribe({
      next: () => {
        alert('Variable eliminada');
        this.crfService.clearCache(); // Clear cache
        this.loadVariables();
      },
      error: (err) => {
        console.error('Error eliminando variable:', err);
        alert('Error al eliminar: ' + (err?.message || 'Error desconocido'));
      }
    });
  }

  onCodeInput(event: any) {
    const input = event.target as HTMLInputElement;
    input.value = input.value.toUpperCase().replace(/[^A-Z0-9_]/g, '');
    this.form.get('codigoVariable')?.setValue(input.value);
  }

  toggleObligatoria(v: any, event: any) {
    const checked = event.target.checked;
    const codigo = v.codigo_variable || v.codigoVariable;

    // Optimistic update
    v.es_obligatoria = checked;
    v.esObligatoria = checked;

    this.crfService.actualizarObligatoria(codigo, checked).subscribe({
      next: () => {
        console.log('Estado actualizado');
        this.crfService.clearCache(); // Clear cache
      },
      error: (err) => {
        console.error('Error actualizando:', err);
        alert('Error al actualizar estado. Se revertirá el cambio.');
        // Revert
        v.es_obligatoria = !checked;
        v.esObligatoria = !checked;
        event.target.checked = !checked;
      }
    });
  }

  close() {
    this.open = false;
    this.closed.emit();
  }
}
