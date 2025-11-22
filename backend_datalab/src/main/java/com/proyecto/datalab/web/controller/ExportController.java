package com.proyecto.datalab.web.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.repository.ParticipanteRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ParticipanteRepository participanteRepository;

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel() {
        // Export básico CSV compatible con Excel; anonimización pendiente.
        List<Participante> participantes = participanteRepository.findAll();
        String header = "codigo,nombre,grupo,estado,fecha_inclusion";
        List<String> rows = participantes.stream()
            .map(p -> String.join(",",
                    safe(p.getCodigoParticipante()),
                    safe(p.getNombreCompleto()),
                    safe(p.getGrupo() != null ? p.getGrupo().name() : ""),
                    safe(p.getEstadoFicha() != null ? p.getEstadoFicha().name() : ""),
                    safe(p.getFechaInclusion() != null ? p.getFechaInclusion().toString() : "")
            ))
            .collect(Collectors.toList());
        String csv = header + "\n" + String.join("\n", rows);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"participantes.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdfPlaceholder() {
        // Placeholder simple en texto; el equipo puede reemplazar por PDF real/anonimización/Stata.
        String content = "Export PDF pendiente de implementación completa.\nTotal participantes: " + participanteRepository.count();
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    private String safe(String v) {
        return v == null ? "" : v.replace(",", " ");
    }
}
