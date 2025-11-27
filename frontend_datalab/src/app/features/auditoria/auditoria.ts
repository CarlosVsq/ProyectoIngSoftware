import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlertPanelComponent } from '../../alert-panel/alert-panel.component';
import { LogoutPanelComponent } from '../../shared/logout-panel/logout-panel.component';
import { AuthService } from '../../shared/auth/auth.service';

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule, AlertPanelComponent, LogoutPanelComponent],
  templateUrl: './auditoria.html',
  styleUrls: ['./auditoria.scss']
})
export class AuditoriaComponent {
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
}
