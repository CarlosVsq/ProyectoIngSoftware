import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { API_BASE_URL } from '../../shared/config/api.config';

@Component({
  selector: 'app-recover',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './recover.html'
})
export class RecoverComponent {
  recoverForm: any;
  isLoading = false;
  msg = '';

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    this.recoverForm = this.fb.group({
      correo: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit() {
    if (this.recoverForm.invalid) {
      this.msg = 'Ingresa un correo válido.';
      return;
    }
    this.isLoading = true;
    this.msg = '';
    this.http.post(`${API_BASE_URL}/password/forgot`, { correo: this.recoverForm.value.correo }).subscribe({
      next: () => {
        this.isLoading = false;
        this.msg = 'Si el correo existe, recibirás un enlace de recuperación.';
      },
      error: () => {
        this.isLoading = false;
        this.msg = 'No se pudo enviar el correo. Intenta más tarde.';
      }
    });
  }
}
