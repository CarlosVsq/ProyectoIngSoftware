package com.proyecto.datalab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.datalab.dto.VariableCreateRequest;
import com.proyecto.datalab.entity.Variable;
import com.proyecto.datalab.repository.VariableRepository;

/**
 * Pruebas unitarias para VariableService
 * Cubre funcionalidad de creación y gestión de variables del CRF
 */
@ExtendWith(MockitoExtension.class)
class VariableServiceTest {

    @Mock
    private VariableRepository variableRepository;

    @InjectMocks
    private VariableService variableService;

    private VariableCreateRequest request;
    private Variable variable;

    @BeforeEach
    void setUp() {
        // Configurar request de prueba
        request = new VariableCreateRequest();
        // Como VariableCreateRequest no tiene setters, usaremos reflection en el test real
        // o simplemente probaremos con lo que retorna save()

        // Configurar variable de prueba
        variable = new Variable();
        variable.setIdVariable(1);
        variable.setEnunciado("¿Cuál es su edad?");
        variable.setCodigoVariable("VAR-EDAD");
        variable.setTipoDato("NUMERICO");
        variable.setOpciones(null);
        variable.setAplicaA("TODOS");
        variable.setSeccion("DEMOGRAFICO");
        variable.setOrdenEnunciado(1);
        variable.setEsObligatoria(true);
        variable.setReglaValidacion("min:0;max:120");
    }

    // ==================== PRUEBAS CREAR VARIABLE ====================

    @Test
    @DisplayName("Crear variable numérica exitosamente")
    void testCrearVariable_NumericaExitoso() {
        // Arrange
        VariableCreateRequest request = createRequest(
            "¿Cuál es su edad?",
            "VAR-EDAD",
            "NUMERICO",
            null,
            "TODOS",
            "DEMOGRAFICO",
            1,
            true,
            "min:0;max:120"
        );

        when(variableRepository.save(any(Variable.class))).thenReturn(variable);

        // Act
        Variable resultado = variableService.crearVariable(request);

        // Assert
        assertNotNull(resultado);
        verify(variableRepository, times(1)).save(any(Variable.class));
    }

    @Test
    @DisplayName("Crear variable de selección múltiple exitosamente")
    void testCrearVariable_SeleccionMultiple() {
        // Arrange
        Variable varSeleccion = new Variable();
        varSeleccion.setIdVariable(2);
        varSeleccion.setEnunciado("Seleccione síntomas");
        varSeleccion.setCodigoVariable("VAR-SINTOMAS");
        varSeleccion.setTipoDato("SELECCION_MULTIPLE");
        varSeleccion.setOpciones("Dolor|Náuseas|Fatiga|Fiebre");
        varSeleccion.setAplicaA("CASO");
        varSeleccion.setEsObligatoria(true);

        VariableCreateRequest request = createRequest(
            "Seleccione síntomas",
            "VAR-SINTOMAS",
            "SELECCION_MULTIPLE",
            "Dolor|Náuseas|Fatiga|Fiebre",
            "CASO",
            "SINTOMAS",
            5,
            true,
            null
        );

        when(variableRepository.save(any(Variable.class))).thenReturn(varSeleccion);

        // Act
        Variable resultado = variableService.crearVariable(request);

        // Assert
        assertNotNull(resultado);
        verify(variableRepository, times(1)).save(any(Variable.class));
    }

    @Test
    @DisplayName("Crear variable de texto libre exitosamente")
    void testCrearVariable_TextoLibre() {
        // Arrange
        Variable varTexto = new Variable();
        varTexto.setIdVariable(3);
        varTexto.setEnunciado("Observaciones adicionales");
        varTexto.setCodigoVariable("VAR-OBS");
        varTexto.setTipoDato("TEXTO");
        varTexto.setAplicaA("TODOS");
        varTexto.setEsObligatoria(false);

        VariableCreateRequest request = createRequest(
            "Observaciones adicionales",
            "VAR-OBS",
            "TEXTO",
            null,
            "TODOS",
            "GENERAL",
            99,
            false,
            null
        );

        when(variableRepository.save(any(Variable.class))).thenReturn(varTexto);

        // Act
        Variable resultado = variableService.crearVariable(request);

        // Assert
        assertNotNull(resultado);
        verify(variableRepository, times(1)).save(any(Variable.class));
    }

