import { Component, signal, OnInit } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from './components/sidebar';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class App implements OnInit {
  // Estado del sidebar (persistente en localStorage)
  isSidebarOpen = signal<boolean>(localStorage.getItem('sidebarOpen') !== 'false');
  isAuthPage = signal<boolean>(false);

  constructor(private router: Router) {}

  ngOnInit(): void {
    // estado inicial
    this.isAuthPage.set(this.isAuthRoute(this.router.url));
    this.router.events.pipe(filter(evt => evt instanceof NavigationEnd))
      .subscribe((evt: any) => {
        const url: string = evt.urlAfterRedirects || evt.url;
        this.isAuthPage.set(this.isAuthRoute(url));
      });
  }

  // Oculta el sidebar en login, register y recover
  isLoginPage(): boolean {
    return this.isAuthPage();
  }

  private isAuthRoute(url: string): boolean {
    return url.includes('/login') || url.includes('/register') || url.includes('/recover');
  }

  setSidebarOpen(v: boolean) {
    this.isSidebarOpen.set(v);
    localStorage.setItem('sidebarOpen', String(v));
  }

  toggleSidebar() {
    this.setSidebarOpen(!this.isSidebarOpen());
  }
}
