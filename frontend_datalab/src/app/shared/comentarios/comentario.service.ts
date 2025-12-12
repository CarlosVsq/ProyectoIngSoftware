import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export interface Comentario {
    idComentario?: number;
    contenido: string;
    fechaCreacion?: string;
    usuario?: { nombreCompleto: string }; // We only need name for display
    nombreUsuario?: string; // Helper from backend
    idParticipante?: number;
}

@Injectable({
    providedIn: 'root'
})
export class ComentarioService {
    private apiUrl = `${API_BASE_URL}/comentarios`;

    constructor(private http: HttpClient) { }

    listarComentarios(idParticipante: number): Observable<Comentario[]> {
        return this.http.get<Comentario[]>(`${this.apiUrl}/${idParticipante}`);
    }

    agregarComentario(idParticipante: number, contenido: string): Observable<Comentario> {
        return this.http.post<Comentario>(this.apiUrl, { idParticipante, contenido });
    }
}
