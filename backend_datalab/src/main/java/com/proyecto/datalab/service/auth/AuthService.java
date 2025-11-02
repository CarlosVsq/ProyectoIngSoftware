package com.proyecto.datalab.service.auth;

import com.proyecto.datalab.enums.EstadoUsuario;
import com.proyecto.datalab.entity.Rol;
import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.repository.RolRepository;
import com.proyecto.datalab.repository.UsuarioRepository;
import com.proyecto.datalab.web.dto.auth.AuthResponse;
import com.proyecto.datalab.web.dto.auth.LoginRequest;
import com.proyecto.datalab.web.dto.auth.RegisterRequest;
import com.proyecto.datalab.web.dto.user.PermisosDTO;
import com.proyecto.datalab.web.dto.user.UsuarioDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de Autenticación
 * HU-40: Mantener sesión iniciada
 * HU-03: Accesos por rol
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Login de usuario - HU-40
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login para: {}", request.getCorreo());
        
        // 1. Autenticar (CustomUserDetailsService se ejecuta aquí)
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getCorreo(), 
                request.getContrasenia()
            )
        );
        
        // 2. Obtener usuario autenticado
        Usuario usuario = (Usuario) authentication.getPrincipal();
        
        log.info("Login exitoso para: {} con rol: {}", 
                 usuario.getCorreo(), 
                 usuario.getRol().getNombreRol());
        
        // 3. Generar tokens
        String accessToken = jwtService.getToken(usuario);
        String refreshToken = jwtService.getRefreshToken(usuario);
        
        // 4. Construir respuesta
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getExpirationTime())
            .usuario(mapToUsuarioDTO(usuario))
            .build();
    }

    /**
     * Registro de usuario
     * Nota: En producción, solo IP/Admin deberían poder registrar
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Intento de registro para: {}", request.getCorreo());
        
        // 1. Validar que el correo no exista
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            log.warn("Intento de registro con correo duplicado: {}", request.getCorreo());
            throw new IllegalArgumentException("El correo ya está registrado");
        }
        
        // 2. Validar que el rol exista
        Rol rol = rolRepository.findById(request.getIdRol())
            .orElseThrow(() -> {
                log.error("Rol no encontrado con ID: {}", request.getIdRol());
                return new IllegalArgumentException(
                    "Rol no encontrado con ID: " + request.getIdRol()
                );
            });
        
        // 3. Crear usuario con contraseña encriptada
        Usuario nuevoUsuario = Usuario.builder()
            .correo(request.getCorreo())
            .contrasenia(passwordEncoder.encode(request.getContrasenia()))
            .nombreCompleto(request.getNombreCompleto())
            .rol(rol)
            .estado(EstadoUsuario.ACTIVO)
            .build();
        
        // 4. Guardar
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        
        log.info("Usuario registrado: {} con rol: {}", 
                 usuarioGuardado.getCorreo(), 
                 rol.getNombreRol());
        
        // 5. Generar tokens
        String accessToken = jwtService.getToken(usuarioGuardado);
        String refreshToken = jwtService.getRefreshToken(usuarioGuardado);
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getExpirationTime())
            .usuario(mapToUsuarioDTO(usuarioGuardado))
            .build();
    }

    /**
     * Mapea Usuario a UsuarioDTO
     */
    private UsuarioDTO mapToUsuarioDTO(Usuario usuario) {
        return UsuarioDTO.builder()
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
    }
}
