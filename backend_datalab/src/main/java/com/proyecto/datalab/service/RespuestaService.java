package com.proyecto.datalab.service;
import org.springframework.stereotype.Service;

import com.proyecto.datalab.dto.RespuestaUpdateRequest;
import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.entity.Respuesta;
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

    RespuestaService(RespuestaRepository respuestaRepository, ParticipanteRepository participanteRepository, VariableRepository variableRepository) {
        this.respuestaRepository = respuestaRepository;
        this.participanteRepository = participanteRepository;
        this.variableRepository = variableRepository;
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
            }

        return respuestaRepository.save(respuesta);
    }
}
