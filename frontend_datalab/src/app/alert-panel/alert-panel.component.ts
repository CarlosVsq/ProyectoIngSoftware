import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

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

  private readonly API_URL = 'http://localhost:8080/api/auditoria';

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.cargarAuditoria();
  }

  cargarAuditoria(): void {
    this.loading = true;
    // Utilizar el endpoint general que retorna las últimas acciones sin filtros
    // Esto asegura que siempre se muestre algo si hay datos
    this.http.get<any>(`${this.API_URL}?limit=${this.limit}`)
      .subscribe({
        next: (res) => {
          // Robust check for response format
          if (res && res.success && Array.isArray(res.data)) {
            this.auditorias = res.data;
          } else if (Array.isArray(res)) {
            this.auditorias = res;
          } else {
            console.warn('Formato de respuesta inesperado en alertas:', res);
            this.auditorias = [];
          }
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