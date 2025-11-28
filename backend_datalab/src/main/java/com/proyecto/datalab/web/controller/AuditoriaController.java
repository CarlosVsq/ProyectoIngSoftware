package com.proyecto.datalab.web.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.entity.Auditoria;
import com.proyecto.datalab.repository.AuditoriaRepository;
import com.proyecto.datalab.web.dto.AuditoriaDTO;
import com.proyecto.datalab.web.dto.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    /**
     * Endpoint para el Panel de Alertas (Widget pequeño)
     * Retorna los últimos 'limit' registros.
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ApiResponse<List<AuditoriaDTO>> listarRecientes(@RequestParam(defaultValue = "10") int limit) {
        Pageable page = PageRequest.of(0, Math.min(limit, 50), Sort.by(Sort.Direction.DESC, "fechaCambio"));
        return ApiResponse.success(convertirLista(auditoriaRepository.findAll(page).getContent()));
    }

    /**
     * Endpoint para la Página Principal con Filtros y Paginación
     * Permite buscar por usuario, acción y rango de fechas.
     */
    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> buscar(
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCambio"));
        
        // Limpieza de parámetros vacíos
        if (accion != null && accion.trim().isEmpty()) accion = null;

        Page<Auditoria> pagina = auditoriaRepository.buscarConFiltros(usuarioId, accion, fechaInicio, fechaFin, pageable);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("content", convertirLista(pagina.getContent()));
        respuesta.put("totalPages", pagina.getTotalPages());
        respuesta.put("totalElements", pagina.getTotalElements());
        
        return ApiResponse.success(respuesta);
    }

    /**
     * Endpoint actualizado: KPIs de las últimas 24 horas
     * Calcula: Accesos (Login), Actividad Total y Cierres (Logout).
     */
    @GetMapping("/stats")
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Long>> obtenerEstadisticas() {
        // Ventana de tiempo: Últimas 24 horas
        LocalDateTime hace24h = LocalDateTime.now().minusHours(24);

        // 1. Accesos (LOGIN)
        long accesos24h = auditoriaRepository.countByAccionAndFechaCambioAfter("LOGIN", hace24h);
        
        // 2. Actividad Total (Cualquier evento registrado)
        long eventos24h = auditoriaRepository.countByFechaCambioAfter(hace24h);
        
        // 3. Cierres de Sesión (LOGOUT) - Reemplaza a "Intentos Fallidos"
        long cierres24h = auditoriaRepository.countByAccionAndFechaCambioAfter("LOGOUT", hace24h);
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("accesos", accesos24h);
        stats.put("actividad", eventos24h);
        stats.put("cierres", cierres24h);
        stats.put("alertas", 0L); // Placeholder para futuras implementaciones de alertas de seguridad
        
        return ApiResponse.success(stats);
    }

    // Helper para convertir entidad a DTO
    private List<AuditoriaDTO> convertirLista(List<Auditoria> lista) {
        return lista.stream().map(this::toDto).collect(Collectors.toList());
    }

    private AuditoriaDTO toDto(Auditoria a) {
        return AuditoriaDTO.builder()
                .idAuditoria(a.getIdAuditoria())
                .usuario(a.getUsuario() != null ? a.getUsuario().getNombreCompleto() : "Sistema")
                // Manejo de nulos para participante (Login/Logout no tienen participante)
                .participante(a.getParticipante() != null ? a.getParticipante().getCodigoParticipante() : null)
                .tablaAfectada(a.getTablaAfectada())
                .accion(a.getAccion())
                .detalleCambio(a.getDetalleCambio())
                .fechaCambio(a.getFechaCambio())
                .build();
    }
}