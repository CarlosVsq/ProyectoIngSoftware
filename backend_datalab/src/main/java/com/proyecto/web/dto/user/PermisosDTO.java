package com.proyecto.web.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermisosDTO {
    private boolean puedeCrudCrf;
    private boolean puedeExportar;
    private boolean puedeReclutar;
    private boolean puedeAdministrarUsuarios;
    private boolean puedeVerAuditoria;
}