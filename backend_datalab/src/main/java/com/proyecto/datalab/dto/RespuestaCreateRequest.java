package com.proyecto.datalab.dto;

import jakarta.validation.constraints.NotNull;

public class RespuestaCreateRequest {
    @NotNull(message = "El ID del participante es obligatorio")
    private Integer idParticipante;

    @NotNull(message = "El ID de la variable es obligatorio")
    private Integer idVariable;

    private String valorIngresado;

    // Getters
    public Integer getIdParticipante() {
        return idParticipante;
    }

    public Integer getIdVariable() {
        return idVariable;
    }

    public String getValorIngresado() {
        return valorIngresado;
    }

    // Setters - Necesarios para la deserialización de JSON por Spring
    public void setIdParticipante(Integer idParticipante) {
        this.idParticipante = idParticipante;
    }

    public void setIdVariable(Integer idVariable) {
        this.idVariable = idVariable;
    }

    public void setValorIngresado(String valorIngresado) {
        this.valorIngresado = valorIngresado;
    }
}
