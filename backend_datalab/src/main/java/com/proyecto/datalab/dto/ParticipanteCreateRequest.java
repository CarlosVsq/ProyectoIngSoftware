package com.proyecto.datalab.dto;

public class ParticipanteCreateRequest {
    private String nombreCompleto;
    private String telefono;
    private String direccion;
    private String grupo;
    private Integer usuarioReclutadorId;

    public String getNombreCompleto() { return nombreCompleto; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getGrupo() { return grupo; }
    public Integer getUsuarioReclutadorId() { return usuarioReclutadorId; }
}
