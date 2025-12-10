import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
    private apiUrl = 'http://localhost:8080/api/roles';

    constructor(private http: HttpClient) { }

    listarRoles(): Observable<Rol[]> {
        return this.http.get<Rol[]>(this.apiUrl);
    }

    actualizarPermisos(rol: Rol): Observable<Rol> {
        return this.http.put<Rol>(`${this.apiUrl}/${rol.idRol}`, rol);
    }
}
