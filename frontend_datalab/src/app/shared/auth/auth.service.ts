import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private role: 'principal' | 'investigador' | 'coordinador' = 'principal'; // por ahora simulado

  // Si la investigadora principal está logueada
  isPI(): boolean {
    return this.role === 'principal';
  }

  getUserName(): string {
    return this.role === 'principal' ? 'Dra. González' : 'Dr. Martínez';
  }

  // Cambia de rol (temporal, útil para pruebas)
  setRole(role: 'principal' | 'investigador' | 'coordinador') {
    this.role = role;
  }
}
