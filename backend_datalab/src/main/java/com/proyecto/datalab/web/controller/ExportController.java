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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // Defines the static columns for the export
    private enum StaticColumn {
        CODIGO_PARTICIPANTE,
        NOMBRE,
        GRUPO,
        ESTADO,
        FECHA_INCLUSION;
    }

    @GetMapping("/participante/{id}/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportPdfParticipante(
            @org.springframework.web.bind.annotation.PathVariable Integer id) {
        Participante p = participanteRepository.findById(id).orElse(null);
        if (p == null)
            return ResponseEntity.notFound().build();

        List<Respuesta> respuestas = respuestaRepository.findByParticipante_IdParticipante(id);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            document.open();

            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                    16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("CRF - Ficha de Participante",
                    titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(title);
            document.add(new com.itextpdf.text.Paragraph(" "));

            com.itextpdf.text.pdf.PdfPTable info = new com.itextpdf.text.pdf.PdfPTable(2);
            info.setWidthPercentage(100);
            addInfoCell(info, "Código", safe(p.getCodigoParticipante()));
            addInfoCell(info, "Nombre", safe(p.getNombreCompleto()));
            addInfoCell(info, "Grupo", safe(p.getGrupo() != null ? p.getGrupo().name() : ""));
            document.add(info);
            document.add(new com.itextpdf.text.Paragraph(" "));

            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(3);
            table.setWidthPercentage(100);
            addHeaderCell(table, "Variable");
            addHeaderCell(table, "Enunciado");
            addHeaderCell(table, "Valor");

            List<Variable> allVariables = variableRepository.findAll().stream()
                    .sorted(Comparator.comparing(Variable::getOrdenEnunciado,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

            Map<String, String> respuestasMap = respuestas.stream()
                    .collect(Collectors.toMap(r -> r.getVariable().getCodigoVariable(), Respuesta::getValorIngresado,
                            (a, b) -> b));

            for (Variable v : allVariables) {
                addBodyCell(table, safe(v.getCodigoVariable()));
                addBodyCell(table, safe(v.getEnunciado()));
                addBodyCell(table, safe(respuestasMap.getOrDefault(v.getCodigoVariable(), "")));
            }

            document.add(table);
            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"crf_" + safe(p.getCodigoParticipante()) + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private void addHeaderCell(com.itextpdf.text.pdf.PdfPTable table, String value) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(value));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addBodyCell(com.itextpdf.text.pdf.PdfPTable table, String value) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(value));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addInfoCell(com.itextpdf.text.pdf.PdfPTable table, String label, String value) {
        table.addCell(label);
        table.addCell(value);
    }

    @GetMapping("/excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportToExcel() {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Datos Completos");

            // --- 1. PREPARE DATA ---
            List<Variable> variables = variableRepository.findAll().stream()
                    .sorted(Comparator
                            .comparing(Variable::getOrdenEnunciado, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Variable::getCodigoVariable))
                    .collect(Collectors.toList());

            List<Participante> participantes = participanteRepository.findAll();

            // Map: ParticipanteID -> { CodigoVariable -> Valor }
            Map<Integer, Map<String, String>> respuestasMap = respuestaRepository.findAll().stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getParticipante().getIdParticipante(),
                            Collectors.toMap(
                                    r -> r.getVariable().getCodigoVariable(),
                                    Respuesta::getValorIngresado,
                                    (a, b) -> b // Keep latest key
                            )));

            // --- 2. HEADER ROW ---
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);

            int colIdx = 0;
            // Static headers
            for (StaticColumn col : StaticColumn.values()) {
                Cell cell = headerRow.createCell(colIdx++);
                cell.setCellValue(col.name()); // Using ENUM NAME (e.g. CODIGO_PARTICIPANTE)
                cell.setCellStyle(headerStyle);
            }
            // Variable headers
            for (Variable v : variables) {
                Cell cell = headerRow.createCell(colIdx++);
                cell.setCellValue(v.getCodigoVariable()); // Using CODE for variable ID
                cell.setCellStyle(headerStyle);
            }

            // --- 3. DATA ROWS ---
            int rowIdx = 1;
            for (Participante p : participantes) {
                Row row = sheet.createRow(rowIdx++);
                colIdx = 0;
                Map<String, String> pRespuestas = respuestasMap.getOrDefault(p.getIdParticipante(), Map.of());

                // Static Data
                row.createCell(colIdx++).setCellValue(safe(p.getCodigoParticipante()));
                row.createCell(colIdx++).setCellValue(safe(p.getNombreCompleto()));
                row.createCell(colIdx++).setCellValue(p.getGrupo() != null ? p.getGrupo().name() : "");
                row.createCell(colIdx++).setCellValue(p.getEstadoFicha() != null ? p.getEstadoFicha().name() : "");
                row.createCell(colIdx++)
                        .setCellValue(p.getFechaInclusion() != null ? p.getFechaInclusion().toString() : "");

                // Dynamic Data
                for (Variable v : variables) {
                    Cell cell = row.createCell(colIdx++);
                    String rawVal = pRespuestas.getOrDefault(v.getCodigoVariable(), "");
                    setCellValueSmart(cell, rawVal, v);
                }
            }

            workbook.write(baos);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"datos_completos.xlsx\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/csv")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportToCsv() {
        List<Variable> variables = variableRepository.findAll().stream()
                .sorted(Comparator
                        .comparing(Variable::getOrdenEnunciado, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Variable::getCodigoVariable))
                .collect(Collectors.toList());

        List<Participante> participantes = participanteRepository.findAll();
        Map<Integer, Map<String, String>> respuestasMap = respuestaRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getParticipante().getIdParticipante(),
                        Collectors.toMap(
                                r -> r.getVariable().getCodigoVariable(),
                                Respuesta::getValorIngresado,
                                (a, b) -> b)));

        StringBuilder csv = new StringBuilder();

        // Header
        List<String> headers = new ArrayList<>();
        for (StaticColumn col : StaticColumn.values())
            headers.add(col.name());
        for (Variable v : variables)
            headers.add(v.getCodigoVariable());
        csv.append(String.join(",", headers)).append("\n");

        // Rows
        for (Participante p : participantes) {
            Map<String, String> pRespuestas = respuestasMap.getOrDefault(p.getIdParticipante(), Map.of());
            List<String> cols = new ArrayList<>();

            cols.add(escapeCsv(p.getCodigoParticipante()));
            cols.add(escapeCsv(p.getNombreCompleto()));
            cols.add(escapeCsv(p.getGrupo() != null ? p.getGrupo().name() : ""));
            cols.add(escapeCsv(p.getEstadoFicha() != null ? p.getEstadoFicha().name() : ""));
            cols.add(escapeCsv(p.getFechaInclusion() != null ? p.getFechaInclusion().toString() : ""));

            for (Variable v : variables) {
                cols.add(escapeCsv(pRespuestas.getOrDefault(v.getCodigoVariable(), "")));
            }
            csv.append(String.join(",", cols)).append("\n");
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"datos_completos.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String escapeCsv(String val) {
        if (val == null)
            return "";
        String escaped = val.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private void setCellValueSmart(Cell cell, String rawVal, Variable v) {
        if (rawVal == null || rawVal.isEmpty()) {
            cell.setCellValue("");
            return;
        }

        if (v.getTipoDato() != null && v.getTipoDato().toLowerCase().contains("numero")) {
            try {
                String clean = rawVal.trim().replace(",", ".");
                double d = Double.parseDouble(clean);
                cell.setCellValue(d);
                return;
            } catch (NumberFormatException e) {
                // Fallback
            }
        }
        cell.setCellValue(rawVal);
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
