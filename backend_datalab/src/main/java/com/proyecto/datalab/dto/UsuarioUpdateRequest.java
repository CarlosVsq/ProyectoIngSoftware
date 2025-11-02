package com.proyecto.datalab.dto;

public class UsuarioUpdateRequest {
    
    private String nombre;
    private String correo;
    private String contrasena;
    private Long rolId;
    

    public String getNombre() { return nombre; }

    public String getCorreo() { return correo; }

    public String getContrasena() { return contrasena; }

    public Long getRolId() { return rolId; }
}