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
    const canvas = document.getElementById('exportChart') as HTMLCanvasElement;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    if (Chart.getChart(canvas)) {
      Chart.getChart(canvas)?.destroy();
    }

    // Create Gradient
    const gradient = ctx.createLinearGradient(0, 0, 0, 300);
    gradient.addColorStop(0, 'rgba(67, 56, 202, 0.4)'); // Indigo 700 with opacity
    gradient.addColorStop(1, 'rgba(67, 56, 202, 0.0)'); // Transparent

    new Chart(canvas as any, {
      type: 'line',
      data: {
        labels: ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'],
        datasets: [{
          label: 'Exportaciones Realizadas',
          data: [2, 5, 3, 8, 4, 1, 6], // Dummy data representing weekly activity
          fill: true,
          backgroundColor: gradient,
          borderColor: '#4338ca', // Indigo 700
          borderWidth: 2,
          tension: 0.4,
          pointBackgroundColor: '#ffffff',
          pointBorderColor: '#4338ca',
          pointBorderWidth: 2,
          pointRadius: 4,
          pointHoverRadius: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false // Hide legend for cleaner look
          },
          tooltip: {
            backgroundColor: '#1e293b',
            padding: 12,
            titleFont: { size: 13 },
            bodyFont: { size: 12 },
            displayColors: false,
            callbacks: {
              label: (context) => ` ${context.parsed.y} archivos exportados`
            }
          }
        },
        scales: {
          x: {
            grid: {
              display: false
            },
            ticks: {
              font: { size: 11 }
            }
          },
          y: {
            beginAtZero: true,
            grid: {
              display: true,
              color: '#f1f5f9', // Very light gray grid
              tickLength: 0
            },
            border: { display: false }, // Hide y-axis line
            ticks: {
              stepSize: 2,
              font: { size: 11 }
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
    window.open('http://localhost:8080/api/export/csv-stata', '_blank');
  }

  descargarLeyenda(): void {
    window.open('http://localhost:8080/api/export/leyenda-pdf', '_blank');
  }
}
