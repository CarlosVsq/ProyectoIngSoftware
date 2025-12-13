import { Component, AfterViewInit, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, registerables } from 'chart.js';
import { AlertPanelComponent } from '../../alert-panel/alert-panel.component';
import { LogoutPanelComponent } from '../../shared/logout-panel/logout-panel.component';
import { FormsModule } from '@angular/forms';
import { CrfModalComponent } from '../crf/crf-modal.component';
import { AuthService } from '../../shared/auth/auth.service';
import { DashboardService, DashboardResumen } from '../../shared/dashboard/dashboard.service';
import { CrfService } from '../crf/crf.service';
import { VariablesModalComponent } from './variables-modal.component';
import { CRFSchema } from '../crf/schema';
import { ActivatedRoute } from '@angular/router';
import { ParticipanteService } from '../../shared/participantes/participante.service';
import { API_BASE_URL } from '../../shared/config/api.config';

Chart.register(...registerables);

@Component({
  selector: 'app-reclutamiento',
  standalone: true,
  imports: [CommonModule, AlertPanelComponent, FormsModule, LogoutPanelComponent, CrfModalComponent, VariablesModalComponent],
  templateUrl: './reclutamiento.html',
  styleUrls: ['./reclutamiento.scss'],
})
export class ReclutamientoComponent implements OnInit, AfterViewInit {
  usuarioNombre = '';
  usuarioRol = '';
  crfOpen = false;
  crfRecordId: string | null = null;
  crfPreload: any = null;
  crfParticipantId: number | null = null;

  // Variables Modal
  variablesOpen = false;

  codigoBusqueda = '';
  resumen?: DashboardResumen;
  private chart?: Chart;
  crfs: any[] = [];
  schema?: CRFSchema;



  constructor(
    public auth: AuthService,
    private dashboard: DashboardService,
    private crfService: CrfService,
    private route: ActivatedRoute,
    private participanteService: ParticipanteService
  ) { }

  ngOnInit(): void {
    this.usuarioNombre = this.auth.getUserName();
    this.usuarioRol = this.auth.getUserRole();
    this.cargarResumen();

    // Cargar schema primero para poder validar estados
    this.crfService.getSchema().subscribe(s => {
      this.schema = s;
      this.cargarCrfs();

      this.route.queryParams.subscribe(params => {
        const editId = params['editId'];
        if (editId) {
          console.log('Intentando cargar participante con ID:', editId);
          this.participanteService.obtenerParticipante(editId).subscribe({
            next: (p) => {
              console.log('Participante cargado correctamente:', p);
              this.editarCRF(p);
            },
            error: (err) => {
              console.error('Error al cargar participante:', err);
              alert('No se pudo cargar el participante para editar. Revisa la consola para más detalles.');
            }
          });
        }
      });
    });
  }

  cargarResumen() {
    this.dashboard.getResumen().subscribe({
      next: (res: { data: DashboardResumen }) => {
        this.resumen = res.data;
        if (this.chart) {
          this.chart.data.datasets[0].data = [
            this.resumen.casos,
            this.resumen.controles,
            this.resumen.completas + this.resumen.incompletas
          ];
          this.chart.update();
        } else {
          this.renderChart();
        }
      },
      error: () => {
        // deja números mock si falla
      }
    });
  }

  abrirLogoutPanel(panel: LogoutPanelComponent) {
    panel.showPanel();
  }

  abrirCRF() {
    // Nueva encuesta fresca
    this.crfRecordId = `CRF_NUEVO_${Date.now()}`;
    this.crfParticipantId = null;
    this.crfPreload = { grupo: 'control' };
    this.crfOpen = true;
  }
  cerrarCRF() {
    this.crfOpen = false;
    this.crfRecordId = null;
    this.crfPreload = null;
    this.crfParticipantId = null;
    this.cargarCrfs(); // Refrescar lista al cerrar
    this.cargarResumen(); // Refrescar metricas
  }

  abrirVariables() {
    this.variablesOpen = true;
  }
  cerrarVariables() {
    this.variablesOpen = false;
    // Recargar schema por si hubo cambios, y luego recargar lista
    this.crfService.getSchema().subscribe(s => {
      this.schema = s;
      this.cargarCrfs();
    });
  }

