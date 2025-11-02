package com.proyecto.datalab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.dto.RespuestaCreateRequest;
import com.proyecto.datalab.dto.RespuestaUpdateRequest;
import com.proyecto.datalab.entity.Respuesta;
import com.proyecto.datalab.service.RespuestaService;



@RestController
@RequestMapping("/api/respuesta")
public class RespuestaController {
    @Autowired
    private RespuestaService respuestaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Respuesta creaRespuesta(@RequestBody RespuestaCreateRequest entity) {
        return respuestaService.responder(entity.getIdParticipante(),entity.getIdVariable(), entity.getValorIngresado());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Respuesta> actualizaRespuesta(@PathVariable Integer id, @RequestBody RespuestaUpdateRequest entity) {
        try{
            Respuesta respuestaActualizada = respuestaService.actualizaRespuesta(id,entity);
            return ResponseEntity.ok(respuestaActualizada);
        }catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
            return ResponseEntity.badRequest().body(null); // 400 Bad Request
        }
    }

}
