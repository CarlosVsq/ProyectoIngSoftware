package com.proyecto.datalab.dto;

import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class GuardarRespuestasRequest {
    @NotNull(message = "Debe indicar el usuario editor")
    private Integer usuarioEditorId;

    @NotEmpty(message = "Debe enviar al menos una respuesta")
<<<<<<< Updated upstream
    private Map<Integer, String> respuestasMap;

    public Integer getUsuarioEditorId() { return usuarioEditorId; }
    public Map<Integer, String> getRespuestasMap() { return respuestasMap; }
=======
    private Map<String, String> respuestasMap;

    public Integer getUsuarioEditorId() { return usuarioEditorId; }
    public Map<String, String> getRespuestasMap() { return respuestasMap; }
>>>>>>> Stashed changes
}
