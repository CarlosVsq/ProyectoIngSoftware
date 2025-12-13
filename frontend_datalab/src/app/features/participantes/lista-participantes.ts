import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { CrfService } from '../crf/crf.service';
import { CRFSchema } from '../crf/schema';
import { CrfModalComponent } from '../crf/crf-modal.component';
import { ParticipanteService } from '../../shared/participantes/participante.service';
import { AuthService } from '../../shared/auth/auth.service';
import { ComentarioService } from '../../shared/comentarios/comentario.service';
import { API_BASE_URL } from '../../shared/config/api.config';

@Component({
    selector: 'app-lista-participantes',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink, CrfModalComponent],
    templateUrl: './lista-participantes.html'
})
export class ListaParticipantesComponent implements OnInit {
    participantes: any[] = [];
    filteredParticipantes: any[] = [];
    schema?: CRFSchema;

    // Filters
    searchTerm: string = '';
    selectedGroup: string = 'Todos';
    selectedReclutador: string = 'Todos';
    filterFechaInicio: string = '';
    filterFechaFin: string = '';

    uniqueReclutadores: string[] = [];

    stats = {
        total: 0,
        casos: 0,
        controles: 0,
        completados: 0
    };

    loading = true;


    // Modal logic
    crfOpen = false;
    crfRecordId: string | null = null;
    crfPreload: any = null;
    crfParticipantId: number | null = null;

    // Comentarios Logic
    showCommentsModal = false;
    currentParticipanteForComments: any = null;
    comentarios: any[] = [];
    newCommentText = '';
    isSendingComment = false;

    constructor(
        private crfService: CrfService,
        public auth: AuthService,
        private router: Router,
        private participanteService: ParticipanteService,
        private comentarioService: ComentarioService
    ) { }

    ngOnInit(): void {
        this.crfService.getSchema().subscribe(s => {
            this.schema = s;
            this.loadParticipantes();
        });
    }

    loadParticipantes() {
        this.loading = true;
        this.crfService.listarCrfs(100).subscribe({
            next: (resp) => {
                const rawData = resp.data || [];
                // Extract unique recruiters
                const recruiters = new Set<string>();

                this.participantes = rawData.map((c: any) => {
                    if (c.nombreReclutador) recruiters.add(c.nombreReclutador);
                    const estadoAnalisis = this.analizarEstado(c);
                    let estado = c.estadoFicha || (estadoAnalisis.completo ? 'COMPLETA' : 'INCOMPLETA');

                    let color = 'bg-purple-600';
                    if (estado === 'COMPLETA' || estado === 'COMPLETO') {
                        color = 'bg-green-600';
                    } else if (estado === 'NO_COMPLETABLE') {
                        color = 'bg-gray-600';
                    }

                    return {
                        ...c,
                        estadoFicha: estado,
                        colorEstado: color,
                        variablesFaltantes: estadoAnalisis.faltantes,
                        resumenRespuestas: c.respuestas && c.respuestas.length
                            ? c.respuestas.slice(0, 3).map((r: any) => `${r.codigoVariable}: ${r.valor} `).join(' | ')
                            : 'Sin respuestas capturadas.'
                    };
                });

                this.uniqueReclutadores = Array.from(recruiters).sort();

                this.calculateStats();
                this.applyFilters();
                this.loading = false;
            },
            error: (err) => {
                console.error('Error loading participants', err);
                this.loading = false;
            }
        });
    }

    // ... calculateStats ...
    calculateStats() {
        this.stats.total = this.participantes.length;
        this.stats.casos = this.participantes.filter(p => (p.grupo || '').toUpperCase() === 'CASO').length;
        this.stats.controles = this.participantes.filter(p => (p.grupo || '').toUpperCase() === 'CONTROL').length;
        this.stats.completados = this.participantes.filter(p => p.estadoFicha === 'COMPLETA' || p.estadoFicha === 'COMPLETO').length;
    }

    applyFilters() {
        this.filteredParticipantes = this.participantes.filter(p => {
            const term = this.searchTerm.toLowerCase();
            const code = (p.codigoParticipante || '').toLowerCase();
            const name = (p.nombreCompleto || '').toLowerCase();

            // Text Search
            const matchSearch = code.includes(term) || name.includes(term);

            // Group Filter
            let matchGroup = true;
            if (this.selectedGroup !== 'Todos') {
                const g = (p.grupo || '').toUpperCase();
                matchGroup = g === this.selectedGroup.toUpperCase();
            }

            // Recruiter Filter
            let matchReclutador = true;
            if (this.selectedReclutador !== 'Todos') {
                matchReclutador = p.nombreReclutador === this.selectedReclutador;
            }

            // Date Filter
            let matchDate = true;
            if (this.filterFechaInicio || this.filterFechaFin) {
                const pDateStr = p.fechaInclusion; // assumed YYYY-MM-DD from backend
                if (pDateStr) {
                    const pDate = new Date(pDateStr).getTime();
                    if (this.filterFechaInicio) {
                        const start = new Date(this.filterFechaInicio).getTime();
                        if (pDate < start) matchDate = false;
                    }
                    if (matchDate && this.filterFechaFin) {
                        const end = new Date(this.filterFechaFin).getTime();
                        // Add 1 day to include the end date fully or compare properly
                        // Simple comparison: as long as pDate <= end (considering time)
                        // Since inputs are YYYY-MM-DD, pDate is YYYY-MM-DD. 
                        // To be inclusive, if pDate > end it fails.
                        if (pDate > end) matchDate = false;
                    }
                }
            }

            return matchSearch && matchGroup && matchReclutador && matchDate;
        });
    }

