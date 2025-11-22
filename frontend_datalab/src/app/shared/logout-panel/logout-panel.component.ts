import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-logout-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './logout-panel.component.html',
  styleUrls: ['./logout-panel.component.scss']
})
export class LogoutPanelComponent {
  visible = false;

  constructor(private router: Router) {}

  showPanel() {
    this.visible = true;
  }

  hidePanel() {
    this.visible = false;
  }

  logout() {
    // Aquí puedes limpiar datos del usuario o token
    this.visible = false;
    this.router.navigate(['/login']);
  }
}
