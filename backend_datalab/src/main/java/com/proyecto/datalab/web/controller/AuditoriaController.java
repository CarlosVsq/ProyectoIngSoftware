package com.proyecto.datalab.web.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * Devuelve los últimos registros de auditoría, limitados para evitar overflow en frontend.
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ApiResponse<List<AuditoriaDTO>> listar(@RequestParam(defaultValue = "10") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100); // entre 1 y 100
        var page = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "fechaCambio"));
        
        var auditorias = auditoriaRepository.findAll(page).getContent();
        
        List<AuditoriaDTO> dto = auditorias.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return ApiResponse.success(dto);
    }

    private AuditoriaDTO toDto(Auditoria a) {
        return AuditoriaDTO.builder()
                .idAuditoria(a.getIdAuditoria())
                .usuario(a.getUsuario() != null ? a.getUsuario().getNombreCompleto() : null)
                // Nota: Asegúrate de que Participante tenga el método getCodigoParticipante() o getCodigo() según tu entidad real
                // Validamos null para evitar NullPointerException en login/logout
                .participante(a.getParticipante() != null ? a.getParticipante().getCodigoParticipante() : null)
                .tablaAfectada(a.getTablaAfectada())
                .accion(a.getAccion())
                .detalleCambio(a.getDetalleCambio())
                .fechaCambio(a.getFechaCambio())
                .build();
    }
}