    onSearchChange() { this.applyFilters(); }
    onGroupChange() { this.applyFilters(); }

    // Logic copied from ReclutamientoComponent
    private analizarEstado(participante: any): { completo: boolean; faltantes: string[] } {
        if (!this.schema) return { completo: false, faltantes: [] };

        const respuestasIds = new Set<string>();
        if (participante.respuestas) {
            participante.respuestas.forEach((r: any) => {
                // Only count as answered if value is not empty
                if (r.valor !== null && r.valor !== undefined && String(r.valor).trim() !== '') {
                    respuestasIds.add(r.codigoVariable);
                }
            });
        }

        const grupo = (participante.grupo || 'CONTROL').toLowerCase();
        const faltantes: string[] = [];

        this.schema.sections.forEach(section => {
            if (section.groupVisibility && !section.groupVisibility.includes(grupo as any)) return;

            section.fields.forEach(field => {
                if (field.groupVisibility && !field.groupVisibility.includes(grupo as any)) return;

                if (field.required) {
                    const fieldId = (field.id || '').toLowerCase().trim();
                    let answered = false;
                    for (const ansId of respuestasIds) {
                        if (ansId.toLowerCase().trim() === fieldId) {
                            answered = true;
                            break;
                        }
                    }
                    if (!answered) {
                        faltantes.push(field.label || field.id);
                    }
                }
            });
        });

        return {
            completo: faltantes.length === 0,
            faltantes
        };
    }

    verPdf(id: number) {
        if (!id) return;
        window.open(`${API_BASE_URL}/export/participante/${id}/pdf`, '_blank');
    }

    editar(p: any) {
        if (!p.idParticipante) return;

        // Fetch full data using ParticipanteService (reusing the logic from Reclutamiento)
        this.participanteService.obtenerParticipante(p.idParticipante).subscribe({
            next: (fullData) => {
                this.prepararYAbirModal(fullData);
            },
            error: (err) => {
                console.error('Error fetching participant details', err);
                alert('No se pudo cargar la información completa del participante.');
            }
        });
    }

    private prepararYAbirModal(crf: any) {
        const preload: any = {
            grupo: crf.grupo ? crf.grupo.toLowerCase() : 'control',
            telefono: crf.telefono || '',
            direccion: crf.direccion || '',
            nombre_completo: crf.nombreCompleto || '',
            fecha_inclusion: crf.fechaInclusion || '',
            // If email is not in core fields, it might be in responses, but mapping it here just in case if added to core
            correo: crf.correo || ''
        };
        if (crf.respuestas) {
            crf.respuestas.forEach((r: any) => {
                const code = r.codigoVariable;
                // The backend sends 'valorIngresado'. properties might differ if using different DTOs, check both
                const val = r.valorIngresado !== undefined ? r.valorIngresado : r.valor;
                if (code) {
                    preload[code] = val;
                }
            });
        }
        this.crfRecordId = crf.codigoParticipante || crf.idParticipante?.toString();
        this.crfParticipantId = crf.idParticipante || null;
        this.crfPreload = preload;
        this.crfOpen = true;
    }

    cerrarCRF() {
        this.crfOpen = false;
        this.crfRecordId = null;
        this.crfPreload = null;
        this.crfParticipantId = null;
        this.loadParticipantes(); // Refresh list to show changes
    }

    eliminar(p: any) {
        if (!confirm('¿Seguro que deseas borrar este participante?')) return;
        if (p.idParticipante) {
            this.crfService.eliminarCrf(p.idParticipante).subscribe({
                next: () => this.loadParticipantes(),
                error: () => alert('No se pudo eliminar')
            });
        }
    }

    // --- Comment Logic ---

    abrirComentarios(p: any) {
        this.currentParticipanteForComments = p;
        this.newCommentText = '';
        this.comentarioService.listarComentarios(p.idParticipante).subscribe(data => {
            this.comentarios = data;
            this.showCommentsModal = true;
        });
    }

    cerrarComentarios() {
        this.showCommentsModal = false;
        this.currentParticipanteForComments = null;
    }

    enviarComentario() {
        if (!this.newCommentText.trim()) return;
        this.isSendingComment = true;
        this.comentarioService.agregarComentario(this.currentParticipanteForComments.idParticipante, this.newCommentText)
            .subscribe({
                next: (nuevo) => {
                    this.comentarios.unshift(nuevo); // Add to top
                    this.newCommentText = '';
                    this.isSendingComment = false;
                },
                error: () => {
                    alert('Error al guardar comentario');
                    this.isSendingComment = false;
                }
            });
    }
}
