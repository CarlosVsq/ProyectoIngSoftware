package com.proyecto.datalab.web.controller;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.entity.Respuesta;
import com.proyecto.datalab.entity.Variable;
import com.proyecto.datalab.repository.ParticipanteRepository;
import com.proyecto.datalab.repository.RespuestaRepository;
import com.proyecto.datalab.repository.VariableRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ParticipanteRepository participanteRepository;
    private final RespuestaRepository respuestaRepository;
    private final VariableRepository variableRepository;

    /**
     * Export simple CSV (compatible con Excel) de respuestas en formato matriz:
     * columnas = variables, filas = participantes.
     */
    @GetMapping("/respuestas/excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportRespuestasMatrizCsv() {
        List<Variable> variables = variableRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Variable::getCodigoVariable))
                .collect(Collectors.toList());

        List<Participante> participantes = participanteRepository.findAll();
        Map<Integer, List<Respuesta>> respuestasPorPart = respuestaRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(r -> r.getParticipante().getIdParticipante()));

        List<String> headers = new ArrayList<>();
        headers.add("codigo_participante");
        headers.add("nombre");
        headers.add("grupo");
        headers.add("estado");
        headers.add("fecha_inclusion");
        headers.addAll(variables.stream().map(Variable::getCodigoVariable).toList());

        List<String> rows = new ArrayList<>();
        for (Participante p : participantes) {
            Map<String, String> mapResp = respuestasPorPart.getOrDefault(p.getIdParticipante(), List.of())
                    .stream()
                    .collect(Collectors.toMap(r -> r.getVariable().getCodigoVariable(), Respuesta::getValorIngresado, (a, b) -> a, LinkedHashMap::new));
            List<String> cols = new ArrayList<>();
            cols.add(safe(p.getCodigoParticipante()));
            cols.add(safe(p.getNombreCompleto()));
            cols.add(safe(p.getGrupo() != null ? p.getGrupo().name() : ""));
            cols.add(safe(p.getEstadoFicha() != null ? p.getEstadoFicha().name() : ""));
            cols.add(safe(p.getFechaInclusion() != null ? p.getFechaInclusion().toString() : ""));
            for (Variable v : variables) {
                cols.add(safe(mapResp.getOrDefault(v.getCodigoVariable(), "")));
            }
            rows.add(String.join(",", cols));
        }

        String csv = String.join(",", headers) + "\n" + String.join("\n", rows);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"respuestas_matriz.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    /**
     * PDF individual por participante con un formato tipo formulario.
     */
    @GetMapping("/participante/{id}/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportPdfParticipante(@PathVariable Integer id) {
        Participante p = participanteRepository.findById(id).orElse(null);
        if (p == null) {
            return ResponseEntity.notFound().build();
        }
        List<Respuesta> respuestas = respuestaRepository.findByParticipante_IdParticipante(id);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("CRF - Ficha de Participante", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            addInfoCell(info, "Código", safe(p.getCodigoParticipante()));
            addInfoCell(info, "Nombre", safe(p.getNombreCompleto()));
            addInfoCell(info, "Grupo", safe(p.getGrupo() != null ? p.getGrupo().name() : ""));
            addInfoCell(info, "Estado ficha", safe(p.getEstadoFicha() != null ? p.getEstadoFicha().name() : ""));
            addInfoCell(info, "Fecha inclusión", p.getFechaInclusion() != null ? p.getFechaInclusion().format(DateTimeFormatter.ISO_DATE) : "");
            addInfoCell(info, "Teléfono", safe(p.getTelefono()));
            addInfoCell(info, "Dirección", safe(p.getDireccion()));
            document.add(info);
            document.add(new Paragraph(" "));

            Font subtitulo = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            document.add(new Paragraph("Respuestas", subtitulo));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            addHeaderCell(table, "Variable");
            addHeaderCell(table, "Enunciado");
            addHeaderCell(table, "Valor");

            respuestas.stream()
                    .sorted(Comparator.comparing(r -> r.getVariable().getCodigoVariable()))
                    .forEach(r -> {
                        addBodyCell(table, safe(r.getVariable() != null ? r.getVariable().getCodigoVariable() : ""));
                        addBodyCell(table, safe(r.getVariable() != null ? r.getVariable().getEnunciado() : ""));
                        addBodyCell(table, safe(r.getValorIngresado()));
                    });

            document.add(table);
            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"crf_" + safe(p.getCodigoParticipante()) + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());
        } catch (Exception e) {
            byte[] fallback = ("No se pudo generar el PDF: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(fallback);
        }
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdfPlaceholder() {
        String content = "Export PDF pendiente de implementación completa.\nTotal participantes: " + participanteRepository.count();
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    private String safe(String v) {
        return v == null ? "" : v.replace(",", " ");
    }

    private void addHeaderCell(PdfPTable table, String value) {
        Font font = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String value) {
        Font font = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private void addInfoCell(PdfPTable table, String label, String value) {
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        PdfPCell c2 = new PdfPCell(new Phrase(value, valueFont));
        c1.setPadding(5f);
        c2.setPadding(5f);
        table.addCell(c1);
        table.addCell(c2);
    }
}
