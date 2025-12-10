package com.proyecto.datalab.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariableCreateRequest {
    private String enunciado;
    private String codigoVariable;
    private String tipoDato;
    private String opciones;
    private String aplicaA;
    private String seccion;
    private Integer ordenEnunciado;
    private boolean esObligatoria;
    private String reglaValidacion;
}
