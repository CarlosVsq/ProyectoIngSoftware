package com.proyecto.datalab.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrfRespuestaDTO {
    private String codigoVariable;
    private String enunciado;
    private String valor;
}
