package com.proyecto.datalab.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.datalab.entity.Auditoria;
import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.repository.AuditoriaRepository;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * CREATE: Registra una nueva acción de auditoría
     * Lógica para HU-11 (Bitácora de cambios)
     */
    @Transactional
    public void registrarAccion(Usuario usuario,Participante participante, String accion, String tablaAfectada, String detalle) {
        
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo para la auditoría");
        }

        Auditoria log = new Auditoria();
        log.setUsuario(usuario);
        log.setParticipante(participante);
        log.setAccion(accion); // Ej: "CREAR", "ACTUALIZAR", "BORRAR"
        log.setTablaAfectada(tablaAfectada); // Ej: "Participante", "Respuesta"
        log.setDetalleCambio(detalle);
        // la fecha_cambio se pone automáticamente por la entidad (si la configuraste así)

        auditoriaRepository.save(log);
    }
}