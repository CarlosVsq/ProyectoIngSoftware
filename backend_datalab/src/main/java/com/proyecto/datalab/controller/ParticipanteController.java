package com.proyecto.datalab.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.dto.GuardarRespuestasRequest;
import com.proyecto.datalab.dto.ParticipanteCreateRequest;
import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.service.ParticipanteService;

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

    // --- POST (Crear Participante) ---
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Participante crear(@RequestBody ParticipanteCreateRequest request) {
        return participanteService.crearParticipante(
                request.getNombreCompleto(),
                request.getTelefono(),
                request.getDireccion(),
                request.getGrupo(),
                request.getUsuarioReclutadorId()
        );
    }

    // --- POST (Guardar Respuestas de un Participante) ---
    // (Este método actualiza las respuestas de un participante existente)
    @PostMapping("/{idParticipante}/respuestas")
    public ResponseEntity<Void> guardarRespuestas(
            @PathVariable Integer idParticipante,
            @RequestBody GuardarRespuestasRequest request) {
        
        // Asumimos que el ID del usuario editor viene en el request
        participanteService.guardarRespuestas(
                idParticipante,
                request.getRespuestasMap(),
                request.getUsuarioEditorId()
        );
        return ResponseEntity.ok().build();
    }
}