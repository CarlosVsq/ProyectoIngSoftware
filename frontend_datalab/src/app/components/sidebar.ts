import { Component, Input, Output, EventEmitter } from '@angular/core';
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
export class SidebarComponent {
  /** true: visible (off-canvas); false: oculto */
  @Input({ required: true }) open = true;
  /** true: modo mini (icon-only) en ≥lg; ignorado en móvil */
  @Input() mini = false;

  @Output() openChange = new EventEmitter<boolean>();
  @Output() miniChange = new EventEmitter<boolean>();

  usuarioNombre = '';
  usuarioRol = '';
  usuarioIniciales = '';

  constructor(private auth: AuthService) {
    this.usuarioNombre = this.auth.getUserName();
    this.usuarioRol = this.auth.getUserRole();
    this.usuarioIniciales = this.buildIniciales(this.usuarioNombre);
  }

  close() { this.openChange.emit(false); }
  openMenu(){ this.openChange.emit(true); }
  toggle(){ this.openChange.emit(!this.open); }

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
