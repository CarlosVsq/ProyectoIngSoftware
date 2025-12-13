import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export interface Rol {
    idRol: number;
    nombreRol: string;
    descripcion?: string;
    permisoVerDatos: boolean;
    permisoModificar: boolean;
    permisoExportar: boolean;
    permisoAdministrar: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class RolService {
    private apiUrl = `${API_BASE_URL}/roles`;

    constructor(private http: HttpClient) { }

    listarRoles(): Observable<Rol[]> {
        return this.http.get<Rol[]>(this.apiUrl);
    }

    actualizarPermisos(rol: Rol): Observable<Rol> {
        return this.http.put<Rol>(`${this.apiUrl}/${rol.idRol}`, rol);
    }
}
