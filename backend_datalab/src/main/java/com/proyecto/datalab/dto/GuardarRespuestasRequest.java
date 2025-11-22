package com.proyecto.datalab.dto;

import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class GuardarRespuestasRequest {
    @NotNull(message = "Debe indicar el usuario editor")
    private Integer usuarioEditorId;

    @NotEmpty(message = "Debe enviar al menos una respuesta")
    private Map<Integer, String> respuestasMap;

    public Integer getUsuarioEditorId() { return usuarioEditorId; }
    public Map<Integer, String> getRespuestasMap() { return respuestasMap; }
}
