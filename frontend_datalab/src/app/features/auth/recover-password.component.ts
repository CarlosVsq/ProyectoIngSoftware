import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../shared/auth/auth.service';
import { RouterLink, Router } from '@angular/router';

@Component({
    selector: 'app-recover-password',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    template: `
    <div class="h-screen flex items-center justify-center bg-accent">
      <div class="bg-white rounded-2xl shadow-2xl p-10 w-[420px] text-center">
        <h2 class="text-xl font-semibold text-primary mb-4">Recuperar Contraseña</h2>
        <p class="text-gray-500 mb-6 text-sm">Ingresa tu correo electrónico y te enviaremos un código de recuperación.</p>

        <div *ngIf="message" class="mb-4 p-3 bg-green-100 text-green-700 rounded text-sm">
            {{ message }}
        </div>

        <div *ngIf="error" class="mb-4 p-3 bg-red-100 text-red-700 rounded text-sm">
            {{ error }}
        </div>

        <input [(ngModel)]="email" type="email" placeholder="correo@datalab.com" 
               class="w-full border rounded-md p-2 mb-4 focus:ring-2 focus:ring-primary outline-none" />

        <button (click)="send()" [disabled]="loading || !email"
            class="w-full bg-primary text-white py-2 rounded-md hover:opacity-90 transition disabled:opacity-50">
            {{ loading ? 'Enviando...' : 'Enviar Código' }}
        </button>

        <div class="mt-4">
            <a routerLink="/login" class="text-sm text-secondary hover:underline">Volver al inicio de sesión</a>
        </div>
      </div>
    </div>
  `
})
export class RecoverPasswordComponent {
    email = '';
    loading = false;
    message = '';
    error = '';

    constructor(private auth: AuthService, private router: Router) { }

    send() {
        if (!this.email) return;
        this.loading = true;
        this.message = '';
        this.error = '';

        this.auth.forgotPassword(this.email).subscribe({
            next: (res) => {
                this.loading = false;
                // Redirect to reset password page to enter the code
                this.router.navigate(['/reset-password'], { state: { email: this.email } });
            },
            error: () => {
                this.loading = false;
                // For security, proceed as if it worked (or show error if preferred, but redirecting is safer against enum)
                // In this case, we'll just redirect anyway to simulate success
                this.router.navigate(['/reset-password'], { state: { email: this.email } });
            }
        });
    }
}
