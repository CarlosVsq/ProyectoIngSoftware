import { Component, AfterViewInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, registerables } from 'chart.js';
import { AlertPanelComponent } from '../../alert-panel/alert-panel.component';
import { LogoutPanelComponent } from '../../shared/logout-panel/logout-panel.component';
import { AuthService } from '../../shared/auth/auth.service';

Chart.register(...registerables);

@Component({
  selector: 'app-estadisticas',
  standalone: true,
  imports: [CommonModule, AlertPanelComponent, LogoutPanelComponent],
  templateUrl: './estadisticas.html',
  styleUrls: ['./estadisticas.scss'],
})
export class EstadisticasComponent implements AfterViewInit {
  usuarioNombre = '';
  usuarioRol = '';
  @ViewChild(LogoutPanelComponent)
  logoutPanel!: LogoutPanelComponent;
  abrirLogoutPanel() {
    this.logoutPanel.showPanel();
  }

  constructor(private auth: AuthService) {
    this.usuarioNombre = this.auth.getUserName();
    this.usuarioRol = this.auth.getUserRole();
  }

  ngAfterViewInit(): void {
    this.renderGenVariantsChart();
  }

  private renderGenVariantsChart(): void {
    const ctx = document.getElementById('genVariantsChart') as HTMLCanvasElement;
    if (!ctx) return;
    new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['CDH1', 'BRCA1', 'BRCA2', 'MLH1', 'MSH2'],
        datasets: [
          {
            label: 'Benignas',
            data: [],
            backgroundColor: '#facc15',
          },
          {
            label: 'Probablemente patogénicas',
            data: [],
            backgroundColor: '#22c55e',
          },
          {
            label: 'Patogénicas',
            data: [],
            backgroundColor: '#ef4444',
          },
        ],
      },
      options: {
        responsive: true,
        scales: {
          y: {
            beginAtZero: true,
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
