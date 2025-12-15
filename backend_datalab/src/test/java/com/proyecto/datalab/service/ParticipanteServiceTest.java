package com.proyecto.datalab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.entity.Respuesta;
import com.proyecto.datalab.entity.Rol;
import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.entity.Variable;
import com.proyecto.datalab.enums.EstadoFicha;
import com.proyecto.datalab.enums.EstadoUsuario;
import com.proyecto.datalab.enums.GrupoParticipante;
import com.proyecto.datalab.repository.ParticipanteRepository;
import com.proyecto.datalab.repository.RespuestaRepository;
import com.proyecto.datalab.repository.UsuarioRepository;
import com.proyecto.datalab.repository.VariableRepository;

/**
 * Pruebas unitarias para ParticipanteService
 * Cubre HU-01: Registro de CRF digital, HU-15: Guardar formularios incompletos
 */
@ExtendWith(MockitoExtension.class)
class ParticipanteServiceTest {

    @Mock
    private ParticipanteRepository participanteRepository;

    @Mock
    private RespuestaRepository respuestaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VariableRepository variableRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ParticipanteService participanteService;

    private Usuario reclutador;
    private Participante participante;
    private Variable variable;
    private Rol rolMedico;

    @BeforeEach
    void setUp() {
        // Configurar rol
        rolMedico = new Rol();
        rolMedico.setIdRol(1);
        rolMedico.setNombreRol("MEDICO");

        // Configurar usuario reclutador
        reclutador = new Usuario();
        reclutador.setIdUsuario(1);
        reclutador.setNombreCompleto("Dr. Juan Pérez");
        reclutador.setCorreo("juan@hospital.com");
        reclutador.setRol(rolMedico);
        reclutador.setEstado(EstadoUsuario.ACTIVO);

        // Configurar participante
        participante = new Participante();
        participante.setIdParticipante(1);
        participante.setCodigoParticipante("CS1");
        participante.setNombreCompleto("María González");
        participante.setTelefono("555-1234");
        participante.setDireccion("Calle Principal 123");
        participante.setGrupo(GrupoParticipante.CASO);
        participante.setReclutador(reclutador);
        participante.setEstadoFicha(EstadoFicha.INCOMPLETA);
        participante.setFechaInclusion(LocalDate.now());

        // Configurar variable
        variable = new Variable();
        variable.setIdVariable(1);
        variable.setCodigoVariable("VAR-001");
        variable.setEnunciado("¿Cuál es su edad?");
        variable.setTipoDato("NUMERICO");
    }

    // ==================== PRUEBAS CREAR PARTICIPANTE ====================

    @Test
    @DisplayName("Crear participante del grupo CASO exitosamente")
    void testCrearParticipante_GrupoCaso_Exitoso() {
        Participante participanteNuevo = new Participante();
        participanteNuevo.setIdParticipante(5);
        participanteNuevo.setGrupo(GrupoParticipante.CASO);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(reclutador));
        when(participanteRepository.save(any(Participante.class)))
                .thenAnswer(invocation -> {
                    Participante p = invocation.getArgument(0);
                    p.setIdParticipante(5);
                    return p;
                });
        doNothing().when(auditoriaService).registrarAccion(
                any(Usuario.class),
                any(Participante.class),
                anyString(),
                anyString(),
                anyString());

        Participante resultado = participanteService.crearParticipante(
                "María González",
                "555-1234",
                "Calle Principal 123",
                "CASO",
                1);

        assertNotNull(resultado);
        assertEquals(GrupoParticipante.CASO, resultado.getGrupo());
        assertEquals(EstadoFicha.INCOMPLETA, resultado.getEstadoFicha());
        assertEquals(LocalDate.now(), resultado.getFechaInclusion());
        assertEquals("CS5", resultado.getCodigoParticipante());

