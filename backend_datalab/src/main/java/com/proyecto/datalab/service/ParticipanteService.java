package com.proyecto.datalab.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.entity.Respuesta;
import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.entity.Variable;
import com.proyecto.datalab.enums.EstadoFicha;
import com.proyecto.datalab.enums.GrupoParticipante;
import com.proyecto.datalab.repository.ParticipanteRepository;
import com.proyecto.datalab.repository.RespuestaRepository;
import com.proyecto.datalab.repository.UsuarioRepository;
import com.proyecto.datalab.repository.VariableRepository;

@Service
public class ParticipanteService {

        private final ParticipanteRepository participanteRepository;
        private final RespuestaRepository respuestaRepository;
        private final UsuarioRepository usuarioRepository;
        private final VariableRepository variableRepository;
        private final AuditoriaService auditoriaService;

        public ParticipanteService(ParticipanteRepository participanteRepository,
                                RespuestaRepository respuestaRepository,
                                UsuarioRepository usuarioRepository,
                                VariableRepository variableRepository,
                                AuditoriaService auditoriaService) {
                this.participanteRepository = participanteRepository;
                this.respuestaRepository = respuestaRepository;
                this.usuarioRepository = usuarioRepository;
                this.variableRepository = variableRepository;
                this.auditoriaService = auditoriaService;
        }

        @Transactional
        public Participante crearParticipante(String nombreCompleto, String telefono, String direccion, String grupo, Long usuarioReclutadorId) {

                Usuario reclutador = usuarioRepository.findById(usuarioReclutadorId)
                        .orElseThrow(() -> new RuntimeException("Usuario reclutador no encontrado"));

                Participante p = new Participante();
                p.setNombreCompleto(nombreCompleto);
                p.setTelefono(telefono);
                p.setDireccion(direccion);
                p.setGrupo(GrupoParticipante.valueOf(grupo)); // "CASO" o "CONTROL"
                p.setReclutador(reclutador);
                p.setEstadoFicha(EstadoFicha.INCOMPLETA);
                p.setFechaInclusion(LocalDate.now());

                Participante participanteGuardado = participanteRepository.save(p);

                if (p.getGrupo() == GrupoParticipante.CASO) {
                        p.setCodigoParticipante("CS"+p.getIdParticipante());
                } else{
                        p.setCodigoParticipante("CT"+p.getIdParticipante());
                }

                auditoriaService.registrarAccion(reclutador,participanteGuardado, "CREAR", "Participante", 
                        "Se creó el participante ID: " + participanteGuardado.getIdParticipante());

                return participanteRepository.save(participanteGuardado);
        }

        @Transactional
        public void guardarRespuestas(int participanteId, Map<Long, String> respuestasMap, Long usuarioEditorId) {
                
                Participante p = participanteRepository.findById(participanteId)
                        .orElseThrow(() -> new RuntimeException("Participante no encontrado"));
                
                Usuario editor = usuarioRepository.findById(usuarioEditorId)
                        .orElseThrow(() -> new RuntimeException("Usuario editor no encontrado"));

                for (Map.Entry<Long, String> entry : respuestasMap.entrySet()) {
                Long variableId = entry.getKey();
                String valor = entry.getValue();

                Variable v = variableRepository.findById(variableId)
                        .orElseThrow(() -> new RuntimeException("Variable no encontrada"));

                Respuesta r = new Respuesta();
                r.setParticipante(p);
                r.setVariable(v);
                r.setValorIngresado(valor);
                
                respuestaRepository.save(r);

                auditoriaService.registrarAccion(editor,p, "ACTUALIZAR", "Respuesta", 
                        "Se guardó respuesta para participante ID: " + p.getIdParticipante() + ", Variable ID: " + v.getIdVariable());
                }
        }

        @Transactional(readOnly = true)
        public List<Participante> obtenerTodosLosParticipantes() {
                return participanteRepository.findAll();
        }
}