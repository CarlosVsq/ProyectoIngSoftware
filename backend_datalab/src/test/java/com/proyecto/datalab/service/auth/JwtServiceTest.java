package com.proyecto.datalab.service.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Pruebas unitarias para JwtService
 * Cubre HU-40: Mantener sesión iniciada mediante tokens JWT
 */
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private static final String SECRET_KEY = "bWlDbGF2ZVNlY3JldGFNdXlTZWd1cmFQYXJhR2VuZXJhclRva2Vuc0pXVDEyMzQ1Njc4OTA=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Configurar propiedades usando reflection
        ReflectionTestUtils.setField(jwtService, "JWT_EXPIRATION", 86400000L); // 24 horas
        ReflectionTestUtils.setField(jwtService, "REFRESH_EXPIRATION", 604800000L); // 7 días

        // Crear usuario de prueba
        userDetails = User.builder()
            .username("juan.perez@hospital.com")
            .password("password123")
            .roles("MEDICO")
            .build();
    }

    // ==================== PRUEBAS GENERACIÓN DE TOKENS ====================

    @Test
    @DisplayName("Generar token JWT exitosamente (HU-40)")
    void testGetToken_Exitoso() {
        // Act
        String token = jwtService.getToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tiene 3 partes: header.payload.signature
    }

    @Test
    @DisplayName("Token contiene el username correcto")
    void testGetToken_ContieneUsername() {
        // Act
        String token = jwtService.getToken(userDetails);
        String username = jwtService.getUsernameFromToken(token);

        // Assert
        assertEquals("juan.perez@hospital.com", username);
    }

    @Test
    @DisplayName("Generar refresh token exitosamente")
    void testGetRefreshToken_Exitoso() {
        // Act
        String refreshToken = jwtService.getRefreshToken(userDetails);

        // Assert
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertTrue(refreshToken.split("\\.").length == 3);
    }

    @Test
    @DisplayName("Refresh token contiene claim 'type' con valor 'refresh'")
    void testGetRefreshToken_ContieneTypeClaim() {
        // Act
        String refreshToken = jwtService.getRefreshToken(userDetails);
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
            .build()
            .parseClaimsJws(refreshToken)
            .getBody();

        // Assert
        assertEquals("refresh", claims.get("type"));
    }

    @Test
    @DisplayName("Token y refresh token son diferentes")
    void testGetToken_DiferentesTokens() {
        // Act
        String token = jwtService.getToken(userDetails);
        String refreshToken = jwtService.getRefreshToken(userDetails);

        // Assert
        assertNotEquals(token, refreshToken);
    }

    // ==================== PRUEBAS VALIDACIÓN DE TOKENS ====================

    @Test
    @DisplayName("Validar token válido exitosamente")
    void testIsTokenValid_TokenValido() {
        // Arrange
        String token = jwtService.getToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Rechazar token con username incorrecto")
    void testIsTokenValid_UsernameIncorrecto() {
        // Arrange
        String token = jwtService.getToken(userDetails);

        UserDetails otroUsuario = User.builder()
            .username("maria.garcia@hospital.com")
            .password("password456")
            .roles("IP")
            .build();

        // Act
        boolean isValid = jwtService.isTokenValid(token, otroUsuario);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Rechazar token malformado")
    void testIsTokenValid_TokenMalformado() {
        // Arrange
        String tokenMalformado = "token.invalido.malformado";

        // Act
        boolean isValid = jwtService.isTokenValid(tokenMalformado, userDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Rechazar token vacío")
    void testIsTokenValid_TokenVacio() {
        // Act
        boolean isValid = jwtService.isTokenValid("", userDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Rechazar token expirado")
    void testIsTokenValid_TokenExpirado() throws InterruptedException {
        // Arrange - Crear token con expiración muy corta (1 milisegundo)
        ReflectionTestUtils.setField(jwtService, "JWT_EXPIRATION", 1L);
        String token = jwtService.getToken(userDetails);

        // Esperar a que expire
        Thread.sleep(10);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertFalse(isValid);
    }

    // ==================== PRUEBAS EXTRACCIÓN DE DATOS ====================

    @Test
    @DisplayName("Extraer username del token")
    void testGetUsernameFromToken() {
        // Arrange
        String token = jwtService.getToken(userDetails);

        // Act
        String username = jwtService.getUsernameFromToken(token);

        // Assert
        assertEquals("juan.perez@hospital.com", username);
    }

    @Test
    @DisplayName("Extraer claim personalizado del token")
    void testGetClaim_ClaimPersonalizado() {
        // Arrange
        String refreshToken = jwtService.getRefreshToken(userDetails);

        // Act
        String type = jwtService.getClaim(refreshToken, claims -> (String) claims.get("type"));

        // Assert
        assertEquals("refresh", type);
    }

    @Test
    @DisplayName("Extraer fecha de expiración del token")
    void testGetClaim_Expiration() {
        // Arrange
        String token = jwtService.getToken(userDetails);

        // Act
        Date expiration = jwtService.getClaim(token, Claims::getExpiration);
        Date now = new Date();

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(now));
    }

    @Test
    @DisplayName("Extraer fecha de emisión del token")
    void testGetClaim_IssuedAt() {
        // Arrange
        String token = jwtService.getToken(userDetails);

        // Act
        Date issuedAt = jwtService.getClaim(token, Claims::getIssuedAt);
        Date now = new Date();

        // Assert
        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(now) || issuedAt.equals(now));
    }

    @Test
    @DisplayName("Extraer subject del token")
    void testGetClaim_Subject() {
        // Arrange
        String token = jwtService.getToken(userDetails);

        // Act
        String subject = jwtService.getClaim(token, Claims::getSubject);

        // Assert
        assertEquals("juan.perez@hospital.com", subject);
    }

    // ==================== PRUEBAS TIEMPO DE EXPIRACIÓN ====================

    @Test
    @DisplayName("Obtener tiempo de expiración configurado")
    void testGetExpirationTime() {
        // Act
        Long expirationTime = jwtService.getExpirationTime();

        // Assert
        assertEquals(86400000L, expirationTime); // 24 horas en milisegundos
    }

    @Test
    @DisplayName("Token regular expira en 24 horas")
    void testGetToken_ExpiraEn24Horas() {
        // Arrange
        long now = System.currentTimeMillis();
        String token = jwtService.getToken(userDetails);

        // Act
        Date expiration = jwtService.getClaim(token, Claims::getExpiration);
        long expirationTime = expiration.getTime();

        // Assert
        long expectedExpiration = now + 86400000L; // 24 horas
        long tolerance = 1000; // 1 segundo de tolerancia

        assertTrue(Math.abs(expirationTime - expectedExpiration) < tolerance);
    }

    @Test
    @DisplayName("Refresh token expira en 7 días")
    void testGetRefreshToken_ExpiraEn7Dias() {
        // Arrange
        long now = System.currentTimeMillis();
        String refreshToken = jwtService.getRefreshToken(userDetails);

        // Act
        Date expiration = jwtService.getClaim(refreshToken, Claims::getExpiration);
        long expirationTime = expiration.getTime();

        // Assert
        long expectedExpiration = now + 604800000L; // 7 días
        long tolerance = 1000; // 1 segundo de tolerancia

        assertTrue(Math.abs(expirationTime - expectedExpiration) < tolerance);
    }

    // ==================== PRUEBAS MÚLTIPLES USUARIOS ====================

    @Test
    @DisplayName("Generar tokens diferentes para usuarios diferentes")
    void testGetToken_DiferentesUsuarios() {
        // Arrange
        UserDetails usuario1 = User.builder()
            .username("usuario1@hospital.com")
            .password("pass1")
            .roles("MEDICO")
            .build();

        UserDetails usuario2 = User.builder()
            .username("usuario2@hospital.com")
            .password("pass2")
            .roles("IP")
            .build();

        // Act
        String token1 = jwtService.getToken(usuario1);
        String token2 = jwtService.getToken(usuario2);

        // Assert
        assertNotEquals(token1, token2);
        assertEquals("usuario1@hospital.com", jwtService.getUsernameFromToken(token1));
        assertEquals("usuario2@hospital.com", jwtService.getUsernameFromToken(token2));
    }

    @Test
    @DisplayName("Token válido para usuario correcto pero inválido para otro")
    void testIsTokenValid_ValidoParaUnSoloUsuario() {
        // Arrange
        UserDetails usuario1 = User.builder()
            .username("usuario1@hospital.com")
            .password("pass1")
            .roles("MEDICO")
            .build();

        UserDetails usuario2 = User.builder()
            .username("usuario2@hospital.com")
            .password("pass2")
            .roles("IP")
            .build();

        String token = jwtService.getToken(usuario1);

        // Act & Assert
        assertTrue(jwtService.isTokenValid(token, usuario1));
        assertFalse(jwtService.isTokenValid(token, usuario2));
    }

    // ==================== PRUEBAS CASOS EDGE ====================

    @Test
    @DisplayName("Generar token para usuario con caracteres especiales en username")
    void testGetToken_UsernameConCaracteresEspeciales() {
        // Arrange
        UserDetails usuarioEspecial = User.builder()
            .username("maría.garcía+test@hospital.com")
            .password("password")
            .roles("MEDICO")
            .build();

        // Act
        String token = jwtService.getToken(usuarioEspecial);
        String username = jwtService.getUsernameFromToken(token);

        // Assert
        assertEquals("maría.garcía+test@hospital.com", username);
    }

    @Test
    @DisplayName("Generar múltiples tokens para el mismo usuario")
    void testGetToken_MultiplesPorUsuario() {
        // Act
        String token1 = jwtService.getToken(userDetails);
        String token2 = jwtService.getToken(userDetails);
        String token3 = jwtService.getToken(userDetails);

        // Assert - Todos deben ser diferentes (diferentes timestamps)
        assertNotEquals(token1, token2);
        assertNotEquals(token2, token3);
        assertNotEquals(token1, token3);

        // Pero todos deben ser válidos
        assertTrue(jwtService.isTokenValid(token1, userDetails));
        assertTrue(jwtService.isTokenValid(token2, userDetails));
        assertTrue(jwtService.isTokenValid(token3, userDetails));
    }
}
