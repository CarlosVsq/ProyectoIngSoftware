import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';

interface Auditoria {
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
  imports: [CommonModule, HttpClientModule],
  templateUrl: './alert-panel.component.html',
  styleUrls: ['./alert-panel.component.scss']
})
export class AlertPanelComponent implements OnInit {
  auditorias: Auditoria[] = [];
  limit = 10;
  loading = false;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.cargarAuditoria();
  }

  cargarAuditoria(): void {
    this.loading = true;
    this.http.get<{ data: Auditoria[] }>(`http://localhost:8080/api/auditoria?limit=${this.limit}`)
      .subscribe({
        next: (res) => {
          this.auditorias = res.data;
          this.loading = false;
        },
        error: () => { this.loading = false; }
      });
  }

  verMas(): void {
    if (this.limit >= 100) return;
    this.limit = Math.min(this.limit + 10, 100);
    this.cargarAuditoria();
  }
}
