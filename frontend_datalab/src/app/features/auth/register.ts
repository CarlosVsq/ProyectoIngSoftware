import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../shared/auth/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class RegisterComponent {
  registerForm: any;
  isLoading = false;
  errorMsg = '';

  constructor(private fb: FormBuilder, private router: Router, private auth: AuthService) {
    this.registerForm = this.fb.group({
      nombreCompleto: ['', Validators.required],
      correo: ['', [Validators.required, Validators.email]],
      contrasenia: ['', [Validators.required, Validators.minLength(6)]],
      rolId: [1, Validators.required] // placeholder: Admin/PI
    });
  }

  onSubmit() {
    if (this.registerForm.invalid) {
      this.errorMsg = 'Completa los campos requeridos.';
      return;
    }
    this.errorMsg = '';
    this.isLoading = true;
    const { nombreCompleto, correo, contrasenia, rolId } = this.registerForm.value;
    this.auth.register(nombreCompleto, correo, contrasenia, rolId).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMsg = err?.error?.message || 'Error al registrar.';
      }
    });
  }
}
