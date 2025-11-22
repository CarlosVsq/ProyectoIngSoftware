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

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}