package com.proyecto.datalab.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio personalizado de autenticación
 * HU-40: Mantener sesión iniciada
 * HU-03: Accesos por rol
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Cargando usuario: {}", username);
        
        Usuario usuario = usuarioRepository.findByCorreo(username)
            .orElseThrow(() -> {
                log.warn("Usuario no encontrado: {}", username);
                return new UsernameNotFoundException(
                    "Usuario no encontrado con correo: " + username
                );
            });
        
        if (usuario.getRol() == null) {
            log.error("Usuario {} no tiene rol asignado", username);
            throw new IllegalStateException("Usuario sin rol asignado");
        }
        
        log.info("Usuario cargado: {} con rol: {}", username, usuario.getRol().getNombreRol());
        
        return usuario;
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Integer idUsuario) throws UsernameNotFoundException {
        log.debug("Cargando usuario por ID: {}", idUsuario);
        
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> {
                log.warn("Usuario no encontrado con ID: {}", idUsuario);
                return new UsernameNotFoundException(
                    "Usuario no encontrado con ID: " + idUsuario
                );
            });
        
        if (usuario.getRol() == null) {
            log.error("Usuario ID {} no tiene rol asignado", idUsuario);
            throw new IllegalStateException("Usuario sin rol asignado");
        }
        
        return usuario;
    }
}
