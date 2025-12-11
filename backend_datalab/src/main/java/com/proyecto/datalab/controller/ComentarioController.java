package com.proyecto.datalab.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.entity.Comentario;
import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.repository.ComentarioRepository;
import com.proyecto.datalab.repository.ParticipanteRepository;
import com.proyecto.datalab.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/comentarios")
public class ComentarioController {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private ParticipanteRepository participanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/{idParticipante}")
    public List<Comentario> listarComentarios(@PathVariable Integer idParticipante) {
        return comentarioRepository.findByParticipante_IdParticipanteOrderByFechaCreacionDesc(idParticipante);
    }

    @PostMapping
    public ResponseEntity<?> agregarComentario(@RequestBody ComentarioRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Participante participante = participanteRepository.findById(request.getIdParticipante())
                .orElseThrow(() -> new RuntimeException("Participante no encontrado"));

        Comentario comentario = new Comentario();
        comentario.setContenido(request.getContenido());
        comentario.setFechaCreacion(LocalDateTime.now());
        comentario.setUsuario(usuario);
        comentario.setParticipante(participante);

        return ResponseEntity.ok(comentarioRepository.save(comentario));
    }

    // Inner DTO Class
    public static class ComentarioRequest {
        private Integer idParticipante;
        private String contenido;

        public Integer getIdParticipante() {
            return idParticipante;
        }

        public void setIdParticipante(Integer idParticipante) {
            this.idParticipante = idParticipante;
        }

        public String getContenido() {
            return contenido;
        }

        public void setContenido(String contenido) {
            this.contenido = contenido;
        }
    }
}