        verify(usuarioRepository, times(1)).findById(1);
        verify(participanteRepository, times(2)).save(any(Participante.class));
        verify(auditoriaService, times(1)).registrarAccion(
                any(Usuario.class),
                any(Participante.class),
                eq("CREAR"),
                eq("Participante"),
                anyString());
    }

    @Test
    @DisplayName("Crear participante del grupo CONTROL exitosamente")
    void testCrearParticipante_GrupoControl_Exitoso() {
        Participante participanteNuevo = new Participante();
        participanteNuevo.setIdParticipante(10);
        participanteNuevo.setGrupo(GrupoParticipante.CONTROL);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(reclutador));
        when(participanteRepository.save(any(Participante.class)))
                .thenAnswer(invocation -> {
                    Participante p = invocation.getArgument(0);
                    p.setIdParticipante(10);
                    return p;
                });
        doNothing().when(auditoriaService).registrarAccion(
                any(Usuario.class),
                any(Participante.class),
                anyString(),
                anyString(),
                anyString());

        Participante resultado = participanteService.crearParticipante(
                "Pedro Ramírez",
                "555-5678",
                "Avenida Libertad 456",
                "CONTROL",
                1);

        assertNotNull(resultado);
        assertEquals(GrupoParticipante.CONTROL, resultado.getGrupo());
        assertEquals("CT10", resultado.getCodigoParticipante());

        verify(usuarioRepository, times(1)).findById(1);
        verify(participanteRepository, times(2)).save(any(Participante.class));
    }

    @Test
    @DisplayName("Lanzar excepción cuando reclutador no existe")
    void testCrearParticipante_ReclutadorNoEncontrado() {
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            participanteService.crearParticipante(
                    "María González",
                    "555-1234",
                    "Calle Principal 123",
                    "CASO",
                    999);
        });

        assertEquals("Usuario reclutador no encontrado", exception.getMessage());
        verify(usuarioRepository, times(1)).findById(999);
        verify(participanteRepository, never()).save(any());
        verify(auditoriaService, never()).registrarAccion(any(), any(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Verificar que se registra auditoría al crear participante")
    void testCrearParticipante_RegistraAuditoria() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(reclutador));
        when(participanteRepository.save(any(Participante.class)))
                .thenAnswer(invocation -> {
                    Participante p = invocation.getArgument(0);
                    p.setIdParticipante(1);
                    return p;
                });
        doNothing().when(auditoriaService).registrarAccion(any(Usuario.class), any(Participante.class), anyString(),
                anyString(), anyString());

        participanteService.crearParticipante("María González", "555-1234", "Calle Principal 123", "CASO", 1);

        verify(auditoriaService, times(1)).registrarAccion(
                eq(reclutador),
                any(Participante.class),
                eq("CREAR"),
                eq("Participante"),
                contains("Se creo el participante ID:"));
    }

    // ==================== PRUEBAS GUARDAR RESPUESTAS ====================

    @Test
    @DisplayName("Guardar respuestas exitosamente (HU-15: Guardar formulario incompleto)")
    void testGuardarRespuestas_Exitoso() {
        Map<String, String> respuestasMap = new HashMap<>();
        respuestasMap.put("1", "45"); // edad
        respuestasMap.put("2", "Masculino"); // género

        Variable variable2 = new Variable();
        variable2.setIdVariable(2);
        variable2.setCodigoVariable("VAR-002");
        variable2.setEnunciado("Género");

        when(participanteRepository.findById(1)).thenReturn(Optional.of(participante));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(reclutador));
        when(variableRepository.findById(1)).thenReturn(Optional.of(variable));
        when(variableRepository.findById(2)).thenReturn(Optional.of(variable2));
        when(respuestaRepository.save(any(Respuesta.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(auditoriaService).registrarAccion(any(Usuario.class), any(Participante.class), anyString(),
                anyString(), anyString());

        participanteService.guardarRespuestas(1, respuestasMap, 1, null, null, null, null);

        verify(participanteRepository, times(1)).findById(1);
        verify(usuarioRepository, times(1)).findById(1);
        verify(variableRepository, times(1)).findById(1);
        verify(variableRepository, times(1)).findById(2);
        verify(respuestaRepository, times(2)).save(any(Respuesta.class));
        // The service performs a grouped audit at the end, so it is called 1 time
        verify(auditoriaService, times(1)).registrarAccion(eq(reclutador), eq(participante), eq("ACTUALIZAR"),
                eq("Respuesta"), anyString());
    }

    @Test
    @DisplayName("Lanzar excepción cuando participante no existe al guardar respuestas")
    void testGuardarRespuestas_ParticipanteNoEncontrado() {
        Map<String, String> respuestasMap = new HashMap<>();
        respuestasMap.put("1", "45");

        when(participanteRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> participanteService.guardarRespuestas(999, respuestasMap, 1, null, null, null, null));

        assertEquals("Participante no encontrado", exception.getMessage());
        verify(participanteRepository, times(1)).findById(999);
        verify(respuestaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanzar excepción cuando usuario editor no existe")
    void testGuardarRespuestas_EditorNoEncontrado() {
        Map<String, String> respuestasMap = new HashMap<>();
        respuestasMap.put("1", "45");

        when(participanteRepository.findById(1)).thenReturn(Optional.of(participante));
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> participanteService.guardarRespuestas(1, respuestasMap, 999, null, null, null, null));

        assertEquals("Usuario editor no encontrado", exception.getMessage());
        verify(participanteRepository, times(1)).findById(1);
        verify(usuarioRepository, times(1)).findById(999);
        verify(respuestaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ignorar variable cuando no existe al guardar respuestas")
    void testGuardarRespuestas_VariableNoEncontrada() {
        Map<String, String> respuestasMap = new HashMap<>();
        respuestasMap.put("999", "valor");

        when(participanteRepository.findById(1)).thenReturn(Optional.of(participante));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(reclutador));
        when(variableRepository.findById(999)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> participanteService.guardarRespuestas(1, respuestasMap, 1, null, null, null, null));

        verify(variableRepository, times(1)).findById(999);
        verify(respuestaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Guardar respuestas vacías (permitir guardar formulario incompleto)")
    void testGuardarRespuestas_RespuestasVacias() {
        Map<String, String> respuestasMap = new HashMap<>();
        respuestasMap.put("1", "");
        respuestasMap.put("2", null);

        Variable variable2 = new Variable();
        variable2.setIdVariable(2);

        when(participanteRepository.findById(1)).thenReturn(Optional.of(participante));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(reclutador));
        when(variableRepository.findById(1)).thenReturn(Optional.of(variable));
        when(variableRepository.findById(2)).thenReturn(Optional.of(variable2));
        when(respuestaRepository.save(any(Respuesta.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(auditoriaService).registrarAccion(any(), any(), anyString(), anyString(), anyString());

        participanteService.guardarRespuestas(1, respuestasMap, 1, null, null, null, null);

        verify(respuestaRepository, times(2)).save(any(Respuesta.class));
    }

    @Test
    @DisplayName("Verificar que se registra auditoría para cada respuesta guardada")
    void testGuardarRespuestas_RegistraAuditoria() {
        Map<String, String> respuestasMap = new HashMap<>();
        respuestasMap.put("1", "45");

        when(participanteRepository.findById(1)).thenReturn(Optional.of(participante));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(reclutador));
        when(variableRepository.findById(1)).thenReturn(Optional.of(variable));
        when(respuestaRepository.save(any(Respuesta.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(auditoriaService).registrarAccion(any(), any(), anyString(), anyString(), anyString());

        participanteService.guardarRespuestas(1, respuestasMap, 1, null, null, null, null);

        verify(auditoriaService, times(1)).registrarAccion(
                eq(reclutador),
                eq(participante),
                eq("ACTUALIZAR"),
                eq("Respuesta"),
                contains("Se guardaron respuestas"));
    }

    // ==================== PRUEBAS OBTENER PARTICIPANTES ====================

    @Test
    @DisplayName("Obtener todos los participantes")
    void testObtenerTodosLosParticipantes() {
        Participante participante2 = new Participante();
        participante2.setIdParticipante(2);
        participante2.setCodigoParticipante("CT2");
        participante2.setNombreCompleto("Pedro Ramírez");
        participante2.setGrupo(GrupoParticipante.CONTROL);

        List<Participante> participantes = Arrays.asList(participante, participante2);
        when(participanteRepository.findAll()).thenReturn(participantes);

        List<Participante> resultado = participanteService.obtenerTodosLosParticipantes();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("CS1", resultado.get(0).getCodigoParticipante());
        assertEquals("CT2", resultado.get(1).getCodigoParticipante());
        verify(participanteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Obtener lista vacía cuando no hay participantes")
    void testObtenerTodosLosParticipantes_ListaVacia() {
        when(participanteRepository.findAll()).thenReturn(Arrays.asList());

        List<Participante> resultado = participanteService.obtenerTodosLosParticipantes();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(participanteRepository, times(1)).findAll();
    }
    // ==================== PRUEBAS VALIDACIONES ADICIONALES ====================

    @Test
    @DisplayName("Lanzar excepción cuando el teléfono tiene formato inválido")
    void testCrearParticipante_TelefonoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> {
            participanteService.crearParticipante("María", "telefono_invalido", "Dir", "CASO", 1);
        });
        verify(participanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanzar excepción cuando el grupo es inválido")
    void testCrearParticipante_GrupoInvalido() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(reclutador));
        assertThrows(IllegalArgumentException.class, () -> {
            participanteService.crearParticipante("María", "555-1234", "Dir", "INVALIDO", 1);
        });
        verify(participanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Marcar ficha como COMPLETA cuando todas las obligatorias están respondidas")
    void testGuardarRespuestas_FichaCompleta() {
        // Variable obligatoria
        Variable varObligatoria = new Variable();
        varObligatoria.setIdVariable(1);
        varObligatoria.setCodigoVariable("V1");
        varObligatoria.setEsObligatoria(true);
        varObligatoria.setAplicaA("AMBOS");

        // Mockear repositorios
        when(participanteRepository.findById(1)).thenReturn(Optional.of(participante));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(reclutador));
        when(variableRepository.findById(1)).thenReturn(Optional.of(varObligatoria));
        // Mockear findAll para que devuelva la variable obligatoria
        when(variableRepository.findAll()).thenReturn(Arrays.asList(varObligatoria));
        when(respuestaRepository.save(any(Respuesta.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, String> respuestasMap = new HashMap<>();
        respuestasMap.put("1", "valor");

        participanteService.guardarRespuestas(1, respuestasMap, 1, null, null, null, null);

        assertEquals(EstadoFicha.COMPLETA, participante.getEstadoFicha());
    }

    @Test
    @DisplayName("Marcar ficha como INCOMPLETA cuando falta una obligatoria")
    void testGuardarRespuestas_FichaIncompleta() {
        // Variable obligatoria
        Variable varObligatoria = new Variable();
        varObligatoria.setIdVariable(1);
        varObligatoria.setCodigoVariable("V1");
        varObligatoria.setEsObligatoria(true);
        varObligatoria.setAplicaA("AMBOS");

        Variable varOpcional = new Variable();
        varOpcional.setIdVariable(2);
        varOpcional.setCodigoVariable("V2");
        varOpcional.setEsObligatoria(false);

        when(participanteRepository.findById(1)).thenReturn(Optional.of(participante));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(reclutador));
        when(variableRepository.findById(2)).thenReturn(Optional.of(varOpcional));
        // Devolvemos ambas
        when(variableRepository.findAll()).thenReturn(Arrays.asList(varObligatoria, varOpcional));
        when(respuestaRepository.save(any(Respuesta.class))).thenAnswer(i -> i.getArgument(0));

        // Solo respondemos la opcional
        Map<String, String> respuestasMap = new HashMap<>();
        respuestasMap.put("2", "valor");

        participanteService.guardarRespuestas(1, respuestasMap, 1, null, null, null, null);

        assertEquals(EstadoFicha.INCOMPLETA, participante.getEstadoFicha());
    }
}
