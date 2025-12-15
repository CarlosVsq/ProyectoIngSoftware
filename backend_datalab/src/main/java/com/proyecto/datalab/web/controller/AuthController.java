package com.proyecto.datalab.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.repository.UsuarioRepository;
import com.proyecto.datalab.service.auth.AuthService;
import com.proyecto.datalab.service.AuditoriaService;
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

/**
 * Controller de Autenticación
 * HU-40: Mantener sesión iniciada
 * HU-11: Auditoría de sesiones
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints de autenticación y registro")
public class AuthController {

    private final AuthService authService;
    private final AuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;

    /**
     * Login de usuario - HU-40
     */
    @PostMapping("/login")
    @Operation(summary = "Login de usuario", description = "Autentica un usuario y retorna JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/login - Correo: {}", request.getCorreo());

        try {
            AuthResponse response = authService.login(request);

            // --- AUDITORÍA DE LOGIN ---
            try {
                // Buscamos la entidad Usuario real usando el ID del DTO para tener la
                // referencia completa
                if (response.getUsuario() != null) {
                    Usuario usuarioEntity = usuarioRepository.findById(response.getUsuario().getIdUsuario())
                            .orElse(null);

                    if (usuarioEntity != null) {
                        // Registramos la acción LOGIN en la tabla SESION
                        // Importante: Enviamos null en participante porque es un evento de sistema
                        auditoriaService.registrarAccion(
                                usuarioEntity,
                                null,
                                "LOGIN",
                                "SESION",
                                "Inicio de sesión exitoso desde web");
                    }
                }
            } catch (Exception ex) {
                // Logueamos el error pero no detenemos el login para no afectar al usuario
                log.error("No se pudo registrar auditoría de login: {}", ex.getMessage());
            }
            // --------------------------

            return ResponseEntity.ok(
                    ApiResponse.success("Login exitoso", response));
        } catch (Exception e) {
            log.error("Error en login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Credenciales inválidas"));
        }
    }

    /**
     * Logout de usuario (Para auditoría)
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout de usuario", description = "Registra la salida del usuario en auditoría")
    public ResponseEntity<ApiResponse<Boolean>> logout(@AuthenticationPrincipal Usuario usuario) {
        if (usuario != null) {
            log.info("POST /auth/logout - Usuario: {}", usuario.getCorreo());
            try {
                // --- AUDITORÍA DE LOGOUT ---
                auditoriaService.registrarAccion(
                        usuario,
                        null,
                        "LOGOUT",
                        "SESION",
                        "Cierre de sesión manual");
            } catch (Exception e) {
                log.error("Error registrando logout: {}", e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success(true));
    }

    @PostMapping("/register")
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
                        .puedeVerDatos(usuario.puedeVerDatos())
                        .puedeModificar(usuario.puedeModificar())
                        .puedeCrudCrf(usuario.puedeCrudCrf())
                        .puedeExportar(usuario.puedeExportar())
                        .puedeReclutar(usuario.puedeReclutar())
                        .puedeAdministrarUsuarios(usuario.puedeAdministrarUsuarios())
                        .puedeVerAuditoria(usuario.puedeVerAuditoria())
                        .build())
                .build();

        return ResponseEntity.ok(ApiResponse.success(usuarioDTO));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validar token", description = "Valida si el token es válido")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(ApiResponse.success(true));
    }

    // --- Password Recovery Endpoints ---

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Envía un correo con el token de recuperación")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email is required"));
        }
        try {
            authService.forgotPassword(email);
            return ResponseEntity.ok(ApiResponse.success("Correo enviado (si el usuario existe)"));
        } catch (Exception e) {
            log.error("Error sending forgot password email: {}", e.getMessage());
            // Always return success to prevent user enum
            return ResponseEntity.ok(ApiResponse.success("Correo enviado (si el usuario existe)"));
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña", description = "Cambia la contraseña usando un token válido")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody java.util.Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Token and newPassword are required"));
        }

        try {
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok(ApiResponse.success("Contraseña actualizada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}