    @Test
    @DisplayName("Crear variable de fecha exitosamente")
    void testCrearVariable_Fecha() {
        // Arrange
        Variable varFecha = new Variable();
        varFecha.setIdVariable(4);
        varFecha.setEnunciado("Fecha de diagnóstico");
        varFecha.setCodigoVariable("VAR-FECHA-DX");
        varFecha.setTipoDato("FECHA");
        varFecha.setAplicaA("CASO");
        varFecha.setSeccion("DIAGNOSTICO");
        varFecha.setOrdenEnunciado(1);
        varFecha.setEsObligatoria(true);

        VariableCreateRequest request = createRequest(
            "Fecha de diagnóstico",
            "VAR-FECHA-DX",
            "FECHA",
            null,
            "CASO",
            "DIAGNOSTICO",
            1,
            true,
            null
        );

        when(variableRepository.save(any(Variable.class))).thenReturn(varFecha);

        // Act
        Variable resultado = variableService.crearVariable(request);

        // Assert
        assertNotNull(resultado);
        verify(variableRepository, times(1)).save(any(Variable.class));
    }

    @Test
    @DisplayName("Crear variable booleana (SI/NO) exitosamente")
    void testCrearVariable_Booleana() {
        // Arrange
        Variable varBool = new Variable();
        varBool.setIdVariable(5);
        varBool.setEnunciado("¿Tiene antecedentes familiares?");
        varBool.setCodigoVariable("VAR-ANT-FAM");
        varBool.setTipoDato("BOOLEANO");
        varBool.setOpciones("SI|NO");
        varBool.setAplicaA("TODOS");
        varBool.setEsObligatoria(true);

        VariableCreateRequest request = createRequest(
            "¿Tiene antecedentes familiares?",
            "VAR-ANT-FAM",
            "BOOLEANO",
            "SI|NO",
            "TODOS",
            "ANTECEDENTES",
            10,
            true,
            null
        );

        when(variableRepository.save(any(Variable.class))).thenReturn(varBool);

        // Act
        Variable resultado = variableService.crearVariable(request);

        // Assert
        assertNotNull(resultado);
        verify(variableRepository, times(1)).save(any(Variable.class));
    }

    @Test
    @DisplayName("Crear variable solo para grupo CASO")
    void testCrearVariable_SoloGrupoCaso() {
        // Arrange
        Variable varCaso = new Variable();
        varCaso.setIdVariable(6);
        varCaso.setEnunciado("Estadio del cáncer");
        varCaso.setCodigoVariable("VAR-ESTADIO");
        varCaso.setTipoDato("SELECCION");
        varCaso.setOpciones("I|II|III|IV");
        varCaso.setAplicaA("CASO");
        varCaso.setSeccion("DIAGNOSTICO");
        varCaso.setOrdenEnunciado(3);
        varCaso.setEsObligatoria(true);

        VariableCreateRequest request = createRequest(
            "Estadio del cáncer",
            "VAR-ESTADIO",
            "SELECCION",
            "I|II|III|IV",
            "CASO",
            "DIAGNOSTICO",
            3,
            true,
            null
        );

        when(variableRepository.save(any(Variable.class))).thenReturn(varCaso);

        // Act
        Variable resultado = variableService.crearVariable(request);

        // Assert
        assertNotNull(resultado);
        verify(variableRepository, times(1)).save(any(Variable.class));
    }

    @Test
    @DisplayName("Crear variable solo para grupo CONTROL")
    void testCrearVariable_SoloGrupoControl() {
        // Arrange
        Variable varControl = new Variable();
        varControl.setIdVariable(7);
        varControl.setEnunciado("Motivo de inclusión en control");
        varControl.setCodigoVariable("VAR-MOTIVO-CONTROL");
        varControl.setTipoDato("TEXTO");
        varControl.setAplicaA("CONTROL");
        varControl.setSeccion("GENERAL");
        varControl.setEsObligatoria(false);

        VariableCreateRequest request = createRequest(
            "Motivo de inclusión en control",
            "VAR-MOTIVO-CONTROL",
            "TEXTO",
            null,
            "CONTROL",
            "GENERAL",
            1,
            false,
            null
        );

        when(variableRepository.save(any(Variable.class))).thenReturn(varControl);

        // Act
        Variable resultado = variableService.crearVariable(request);

        // Assert
        assertNotNull(resultado);
        verify(variableRepository, times(1)).save(any(Variable.class));
    }

