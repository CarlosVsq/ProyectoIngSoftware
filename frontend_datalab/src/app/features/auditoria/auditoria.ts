import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlertPanelComponent } from '../../alert-panel/alert-panel.component';
import { LogoutPanelComponent } from '../../shared/logout-panel/logout-panel.component'; // 👈 Importa tu panel

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule, AlertPanelComponent, LogoutPanelComponent], // 👈 Asegúrate de incluirlo aquí
  templateUrl: './auditoria.html',
  styleUrls: ['./auditoria.scss']
})
export class AuditoriaComponent {
  usuarioNombre = 'Dra. González';
  @ViewChild(LogoutPanelComponent)
  logoutPanel!: LogoutPanelComponent;
  abrirLogoutPanel() {
    this.logoutPanel.showPanel();
  }
}
