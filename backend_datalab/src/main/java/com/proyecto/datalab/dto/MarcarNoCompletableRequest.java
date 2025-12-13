package com.proyecto.datalab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MarcarNoCompletableRequest {

    @NotNull(message = "Debe indicar el usuario que marca la ficha")
    private Integer usuarioEditorId;

    @NotBlank(message = "La justificacion es obligatoria")
    @Size(max = 2000, message = "La justificacion es demasiado larga")
    private String justificacion;

    public Integer getUsuarioEditorId() {
        return usuarioEditorId;
    }

    public String getJustificacion() {
        return justificacion;
    }
}
