package com.proyecto.datalab.web.controller;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.enums.EstadoFicha;
import com.proyecto.datalab.enums.GrupoParticipante;
import com.proyecto.datalab.repository.ParticipanteRepository;
import com.proyecto.datalab.repository.RespuestaRepository;
import com.proyecto.datalab.web.dto.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ParticipanteRepository participanteRepository;
    private final RespuestaRepository respuestaRepository;

    @GetMapping("/resumen")
    public ApiResponse<Map<String, Object>> resumen() {
        Map<String, Object> data = new HashMap<>();
        long total = participanteRepository.count();
        data.put("total", total);
        data.put("casos", participanteRepository.countByGrupo(GrupoParticipante.CASO));
        data.put("controles", participanteRepository.countByGrupo(GrupoParticipante.CONTROL));
        data.put("completas", participanteRepository.countByEstadoFicha(EstadoFicha.COMPLETA));
        data.put("incompletas", participanteRepository.countByEstadoFicha(EstadoFicha.INCOMPLETA));
        data.put("noCompletable", participanteRepository.countByEstadoFicha(EstadoFicha.NO_COMPLETABLE));
        data.put("meta", 500); // meta fija, ajustable

        // Serie simple por mes (últimos 6 meses) usando fechaInclusion
        var participantes = participanteRepository.findAll();
        LocalDate hoy = LocalDate.now();
        var serie = new java.util.ArrayList<Map<String, Object>>();
        for (int i = 5; i >= 0; i--) {
            LocalDate inicioMes = hoy.minusMonths(i).withDayOfMonth(1);
            LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
            long countMes = participantes.stream()
                    .filter(p -> p.getFechaInclusion() != null
                            && !p.getFechaInclusion().isBefore(inicioMes)
                            && !p.getFechaInclusion().isAfter(finMes))
                    .count();
            Map<String, Object> punto = new HashMap<>();
            punto.put("label", inicioMes.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            punto.put("valor", countMes);
            serie.add(punto);
        }
        data.put("serieReclutamiento", serie);

        Map<String, Object> sexo = calcularSexo();
        Map<String, Object> edades = calcularEdad();
        data.put("porSexo", sexo);
        data.put("porEdad", edades);

        return ApiResponse.success(data);
    }

    private Map<String, Object> calcularSexo() {
        Map<String, AtomicInteger> conteo = new HashMap<>();
        conteo.put("masculino", new AtomicInteger());
        conteo.put("femenino", new AtomicInteger());
        conteo.put("otros", new AtomicInteger());

        respuestaRepository.findAll().stream()
                .filter(r -> r.getVariable() != null && r.getVariable().getCodigoVariable() != null)
                .filter(r -> "SEXO".equalsIgnoreCase(r.getVariable().getCodigoVariable()))
                .forEach(r -> {
                    String val = r.getValorIngresado() != null ? r.getValorIngresado().toLowerCase() : "";
                    if (val.contains("masc") || val.contains("hombre"))
                        conteo.get("masculino").incrementAndGet();
                    else if (val.contains("fem") || val.contains("mujer"))
                        conteo.get("femenino").incrementAndGet();
                    else
                        conteo.get("otros").incrementAndGet();
                });

        Map<String, Object> res = new HashMap<>();
        res.put("masculino", conteo.get("masculino").get());
        res.put("femenino", conteo.get("femenino").get());
        res.put("otros", conteo.get("otros").get());
        return res;
    }

    private Map<String, Object> calcularEdad() {
        Map<String, AtomicInteger> conteo = new HashMap<>();
        conteo.put("18-30", new AtomicInteger());
        conteo.put("31-45", new AtomicInteger());
        conteo.put("46-60", new AtomicInteger());
        conteo.put("61-75", new AtomicInteger());
        conteo.put("76+", new AtomicInteger());

        respuestaRepository.findAll().stream()
                .filter(r -> r.getVariable() != null && r.getVariable().getCodigoVariable() != null)
                .filter(r -> "EDAD".equalsIgnoreCase(r.getVariable().getCodigoVariable()))
                .forEach(r -> {
                    try {
                        double edad = Double.parseDouble(r.getValorIngresado());
                        if (edad >= 18 && edad <= 30)
                            conteo.get("18-30").incrementAndGet();
                        else if (edad <= 45)
                            conteo.get("31-45").incrementAndGet();
                        else if (edad <= 60)
                            conteo.get("46-60").incrementAndGet();
                        else if (edad <= 75)
                            conteo.get("61-75").incrementAndGet();
                        else
                            conteo.get("76+").incrementAndGet();
                    } catch (Exception ignored) {
                    }
                });

        Map<String, Object> res = new HashMap<>();
        res.put("18-30", conteo.get("18-30").get());
        res.put("31-45", conteo.get("31-45").get());
        res.put("46-60", conteo.get("46-60").get());
        res.put("61-75", conteo.get("61-75").get());
        res.put("76+", conteo.get("76+").get());
        return res;
    }
}
