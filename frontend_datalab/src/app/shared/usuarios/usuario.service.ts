import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export interface Usuario {
    idUsuario?: number;
    nombreCompleto: string;
    correo: string;
    rol: { idRol: number; nombreRol: string };
    estado: 'ACTIVO' | 'INACTIVO';
}

export interface UsuarioCreateRequest {
    nombre: string;
    correo: string;
    contrasena: string;
    rolId: number;
}

@Injectable({
    providedIn: 'root'
})
export class UsuarioService {
    private apiUrl = `${API_BASE_URL}/usuarios`;

    constructor(private http: HttpClient) { }

    listarUsuarios(): Observable<Usuario[]> {
        return this.http.get<Usuario[]>(this.apiUrl);
    }

    crearUsuario(request: UsuarioCreateRequest): Observable<Usuario> {
        return this.http.post<Usuario>(this.apiUrl, request);
    }

    borrarUsuario(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    cambiarEstado(id: number): Observable<Usuario> {
        return this.http.patch<Usuario>(`${this.apiUrl}/${id}/estado`, {});
    }
}
