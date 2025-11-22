import { Component, AfterViewInit,ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, registerables } from 'chart.js';
import { AlertPanelComponent } from '../../alert-panel/alert-panel.component';
import { LogoutPanelComponent } from '../../shared/logout-panel/logout-panel.component';
import { FormsModule } from '@angular/forms';

import { CrfModalComponent } from '../crf/crf-modal.component';
import { AuthService } from '../../shared/auth/auth.service'; // tu servicio de roles

Chart.register(...registerables);

@Component({
  selector: 'app-reclutamiento',
  standalone: true,
  imports: [CommonModule,AlertPanelComponent, FormsModule, LogoutPanelComponent, CrfModalComponent],
  templateUrl: './reclutamiento.html',
  styleUrls: ['./reclutamiento.scss'],
})
export class ReclutamientoComponent implements AfterViewInit {
  usuarioNombre = 'Dra. González';

  @ViewChild(LogoutPanelComponent)
  logoutPanel!: LogoutPanelComponent;

  // 👇 Nuevas propiedades
  crfOpen = false; // controla el modal del CRF
  codigoBusqueda = ''; // búsqueda por código

 @ViewChild(CrfModalComponent) crfModal!: CrfModalComponent;
constructor(public auth: AuthService) {}

abrirCRF() { this.crfModal.open = true; }
cerrarCRF() { this.crfModal.open = false; }

  abrirLogoutPanel() {
    this.logoutPanel.showPanel();
  }

  // 👇 Abre/cierra modal CRF

  // 👇 Buscar un CRF por código
  buscarPorCodigo() {
    if (!this.codigoBusqueda.trim()) {
      alert('Ingresa un código para buscar.');
      return;
    }

  }
    
  ngAfterViewInit(): void {
    const ctx = document.getElementById('progresoChart') as HTMLCanvasElement;
    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun'],
        datasets: [
          {
            label: 'Participantes Reclutados',
            data: [60, 80, 120, 160, 220, 300],
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
    });
  }
}
