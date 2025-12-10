import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
    private apiUrl = 'http://localhost:8080/api/usuarios';

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
}
