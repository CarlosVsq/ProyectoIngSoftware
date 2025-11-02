package com.proyecto.datalab.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Servicio JWT - HU-40: Mantener sesión iniciada
 * Genera y valida tokens JWT para autenticación
 */
@Service
@Slf4j
public class JwtService {

    //private static final String SECRET_KEY = "miClaveSecretaMuySeguraParaGenerarTokensJWT1234567890";  
    private static final String SECRET_KEY = "bWlDbGF2ZVNlY3JldGFNdXlTZWd1cmFQYXJhR2VuZXJhclRva2Vuc0pXVDEyMzQ1Njc4OTA=";
    
    @Value("${jwt.expiration:86400000}") // 24 horas
    private Long JWT_EXPIRATION;
    
    @Value("${jwt.refresh-expiration:604800000}") // 7 días
    private Long REFRESH_EXPIRATION;

    public String getToken(UserDetails user) {
        return getToken(new HashMap<>(), user, JWT_EXPIRATION);
    }

    private String getToken(Map<String, Object> extraClaims, UserDetails user, Long expiration) {
        long now = System.currentTimeMillis();
        
        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(user.getUsername())
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + expiration))
            .signWith(getKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String getRefreshToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return getToken(claims, user, REFRESH_EXPIRATION);
    }

    private Key getKey() {
        if (SECRET_KEY == null || SECRET_KEY.trim().isEmpty()) {
      throw new IllegalStateException("jwt.secret no está configurado");
        }

        String s = SECRET_KEY.trim();
        byte[] keyBytes;

        // 1) Intenta Base64 estándar
        try {
        keyBytes = Decoders.BASE64.decode(s);
        } catch (Exception e1) {
        // 2) Intenta Base64URL
        try {
            keyBytes = Decoders.BASE64URL.decode(s);
        } catch (Exception e2) {
            // 3) Usa texto plano
            keyBytes = s.getBytes(StandardCharsets.UTF_8);
        }
        }

        if (keyBytes.length < 32) {
        // JJWT también lanzaría WeakKeyException al signWith, pero damos un mensaje claro
        throw new WeakKeyException("La clave JWT debe tener al menos 256 bits (32 bytes). " +
            "Aumenta jwt.secret o usa un secreto Base64 de >=32 bytes.");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    private Claims getAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    public Long getExpirationTime() {
        return JWT_EXPIRATION;
    }
}
