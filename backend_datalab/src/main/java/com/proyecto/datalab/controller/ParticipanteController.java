package com.proyecto.datalab.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.dto.GuardarRespuestasRequest;
import com.proyecto.datalab.dto.MarcarNoCompletableRequest;
import com.proyecto.datalab.dto.ParticipanteCreateRequest;
import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.service.ParticipanteService;
import com.proyecto.datalab.web.dto.CrfListadoDTO;
import com.proyecto.datalab.web.dto.common.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/participantes")
public class ParticipanteController {

    @Autowired
    private ParticipanteService participanteService;

    // --- GET (Obtener Todos) ---
    @GetMapping
    public List<Participante> obtenerTodos() {
        return participanteService.obtenerTodosLosParticipantes();
    }

    @GetMapping("/{id}")
    public Participante obtenerPorId(@PathVariable Integer id) {
        return participanteService.obtenerParticipantePorId(id);
    }

    @GetMapping("/resumen")
    public ApiResponse<List<CrfListadoDTO>> obtenerResumenCrf(
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(participanteService.listarCrfConRespuestas(limit));
    }

    @DeleteMapping("/{idParticipante}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer idParticipante) {
        participanteService.eliminarParticipante(idParticipante);
        return ResponseEntity.noContent().build();
    }

    // --- POST (Crear Participante) ---
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Participante crear(@Valid @RequestBody ParticipanteCreateRequest request) {
        return participanteService.crearParticipante(
                request.getNombreCompleto(),
                request.getTelefono(),
                request.getDireccion(),
                request.getGrupo(),
                request.getUsuarioReclutadorId());
    }

    // --- POST (Marcar ficha como NO_COMPLETABLE) ---
    @PostMapping("/{idParticipante}/no-completable")
    public Participante marcarNoCompletable(
            @PathVariable Integer idParticipante,
            @Valid @RequestBody MarcarNoCompletableRequest request) {
        return participanteService.marcarNoCompletable(
                idParticipante,
                request.getJustificacion(),
                request.getUsuarioEditorId());
    }

    // --- POST (Guardar Respuestas de un Participante) ---
    // Este metodo actualiza o crea las respuestas de un participante existente
    @PostMapping("/{idParticipante}/respuestas")
    public ResponseEntity<Void> guardarRespuestas(
            @PathVariable Integer idParticipante,
            @Valid @RequestBody GuardarRespuestasRequest request) {

        participanteService.guardarRespuestas(
                idParticipante,
                request.getRespuestasMap(),
                request.getUsuarioEditorId(),
                request.getNombreCompleto(),
                request.getTelefono(),
                request.getDireccion(),
                request.getGrupo());
        return ResponseEntity.ok().build();
    }
}
