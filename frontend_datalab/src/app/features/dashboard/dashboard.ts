import { Component, AfterViewInit, ViewChild, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, ChartOptions, registerables } from 'chart.js';
import { AlertPanelComponent } from '../../alert-panel/alert-panel.component';
import { LogoutPanelComponent } from '../../shared/logout-panel/logout-panel.component';
import { DashboardService, DashboardResumen } from '../../shared/dashboard/dashboard.service';
import { AuthService } from '../../shared/auth/auth.service';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, AlertPanelComponent, LogoutPanelComponent],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class DashboardComponent implements OnInit, AfterViewInit {
  menuAbierto: boolean = false;

  usuarioNombre = '';
  usuarioRol = '';
  @ViewChild(LogoutPanelComponent)
  logoutPanel!: LogoutPanelComponent;
  abrirLogoutPanel() {
    this.logoutPanel.showPanel();
  }

  resumen?: DashboardResumen;
  chartEdad?: Chart;
  chartSexo?: Chart;
  chartReclutamiento?: Chart;

  constructor(private dashboard: DashboardService, private auth: AuthService) {}

  ngOnInit(): void {
    this.usuarioNombre = this.auth.getUserName();
    this.usuarioRol = this.auth.getUserRole();
    this.dashboard.getResumen().subscribe({
      next: (res) => {
        this.resumen = res.data;
        this.updateCharts();
      },
      error: () => {
        this.resumen = undefined;
        this.updateCharts();
      }
    });
  }

  ngAfterViewInit(): void {
    this.renderEdadChart();
    this.renderSexoChart();
    this.renderReclutamientoChart();
  }

  private updateCharts(): void {
    if (this.chartEdad) {
      const edades = this.resumen?.porEdad || {};
      this.chartEdad.data.datasets[0].data = [
        edades['18-30'] || 0,
        edades['31-45'] || 0,
        edades['46-60'] || 0,
        edades['61-75'] || 0,
        edades['76+'] || 0
      ];
      this.chartEdad.update();
    }
    if (this.chartSexo) {
      const sexo = this.resumen?.porSexo || { masculino: 0, femenino: 0, otros: 0 };
      this.chartSexo.data.datasets[0].data = [sexo.masculino, sexo.femenino, sexo.otros];
      this.chartSexo.update();
    }
    if (this.chartReclutamiento && this.resumen?.serieReclutamiento) {
      this.chartReclutamiento.data.labels = this.resumen.serieReclutamiento.map(p => p.label);
      this.chartReclutamiento.data.datasets[0].data = this.resumen.serieReclutamiento.map(p => p.valor);
      this.chartReclutamiento.update();
    }
  }

  // === Distribución por Edad ===
  private renderEdadChart(): void {
    const ctx = document.getElementById('edadChart') as HTMLCanvasElement;
    if (!ctx) return;
    const edades = this.resumen?.porEdad || {};
    this.chartEdad = new Chart(ctx as any, {
      type: 'bar',
      data: {
        labels: ['18-30', '31-45', '46-60', '61-75', '76+'],
        datasets: [
          {
            label: 'Participantes',
            data: [
              edades['18-30'] || 0,
              edades['31-45'] || 0,
              edades['46-60'] || 0,
              edades['61-75'] || 0,
              edades['76+'] || 0
            ],
            backgroundColor: '#0056A3'
          }
        ]
      },
      options: {
        responsive: true,
        plugins: { legend: { position: 'bottom' } }
      } as ChartOptions
    });
  }

  // === Distribución por Sexo ===
  private renderSexoChart(): void {
    const ctx = document.getElementById('sexoChart') as HTMLCanvasElement;
    if (!ctx) return;
    const sexo = this.resumen?.porSexo || { masculino: 0, femenino: 0, otros: 0 };
    this.chartSexo = new Chart(ctx as any, {
      type: 'pie',
      data: {
        labels: ['Masculino', 'Femenino', 'Otros'],
        datasets: [
          {
            data: [sexo.masculino, sexo.femenino, sexo.otros],
            backgroundColor: ['#0056A3', '#2BB673', '#f59e0b']
          }
        ]
      },
      options: {
        responsive: true,
        plugins: { legend: { position: 'bottom' } }
      } as ChartOptions
    });
  }

  // === Evolución del Reclutamiento ===
  private renderReclutamientoChart(): void {
    const ctx = document.getElementById('reclutamientoChart') as HTMLCanvasElement;
    if (!ctx) return;
    const labels = this.resumen?.serieReclutamiento?.map(p => p.label) || ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun'];
    const valores = this.resumen?.serieReclutamiento?.map(p => p.valor) || [0, 0, 0, 0, 0, 0];
    this.chartReclutamiento = new Chart(ctx as any, {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: 'Participantes acumulados',
            data: valores,
            borderColor: '#2BB673',
            backgroundColor: 'rgba(43,182,115,0.2)',
            fill: true,
            tension: 0.3,
            pointRadius: 5,
            pointBackgroundColor: '#FF4081'
          }
        ]
      },
      options: {
        responsive: true,
        plugins: { legend: { position: 'bottom' } },
        scales: { y: { beginAtZero: true } }
      } as ChartOptions
    });
  }
}
