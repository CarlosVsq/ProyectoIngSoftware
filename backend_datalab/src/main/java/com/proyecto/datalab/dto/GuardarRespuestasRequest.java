package com.proyecto.datalab.dto;

import java.util.Map;

public class GuardarRespuestasRequest {
    private Integer usuarioEditorId;
    private Map<Integer, String> respuestasMap;

    public Integer getUsuarioEditorId() { return usuarioEditorId; }
    public Map<Integer, String> getRespuestasMap() { return respuestasMap; }
}