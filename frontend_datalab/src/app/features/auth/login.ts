import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../shared/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class LoginComponent implements OnInit {
  loginForm: any;
  isLoading = false;
  errorMsg = '';

  constructor(private fb: FormBuilder, private router: Router, private auth: AuthService) {
    this.loginForm = this.fb.group({
      correo: ['', [Validators.required, Validators.email]],
      contrasenia: ['', Validators.required],
    });
  }

  /**
   * Verifica al cargar si el usuario ya tiene sesión.
   * Si es así, lo manda directo al dashboard.
   */
  ngOnInit(): void {
    // Force logout when visiting login page to ensure user sees login screen
    this.auth.logout();
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      this.errorMsg = 'Por favor, completa los campos requeridos.';
      return;
    }

    this.errorMsg = '';
    this.isLoading = true;
    const { correo, contrasenia } = this.loginForm.value;

    this.auth.login(correo, contrasenia).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMsg = err?.error?.message || 'Credenciales incorrectas o servidor no disponible.';
      }
    });
  }
}