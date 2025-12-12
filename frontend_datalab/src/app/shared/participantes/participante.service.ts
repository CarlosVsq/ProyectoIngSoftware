import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

@Injectable({
    providedIn: 'root'
})
export class ParticipanteService {
    private apiUrl = `${API_BASE_URL}/participantes`;

    constructor(private http: HttpClient) { }

    listarParticipantes(): Observable<any[]> {
        return this.http.get<any[]>(this.apiUrl);
    }

    // Helper methods can be added here
    eliminarParticipante(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    obtenerParticipante(id: number): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/${id}`);
    }
}
