package com.proyecto.datalab.web.controller;

import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.service.auth.AuthService;
import com.proyecto.datalab.web.dto.auth.AuthResponse;
import com.proyecto.datalab.web.dto.auth.LoginRequest;
import com.proyecto.datalab.web.dto.auth.RegisterRequest;
import com.proyecto.datalab.web.dto.common.ApiResponse;
import com.proyecto.datalab.web.dto.user.PermisosDTO;
import com.proyecto.datalab.web.dto.user.UsuarioDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de Autenticación
 * HU-40: Mantener sesión iniciada
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints de autenticación y registro")
public class AuthController {

    private final AuthService authService;
    
    /**
     * Login de usuario - HU-40
     */
    @PostMapping("/login")
    @Operation(summary = "Login de usuario", description = "Autentica un usuario y retorna JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/login - Correo: {}", request.getCorreo());
        
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(
                ApiResponse.success("Login exitoso", response)
            );
        } catch (Exception e) {
            log.error("Error en login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Credenciales inválidas"));
        }
    }

    @PostMapping("/register")
    //@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'INVESTIGADORA_PRINCIPAL')")
    @Operation(summary = "Registro de usuario", description = "Registra un nuevo usuario")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /auth/register - Correo: {}", request.getCorreo());
        
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario registrado exitosamente", response));
        } catch (IllegalArgumentException e) {
            log.warn("Error en registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error en registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al registrar usuario"));
        }
    }
    
    /**
     * Obtener perfil del usuario autenticado
    */
    @GetMapping("/me")
    @Operation(summary = "Obtener perfil", description = "Obtiene el perfil del usuario autenticado")
    public ResponseEntity<ApiResponse<UsuarioDTO>> getProfile(@AuthenticationPrincipal Usuario usuario) {
        log.info("GET /auth/me - Usuario: {}", usuario.getCorreo());
        
        UsuarioDTO usuarioDTO = UsuarioDTO.builder()
            .idUsuario(usuario.getIdUsuario())
            .nombreCompleto(usuario.getNombreCompleto())
            .correo(usuario.getCorreo())
            .rol(usuario.getRol().getNombreRol())
            .estado(usuario.getEstado().name())
            .permisos(PermisosDTO.builder()
                .puedeCrudCrf(usuario.puedeCrudCrf())
                .puedeExportar(usuario.puedeExportar())
                .puedeReclutar(usuario.puedeReclutar())
                .puedeAdministrarUsuarios(usuario.puedeAdministrarUsuarios())
                .puedeVerAuditoria(usuario.puedeVerAuditoria())
                .build())
            .build();
        
        return ResponseEntity.ok(ApiResponse.success(usuarioDTO));
    }

    /**
     * Validar token (útil para el frontend)
     */
    @GetMapping("/validate")
    @Operation(summary = "Validar token", description = "Valida si el token es válido")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@AuthenticationPrincipal Usuario usuario) {
        log.debug("GET /auth/validate - Usuario: {}", usuario.getCorreo());
        return ResponseEntity.ok(ApiResponse.success(true));
    }
}
