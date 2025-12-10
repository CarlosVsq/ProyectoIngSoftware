import { Component, AfterViewInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, registerables } from 'chart.js';
import { AlertPanelComponent } from '../../alert-panel/alert-panel.component';
import { LogoutPanelComponent } from '../../shared/logout-panel/logout-panel.component';
import { AuthService } from '../../shared/auth/auth.service';

Chart.register(...registerables);

@Component({
  selector: 'app-exportaciones',
  standalone: true,
  imports: [CommonModule, AlertPanelComponent, LogoutPanelComponent],
  templateUrl: './exportaciones.html',
  styleUrls: ['./exportaciones.scss']
})
export class ExportacionesComponent implements AfterViewInit {
  usuarioNombre = '';
  usuarioRol = '';

  @ViewChild(LogoutPanelComponent)
  logoutPanel!: LogoutPanelComponent;

  recentExports = [
    { title: 'Base Completa', date: '08 Dec 2025', status: 'Completado', icon: 'table_view', color: 'bg-indigo-100 text-indigo-700' },
    { title: 'Casos vs Controles', date: '07 Dec 2025', status: 'Completado', icon: 'analytics', color: 'bg-green-100 text-green-700' },
    { title: 'Reporte Mensual', date: '01 Dec 2025', status: 'Archivado', icon: 'assignment', color: 'bg-gray-100 text-gray-700' }
  ];

  constructor(private auth: AuthService) {
    this.usuarioNombre = this.auth.getUserName();
    this.usuarioRol = this.auth.getUserRole();
  }

  abrirLogoutPanel() {
    this.logoutPanel.showPanel();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.renderExportChart();
    }, 100);
  }

  private renderExportChart(): void {
    const ctx = document.getElementById('exportChart') as HTMLCanvasElement;
    if (!ctx) return;

    if (Chart.getChart(ctx)) {
      Chart.getChart(ctx)?.destroy();
    }

    new Chart(ctx as any, {
      type: 'doughnut',
      data: {
        labels: ['Completadas', 'En proceso', 'Pendientes'],
        datasets: [{
          data: [15, 3, 2],
          backgroundColor: [
            '#4338ca', // Indigo 700
            '#0ea5e9', // Sky 500
            '#e2e8f0', // Slate 200
          ],
          borderWidth: 0,
          hoverOffset: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '80%',
        plugins: {
          legend: {
            position: 'right',
            labels: {
              usePointStyle: true,
              boxWidth: 8,
              padding: 15,
              font: { family: "'Inter', sans-serif", size: 11 }
            }
          }
        }
      }
    });
  }

  descargarExcel(): void {
    window.open('http://localhost:8080/api/export/excel', '_blank');
  }

  descargarCsv(): void {
    window.open('http://localhost:8080/api/export/csv', '_blank');
  }
}
