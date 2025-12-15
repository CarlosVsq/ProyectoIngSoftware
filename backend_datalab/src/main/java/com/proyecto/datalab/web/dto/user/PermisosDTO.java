package com.proyecto.datalab.web.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermisosDTO {
    private boolean puedeVerDatos;
    private boolean puedeModificar; // Add this too for explicit 'Modificar' permission
    private boolean puedeCrudCrf;
    private boolean puedeExportar;
    private boolean puedeReclutar;
    private boolean puedeAdministrarUsuarios;
    private boolean puedeVerAuditoria;
}