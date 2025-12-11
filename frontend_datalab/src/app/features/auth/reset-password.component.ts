import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../shared/auth/auth.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
    selector: 'app-reset-password',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    template: `
    <div class="h-screen flex items-center justify-center bg-accent">
      <div class="bg-white rounded-2xl shadow-2xl p-10 w-[420px] text-center">
        <h2 class="text-xl font-semibold text-primary mb-4">Ingresar Código</h2>
        
        <div *ngIf="success" class="mb-4">
            <p class="text-green-600 mb-4">¡Contraseña actualizada correctamente!</p>
            <a routerLink="/login" class="text-primary hover:underline font-medium">Ir al inicio de sesión</a>
        </div>

        <div *ngIf="!success">
            <p class="text-gray-500 mb-6 text-sm">Introduce el código de 6 dígitos que enviamos a tu correo y tu nueva contraseña.</p>

            <div *ngIf="error" class="mb-4 p-3 bg-red-100 text-red-700 rounded text-sm">
                {{ error }}
            </div>

            <input [(ngModel)]="token" type="text" placeholder="Código de 6 dígitos" maxlength="6"
                class="w-full border rounded-md p-2 mb-4 focus:ring-2 focus:ring-primary outline-none tracking-widest text-center" />

            <input [(ngModel)]="password" type="password" placeholder="Nueva contraseña" 
                class="w-full border rounded-md p-2 mb-4 focus:ring-2 focus:ring-primary outline-none" />

            <button (click)="reset()" [disabled]="loading || !password || !token"
                class="w-full bg-primary text-white py-2 rounded-md hover:opacity-90 transition disabled:opacity-50">
                {{ loading ? 'Actualizando...' : 'Cambiar contraseña' }}
            </button>
        </div>
      </div>
    </div>
  `
})
export class ResetPasswordComponent implements OnInit {
    token = '';
    password = '';
    loading = false;
    success = false;
    error = '';

    constructor(
        private auth: AuthService,
        private route: ActivatedRoute,
        private router: Router
    ) { }

    ngOnInit() {
        // We can check if email was passed in state for personalized message if we wanted to
        // const email = history.state.email;
    }

    reset() {
        if (!this.password || !this.token) return;
        this.loading = true;
        this.error = '';

        this.auth.resetPassword(this.token, this.password).subscribe({
            next: () => {
                this.loading = false;
                this.success = true;
            },
            error: (err) => {
                this.loading = false;
                this.error = 'Código inválido o expirado.';
            }
        });
    }
}
