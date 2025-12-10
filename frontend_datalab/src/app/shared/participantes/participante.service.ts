import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class ParticipanteService {
    private apiUrl = 'http://localhost:8080/api/participantes';

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
