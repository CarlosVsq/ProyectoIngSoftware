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

  constructor(private auth: AuthService) {
    this.usuarioNombre = this.auth.getUserName();
    this.usuarioRol = this.auth.getUserRole();
  }

  abrirLogoutPanel() {
    this.logoutPanel.showPanel();
  }
  
  ngAfterViewInit(): void {
    this.renderExportChart();
  }

  private renderExportChart(): void {
    const ctx = document.getElementById('exportChart') as HTMLCanvasElement;
    if (!ctx) return;

    new Chart(ctx as any, {
      type: 'doughnut',
      data: {
        labels: ['Completadas', 'En proceso', 'Pendientes'],
        datasets: [{
          label: 'Exportaciones',
          data: [12, 3, 2],
          backgroundColor: ['#22c55e', '#facc15', '#e11d48'],
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: 'bottom'
          },
          tooltip: {
            backgroundColor: '#1f2937',
            titleColor: '#fff',
            bodyColor: '#fff'
          }
        }
      }
    });
  }

  descargarExcel(): void {
    window.open('http://localhost:8080/api/export/respuestas/excel', '_blank');
  }

  descargarPdf(): void {
    window.open('http://localhost:8080/api/export/respuestas/pdf', '_blank');
  }
}
