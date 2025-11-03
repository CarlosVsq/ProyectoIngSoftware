package com.proyecto.datalab.dto;

import jakarta.validation.constraints.NotBlank;

public class RespuestaUpdateRequest {
    @NotBlank(message = "El valor ingresado no puede estar vacío")
    private String valorIngresado;
    private String comentario;

    // Getter
    public String getValorIngresado() {
        return valorIngresado;
    }

    public String getComentario(){
        return comentario;
    }

    // Setter - Necesario para la deserialización de JSON por Spring
    public void setValorIngresado(String valorIngresado) {
        this.valorIngresado = valorIngresado;
    }

    public void setComentario(String comentario){
        this.comentario = comentario;
    }
}
