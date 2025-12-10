import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-logout-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './logout-panel.component.html',
  styleUrls: ['./logout-panel.component.scss']
})
export class LogoutPanelComponent {
  visible = false;

  constructor(private router: Router, private authService: AuthService) {}

  showPanel() {
    this.visible = true;
  }

  hidePanel() {
    this.visible = false;
  }

  logout() {
    this.visible = false;
    // Delegamos la lógica completa al servicio para asegurar 
    // que se registre la auditoría y se borre el token correctamente.
    this.authService.logout();
  }
}