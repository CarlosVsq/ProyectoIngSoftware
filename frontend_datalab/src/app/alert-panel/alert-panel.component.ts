import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { API_BASE_URL } from '../shared/config/api.config';

export interface Auditoria {
  idAuditoria: number;
  usuario?: string;
  participante?: string;
  tablaAfectada: string;
  accion: string;
  detalleCambio: string;
  fechaCambio: string;
}

@Component({
  selector: 'app-alert-panel',
  standalone: true,
  imports: [CommonModule], 
  templateUrl: './alert-panel.component.html',
  styleUrls: ['./alert-panel.component.scss']
})
export class AlertPanelComponent implements OnInit {
  auditorias: Auditoria[] = [];
  limit = 10;
  loading = false;
  
  private readonly API_URL = `${API_BASE_URL}/auditoria`;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.cargarAuditoria();
  }

  cargarAuditoria(): void {
    this.loading = true;
    this.http.get<{ data: Auditoria[] }>(`${this.API_URL}?limit=${this.limit}`)
      .subscribe({
        next: (res) => {
          this.auditorias = res.data || [];
          this.loading = false;
        },
        error: (err) => { 
          console.error('Error cargando alertas:', err);
          this.loading = false; 
        }
      });
  }

  verMas(): void {
    if (this.limit >= 50) return;
    this.limit += 10;
    this.cargarAuditoria();
  }
  
  getActionColor(accion: string): string {
    if (accion === 'LOGIN') return 'text-green-600';
    if (accion === 'LOGOUT') return 'text-orange-500';
    if (accion === 'ELIMINAR' || accion === 'BORRAR') return 'text-red-600';
    return 'text-blue-600';
  }
}
