import { Component, AfterViewInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';
import { AlertPanelComponent } from '../../alert-panel/alert-panel.component';
import { LogoutPanelComponent } from '../../shared/logout-panel/logout-panel.component';
import { AuthService } from '../../shared/auth/auth.service';
import { API_BASE_URL } from '../../shared/config/api.config';

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




  constructor(private auth: AuthService, private http: HttpClient) {
    this.usuarioNombre = this.auth.getUserName();
    this.usuarioRol = this.auth.getUserRole();
  }

  abrirLogoutPanel() {
    this.logoutPanel.showPanel();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.loadChartData();
    }, 100);
  }

  private loadChartData() {
    this.http.get<any>(`${API_BASE_URL}/export/stats`).subscribe({
      next: (data) => {
        this.renderExportChart(data);
      },
      error: (err) => {
        console.error('Error loading chart stats', err);
        // Fallback to empty chart
        this.renderExportChart({});
      }
    });
  }

  private renderExportChart(apiData: any): void {
    const canvas = document.getElementById('exportChart') as HTMLCanvasElement;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    if (Chart.getChart(canvas)) {
      Chart.getChart(canvas)?.destroy();
    }

    // Process data
    const labels = Object.keys(apiData); // Dates
    const values = Object.values(apiData); // Counts

    // Create Gradient
    const gradient = ctx.createLinearGradient(0, 0, 0, 300);
    gradient.addColorStop(0, 'rgba(67, 56, 202, 0.4)'); // Indigo 700 with opacity
    gradient.addColorStop(1, 'rgba(67, 56, 202, 0.0)'); // Transparent

    new Chart(canvas as any, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [{
          label: 'Exportaciones Realizadas',
          data: values,
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
              label: (context: any) => ` ${context.parsed.y} archivos exportados`
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
    this.downloadFile(`${API_BASE_URL}/export/excel`, 'datos_completos.xlsx');
  }

  descargarCsv(): void {
    this.downloadFile(`${API_BASE_URL}/export/csv-stata`, 'datos_stata.csv');
  }

  descargarExcelDicotomizado(): void {
    this.downloadFile(`${API_BASE_URL}/export/excel-dicotomizado`, 'datos_codificados.xlsx');
  }

  descargarCsvDicotomizado(): void {
    this.downloadFile(`${API_BASE_URL}/export/csv-dicotomizado`, 'datos_codificados.csv');
  }

  descargarLeyenda(): void {
    this.downloadFile(`${API_BASE_URL}/export/leyenda-pdf`, 'leyenda_variables.pdf');
  }

  private downloadFile(url: string, filename: string) {
    this.http.get(url, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const a = document.createElement('a');
        const objectUrl = URL.createObjectURL(blob);
        a.href = objectUrl;
        a.download = filename;
        a.click();
        URL.revokeObjectURL(objectUrl);

        // Refresh chart immediately since request completed
        this.loadChartData();
      },
      error: (err) => {
        console.error('Download failed', err);
        alert('Error al descargar el archivo.');
      }
    });
  }
}
