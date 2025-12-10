package com.proyecto.datalab.web.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditoriaDTO {
    private Integer idAuditoria;
    private String usuario;
    private String participante;
    private String tablaAfectada;
    private String accion;
    private String detalleCambio;
    private LocalDateTime fechaCambio;
}
