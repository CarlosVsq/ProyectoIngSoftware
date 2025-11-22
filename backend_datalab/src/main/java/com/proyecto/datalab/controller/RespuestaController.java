package com.proyecto.datalab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.dto.RespuestaCreateRequest;
import com.proyecto.datalab.dto.RespuestaUpdateRequest;
import com.proyecto.datalab.entity.Respuesta;
import com.proyecto.datalab.service.RespuestaService;

import jakarta.validation.Valid;

/**
 * Controlador REST para gestionar las respuestas de los participantes
 */
@RestController
@RequestMapping("/api/respuesta")
public class RespuestaController {

    @Autowired
    private RespuestaService respuestaService;

    /**
     * Crea una nueva respuesta para un participante en una variable específica
     *
     * @param entity DTO con los datos de la respuesta a crear
     * @return ResponseEntity con la respuesta creada y código HTTP 201
     */
    @PostMapping
    public ResponseEntity<Respuesta> creaRespuesta(@Valid @RequestBody RespuestaCreateRequest entity) {
        try {
            Respuesta nuevaRespuesta = respuestaService.responder(
                entity.getIdParticipante(),
                entity.getIdVariable(),
                entity.getValorIngresado()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaRespuesta);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado") || e.getMessage().contains("no encontrada")) {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
            return ResponseEntity.badRequest().body(null); // 400 Bad Request
        }
    }

    /**
     * Actualiza el valor de una respuesta existente
     *
     * @param id ID de la respuesta a actualizar
     * @param entity DTO con el nuevo valor de la respuesta
     * @return ResponseEntity con la respuesta actualizada y código HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<Respuesta> actualizaRespuesta(
            @PathVariable Integer id,
            @Valid @RequestBody RespuestaUpdateRequest entity) {
        try {
            Respuesta respuestaActualizada = respuestaService.actualizaRespuesta(id, entity);
            return ResponseEntity.ok(respuestaActualizada);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado") || e.getMessage().contains("no encontrada")) {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
            return ResponseEntity.badRequest().body(null); // 400 Bad Request
        }
    }

}
