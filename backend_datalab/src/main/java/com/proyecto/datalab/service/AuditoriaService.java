package com.proyecto.datalab.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.datalab.entity.Auditoria;
import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.repository.AuditoriaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public void registrarAccion(Usuario usuario, Participante participante, String accion, String tablaAfectada,
            String detalle) {

        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo para la auditoría");
        }

        Auditoria log = new Auditoria();
        log.setUsuario(usuario);
        log.setParticipante(participante);
        log.setAccion(accion); // Ej: "CREAR", "ACTUALIZAR", "BORRAR"
        log.setTablaAfectada(tablaAfectada); // Ej: "Participante", "Respuesta"
        log.setDetalleCambio(detalle);
        // la fecha_cambio se pone automáticamente por la entidad (si la configuraste
        // así)

        auditoriaRepository.save(log);
    }

    public Map<String, Long> getExportStatsLast7Days() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(6).withHour(0).withMinute(0).withSecond(0);
        List<Auditoria> logs = auditoriaRepository.findByAccionAndFechaCambioAfter("EXPORTAR", sevenDaysAgo);

        // Initialize map with last 7 days (including today)
        Map<String, Long> stats = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            stats.put(today.minusDays(i).toString(), 0L);
        }

        // Fill with real data
        Map<String, Long> realData = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getFechaCambio().toLocalDate().toString(),
                        Collectors.counting()));

        realData.forEach(stats::put);

        return stats;
    }
}