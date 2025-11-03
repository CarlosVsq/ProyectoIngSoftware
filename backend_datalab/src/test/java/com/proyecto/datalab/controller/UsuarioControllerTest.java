package com.proyecto.datalab.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.datalab.config.SecurityConfig;
import com.proyecto.datalab.dto.UsuarioCreateRequest;
import com.proyecto.datalab.dto.UsuarioUpdateRequest;
import com.proyecto.datalab.entity.Rol;
import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.enums.EstadoUsuario;
import com.proyecto.datalab.service.UsuarioService;
import com.proyecto.datalab.service.auth.JwtService;

/**
 * Pruebas de integración para UsuarioController
 * Valida: Controlador -> Servicio -> Lógica de negocio
 */
@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private JwtService jwtService;

    // ==================== PRUEBAS GET ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/usuarios - Obtener todos los usuarios")
    void testObtenerTodos() throws Exception {
        // Arrange
        Rol rol = new Rol();
        rol.setIdRol(1);
        rol.setNombreRol("MEDICO");

        Usuario usuario1 = new Usuario();
        usuario1.setIdUsuario(1);
        usuario1.setNombreCompleto("Dr. Juan Pérez");
        usuario1.setCorreo("juan@hospital.com");
        usuario1.setRol(rol);
        usuario1.setEstado(EstadoUsuario.ACTIVO);

        when(usuarioService.obtenerTodosLosUsuarios()).thenReturn(Arrays.asList(usuario1));

        // Act & Assert
        mockMvc.perform(get("/api/usuarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idUsuario").value(1))
            .andExpect(jsonPath("$[0].nombreCompleto").value("Dr. Juan Pérez"))
            .andExpect(jsonPath("$[0].correo").value("juan@hospital.com"));

        verify(usuarioService, times(1)).obtenerTodosLosUsuarios();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/usuarios/{id} - Obtener usuario por ID")
    void testObtenerPorId_Exitoso() throws Exception {
        // Arrange
        Rol rol = new Rol();
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombreCompleto("Dr. Juan Pérez");
        usuario.setCorreo("juan@hospital.com");
        usuario.setRol(rol);

        when(usuarioService.obtenerUsuarioPorId(1)).thenReturn(Optional.of(usuario));

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idUsuario").value(1))
            .andExpect(jsonPath("$.nombreCompleto").value("Dr. Juan Pérez"));

        verify(usuarioService, times(1)).obtenerUsuarioPorId(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/usuarios/{id} - Usuario no encontrado (404)")
    void testObtenerPorId_NoEncontrado() throws Exception {
        // Arrange
        when(usuarioService.obtenerUsuarioPorId(999)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/999"))
            .andExpect(status().isNotFound());

        verify(usuarioService, times(1)).obtenerUsuarioPorId(999);
    }

    // ==================== PRUEBAS POST (Crear) ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/usuarios - Crear usuario exitosamente")
    void testCrearUsuario_Exitoso() throws Exception {
        // Arrange
        UsuarioCreateRequest request = new UsuarioCreateRequest();
        setField(request, "nombre", "Dr. Juan Pérez");
        setField(request, "correo", "juan@hospital.com");
        setField(request, "contrasena", "password123");
        setField(request, "rolId", 1);

        Rol rol = new Rol();
        Usuario usuarioCreado = new Usuario();
        usuarioCreado.setIdUsuario(1);
        usuarioCreado.setNombreCompleto("Dr. Juan Pérez");
        usuarioCreado.setCorreo("juan@hospital.com");
        usuarioCreado.setRol(rol);
        usuarioCreado.setEstado(EstadoUsuario.ACTIVO);

        when(usuarioService.crearUsuario(anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(usuarioCreado);

        // Act & Assert
        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.idUsuario").value(1))
            .andExpect(jsonPath("$.nombreCompleto").value("Dr. Juan Pérez"));

        verify(usuarioService, times(1)).crearUsuario(anyString(), anyString(), anyString(), anyInt());
    }

    // ==================== PRUEBAS PUT (Actualizar) ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/usuarios/{id} - Actualizar usuario exitosamente")
    void testActualizarUsuario_Exitoso() throws Exception {
        // Arrange
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setNombre("Dr. Juan Carlos Pérez");

        Rol rol = new Rol();
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setIdUsuario(1);
        usuarioActualizado.setNombreCompleto("Dr. Juan Carlos Pérez");
        usuarioActualizado.setRol(rol);

        when(usuarioService.actualizarUsuario(eq(1), any(UsuarioUpdateRequest.class)))
            .thenReturn(usuarioActualizado);

        // Act & Assert
        mockMvc.perform(put("/api/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombreCompleto").value("Dr. Juan Carlos Pérez"));

        verify(usuarioService, times(1)).actualizarUsuario(eq(1), any(UsuarioUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/usuarios/{id} - Usuario no encontrado (404)")
    void testActualizarUsuario_NoEncontrado() throws Exception {
        // Arrange
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setNombre("Nuevo Nombre");

        when(usuarioService.actualizarUsuario(eq(999), any(UsuarioUpdateRequest.class)))
            .thenThrow(new RuntimeException("Usuario no encontrado con id: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/usuarios/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());

        verify(usuarioService, times(1)).actualizarUsuario(eq(999), any(UsuarioUpdateRequest.class));
    }

    // ==================== PRUEBAS DELETE ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/usuarios/{id} - Borrar usuario exitosamente")
    void testBorrarUsuario() throws Exception {
        // Arrange
        doNothing().when(usuarioService).borrarUsuario(1);

        // Act & Assert
        mockMvc.perform(delete("/api/usuarios/1"))
            .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).borrarUsuario(1);
    }

    // Método auxiliar para establecer campos privados usando reflexión
    private void setField(Object object, String fieldName, Object value) throws Exception {
        var field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
