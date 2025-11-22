package com.proyecto.datalab.dto;

import jakarta.validation.constraints.NotBlank;

public class RespuestaUpdateRequest {
    @NotBlank(message = "El valor ingresado no puede estar vacío")
    private String valorIngresado;

    // Getter
    public String getValorIngresado() {
        return valorIngresado;
    }

    // Setter - Necesario para la deserialización de JSON por Spring
    public void setValorIngresado(String valorIngresado) {
        this.valorIngresado = valorIngresado;
    }
}
