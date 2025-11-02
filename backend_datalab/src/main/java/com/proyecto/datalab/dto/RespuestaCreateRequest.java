package com.proyecto.datalab.dto;

public class RespuestaCreateRequest {
    private Integer idParticipante;
    private Integer idVariable;
    private String valorIngresado;

    public Integer getIdParticipante(){ return idParticipante;}

    public Integer getIdVariable(){ return idVariable;}

    public String getValorIngresado(){ return valorIngresado;}
}
