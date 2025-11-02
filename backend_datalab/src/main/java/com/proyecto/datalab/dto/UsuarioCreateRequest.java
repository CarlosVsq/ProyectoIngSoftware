package com.proyecto.datalab.dto;

public class UsuarioCreateRequest {
    private String nombre;
    private String correo;
    private String contrasena;
    private Integer rolId;

    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getContrasena() { return contrasena; }
    public Integer getRolId() { return rolId; }
}