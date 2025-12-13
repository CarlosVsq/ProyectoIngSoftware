import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AlertPanelComponent } from '../../alert-panel/alert-panel.component';
import { LogoutPanelComponent } from '../../shared/logout-panel/logout-panel.component';
import { AuthService } from '../../shared/auth/auth.service';
import { API_BASE_URL } from '../../shared/config/api.config';

interface Auditoria {
  idAuditoria: number;
  usuario: string;
  participante?: string;
  tablaAfectada: string;
  accion: string;
  detalleCambio: string;
  fechaCambio: string;
}

// Interfaz actualizada para los nuevos KPIs
interface Stats {
  accesos: number;    // Login 24h
  actividad: number;  // Total eventos 24h
  cierres: number;    // Logout 24h (Nuevo)
  alertas: number;
}

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule, FormsModule, AlertPanelComponent, LogoutPanelComponent],
  templateUrl: './auditoria.html',
  styleUrls: ['./auditoria.scss']
})
export class AuditoriaComponent implements OnInit {
  usuarioNombre = '';
  usuarioRol = '';

  registros: Auditoria[] = [];
  // Inicializamos con los nuevos campos
  stats: Stats = { accesos: 0, actividad: 0, cierres: 0, alertas: 0 };

  filtroUsuario: string = '';
  filtroEvento: string = '';
  filtroFechaInicio: string = '';
  filtroFechaFin: string = '';

  loading = false;
  page = 0;
  totalPages = 0;
  private readonly API_BASE = `${API_BASE_URL}/auditoria`;

  @ViewChild(LogoutPanelComponent)
  logoutPanel!: LogoutPanelComponent;

  constructor(private auth: AuthService, private http: HttpClient) {
    this.usuarioNombre = this.auth.getUserName();
    this.usuarioRol = this.auth.getUserRole();
  }

  ngOnInit(): void {
    this.cargarStats();
    this.buscarEventos();
  }

  abrirLogoutPanel() {
    this.logoutPanel.showPanel();
  }

  cargarStats() {
    this.http.get<{ data: Stats }>(`${this.API_BASE}/stats`).subscribe({
      next: (res) => this.stats = res.data,
      error: (e) => console.error('Error cargando stats', e)
    });
  }

  buscarEventos() {
    this.loading = true;
    let params: any = { page: this.page, size: 10 };

    if (this.filtroEvento && this.filtroEvento !== 'Todos los eventos') {
      params.accion = this.filtroEvento;
    }
    if (this.filtroFechaInicio) params.fechaInicio = this.filtroFechaInicio;
    if (this.filtroFechaFin) params.fechaFin = this.filtroFechaFin;

    this.http.get<{ data: { content: Auditoria[], totalPages: number } }>(`${this.API_BASE}/search`, { params })
      .subscribe({
        next: (res) => {
          this.registros = res.data.content;
          this.totalPages = res.data.totalPages;
          this.loading = false;
        },
        error: (e) => {
          console.error('Error buscando eventos', e);
          this.loading = false;
        }
      });
  }

  getIconForAction(accion: string): string {
    if (accion === 'LOGIN') return 'login';
    if (accion === 'LOGOUT') return 'logout';
    if (accion === 'CREAR') return 'add_circle';
    if (accion === 'ACTUALIZAR') return 'edit';
    if (accion === 'BORRAR') return 'delete';
    return 'info';
  }

  getClassForAction(accion: string): string {
    if (accion === 'LOGIN') return 'text-green-700 bg-green-50 border-green-200';
    if (accion === 'LOGOUT') return 'text-orange-700 bg-orange-50 border-orange-200';
    if (accion.includes('ERROR') || accion.includes('DENEGADO')) return 'text-red-700 bg-red-50 border-red-200';
    if (accion === 'ACTUALIZAR') return 'text-blue-700 bg-blue-50 border-blue-200';
    return 'text-gray-700 bg-gray-50 border-gray-200';
  }
}
