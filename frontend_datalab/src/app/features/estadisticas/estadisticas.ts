import { Component, AfterViewInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, registerables } from 'chart.js';
import { AlertPanelComponent } from '../../alert-panel/alert-panel.component';
import { LogoutPanelComponent } from '../../shared/logout-panel/logout-panel.component';

Chart.register(...registerables);

@Component({
  selector: 'app-estadisticas',
  standalone: true,
  imports: [CommonModule,AlertPanelComponent, LogoutPanelComponent],
  templateUrl: './estadisticas.html',
  styleUrls: ['./estadisticas.scss'],
})
export class EstadisticasComponent implements AfterViewInit {
   usuarioNombre = 'Dra. González';
    @ViewChild(LogoutPanelComponent)
    logoutPanel!: LogoutPanelComponent;
    abrirLogoutPanel() {
      this.logoutPanel.showPanel();
    }

  ngAfterViewInit(): void {
    this.renderGenVariantsChart();
  }

  private renderGenVariantsChart(): void {
    const ctx = document.getElementById('genVariantsChart') as HTMLCanvasElement;
    new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['CDH1', 'BRCA1', 'BRCA2', 'MLH1', 'MSH2'],
        datasets: [
          {
            label: 'Benignas',
            data: [2, 1, 3, 2, 1],
            backgroundColor: '#facc15',
          },
          {
            label: 'Probablemente patogénicas',
            data: [3, 2, 3, 1, 2],
            backgroundColor: '#22c55e',
          },
          {
            label: 'Patogénicas',
            data: [12, 8, 6, 9, 5],
            backgroundColor: '#ef4444',
          },
        ],
      },
      options: {
        responsive: true,
        scales: {
          y: {
            beginAtZero: true,
            ticks: { stepSize: 3 },
            grid: { color: '#f3f4f6' },
          },
          x: { grid: { display: false } },
        },
        plugins: {
          legend: { position: 'bottom', labels: { boxWidth: 12 } },
        },
      },
    });
  }
}
