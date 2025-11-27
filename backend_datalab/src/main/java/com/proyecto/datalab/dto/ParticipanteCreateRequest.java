package com.proyecto.datalab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ParticipanteCreateRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre excede el largo permitido")
    private String nombreCompleto;

    // Teléfono opcional, pero si viene debe tener formato válido (números, +, espacios o guiones)
    @Pattern(
        regexp = "^$|[0-9+\\s-]{8,20}$",
        message = "El teléfono debe tener entre 8 y 20 caracteres y solo puede contener números, +, espacios o guiones"
    )
    private String telefono;

    @Size(max = 255, message = "La dirección excede el largo permitido")
    private String direccion;

    @NotBlank(message = "El grupo es obligatorio (CASO o CONTROL)")
    private String grupo;

    @NotNull(message = "Debe indicar el reclutador")
    private Integer usuarioReclutadorId;

    public String getNombreCompleto() { return nombreCompleto; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getGrupo() { return grupo; }
    public Integer getUsuarioReclutadorId() { return usuarioReclutadorId; }
}
