package com.proyecto.datalab.service;

import org.springframework.stereotype.Service;

import com.proyecto.datalab.dto.VariableCreateRequest;
import com.proyecto.datalab.entity.Variable;
import com.proyecto.datalab.repository.VariableRepository;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.domain.Sort;

@Service
public class VariableService {

    private final VariableRepository variableRepository;

    VariableService(VariableRepository variableRepository) {
        this.variableRepository = variableRepository;
    }

    @Transactional
    public Variable crearVariable(VariableCreateRequest request) {
        Variable variable = new Variable();

        variable.setEnunciado(request.getEnunciado());
        variable.setCodigoVariable(request.getCodigoVariable());
        variable.setTipoDato(request.getTipoDato());
        variable.setOpciones(request.getOpciones());
        variable.setAplicaA(request.getAplicaA());
        variable.setSeccion(request.getSeccion());
        variable.setOrdenEnunciado(request.getOrdenEnunciado());
        variable.setEsObligatoria(request.isEsObligatoria());
        variable.setReglaValidacion(request.getReglaValidacion());

        return variableRepository.save(variable);
    }

    @Transactional
    public List<Variable> listarVariables() {
        Sort sort = Sort.by(
                Sort.Order.asc("seccion"),
                Sort.Order.asc("ordenEnunciado"),
                Sort.Order.asc("idVariable"));
        return variableRepository.findAll(sort);
    }

    @Transactional
    public void eliminarVariable(String codigo) {
        variableRepository.deleteByCodigoVariable(codigo);
    }
}
