package com.proyecto.datalab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.datalab.entity.Auditoria;
import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.entity.Rol;
import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.enums.EstadoFicha;
import com.proyecto.datalab.enums.EstadoUsuario;
import com.proyecto.datalab.enums.GrupoParticipante;
import com.proyecto.datalab.repository.AuditoriaRepository;

/**
 * Pruebas unitarias para AuditoriaService
 * Cubre HU-11: Bitácora de cambios - registro de quién y cuándo modificó datos
 */
@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    private Usuario usuario;
    private Participante participante;
    private Rol rolMedico;

    @BeforeEach
    void setUp() {
        // Configurar rol
        rolMedico = new Rol();
        rolMedico.setIdRol(1);
        rolMedico.setNombreRol("MEDICO");

        // Configurar usuario
        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombreCompleto("Dr. Juan Pérez");
        usuario.setCorreo("juan@hospital.com");
        usuario.setRol(rolMedico);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        // Configurar participante
        participante = new Participante();
        participante.setIdParticipante(1);
        participante.setCodigoParticipante("CS1");
        participante.setNombreCompleto("María González");
        participante.setGrupo(GrupoParticipante.CASO);
        participante.setEstadoFicha(EstadoFicha.INCOMPLETA);
        participante.setFechaInclusion(LocalDate.now());
    }

    // ==================== PRUEBAS REGISTRAR ACCIÓN ====================

    @Test
    @DisplayName("Registrar acción de CREAR exitosamente (HU-11)")
    void testRegistrarAccion_Crear_Exitoso() {
        // Arrange
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));
        ArgumentCaptor<Auditoria> auditoriaCaptor = ArgumentCaptor.forClass(Auditoria.class);

        // Act
        auditoriaService.registrarAccion(
            usuario,
            participante,
            "CREAR",
            "Participante",
            "Se creó el participante ID: 1"
        );

        // Assert
        verify(auditoriaRepository, times(1)).save(auditoriaCaptor.capture());

        Auditoria auditoriaGuardada = auditoriaCaptor.getValue();
        assertNotNull(auditoriaGuardada);
        assertEquals(usuario, auditoriaGuardada.getUsuario());
        assertEquals(participante, auditoriaGuardada.getParticipante());
        assertEquals("CREAR", auditoriaGuardada.getAccion());
        assertEquals("Participante", auditoriaGuardada.getTablaAfectada());
        assertEquals("Se creó el participante ID: 1", auditoriaGuardada.getDetalleCambio());
    }

    @Test
    @DisplayName("Registrar acción de ACTUALIZAR exitosamente (HU-11)")
    void testRegistrarAccion_Actualizar_Exitoso() {
        // Arrange
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));
        ArgumentCaptor<Auditoria> auditoriaCaptor = ArgumentCaptor.forClass(Auditoria.class);

        // Act
        auditoriaService.registrarAccion(
            usuario,
            participante,
            "ACTUALIZAR",
            "Respuesta",
            "Se actualizó respuesta para variable VAR-001"
        );

        // Assert
        verify(auditoriaRepository, times(1)).save(auditoriaCaptor.capture());

        Auditoria auditoriaGuardada = auditoriaCaptor.getValue();
        assertEquals("ACTUALIZAR", auditoriaGuardada.getAccion());
        assertEquals("Respuesta", auditoriaGuardada.getTablaAfectada());
        assertTrue(auditoriaGuardada.getDetalleCambio().contains("variable VAR-001"));
    }

    @Test
    @DisplayName("Registrar acción de BORRAR exitosamente")
    void testRegistrarAccion_Borrar_Exitoso() {
        // Arrange
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));
        ArgumentCaptor<Auditoria> auditoriaCaptor = ArgumentCaptor.forClass(Auditoria.class);

        // Act
        auditoriaService.registrarAccion(
            usuario,
            participante,
            "BORRAR",
            "Respuesta",
            "Se eliminó respuesta ID: 5"
        );

        // Assert
        verify(auditoriaRepository, times(1)).save(auditoriaCaptor.capture());

        Auditoria auditoriaGuardada = auditoriaCaptor.getValue();
        assertEquals("BORRAR", auditoriaGuardada.getAccion());
    }

    @Test
    @DisplayName("Registrar acción con diferentes tablas afectadas")
    void testRegistrarAccion_DiferentesTablas() {
        // Arrange
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));
        ArgumentCaptor<Auditoria> auditoriaCaptor = ArgumentCaptor.forClass(Auditoria.class);

        // Act & Assert - Participante
        auditoriaService.registrarAccion(usuario, participante, "ACTUALIZAR", "Participante", "Cambio en participante");
        verify(auditoriaRepository, times(1)).save(any(Auditoria.class));

        // Act & Assert - Respuesta
        auditoriaService.registrarAccion(usuario, participante, "CREAR", "Respuesta", "Nueva respuesta");
        verify(auditoriaRepository, times(2)).save(any(Auditoria.class));

        // Act & Assert - Variable
        auditoriaService.registrarAccion(usuario, participante, "ACTUALIZAR", "Variable", "Cambio en variable");
        verify(auditoriaRepository, times(3)).save(auditoriaCaptor.capture());

        // Verificar la última auditoría
        Auditoria ultimaAuditoria = auditoriaCaptor.getValue();
        assertEquals("Variable", ultimaAuditoria.getTablaAfectada());
    }

    @Test
    @DisplayName("Lanzar excepción cuando usuario es nulo (HU-11: Requisito de auditoría)")
    void testRegistrarAccion_UsuarioNulo() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            auditoriaService.registrarAccion(
                null,  // usuario nulo
                participante,
                "CREAR",
                "Participante",
                "Intento de crear sin usuario"
            );
        });

        assertEquals("El usuario no puede ser nulo para la auditoría", exception.getMessage());
        verify(auditoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Permitir participante nulo (puede no aplicar a todas las acciones)")
    void testRegistrarAccion_ParticipanteNulo() {
        // Arrange
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));
        ArgumentCaptor<Auditoria> auditoriaCaptor = ArgumentCaptor.forClass(Auditoria.class);

        // Act
        auditoriaService.registrarAccion(
            usuario,
            null,  // participante nulo
            "ACTUALIZAR",
            "Usuario",
            "Se actualizó perfil de usuario"
        );

        // Assert
        verify(auditoriaRepository, times(1)).save(auditoriaCaptor.capture());

        Auditoria auditoriaGuardada = auditoriaCaptor.getValue();
        assertNull(auditoriaGuardada.getParticipante());
        assertEquals("Usuario", auditoriaGuardada.getTablaAfectada());
    }

    @Test
    @DisplayName("Registrar múltiples acciones del mismo usuario")
    void testRegistrarAccion_MultiplesPorUsuario() {
        // Arrange
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));

        // Act - Registrar varias acciones
        auditoriaService.registrarAccion(usuario, participante, "CREAR", "Respuesta", "Acción 1");
        auditoriaService.registrarAccion(usuario, participante, "ACTUALIZAR", "Respuesta", "Acción 2");
        auditoriaService.registrarAccion(usuario, participante, "ACTUALIZAR", "Respuesta", "Acción 3");

        // Assert
        verify(auditoriaRepository, times(3)).save(any(Auditoria.class));
    }

    @Test
    @DisplayName("Registrar acción con detalle largo (texto extenso)")
    void testRegistrarAccion_DetalleLargo() {
        // Arrange
        String detalleLargo = "Se realizaron los siguientes cambios: " +
                             "1. Actualización de edad de 45 a 46 años. " +
                             "2. Modificación de estado de incompleta a completa. " +
                             "3. Corrección de fecha de inclusión. " +
                             "4. Agregación de observaciones médicas detalladas sobre el caso clínico.";

        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));
        ArgumentCaptor<Auditoria> auditoriaCaptor = ArgumentCaptor.forClass(Auditoria.class);

        // Act
        auditoriaService.registrarAccion(
            usuario,
            participante,
            "ACTUALIZAR",
            "Participante",
            detalleLargo
        );

        // Assert
        verify(auditoriaRepository, times(1)).save(auditoriaCaptor.capture());

        Auditoria auditoriaGuardada = auditoriaCaptor.getValue();
        assertEquals(detalleLargo, auditoriaGuardada.getDetalleCambio());
    }

    @Test
    @DisplayName("Registrar acción con caracteres especiales en detalle")
    void testRegistrarAccion_CaracteresEspeciales() {
        // Arrange
        String detalleConEspeciales = "Usuario modificó: Ñoño's test & validación de «comillas» y símbolos @#$%";

        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));
        ArgumentCaptor<Auditoria> auditoriaCaptor = ArgumentCaptor.forClass(Auditoria.class);

        // Act
        auditoriaService.registrarAccion(
            usuario,
            participante,
            "ACTUALIZAR",
            "Respuesta",
            detalleConEspeciales
        );

        // Assert
        verify(auditoriaRepository, times(1)).save(auditoriaCaptor.capture());

        Auditoria auditoriaGuardada = auditoriaCaptor.getValue();
        assertEquals(detalleConEspeciales, auditoriaGuardada.getDetalleCambio());
    }

    @Test
    @DisplayName("Verificar que la auditoría captura al usuario correcto (HU-11)")
    void testRegistrarAccion_UsuarioCorrecto() {
        // Arrange
        Usuario otroUsuario = new Usuario();
        otroUsuario.setIdUsuario(2);
        otroUsuario.setNombreCompleto("Dra. María García");
        otroUsuario.setCorreo("maria@hospital.com");

        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));
        ArgumentCaptor<Auditoria> auditoriaCaptor = ArgumentCaptor.forClass(Auditoria.class);

        // Act
        auditoriaService.registrarAccion(otroUsuario, participante, "CREAR", "Respuesta", "Acción por María");

        // Assert
        verify(auditoriaRepository, times(1)).save(auditoriaCaptor.capture());

        Auditoria auditoriaGuardada = auditoriaCaptor.getValue();
        assertEquals(otroUsuario, auditoriaGuardada.getUsuario());
        assertEquals("Dra. María García", auditoriaGuardada.getUsuario().getNombreCompleto());
    }

    @Test
    @DisplayName("Registrar acción de exportación de datos")
    void testRegistrarAccion_Exportacion() {
        // Arrange
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));
        ArgumentCaptor<Auditoria> auditoriaCaptor = ArgumentCaptor.forClass(Auditoria.class);

        // Act
        auditoriaService.registrarAccion(
            usuario,
            null,
            "EXPORTAR",
            "BaseDatos",
            "Se exportó base de datos a Excel para análisis estadístico"
        );

        // Assert
        verify(auditoriaRepository, times(1)).save(auditoriaCaptor.capture());

        Auditoria auditoriaGuardada = auditoriaCaptor.getValue();
        assertEquals("EXPORTAR", auditoriaGuardada.getAccion());
        assertEquals("BaseDatos", auditoriaGuardada.getTablaAfectada());
    }

    @Test
    @DisplayName("Registrar acción de cambio de permisos o roles")
    void testRegistrarAccion_CambioPermisos() {
        // Arrange
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));
        ArgumentCaptor<Auditoria> auditoriaCaptor = ArgumentCaptor.forClass(Auditoria.class);

        // Act
        auditoriaService.registrarAccion(
            usuario,
            null,
            "ACTUALIZAR",
            "Usuario",
            "Se cambió el rol del usuario ID: 5 de ESTUDIANTE a MEDICO"
        );

        // Assert
        verify(auditoriaRepository, times(1)).save(auditoriaCaptor.capture());

        Auditoria auditoriaGuardada = auditoriaCaptor.getValue();
        assertEquals("Usuario", auditoriaGuardada.getTablaAfectada());
        assertTrue(auditoriaGuardada.getDetalleCambio().contains("rol"));
    }
}
