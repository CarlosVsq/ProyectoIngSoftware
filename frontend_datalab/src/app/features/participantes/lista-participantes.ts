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
        // Load schema first for status analysis
        this.crfService.getSchema().subscribe(s => {
            this.schema = s;
            this.loadParticipantes();
        });
    }

    loadParticipantes() {
        this.loading = true;
        // Fetch a larger limit or all if possible. Using 100 for now.
        this.crfService.listarCrfs(100).subscribe({
            next: (resp) => {
                const rawData = resp.data || [];
                this.participantes = rawData.map((c: any) => {
                    const estadoAnalisis = this.analizarEstado(c);
                    return {
                        ...c,
                        estadoFicha: estadoAnalisis.completo ? 'COMPLETO' : 'INCOMPLETO',
                        colorEstado: estadoAnalisis.completo ? 'bg-green-600' : 'bg-purple-600',
                        variablesFaltantes: estadoAnalisis.faltantes,
                        resumenRespuestas: c.respuestas && c.respuestas.length
                            ? c.respuestas.slice(0, 3).map((r: any) => `${r.codigoVariable}: ${r.valor} `).join(' | ')
                            : 'Sin respuestas capturadas.'
                    };
                });

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

    calculateStats() {
        this.stats.total = this.participantes.length;
        this.stats.casos = this.participantes.filter(p => (p.grupo || '').toUpperCase() === 'CASO').length;
        this.stats.controles = this.participantes.filter(p => (p.grupo || '').toUpperCase() === 'CONTROL').length;
        this.stats.completados = this.participantes.filter(p => p.estadoFicha === 'COMPLETO').length;
    }

    applyFilters() {
        this.filteredParticipantes = this.participantes.filter(p => {
            const term = this.searchTerm.toLowerCase();
            const code = (p.codigoParticipante || '').toLowerCase();
            const name = (p.nombreCompleto || '').toLowerCase();

            const matchSearch = code.includes(term) || name.includes(term);

            let matchGroup = true;
            if (this.selectedGroup !== 'Todos') {
                const g = (p.grupo || '').toUpperCase();
                matchGroup = g === this.selectedGroup.toUpperCase();
            }

            return matchSearch && matchGroup;
        });
    }

    onSearchChange() { this.applyFilters(); }
    onGroupChange() { this.applyFilters(); }

    // Logic copied from ReclutamientoComponent
    private analizarEstado(participante: any): { completo: boolean; faltantes: string[] } {
        if (!this.schema) return { completo: false, faltantes: [] };

        const respuestasIds = new Set<string>();
        if (participante.respuestas) {
            participante.respuestas.forEach((r: any) => respuestasIds.add(r.codigoVariable));
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
        window.open(`http://localhost:8080/api/export/participante/${id}/pdf`, '_blank');
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