    @Test
    @DisplayName("Crear variable con regla de validación compleja")
    void testCrearVariable_ConValidacion() {
        // Arrange
        Variable varConValidacion = new Variable();
        varConValidacion.setIdVariable(8);
        varConValidacion.setEnunciado("IMC (Índice de Masa Corporal)");
        varConValidacion.setCodigoVariable("VAR-IMC");
        varConValidacion.setTipoDato("DECIMAL");
        varConValidacion.setAplicaA("TODOS");
        varConValidacion.setEsObligatoria(true);
        varConValidacion.setReglaValidacion("min:10.0;max:60.0;decimales:1");

        VariableCreateRequest request = createRequest(
            "IMC (Índice de Masa Corporal)",
            "VAR-IMC",
            "DECIMAL",
            null,
            "TODOS",
            "ANTROPOMETRIA",
            5,
            true,
            "min:10.0;max:60.0;decimales:1"
        );

        when(variableRepository.save(any(Variable.class))).thenReturn(varConValidacion);

        // Act
        Variable resultado = variableService.crearVariable(request);

        // Assert
        assertNotNull(resultado);
        verify(variableRepository, times(1)).save(any(Variable.class));
    }

    @Test
    @DisplayName("Crear variable no obligatoria")
    void testCrearVariable_NoObligatoria() {
        // Arrange
        Variable varNoOblig = new Variable();
        varNoOblig.setIdVariable(9);
        varNoOblig.setEnunciado("Correo electrónico (opcional)");
        varNoOblig.setCodigoVariable("VAR-EMAIL");
        varNoOblig.setTipoDato("TEXTO");
        varNoOblig.setAplicaA("TODOS");
        varNoOblig.setEsObligatoria(false);

        VariableCreateRequest request = createRequest(
            "Correo electrónico (opcional)",
            "VAR-EMAIL",
            "TEXTO",
            null,
            "TODOS",
            "CONTACTO",
            20,
            false,
            "email"
        );

        when(variableRepository.save(any(Variable.class))).thenReturn(varNoOblig);

        // Act
        Variable resultado = variableService.crearVariable(request);

        // Assert
        assertNotNull(resultado);
        verify(variableRepository, times(1)).save(any(Variable.class));
    }

    @Test
    @DisplayName("Verificar que se guardan todos los campos correctamente")
    void testCrearVariable_TodosLosCampos() {
        // Arrange
        VariableCreateRequest request = createRequest(
            "Pregunta completa",
            "VAR-COMPLETA",
            "SELECCION",
            "A|B|C",
            "TODOS",
            "SECCION_TEST",
            15,
            true,
            "required"
        );

        when(variableRepository.save(any(Variable.class))).thenAnswer(invocation -> {
            Variable v = invocation.getArgument(0);
            // Verificar que todos los campos se establecieron
            assertEquals("Pregunta completa", v.getEnunciado());
            assertEquals("VAR-COMPLETA", v.getCodigoVariable());
            assertEquals("SELECCION", v.getTipoDato());
            assertEquals("A|B|C", v.getOpciones());
            assertEquals("TODOS", v.getAplicaA());
            assertEquals("SECCION_TEST", v.getSeccion());
            assertEquals(15, v.getOrdenEnunciado());
            assertTrue(v.isEsObligatoria());
            assertEquals("required", v.getReglaValidacion());

            v.setIdVariable(10);
            return v;
        });

        // Act
        Variable resultado = variableService.crearVariable(request);

        // Assert
        assertNotNull(resultado);
        assertEquals(10, resultado.getIdVariable());
        verify(variableRepository, times(1)).save(any(Variable.class));
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Método auxiliar para crear un VariableCreateRequest usando reflexión
     * ya que la clase no tiene setters públicos
     */
    private VariableCreateRequest createRequest(
            String enunciado,
            String codigoVariable,
            String tipoDato,
            String opciones,
            String aplicaA,
            String seccion,
            Integer ordenEnunciado,
            boolean esObligatoria,
            String reglaValidacion) {

        VariableCreateRequest request = new VariableCreateRequest();

        try {
            setField(request, "enunciado", enunciado);
            setField(request, "codigoVariable", codigoVariable);
            setField(request, "tipoDato", tipoDato);
            setField(request, "opciones", opciones);
            setField(request, "aplicaA", aplicaA);
            setField(request, "seccion", seccion);
            setField(request, "ordenEnunciado", ordenEnunciado);
            setField(request, "esObligatoria", esObligatoria);
            setField(request, "reglaValidacion", reglaValidacion);
        } catch (Exception e) {
            fail("Error al configurar request: " + e.getMessage());
        }

        return request;
    }

    private void setField(Object object, String fieldName, Object value) throws Exception {
        var field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
