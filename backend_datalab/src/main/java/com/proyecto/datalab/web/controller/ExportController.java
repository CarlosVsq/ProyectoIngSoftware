package com.proyecto.datalab.web.controller;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

import com.proyecto.datalab.service.AuditoriaService;
import com.proyecto.datalab.repository.UsuarioRepository;
import com.proyecto.datalab.entity.Usuario;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Map;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ParticipanteRepository participanteRepository;
    private final RespuestaRepository respuestaRepository;
    private final VariableRepository variableRepository;
    private final AuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;
    private final com.proyecto.datalab.service.VariableCodingService variableCodingService;

    // Helper method to get current user
    private Usuario getCurrentUser() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            Object principal = auth.getPrincipal();
            String username;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }
            return usuarioRepository.findByCorreo(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getExportStats() {
        return ResponseEntity.ok(auditoriaService.getExportStatsLast7Days());
    }

    // Defines the static columns for the export
    private enum StaticColumn {
        CODIGO_PARTICIPANTE;
    }

    @GetMapping("/participante/{id}/pdf")

    public ResponseEntity<byte[]> exportPdfParticipante(
            @org.springframework.web.bind.annotation.PathVariable Integer id) {
        Participante p = participanteRepository.findById(id).orElse(null);
        if (p == null)
            return ResponseEntity.notFound().build();

        // LOG
        // LOG
        try {
            Usuario u = getCurrentUser();
            if (u != null) {
                auditoriaService.registrarAccion(u, p, "EXPORTAR", "Participante",
                        "Exportó PDF del participante " + p.getCodigoParticipante());
            }
        } catch (Exception e) {
            e.printStackTrace(); // Non-blocking logging
        }

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

            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(2);
            table.setWidthPercentage(100);
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
                // Filter by Group (Aplica A)
                String aplica = v.getAplicaA() != null ? v.getAplicaA() : "Ambos";
                String grupo = p.getGrupo() != null ? p.getGrupo().name() : ""; // CASO / CONTROL

                boolean show = aplica.equalsIgnoreCase("Ambos") || aplica.equalsIgnoreCase(grupo);
                if (!show)
                    continue;

                addBodyCell(table, safe(v.getEnunciado()));

                // Inject value for codigo_participante if missing in responses
                String val = respuestasMap.get(v.getCodigoVariable());
                if (v.getCodigoVariable().equalsIgnoreCase("codigo_participante") && (val == null || val.isEmpty())) {
                    val = p.getCodigoParticipante();
                }

                addBodyCell(table, safe(val));
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

    @GetMapping("/leyenda-pdf")

    public ResponseEntity<byte[]> exportLegendPdf() {
        // LOG
        // LOG
        try {
            Usuario u = getCurrentUser();
            if (u != null) {
                auditoriaService.registrarAccion(u, null, "EXPORTAR", "Variables", "Exportó Leyenda en PDF");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            document.open();

            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                    16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(
                    "Leyenda de Variables - Diccionario de Datos",
                    titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(title);
            document.add(new com.itextpdf.text.Paragraph(" "));

            // Expanded to 4 columns to include Encoding Rules
            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 2, 5, 2, 4 }); // Relative widths

            addHeaderCell(table, "Código Variable");
            addHeaderCell(table, "Descripción / Enunciado");
            addHeaderCell(table, "Tipo de Dato");
            addHeaderCell(table, "Codificación / Reglas");

            List<Variable> allVariables = variableRepository.findAll().stream()
                    .sorted(Comparator.comparing(Variable::getOrdenEnunciado,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

            for (Variable v : allVariables) {
                addBodyCell(table, safe(v.getCodigoVariable()));
                addBodyCell(table, safe(v.getEnunciado()));
                addBodyCell(table, safe(v.getTipoDato()));
                addBodyCell(table, variableCodingService.getEncodingDescription(v));
            }

            document.add(table);
            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"leyenda_variables.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/excel")

    public ResponseEntity<byte[]> exportToExcel() {
        // LOG
        // LOG
        try {
            Usuario u = getCurrentUser();
            if (u != null) {
                auditoriaService.registrarAccion(u, null, "EXPORTAR", "Base de Datos",
                        "Exportó Base Completa (Excel)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Datos Completos");

            // --- 1. PREPARE DATA ---
            List<Variable> variables = getSafeVariables();

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
                // No más datos del participante aparte del código

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

    public ResponseEntity<byte[]> exportToCsv() {
        // LOG
        // LOG
        try {
            Usuario u = getCurrentUser();
            if (u != null) {
                auditoriaService.registrarAccion(u, null, "EXPORTAR", "Base de Datos",
                        "Exportó Base Completa (CSV)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            // No más datos del participante aparte del código

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

    @GetMapping("/excel-dicotomizado")
    public ResponseEntity<byte[]> exportExcelCoded() {
        // LOG
        try {
            Usuario u = getCurrentUser();
            if (u != null) {
                auditoriaService.registrarAccion(u, null, "EXPORTAR", "Base de Datos",
                        "Exportó Base Codificada (Excel)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Datos Codificados");

            List<Variable> variables = getSafeVariables();
            List<Participante> participantes = participanteRepository.findAll();

            // Map: ParticipanteID -> { CodigoVariable -> Valor }
            Map<Integer, Map<String, String>> respuestasMap = respuestaRepository.findAll().stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getParticipante().getIdParticipante(),
                            Collectors.toMap(
                                    r -> r.getVariable().getCodigoVariable(),
                                    Respuesta::getValorIngresado,
                                    (a, b) -> b)));

            // Pre-Calculate Coded Values to avoid double processing (needed for stats)
            // Structure: ParticipanteID -> { CodeVar -> CodedVal }
            Map<Integer, Map<String, String>> codedValues = new java.util.HashMap<>();
            for (Participante p : participantes) {
                Map<String, String> pRespuestas = respuestasMap.getOrDefault(p.getIdParticipante(), Map.of());
                Map<String, String> pCoded = new java.util.HashMap<>();

                // Static
                pCoded.put("CODIGO_PARTICIPANTE", p.getCodigoParticipante());

                for (Variable v : variables) {
                    String raw = pRespuestas.getOrDefault(v.getCodigoVariable(), "");
                    String coded = variableCodingService.encodeValue(v, raw);
                    pCoded.put(v.getCodigoVariable(), coded);
                }
                codedValues.put(p.getIdParticipante(), pCoded);
            }

            // --- HEADER ROW (Row 0) ---
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            int colIdx = 0;

            // Static Headers
            for (StaticColumn col : StaticColumn.values()) {
                Cell cell = headerRow.createCell(colIdx++);
                cell.setCellValue(col.name());
                cell.setCellStyle(headerStyle);
            }
            // Variable Headers
            for (Variable v : variables) {
                Cell cell = headerRow.createCell(colIdx++);
                cell.setCellValue(v.getCodigoVariable());
                cell.setCellStyle(headerStyle);
            }

            // --- SUMMARY ROWS (Rows 1, 2, 3) ---
            // Row 1: Count "0"
            // Row 2: Count "1" (or >0 for Ordinals?) - User asked for 0/1 counts.
            // For ordinals (0,1,2,3), "1" count is specific. Maybe better to just count 0
            // and 1 as requested?
            // "Contadores 0/1/vacíos" -> I will count exact matches for 0 and 1, and empty.
            Row rowCount0 = sheet.createRow(1);
            Row rowCount1 = sheet.createRow(2);
            Row rowCountEmpty = sheet.createRow(3);

            rowCount0.createCell(0).setCellValue("Total '0'");
            rowCount1.createCell(0).setCellValue("Total '1'");
            rowCountEmpty.createCell(0).setCellValue("Vacíos/Nulos");

            colIdx = 1; // start after header static col (assuming only 1 static col
                        // CODIGO_PARTICIPANTE)
            // Note: Static column has no 0/1 stats usually, just skip or empty.

            for (Variable v : variables) {
                long c0 = 0;
                long c1 = 0;
                long cEmpty = 0;

                for (Participante p : participantes) {
                    Map<String, String> vals = codedValues.get(p.getIdParticipante());
                    String val = vals.get(v.getCodigoVariable());

                    if (val == null || val.isEmpty())
                        cEmpty++;
                    else if (val.equals("0"))
                        c0++;
                    else if (val.equals("1"))
                        c1++;
                }

                rowCount0.createCell(colIdx).setCellValue(c0);
                rowCount1.createCell(colIdx).setCellValue(c1);
                rowCountEmpty.createCell(colIdx).setCellValue(cEmpty);
                colIdx++;
            }

            // --- DATA ROWS (Row 4+) ---
            int rowIdx = 4;
            for (Participante p : participantes) {
                Row row = sheet.createRow(rowIdx++);
                colIdx = 0;
                Map<String, String> vals = codedValues.get(p.getIdParticipante());

                // Static
                row.createCell(colIdx++).setCellValue(vals.get("CODIGO_PARTICIPANTE"));

                // Variables
                for (Variable v : variables) {
                    Cell cell = row.createCell(colIdx++);
                    String val = vals.get(v.getCodigoVariable());
                    // Try to store as number if possible for better excel handling
                    try {
                        if (val != null && !val.isEmpty()) {
                            double d = Double.parseDouble(val);
                            cell.setCellValue(d);
                        } else {
                            cell.setCellValue("");
                        }
                    } catch (NumberFormatException e) {
                        cell.setCellValue(safe(val));
                    }
                }
            }

            workbook.write(baos);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"datos_codificados.xlsx\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/csv-dicotomizado")
    public ResponseEntity<byte[]> exportCsvCoded() {
        // LOG
        try {
            Usuario u = getCurrentUser();
            if (u != null) {
                auditoriaService.registrarAccion(u, null, "EXPORTAR", "Base de Datos",
                        "Exportó Base Codificada (CSV)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Variable> variables = getSafeVariables();
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

        for (Participante p : participantes) {
            List<String> cols = new ArrayList<>();
            cols.add(escapeCsv(p.getCodigoParticipante()));

            Map<String, String> pRespuestas = respuestasMap.getOrDefault(p.getIdParticipante(), Map.of());

            for (Variable v : variables) {
                String raw = pRespuestas.getOrDefault(v.getCodigoVariable(), "");
                String coded = variableCodingService.encodeValue(v, raw);
                cols.add(escapeCsv(coded));
            }
            csv.append(String.join(",", cols)).append("\n");
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"datos_codificados.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    public ResponseEntity<byte[]> exportToCsvStata() {
        // LOG
        // LOG
        try {
            Usuario u = getCurrentUser();
            if (u != null) {
                auditoriaService.registrarAccion(u, null, "EXPORTAR", "Base de Datos",
                        "Exportó Base Completa (CSV STATA)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            // No más datos del participante aparte del código

            for (Variable v : variables) {
                String rawVal = pRespuestas.getOrDefault(v.getCodigoVariable(), "");
                // STATA COMPATIBILITY: Force dot as decimal separator for numbers
                if (v.getTipoDato() != null && v.getTipoDato().toLowerCase().contains("numero") && rawVal != null) {
                    rawVal = rawVal.replace(",", ".");
                }
                cols.add(escapeCsv(rawVal));
            }
            csv.append(String.join(",", cols)).append("\n");
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"datos_stata.csv\"")
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

    /**
     * Obtiene variables de forma determinista, eliminando duplicados y
     * excluyendo campos sensibles que no deben exportarse.
     */
    private List<Variable> getSafeVariables() {
        // Campos sensibles a excluir (minúsculas)
        var sensitiveCodes = new LinkedHashSet<>(List.of(
                "nombre",
                "telefono",
                "nombre_completo",
                "correo_electronico",
                "direccion"));

        return variableRepository.findAll().stream()
                .sorted(Comparator
                        .comparing(Variable::getOrdenEnunciado, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Variable::getCodigoVariable))
                // Elimina duplicados por código manteniendo el primero
                .collect(Collectors.toMap(
                        v -> v.getCodigoVariable() != null ? v.getCodigoVariable().toLowerCase() : "",
                        v -> v,
                        (a, b) -> a,
                        LinkedHashMap::new))
                .values().stream()
                // Excluir sensibles y columnas estáticas
                .filter(v -> {
                    String code = v.getCodigoVariable();
                    if (code == null)
                        return false;
                    String lower = code.toLowerCase();
                    return !sensitiveCodes.contains(lower)
                            && !lower.equals(StaticColumn.CODIGO_PARTICIPANTE.name().toLowerCase());
                })
                .collect(Collectors.toList());
    }
}
