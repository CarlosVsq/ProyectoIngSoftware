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

Chart.register(...registerables);

@Component({
  selector: 'app-reclutamiento',
  standalone: true,
  imports: [CommonModule, AlertPanelComponent, FormsModule, LogoutPanelComponent, CrfModalComponent],
  templateUrl: './reclutamiento.html',
  styleUrls: ['./reclutamiento.scss'],
})
export class ReclutamientoComponent implements OnInit, AfterViewInit {
  usuarioNombre = 'Dra. González';
  crfOpen = false;
  codigoBusqueda = '';
  resumen?: DashboardResumen;
  private chart?: Chart;
  crfs: any[] = [];

  constructor(
    public auth: AuthService,
    private dashboard: DashboardService,
    private crfService: CrfService
  ) {}

  ngOnInit(): void {
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

    this.cargarCrfs();
  }

  abrirLogoutPanel(panel: LogoutPanelComponent) {
    panel.showPanel();
  }

  abrirCRF() { this.crfOpen = true; }
  cerrarCRF() { this.crfOpen = false; }
  verPdf(id: number) {
    window.open(`http://localhost:8080/api/export/participante/${id}/pdf`, '_blank');
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
        this.crfs = (resp.data || []).map((c: any) => ({
          ...c,
          resumenRespuestas: c.respuestas && c.respuestas.length
            ? c.respuestas.slice(0, 3).map((r: any) => `${r.codigoVariable}: ${r.valor}`).join(' | ')
            : ''
        }));
      },
      error: () => {
        this.crfs = [];
      }
    });
  }
}
