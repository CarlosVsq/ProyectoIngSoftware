package com.proyecto.datalab.servicio;

import com.proyecto.datalab.entidades.Auditoria;
import com.proyecto.datalab.entidades.Usuario;
import com.proyecto.datalab.repositorio.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    @Autowired
    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * CREATE: Registra una nueva acción de auditoría
     * Lógica para HU-11 (Bitácora de cambios)
     */
    @Transactional
    public void registrarAccion(Usuario usuario, String accion, String tablaAfectada, String detalle) {
        
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo para la auditoría");
        }

        Auditoria log = new Auditoria();
        log.setUsuario(usuario);
        log.setAccion(accion); // Ej: "CREAR", "ACTUALIZAR", "BORRAR"
        log.setTablaAfectada(tablaAfectada); // Ej: "Participante", "Respuesta"
        log.setDetalleCambio(detalle);
        // la fecha_cambio se pone automáticamente por la entidad (si la configuraste así)

        auditoriaRepository.save(log);
    }
}