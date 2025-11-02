package com.proyecto.datalab.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.dto.UsuarioCreateRequest;
import com.proyecto.datalab.dto.UsuarioUpdateRequest;
import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // --- GET (Todos) ---
    @GetMapping
    public List<Usuario> obtenerTodos() {
        return usuarioService.obtenerTodosLosUsuarios();
    }

    // --- GET (Id especifico) ---
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerUsuarioPorId(id)
                .map(usuario -> ResponseEntity.ok(usuario)) // 200 OK si se encuentra
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found si no
    }

    // --- POST ---
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201 Created
    public Usuario crearUsuario(@RequestBody UsuarioCreateRequest request) {
        return usuarioService.crearUsuario(
                request.getNombre(),
                request.getCorreo(),
                request.getContrasena(),
                request.getRolId()
        );
    }

    // --- DELETE ---
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Devuelve un 204 No Content
    public void borrarUsuario(@PathVariable Long id) {
        usuarioService.borrarUsuario(id);
    }

    // --- PUT ---
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id,@RequestBody UsuarioUpdateRequest request) {
        try {
            Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, request);
            return ResponseEntity.ok(usuarioActualizado); // 200 OK
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
            return ResponseEntity.badRequest().body(null); // 400 Bad Request
        }
    }
}