package com.proyecto.datalab.dto;

import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class GuardarRespuestasRequest {
    @NotNull(message = "Debe indicar el usuario editor")
    private Integer usuarioEditorId;

    @NotEmpty(message = "Debe enviar al menos una respuesta")
    private Map<String, String> respuestasMap;

    private String nombreCompleto;
    private String telefono;
    private String direccion;
    private String grupo;

    public Integer getUsuarioEditorId() {
        return usuarioEditorId;
    }

    public Map<String, String> getRespuestasMap() {
        return respuestasMap;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getGrupo() {
        return grupo;
    }
}
