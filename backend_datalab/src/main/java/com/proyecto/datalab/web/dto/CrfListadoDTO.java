package com.proyecto.datalab.web.dto;

import java.time.LocalDate;
import java.util.List;

import com.proyecto.datalab.enums.EstadoFicha;
import com.proyecto.datalab.enums.GrupoParticipante;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrfListadoDTO {
    private Integer idParticipante;
    private String codigoParticipante;
    private String nombreCompleto;
    private GrupoParticipante grupo;
    private EstadoFicha estadoFicha;
    private LocalDate fechaInclusion;
    private String telefono;
    private String direccion;
    private String nombreReclutador;
    private List<CrfRespuestaDTO> respuestas;
}
