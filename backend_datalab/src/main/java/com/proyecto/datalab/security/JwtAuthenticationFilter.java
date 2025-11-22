package com.proyecto.datalab.security;

import com.proyecto.datalab.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT - Intercepta todas las requests y valida el token
 * HU-40: Mantener sesión iniciada
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 1. Obtener token del header
            String token = getTokenFromRequest(request);
            
            // 2. Si no hay token, continuar con el filtro
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // 3. Extraer username del token
            String username = jwtService.getUsernameFromToken(token);
            
            // 4. Si hay username y no está autenticado
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // 5. Cargar usuario desde BD
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 6. Validar token
                if (jwtService.isTokenValid(token, userDetails)) {
                    
                    // 7. Crear Authentication y guardarlo en el contexto
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("Usuario autenticado: {}", username);
                }
            }
            
        } catch (Exception e) {
            log.error("Error en filtro JWT: {}", e.getMessage());
        }
        
        // 8. Continuar con el filtro
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token del header Authorization
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}
