package com.proyecto.datalab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.datalab.dto.RespuestaUpdateRequest;
import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.entity.Respuesta;
import com.proyecto.datalab.entity.Variable;
import com.proyecto.datalab.enums.EstadoFicha;
import com.proyecto.datalab.enums.GrupoParticipante;
import com.proyecto.datalab.repository.ParticipanteRepository;
import com.proyecto.datalab.repository.RespuestaRepository;
import com.proyecto.datalab.repository.VariableRepository;

/**
 * Pruebas unitarias para RespuestaService
 */
@ExtendWith(MockitoExtension.class)
class RespuestaServiceTest {

    @Mock
    private RespuestaRepository respuestaRepository;

    @Mock
    private ParticipanteRepository participanteRepository;

    @Mock
    private VariableRepository variableRepository;

    @InjectMocks
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
        participante.setEstadoFicha(EstadoFicha.ACTIVA);
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
    @DisplayName("Crear respuesta exitosamente")
    void testResponder_Exitoso() {
        // Arrange
        when(participanteRepository.findById(1)).thenReturn(Optional.of(participante));
        when(variableRepository.findById(1)).thenReturn(Optional.of(variable));
        when(respuestaRepository.save(any(Respuesta.class))).thenReturn(respuesta);

        // Act
        Respuesta resultado = respuestaService.responder(1, 1, "25");

        // Assert
        assertNotNull(resultado);
        assertEquals("25", resultado.getValorIngresado());
        assertEquals(participante.getIdParticipante(), resultado.getParticipante().getIdParticipante());
        assertEquals(variable.getIdVariable(), resultado.getVariable().getIdVariable());

        verify(participanteRepository, times(1)).findById(1);
        verify(variableRepository, times(1)).findById(1);
        verify(respuestaRepository, times(1)).save(any(Respuesta.class));
    }

    @Test
    @DisplayName("Lanzar excepción cuando participante no existe")
    void testResponder_ParticipanteNoEncontrado() {
        // Arrange
        when(participanteRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            respuestaService.responder(999, 1, "25");
        });

        assertEquals("Participante no encontrado", exception.getMessage());
        verify(participanteRepository, times(1)).findById(999);
        verify(variableRepository, never()).findById(any());
        verify(respuestaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanzar excepción cuando variable no existe")
    void testResponder_VariableNoEncontrada() {
        // Arrange
        when(participanteRepository.findById(1)).thenReturn(Optional.of(participante));
        when(variableRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            respuestaService.responder(1, 999, "25");
        });

        assertEquals("Variable no encontrada", exception.getMessage());
        verify(participanteRepository, times(1)).findById(1);
        verify(variableRepository, times(1)).findById(999);
        verify(respuestaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear respuesta con valor nulo")
    void testResponder_ValorNulo() {
        // Arrange
        Respuesta respuestaNula = new Respuesta();
        respuestaNula.setIdRespuesta(1);
        respuestaNula.setParticipante(participante);
        respuestaNula.setVariable(variable);
        respuestaNula.setValorIngresado(null);

        when(participanteRepository.findById(1)).thenReturn(Optional.of(participante));
        when(variableRepository.findById(1)).thenReturn(Optional.of(variable));
        when(respuestaRepository.save(any(Respuesta.class))).thenReturn(respuestaNula);

        // Act
        Respuesta resultado = respuestaService.responder(1, 1, null);

        // Assert
        assertNotNull(resultado);
        assertNull(resultado.getValorIngresado());
        verify(respuestaRepository, times(1)).save(any(Respuesta.class));
    }

    @Test
    @DisplayName("Actualizar respuesta exitosamente")
    void testActualizaRespuesta_Exitoso() {
        // Arrange
        RespuestaUpdateRequest request = new RespuestaUpdateRequest();
        request.setValorIngresado("30");

        Respuesta respuestaActualizada = new Respuesta();
        respuestaActualizada.setIdRespuesta(1);
        respuestaActualizada.setParticipante(participante);
        respuestaActualizada.setVariable(variable);
        respuestaActualizada.setValorIngresado("30");

        when(respuestaRepository.findById(1)).thenReturn(Optional.of(respuesta));
        when(respuestaRepository.save(any(Respuesta.class))).thenReturn(respuestaActualizada);

        // Act
        Respuesta resultado = respuestaService.actualizaRespuesta(1, request);

        // Assert
        assertNotNull(resultado);
        assertEquals("30", resultado.getValorIngresado());
        verify(respuestaRepository, times(1)).findById(1);
        verify(respuestaRepository, times(1)).save(any(Respuesta.class));
    }

    @Test
    @DisplayName("Lanzar excepción al actualizar respuesta inexistente")
    void testActualizaRespuesta_RespuestaNoEncontrada() {
        // Arrange
        RespuestaUpdateRequest request = new RespuestaUpdateRequest();
        request.setValorIngresado("30");

        when(respuestaRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            respuestaService.actualizaRespuesta(999, request);
        });

        assertEquals("Respuesta no encontrada", exception.getMessage());
        verify(respuestaRepository, times(1)).findById(999);
        verify(respuestaRepository, never()).save(any());
    }

    @Test
    @DisplayName("No actualizar respuesta con valor vacío")
    void testActualizaRespuesta_ValorVacio() {
        // Arrange
        RespuestaUpdateRequest request = new RespuestaUpdateRequest();
        request.setValorIngresado("");

        when(respuestaRepository.findById(1)).thenReturn(Optional.of(respuesta));
        when(respuestaRepository.save(any(Respuesta.class))).thenReturn(respuesta);

        // Act
        Respuesta resultado = respuestaService.actualizaRespuesta(1, request);

        // Assert
        assertNotNull(resultado);
        assertEquals("25", resultado.getValorIngresado()); // Debe mantener el valor original
        verify(respuestaRepository, times(1)).findById(1);
        verify(respuestaRepository, times(1)).save(any(Respuesta.class));
    }

    @Test
    @DisplayName("No actualizar respuesta con valor nulo")
    void testActualizaRespuesta_ValorNulo() {
        // Arrange
        RespuestaUpdateRequest request = new RespuestaUpdateRequest();
        request.setValorIngresado(null);

        when(respuestaRepository.findById(1)).thenReturn(Optional.of(respuesta));
        when(respuestaRepository.save(any(Respuesta.class))).thenReturn(respuesta);

        // Act
        Respuesta resultado = respuestaService.actualizaRespuesta(1, request);

        // Assert
        assertNotNull(resultado);
        assertEquals("25", resultado.getValorIngresado()); // Debe mantener el valor original
        verify(respuestaRepository, times(1)).findById(1);
        verify(respuestaRepository, times(1)).save(any(Respuesta.class));
    }
}
