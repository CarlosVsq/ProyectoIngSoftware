package com.proyecto.datalab.web.controller;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.enums.EstadoFicha;
import com.proyecto.datalab.enums.GrupoParticipante;
import com.proyecto.datalab.repository.ParticipanteRepository;
import com.proyecto.datalab.web.dto.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ParticipanteRepository participanteRepository;

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

        // Serie simple por mes (��ltimos 6 meses) usando fechaInclusion
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

        // Placeholders para distribuciones (se poblarán cuando se capturen sexo/edad en BD)
        Map<String, Object> sexo = new HashMap<>();
        sexo.put("masculino", 0);
        sexo.put("femenino", 0);
        sexo.put("otros", 0);
        data.put("porSexo", sexo);

        Map<String, Object> edades = new HashMap<>();
        edades.put("18-30", 0);
        edades.put("31-45", 0);
        edades.put("46-60", 0);
        edades.put("61-75", 0);
        edades.put("76+", 0);
        data.put("porEdad", edades);

        return ApiResponse.success(data);
    }
}
