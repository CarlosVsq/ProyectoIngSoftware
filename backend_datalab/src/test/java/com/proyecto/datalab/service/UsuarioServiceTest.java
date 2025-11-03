package com.proyecto.datalab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.proyecto.datalab.dto.UsuarioUpdateRequest;
import com.proyecto.datalab.entity.Rol;
import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.enums.EstadoUsuario;
import com.proyecto.datalab.repository.RolRepository;
import com.proyecto.datalab.repository.UsuarioRepository;

/**
 * Pruebas unitarias para UsuarioService
 * Cubre HU-27: Crear usuario, HU-28: Bloquear/activar usuario
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Rol rolMedico;
    private Rol rolIP;
    private Usuario usuario;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        // Configurar rol de prueba
        rolMedico = new Rol();
        rolMedico.setIdRol(1);
        rolMedico.setNombreRol("MEDICO");

        rolIP = new Rol();
        rolIP.setIdRol(2);
        rolIP.setNombreRol("INVESTIGADORA_PRINCIPAL");

        // Configurar usuario de prueba
        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombreCompleto("Dr. Juan Pérez");
        usuario.setCorreo("juan.perez@hospital.com");
        usuario.setContrasenia(passwordEncoder.encode("password123"));
        usuario.setRol(rolMedico);
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setFechaCreacion(LocalDateTime.now());
    }

    // ==================== PRUEBAS CREAR USUARIO ====================

    @Test
    @DisplayName("Crear usuario exitosamente")
    void testCrearUsuario_Exitoso() {
        // Arrange
        when(usuarioRepository.findByCorreo("nuevo@hospital.com")).thenReturn(Optional.empty());
        when(rolRepository.findById(1)).thenReturn(Optional.of(rolMedico));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioService.crearUsuario(
            "Dr. Juan Pérez",
            "nuevo@hospital.com",
            "password123",
            1
        );

        // Assert
        assertNotNull(resultado);
        assertEquals("Dr. Juan Pérez", resultado.getNombreCompleto());
        assertEquals(EstadoUsuario.ACTIVO, resultado.getEstado());

        verify(usuarioRepository, times(1)).findByCorreo("nuevo@hospital.com");
        verify(rolRepository, times(1)).findById(1);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }







    @Test
    @DisplayName("Verificar que la contraseña se encripta al crear usuario")
    void testCrearUsuario_ContrasenaEncriptada() {
        // Arrange
        String passwordPlain = "password123";
        when(usuarioRepository.findByCorreo("nuevo@hospital.com")).thenReturn(Optional.empty());
        when(rolRepository.findById(1)).thenReturn(Optional.of(rolMedico));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            // Verificar que la contraseña no es texto plano
            assertNotEquals(passwordPlain, u.getContrasenia());
            // Verificar que está encriptada (BCrypt genera hashes que empiezan con $2a$)
            assertTrue(u.getContrasenia().startsWith("$2a$") ||
                      u.getContrasenia().startsWith("$2b$") ||
                      u.getContrasenia().startsWith("$2y$"));
            return u;
        });

        // Act
        usuarioService.crearUsuario("Dr. Juan Pérez", "nuevo@hospital.com", passwordPlain, 1);

        // Assert
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    // ==================== PRUEBAS OBTENER USUARIOS ====================

    @Test
    @DisplayName("Obtener todos los usuarios")
    void testObtenerTodosLosUsuarios() {
        // Arrange
        Usuario usuario2 = new Usuario();
        usuario2.setIdUsuario(2);
        usuario2.setNombreCompleto("Dra. María García");
        usuario2.setCorreo("maria@hospital.com");
        usuario2.setRol(rolIP);

        List<Usuario> usuarios = Arrays.asList(usuario, usuario2);
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        // Act
        List<Usuario> resultado = usuarioService.obtenerTodosLosUsuarios();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Obtener usuario por ID exitosamente")
    void testObtenerUsuarioPorId_Exitoso() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        // Act
        Optional<Usuario> resultado = usuarioService.obtenerUsuarioPorId(1);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().getIdUsuario());
        assertEquals("Dr. Juan Pérez", resultado.get().getNombreCompleto());
        verify(usuarioRepository, times(1)).findById(1);
    }


    // ==================== PRUEBAS ACTUALIZAR USUARIO ====================

    @Test
    @DisplayName("Actualizar nombre de usuario exitosamente")
    void testActualizarUsuario_Nombre() {
        // Arrange
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setNombre("Dr. Juan Carlos Pérez");

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioService.actualizarUsuario(1, request);

        // Assert
        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).findById(1);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Actualizar contraseña y verificar encriptación")
    void testActualizarUsuario_Contrasena() {
        // Arrange
        String nuevaPassword = "newPassword456";
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setContrasena(nuevaPassword);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            // Verificar que la contraseña no es texto plano
            assertNotEquals(nuevaPassword, u.getContrasenia());
            // Verificar que está encriptada
            assertTrue(u.getContrasenia().startsWith("$2a$") ||
                      u.getContrasenia().startsWith("$2b$") ||
                      u.getContrasenia().startsWith("$2y$"));
            return u;
        });

        // Act
        usuarioService.actualizarUsuario(1, request);

        // Assert
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Actualizar estado de usuario (HU-28: Activar/Desactivar)")
    void testActualizarUsuario_Estado() {
        // Arrange
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setEstado("INACTIVO");

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioService.actualizarUsuario(1, request);

        // Assert
        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).findById(1);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }


    @Test
    @DisplayName("No actualizar con datos vacíos o nulos")
    void testActualizarUsuario_DatosVacios() {
        // Arrange
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setNombre("");
        request.setCorreo("");
        request.setContrasena("");

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioService.actualizarUsuario(1, request);

        // Assert
        assertNotNull(resultado);
        // Verificar que el usuario original no fue modificado significativamente
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }
}