  eliminarCRF(crf: any) {
    if (!confirm('¿Seguro que deseas borrar este CRF?')) return;
    if (crf.idParticipante) {
      this.crfService.eliminarCrf(crf.idParticipante).subscribe({
        next: () => this.cargarCrfs(),
        error: () => alert('No se pudo eliminar el CRF')
      });
    }
    // limpiar drafts relacionados
    if (crf.codigoParticipante) {
      localStorage.removeItem(`crf_${crf.codigoParticipante}`);
    }
  }
  editarCRF(crf: any) {
    const preload: any = {
      grupo: crf.grupo ? crf.grupo.toLowerCase() : 'control',
      telefono: crf.telefono || '',
      direccion: crf.direccion || ''
    };
    if (crf.respuestas) {
      crf.respuestas.forEach((r: any) => {
        if (r.codigoVariable) {
          preload[r.codigoVariable] = r.valor;
        }
      });
    }
    this.crfRecordId = crf.codigoParticipante || crf.idParticipante?.toString();
    this.crfParticipantId = crf.idParticipante || null;
    this.crfPreload = preload;
    this.crfOpen = true;
  }
  verPdf(id: number) {
    window.open(`${API_BASE_URL}/export/participante/${id}/pdf`, '_blank');
  }

  buscarPorCodigo() {
    if (!this.codigoBusqueda.trim()) {
      alert('Ingresa un código para buscar.');
      return;
    }
    // TODO: implementar búsqueda
  }

  ngAfterViewInit(): void {
    this.renderChart();
  }

  private renderChart(): void {
    const ctx = document.getElementById('progresoChart') as HTMLCanvasElement;
    if (!ctx) return;
    const dataPoints = this.resumen
      ? [this.resumen.casos, this.resumen.controles, this.resumen.completas + this.resumen.incompletas]
      : [60, 80, 100];
    const config: any = {
      type: 'line',
      data: {
        labels: ['Casos', 'Controles', 'Fichas'],
        datasets: [
          {
            label: 'Reclutamiento',
            data: dataPoints,
            borderColor: '#22c55e',
            backgroundColor: 'rgba(34, 197, 94, 0.2)',
            fill: true,
            tension: 0.3,
            borderWidth: 2,
            pointBackgroundColor: '#16a34a',
            pointRadius: 4,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false } },
        scales: {
          x: { grid: { display: false } },
          y: { grid: { color: '#f3f4f6' } },
        },
      },
    };
    this.chart = new Chart(ctx as any, config);
  }

  private cargarCrfs(): void {
    this.crfService.listarCrfs(30).subscribe({
      next: (resp) => {
        const rawData = resp.data || [];

        this.crfs = rawData.map((c: any) => {
          // Analizar estado real basado en schema
          const estadoAnalisis = this.analizarEstado(c);

          return {
            ...c,
            estadoFicha: estadoAnalisis.completo ? 'COMPLETO' : 'INCOMPLETO',
            colorEstado: estadoAnalisis.completo ? 'green' : 'purple', // purple para incompleto
            variablesFaltantes: estadoAnalisis.faltantes,
            resumenRespuestas: c.respuestas && c.respuestas.length
              ? c.respuestas.slice(0, 3).map((r: any) => `${r.codigoVariable}: ${r.valor}`).join(' | ')
              : 'Sin respuestas capturadas.'
          };
        });
      },
      error: () => {
        this.crfs = [];
      }
    });
  }

  private analizarEstado(participante: any): { completo: boolean; faltantes: string[] } {
    if (!this.schema) return { completo: false, faltantes: [] };

    // Convertir respuestas a Set de codigos
    const respuestasIds = new Set<string>();
    if (participante.respuestas) {
      participante.respuestas.forEach((r: any) => respuestasIds.add(r.codigoVariable));
    }

    const grupo = (participante.grupo || 'CONTROL').toLowerCase();
    const faltantes: string[] = [];

    this.schema.sections.forEach(section => {
      // Checar visibilidad de seccion (si tiene section.groupVisibility)
      if (section.groupVisibility && !section.groupVisibility.includes(grupo as any)) return;

      section.fields.forEach(field => {
        // Checar visibilidad de campo
        if (field.groupVisibility && !field.groupVisibility.includes(grupo as any)) return;

        // Validar requeridos
        if (field.required) {
          // Normalizar ambas partes para comparar (lowercase y trim)
          const fieldId = (field.id || '').toLowerCase().trim();
          let answered = false;

          for (const ansId of respuestasIds) {
            if (ansId.toLowerCase().trim() === fieldId) {
              answered = true;
              break;
            }
          }

          if (!answered) {
            faltantes.push(field.label || field.id);
          }
        }
      });
    });

    return {
      completo: faltantes.length === 0,
      faltantes
    };
  }
}
