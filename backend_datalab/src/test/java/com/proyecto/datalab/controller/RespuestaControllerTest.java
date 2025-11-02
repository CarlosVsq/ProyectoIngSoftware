package com.proyecto.datalab.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.datalab.dto.RespuestaCreateRequest;
import com.proyecto.datalab.dto.RespuestaUpdateRequest;
import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.entity.Respuesta;
import com.proyecto.datalab.entity.Variable;
import com.proyecto.datalab.enums.EstadoFicha;
import com.proyecto.datalab.enums.GrupoParticipante;
import com.proyecto.datalab.service.RespuestaService;

/**
 * Pruebas de integración para RespuestaController
 */
@WebMvcTest(RespuestaController.class)
@AutoConfigureMockMvc(addFilters = false) // Desactiva filtros de seguridad para las pruebas
class RespuestaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RespuestaService respuestaService;

    private Participante participante;
    private Variable variable;
    private Respuesta respuesta;

    @BeforeEach
    void setUp() {
        // Configurar participante de prueba
        participante = new Participante();
        participante.setIdParticipante(1);
        participante.setCodigoParticipante("PART-001");
        participante.setNombreCompleto("Juan Pérez");
        participante.setGrupo(GrupoParticipante.CONTROL);
        participante.setEstadoFicha(EstadoFicha.INCOMPLETA);
        participante.setFechaInclusion(LocalDate.now());

        // Configurar variable de prueba
        variable = new Variable();
        variable.setIdVariable(1);
        variable.setCodigoVariable("VAR-001");
        variable.setEnunciado("¿Cuál es su edad?");
        variable.setTipoDato("NUMERICO");
        variable.setAplicaA("TODOS");
        variable.setEsObligatoria(true);

        // Configurar respuesta de prueba
        respuesta = new Respuesta();
        respuesta.setIdRespuesta(1);
        respuesta.setParticipante(participante);
        respuesta.setVariable(variable);
        respuesta.setValorIngresado("25");
    }

    @Test
    @DisplayName("POST /api/respuesta - Crear respuesta exitosamente")
    void testCreaRespuesta_Exitoso() throws Exception {
        // Arrange
        RespuestaCreateRequest request = new RespuestaCreateRequest();
        request.setIdParticipante(1);
        request.setIdVariable(1);
        request.setValorIngresado("25");

        when(respuestaService.responder(1, 1, "25")).thenReturn(respuesta);

        // Act & Assert
        mockMvc.perform(post("/api/respuesta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idRespuesta").value(1))
                .andExpect(jsonPath("$.valorIngresado").value("25"));
    }

    @Test
    @DisplayName("POST /api/respuesta - Participante no encontrado")
    void testCreaRespuesta_ParticipanteNoEncontrado() throws Exception {
        // Arrange
        RespuestaCreateRequest request = new RespuestaCreateRequest();
        request.setIdParticipante(999);
        request.setIdVariable(1);
        request.setValorIngresado("25");

        when(respuestaService.responder(999, 1, "25"))
                .thenThrow(new RuntimeException("Participante no encontrado"));

        // Act & Assert
        mockMvc.perform(post("/api/respuesta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/respuesta - Variable no encontrada")
    void testCreaRespuesta_VariableNoEncontrada() throws Exception {
        // Arrange
        RespuestaCreateRequest request = new RespuestaCreateRequest();
        request.setIdParticipante(1);
        request.setIdVariable(999);
        request.setValorIngresado("25");

        when(respuestaService.responder(1, 999, "25"))
                .thenThrow(new RuntimeException("Variable no encontrada"));

        // Act & Assert
        mockMvc.perform(post("/api/respuesta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/respuesta - Validación falla sin ID participante")
    void testCreaRespuesta_ValidacionFallaParticipante() throws Exception {
        // Arrange
        RespuestaCreateRequest request = new RespuestaCreateRequest();
        // No se establece idParticipante
        request.setIdVariable(1);
        request.setValorIngresado("25");

        // Act & Assert
        mockMvc.perform(post("/api/respuesta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/respuesta - Validación falla sin ID variable")
    void testCreaRespuesta_ValidacionFallaVariable() throws Exception {
        // Arrange
        RespuestaCreateRequest request = new RespuestaCreateRequest();
        request.setIdParticipante(1);
        // No se establece idVariable
        request.setValorIngresado("25");

        // Act & Assert
        mockMvc.perform(post("/api/respuesta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/respuesta - Crear respuesta con valor nulo")
    void testCreaRespuesta_ValorNulo() throws Exception {
        // Arrange
        RespuestaCreateRequest request = new RespuestaCreateRequest();
        request.setIdParticipante(1);
        request.setIdVariable(1);
        request.setValorIngresado(null);

        Respuesta respuestaNula = new Respuesta();
        respuestaNula.setIdRespuesta(1);
        respuestaNula.setParticipante(participante);
        respuestaNula.setVariable(variable);
        respuestaNula.setValorIngresado(null);

        when(respuestaService.responder(1, 1, null)).thenReturn(respuestaNula);

        // Act & Assert
        mockMvc.perform(post("/api/respuesta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idRespuesta").value(1));
    }

    @Test
    @DisplayName("PUT /api/respuesta/{id} - Actualizar respuesta exitosamente")
    void testActualizaRespuesta_Exitoso() throws Exception {
        // Arrange
        RespuestaUpdateRequest request = new RespuestaUpdateRequest();
        request.setValorIngresado("30");

        Respuesta respuestaActualizada = new Respuesta();
        respuestaActualizada.setIdRespuesta(1);
        respuestaActualizada.setParticipante(participante);
        respuestaActualizada.setVariable(variable);
        respuestaActualizada.setValorIngresado("30");

        when(respuestaService.actualizaRespuesta(eq(1), any(RespuestaUpdateRequest.class)))
                .thenReturn(respuestaActualizada);

        // Act & Assert
        mockMvc.perform(put("/api/respuesta/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRespuesta").value(1))
                .andExpect(jsonPath("$.valorIngresado").value("30"));
    }

    @Test
    @DisplayName("PUT /api/respuesta/{id} - Respuesta no encontrada")
    void testActualizaRespuesta_RespuestaNoEncontrada() throws Exception {
        // Arrange
        RespuestaUpdateRequest request = new RespuestaUpdateRequest();
        request.setValorIngresado("30");

        when(respuestaService.actualizaRespuesta(eq(999), any(RespuestaUpdateRequest.class)))
                .thenThrow(new RuntimeException("Respuesta no encontrada"));

        // Act & Assert
        mockMvc.perform(put("/api/respuesta/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/respuesta/{id} - Validación falla con valor vacío")
    void testActualizaRespuesta_ValidacionFallaValorVacio() throws Exception {
        // Arrange
        RespuestaUpdateRequest request = new RespuestaUpdateRequest();
        request.setValorIngresado("");

        // Act & Assert
        mockMvc.perform(put("/api/respuesta/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/respuesta/{id} - Validación falla con valor nulo")
    void testActualizaRespuesta_ValidacionFallaValorNulo() throws Exception {
        // Arrange
        RespuestaUpdateRequest request = new RespuestaUpdateRequest();
        request.setValorIngresado(null);

        // Act & Assert
        mockMvc.perform(put("/api/respuesta/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/respuesta - Error genérico retorna 400")
    void testCreaRespuesta_ErrorGenerico() throws Exception {
        // Arrange
        RespuestaCreateRequest request = new RespuestaCreateRequest();
        request.setIdParticipante(1);
        request.setIdVariable(1);
        request.setValorIngresado("25");

        when(respuestaService.responder(1, 1, "25"))
                .thenThrow(new RuntimeException("Error inesperado"));

        // Act & Assert
        mockMvc.perform(post("/api/respuesta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
