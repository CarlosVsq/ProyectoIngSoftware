package com.proyecto.datalab.dto;

public class VariableCreateRequest {
    private String enunciado;
    private String codigoVariable;
    private String tipoDato;
    private String opciones;
    private String aplicaA;
    private String seccion;
    private Integer ordenEnunciado;
    private boolean esObligatoria;
    private String reglaValidacion;

    public String getEnunciado() {return enunciado;}
    public String getCodigoVariable() {return codigoVariable;}
    public String getTipoDato() {return tipoDato;}
    public String getOpciones() {return opciones;}
    public String getAplicaA() {return aplicaA;}
    public String getSeccion() {return seccion;}
    public Integer getOrdenEnunciado() {return ordenEnunciado;}
    public boolean getEsObligatoria() {return esObligatoria;}
    public String getReglaValidacion() {return reglaValidacion;}
}
