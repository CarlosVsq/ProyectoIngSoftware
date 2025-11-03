package com.proyecto.datalab.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.proyecto.datalab.dto.RespuestaUpdateRequest;
import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.entity.Respuesta;
import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.entity.Variable;
import com.proyecto.datalab.repository.ParticipanteRepository;
import com.proyecto.datalab.repository.RespuestaRepository;
import com.proyecto.datalab.repository.VariableRepository;

import jakarta.transaction.Transactional;

@Service
public class RespuestaService {

    private final ParticipanteRepository participanteRepository;
    private final RespuestaRepository respuestaRepository;
    private final VariableRepository variableRepository;
    private final AuditoriaService auditoriaService;

    RespuestaService(RespuestaRepository respuestaRepository, ParticipanteRepository participanteRepository, VariableRepository variableRepository, AuditoriaService auditoriaService) {
        this.respuestaRepository = respuestaRepository;
        this.participanteRepository = participanteRepository;
        this.variableRepository = variableRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Respuesta responder(Integer idParticipante, Integer idVariable, String valorRespuesta){

        Participante participante = participanteRepository.findById(idParticipante)
            .orElseThrow(() -> new RuntimeException("Participante no encontrado"));

        Variable columna = variableRepository.findById(idVariable)
            .orElseThrow(() -> new RuntimeException("Variable no encontrada"));

        Respuesta newRespuesta = new Respuesta();
        newRespuesta.setParticipante(participante);
        newRespuesta.setValorIngresado(valorRespuesta);
        newRespuesta.setVariable(columna);

        return respuestaRepository.save(newRespuesta);
    }

    @Transactional
    public Respuesta actualizaRespuesta(Integer idRespuesta, RespuestaUpdateRequest request){
        Respuesta respuesta = respuestaRepository.findById(idRespuesta)
            .orElseThrow(() -> new RuntimeException("Respuesta no encontrada"));

            if (request.getValorIngresado() !=null && !request.getValorIngresado().isEmpty()) {
                respuesta.setValorIngresado(request.getValorIngresado());

                Usuario editor = respuesta.getParticipante().getReclutador();

                Participante p = respuesta.getParticipante();

                auditoriaService.registrarAccion(editor, p, "ACTUALIZAR", "Respuesta", 
                        "Se guardó respuesta para participante ID: " + p.getIdParticipante() + ", Variable ID: " + respuesta.getVariable().getIdVariable());
            }

        return respuestaRepository.save(respuesta);
    }

    @Transactional
    public List<Respuesta> obtenerTodasRespuestas(){
        return respuestaRepository.findAll();
    }
}
