import { Component, AfterViewInit,ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  Chart,
  ChartConfiguration,
  ChartOptions,
  registerables
} from 'chart.js';
import { AlertPanelComponent } from '../../alert-panel/alert-panel.component';
import { LogoutPanelComponent } from '../../shared/logout-panel/logout-panel.component';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule,AlertPanelComponent, LogoutPanelComponent],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class DashboardComponent implements AfterViewInit {
  menuAbierto: boolean = false;

   usuarioNombre = 'Dra. González';
    @ViewChild(LogoutPanelComponent)
    logoutPanel!: LogoutPanelComponent;
    abrirLogoutPanel() {
      this.logoutPanel.showPanel();
    }
  ngAfterViewInit(): void {
    this.renderEdadChart();
    this.renderSexoChart();
    this.renderReclutamientoChart();
  }

  // === Distribución por Edad ===
  private renderEdadChart(): void {
    const ctx = document.getElementById('edadChart') as HTMLCanvasElement;
    if (!ctx) return;

    new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['18-30', '31-45', '46-60', '61-75', '76+'],
        datasets: [
          {
            label: 'Participantes',
            data: [45, 70, 95, 75, 30],
            backgroundColor: '#0056A3'
          },
          {
            label: 'Controles',
            data: [38, 60, 80, 70, 25],
            backgroundColor: '#2BB673'
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

    new Chart(ctx, {
      type: 'pie',
      data: {
        labels: ['Masculino', 'Femenino'],
        datasets: [
          {
            data: [52, 48],
            backgroundColor: ['#0056A3', '#2BB673']
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

    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun'],
        datasets: [
          {
            label: 'Participantes acumulados',
            data: [50, 80, 120, 160, 220, 300],
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
