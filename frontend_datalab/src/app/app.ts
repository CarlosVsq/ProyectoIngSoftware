import { Component, signal } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from './components/sidebar';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class App {
  // Estado del sidebar (persistente en localStorage)
  isSidebarOpen = signal<boolean>(localStorage.getItem('sidebarOpen') !== 'false');

  constructor(private router: Router) {}

  // Detecta si la página actual es login (para no mostrar el sidebar ahí)
  isLoginPage(): boolean {
    return this.router.url.includes('login');
  }

  setSidebarOpen(v: boolean) {
    this.isSidebarOpen.set(v);
    localStorage.setItem('sidebarOpen', String(v));
  }

  toggleSidebar() {
    this.setSidebarOpen(!this.isSidebarOpen());
  }
}
