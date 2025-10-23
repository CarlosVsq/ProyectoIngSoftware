package com.proyecto.datalab.servicio;

import com.proyecto.datalab.entidades.Participante;
import com.proyecto.datalab.entidades.Respuesta;
import com.proyecto.datalab.entidades.Usuario;
import com.proyecto.datalab.entidades.Variable;
import com.proyecto.datalab.enums.EstadoFicha;
import com.proyecto.datalab.enums.GrupoParticipante;
import com.proyecto.datalab.repositorio.ParticipanteRepository;
import com.proyecto.datalab.repositorio.RespuestaRepository;
import com.proyecto.datalab.repositorio.UsuarioRepository;
import com.proyecto.datalab.repositorio.VariableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map; // Para recibir las respuestas

@Service
public class ParticipanteService {

    private final ParticipanteRepository participanteRepository;
    private final RespuestaRepository respuestaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VariableRepository variableRepository;
    private final AuditoriaService auditoriaService; // Dependencia del servicio de auditoría

    @Autowired
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

    /**
     * CREATE: Crea un nuevo participante
     * Esta es la lógica para HU-01 (Registro de CRF) y HU-29 (Seleccionar grupo)
     */
    @Transactional
    public Participante crearParticipante(String nombreCompleto, String telefono, String direccion, String grupo, Long usuarioReclutadorId) {
        
        // 1. Lógica de negocio: Buscar al usuario que lo recluta
        Usuario reclutador = usuarioRepository.findById(usuarioReclutadorId)
                .orElseThrow(() -> new RuntimeException("Usuario reclutador no encontrado"));

        // 2. Crear la entidad
        Participante p = new Participante();
        p.setNombreCompleto(nombreCompleto);
        p.setTelefono(telefono);
        p.setDireccion(direccion);
        p.setGrupo(GrupoParticipante.valueOf(grupo)); // "CASO" o "CONTROL"
        p.setReclutador(reclutador); // Asigna el reclutador
        p.setEstadoFicha(EstadoFicha.Incompleta);

        // 3. Guardar (CRUD)
        Participante participanteGuardado = participanteRepository.save(p);

        // 4. Lógica de auditoría (HU-11)
        auditoriaService.registrarAccion(reclutador, "CREAR", "Participante", 
                "Se creó el participante ID: " + participanteGuardado.getIdParticipante());

        return participanteGuardado;
    }

    /**
     * UPDATE: Guarda o actualiza las respuestas de un participante
     * Lógica para HU-01 y HU-33 (Guardar fichas incompletas)
     */
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

            // (Aquí podrías buscar si ya existe una respuesta para p y v, y si no, crearla)
            
            Respuesta r = new Respuesta();
            r.setParticipante(p);
            r.setVariable(v);
            r.setValorIngresado(valor);
            
            respuestaRepository.save(r);

            // 4. Lógica de auditoría (HU-11)
            auditoriaService.registrarAccion(editor, "ACTUALIZAR", "Respuesta", 
                    "Se guardó respuesta para participante ID: " + p.getIdParticipante() + ", Variable ID: " + v.getIdVariable());
        }
        
        // (Podrías agregar lógica para marcar la ficha como "COMPLETA")
    }
    
    /**
     * READ: Obtener todos los participantes
     */
    @Transactional(readOnly = true)
    public List<Participante> obtenerTodosLosParticipantes() {
        return participanteRepository.findAll();
    }
}