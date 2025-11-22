package com.proyecto.datalab.web.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.entity.Auditoria;
import com.proyecto.datalab.repository.AuditoriaRepository;
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
    public ApiResponse<List<Auditoria>> listar(
            @RequestParam(defaultValue = "10") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100); // entre 1 y 100
        var page = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "fechaCambio"));
        var auditorias = auditoriaRepository.findAll(page).getContent();
        return ApiResponse.success(auditorias);
    }
}
