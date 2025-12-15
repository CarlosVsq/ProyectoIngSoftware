import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../shared/auth/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.scss'],
})
export class SidebarComponent implements OnInit {
  /** true: visible (off-canvas); false: oculto */
  @Input({ required: true }) open = true;
  /** true: modo mini (icon-only) en ≥lg; ignorado en móvil */
  @Input() mini = false;

  @Output() openChange = new EventEmitter<boolean>();
  @Output() miniChange = new EventEmitter<boolean>();

  usuarioNombre = '';
  usuarioRol = '';
  usuarioIniciales = '';

  constructor(public auth: AuthService) {
    this.usuarioNombre = this.auth.getUserName();
    this.usuarioRol = this.auth.getUserRole();
    this.usuarioIniciales = this.buildIniciales(this.usuarioNombre);
  }

  ngOnInit() {
    this.auth.refreshProfile().subscribe({
      next: (user) => {
        console.log('Profile refreshed:', user);
        this.usuarioNombre = this.auth.getUserName();
        this.usuarioRol = this.auth.getUserRole();
        // Force change detection if needed, or rely on getters in template which is better
      },
      error: (err) => console.warn('Failed to refresh profile', err)
    });

    // Debug RBAC - moved from constructor
    console.log('Sidebar RBAC Check:', {
      ver: this.auth.canViewData(),
      mod: this.auth.canModify(),
      exp: this.auth.canExport(),
      adm: this.auth.canAdmin(),
      user: this.auth.getUser()
    });
  }

  close() { this.openChange.emit(false); }
  openMenu() { this.openChange.emit(true); }
  toggle() { this.openChange.emit(!this.open); }

  toggleMini() { this.miniChange.emit(!this.mini); }

  private buildIniciales(nombre: string): string {
    if (!nombre) return 'US';
    const parts = nombre.trim().split(/\s+/);
    const first = parts[0]?.charAt(0) ?? '';
    const last = parts[parts.length - 1]?.charAt(0) ?? '';
    const init = (first + last).toUpperCase();
    return init || 'US';
  }
}
