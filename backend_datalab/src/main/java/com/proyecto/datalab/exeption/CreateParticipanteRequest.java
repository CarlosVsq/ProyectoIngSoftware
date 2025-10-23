package com.proyecto.datalab.exeption;

import java.time.LocalDate;

import com.proyecto.datalab.enums.GrupoParticipante;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

@Data
public class CreateParticipanteRequest {
    
    @NotBlank(message = "El RUT es obligatorio")
    private String rut;
    
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate fechaNacimiento;
    
    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;
    
    private String telefono;
    private String correo;
    private String direccion;
    
    @NotNull(message = "El grupo es obligatorio")
    private GrupoParticipante grupo;
    
    private String observaciones;
}
