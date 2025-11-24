package com.proyecto.datalab.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.entity.Respuesta;
import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.entity.Variable;
import com.proyecto.datalab.enums.EstadoFicha;
import com.proyecto.datalab.enums.GrupoParticipante;
import com.proyecto.datalab.repository.ParticipanteRepository;
import com.proyecto.datalab.repository.RespuestaRepository;
import com.proyecto.datalab.repository.UsuarioRepository;
import com.proyecto.datalab.repository.VariableRepository;

/**
 * Servicio para gestion de participantes y sus respuestas
 * HU-01, HU-02, HU-15, HU-23
 */
@Service
public class ParticipanteService {

        private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\s-]{8,20}$");
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        private final ParticipanteRepository participanteRepository;
        private final RespuestaRepository respuestaRepository;
        private final UsuarioRepository usuarioRepository;
        private final VariableRepository variableRepository;
        private final AuditoriaService auditoriaService;

        public ParticipanteService(ParticipanteRepository participanteRepository,
                                RespuestaRepository respuestaRepository,
                                UsuarioRepository usuarioRepository,
                                VariableRepository variableRepository,
                                AuditoriaService auditoriaService) {
                this.participanteRepository = participanteRepository;
                this.respuestaRepository = respuestaRepository;
                this.usuarioRepository = usuarioRepository;
                this.variableRepository = variableRepository;
                this.auditoriaService = auditoriaService;
        }

        /**
         * Crear participante validando formato de telefono y grupo.
         */
        @Transactional
        public Participante crearParticipante(String nombreCompleto, String telefono, String direccion, String grupo, Integer usuarioReclutadorId) {

                String telefonoLimpio = telefono != null ? telefono.trim() : null;
                if (telefonoLimpio != null && !telefonoLimpio.isEmpty() && !PHONE_PATTERN.matcher(telefonoLimpio).matches()) {
                        throw new IllegalArgumentException("Formato de telefono invalido");
                }

                Usuario reclutador = usuarioRepository.findById(usuarioReclutadorId)
                        .orElseThrow(() -> new RuntimeException("Usuario reclutador no encontrado"));

                GrupoParticipante grupoNormalizado;
                try {
                        grupoNormalizado = GrupoParticipante.valueOf(grupo.trim().toUpperCase());
                } catch (Exception e) {
                        throw new IllegalArgumentException("Grupo invalido, debe ser CASO o CONTROL");
                }

                Participante participante = new Participante();
                participante.setNombreCompleto(nombreCompleto);
                participante.setTelefono(telefonoLimpio);
                participante.setDireccion(direccion);
                participante.setGrupo(grupoNormalizado);
                participante.setReclutador(reclutador);
                participante.setEstadoFicha(EstadoFicha.INCOMPLETA);
                participante.setFechaInclusion(LocalDate.now());

                Participante participanteGuardado = participanteRepository.save(participante);

                if (participanteGuardado.getGrupo() == GrupoParticipante.CASO) {
                        participanteGuardado.setCodigoParticipante("CS" + participanteGuardado.getIdParticipante());
                } else {
                        participanteGuardado.setCodigoParticipante("CT" + participanteGuardado.getIdParticipante());
                }

                auditoriaService.registrarAccion(
                        reclutador,
                        participanteGuardado,
                        "CREAR",
                        "Participante",
                        "Se creo el participante ID: " + participanteGuardado.getIdParticipante()
                );

                return participanteRepository.save(participanteGuardado);
        }

        /**
         * Guardar respuestas con validacion basica por tipo y reglas declaradas en la variable.
         */
        @Transactional
        public void guardarRespuestas(Integer participanteId, Map<String, String> respuestasMap, Integer usuarioEditorId) {

                if (respuestasMap == null || respuestasMap.isEmpty()) {
                        throw new IllegalArgumentException("No se enviaron respuestas para guardar");
                }

                Participante participante = participanteRepository.findById(participanteId)
                        .orElseThrow(() -> new RuntimeException("Participante no encontrado"));

                Usuario editor = usuarioRepository.findById(usuarioEditorId)
                        .orElseThrow(() -> new RuntimeException("Usuario editor no encontrado"));

                for (Map.Entry<String, String> entry : respuestasMap.entrySet()) {
                        String variableKey = entry.getKey();
                        String valor = entry.getValue();

                        Optional<Variable> variableOpt = resolverVariable(variableKey);
                        if (variableOpt.isEmpty()) {
                                // si el frontend envía campos que no están en BD, los omitimos
                                continue;
                        }
                        Variable variable = variableOpt.get();

                        validarRespuesta(variable, valor);

                        Optional<Respuesta> existente = respuestaRepository
                                .findByParticipante_IdParticipanteAndVariable_IdVariable(participanteId, variable.getIdVariable());

                        Respuesta respuesta = existente.orElseGet(Respuesta::new);
                        respuesta.setParticipante(participante);
                        respuesta.setVariable(variable);
                        respuesta.setValorIngresado(valor);

                        respuestaRepository.save(respuesta);

                        auditoriaService.registrarAccion(
                                editor,
                                participante,
                                "ACTUALIZAR",
                                "Respuesta",
                                "Se guardo respuesta para participante ID: " + participante.getIdParticipante()
                                        + ", Variable: " + variable.getCodigoVariable()
                        );
                }
        }

        @Transactional(readOnly = true)
        public List<Participante> obtenerTodosLosParticipantes() {
                return participanteRepository.findAll();
        }

        /**
         * Marca la ficha como NO_COMPLETABLE con una justificacion.
         */
        @Transactional
        public Participante marcarNoCompletable(Integer participanteId, String justificacion, Integer usuarioEditorId) {
                if (justificacion == null || justificacion.trim().isEmpty()) {
                        throw new IllegalArgumentException("Debe ingresar una justificacion para marcar como no completable");
                }

                Participante participante = participanteRepository.findById(participanteId)
                        .orElseThrow(() -> new RuntimeException("Participante no encontrado"));

                Usuario editor = usuarioRepository.findById(usuarioEditorId)
                        .orElseThrow(() -> new RuntimeException("Usuario editor no encontrado"));

                participante.setEstadoFicha(EstadoFicha.NO_COMPLETABLE);
                participante.setObservacion(justificacion.trim());

                Participante actualizado = participanteRepository.save(participante);

                auditoriaService.registrarAccion(
                        editor,
                        participante,
                        "ACTUALIZAR",
                        "Participante",
                        "Se marco participante ID: " + participante.getIdParticipante() + " como NO_COMPLETABLE. Motivo: " + justificacion
                );

                return actualizado;
        }

        /**
         * Valida el valor segun el tipo de dato y la regla de validacion JSON de la Variable.
         * Reglas soportadas (todas opcionales):
         *  - regex: expresion regular a cumplir
         *  - min / max: limites numericos
         *  - minLength / maxLength: limites de largo
         */
        private void validarRespuesta(Variable variable, String valor) {
                if (variable.isEsObligatoria() && (valor == null || valor.trim().isEmpty())) {
                        throw new IllegalArgumentException("El campo " + variable.getCodigoVariable() + " es obligatorio");
                }

                if (valor == null || valor.trim().isEmpty()) {
                        return; // no hay nada que validar
                }

                String tipoDato = variable.getTipoDato() != null ? variable.getTipoDato().trim().toLowerCase() : "";

                if ("numero".equals(tipoDato) || "number".equals(tipoDato)) {
                        double numero;
                        try {
                                numero = Double.parseDouble(valor);
                        } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("El campo " + variable.getCodigoVariable() + " debe ser numerico");
                        }
                        applyNumericRules(variable, numero);
                }

                Map<String, Object> reglas = parseReglas(variable.getReglaValidacion());
                applyRegexRule(variable, valor, reglas);
                applyLengthRules(variable, valor, reglas);
        }

        private void applyNumericRules(Variable variable, double numero) {
                Map<String, Object> reglas = parseReglas(variable.getReglaValidacion());
                if (reglas.containsKey("min")) {
                        double min = Double.parseDouble(reglas.get("min").toString());
                        if (numero < min) {
                                throw new IllegalArgumentException("El valor de " + variable.getCodigoVariable() + " es menor al minimo permitido");
                        }
                }
                if (reglas.containsKey("max")) {
                        double max = Double.parseDouble(reglas.get("max").toString());
                        if (numero > max) {
                                throw new IllegalArgumentException("El valor de " + variable.getCodigoVariable() + " excede el maximo permitido");
                        }
                }
        }

        private void applyRegexRule(Variable variable, String valor, Map<String, Object> reglas) {
                if (!reglas.containsKey("regex")) {
                        return;
                }
                String regex = reglas.get("regex").toString();
                if (!Pattern.compile(regex).matcher(valor).matches()) {
                        throw new IllegalArgumentException("El valor de " + variable.getCodigoVariable() + " no cumple el formato esperado");
                }
        }

        private void applyLengthRules(Variable variable, String valor, Map<String, Object> reglas) {
                if (reglas.containsKey("minLength")) {
                        int minLength = Integer.parseInt(reglas.get("minLength").toString());
                        if (valor.length() < minLength) {
                                throw new IllegalArgumentException("El valor de " + variable.getCodigoVariable() + " es demasiado corto");
                        }
                }
                if (reglas.containsKey("maxLength")) {
                        int maxLength = Integer.parseInt(reglas.get("maxLength").toString());
                        if (valor.length() > maxLength) {
                                throw new IllegalArgumentException("El valor de " + variable.getCodigoVariable() + " excede el largo maximo");
                        }
                }
        }

        private Map<String, Object> parseReglas(String reglaValidacion) {
                if (reglaValidacion == null || reglaValidacion.trim().isEmpty()) {
                        return Map.of();
                }
                try {
                        return OBJECT_MAPPER.readValue(reglaValidacion, new TypeReference<Map<String, Object>>(){});
                } catch (Exception e) {
                        throw new IllegalArgumentException("Regla de validacion invalida para la variable " + reglaValidacion);
                }
        }

        /**
         * Permite resolver variables por id numérico o por código (string).
         */
        private Optional<Variable> resolverVariable(String variableKey) {
                // Intentar como ID
                try {
                        Integer id = Integer.valueOf(variableKey);
                        return variableRepository.findById(id);
                } catch (NumberFormatException ignored) {
                        // intentar por código
                        return variableRepository.findByCodigoVariable(variableKey);
                }
        }
}
