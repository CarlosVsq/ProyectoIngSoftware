package com.proyecto.datalab.dto;

public class UsuarioUpdateRequest {
    
    private String nombre;
    private String correo;
    private String contrasena;
    private Integer rolId;
    private String estado;
    

    public String getNombre() { return nombre; }

    public String getCorreo() { return correo; }

    public String getContrasena() { return contrasena; }

    public Integer getRolId() { return rolId; }

    public String getEstado() { return estado; }
}