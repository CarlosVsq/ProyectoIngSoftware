package com.proyecto.datalab.dto;

import java.util.Map;

public class GuardarRespuestasRequest {
    private Long usuarioEditorId;
    private Map<Long, String> respuestasMap;

    public Long getUsuarioEditorId() { return usuarioEditorId; }
    public Map<Long, String> getRespuestasMap() { return respuestasMap; }
}
