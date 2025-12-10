package com.proyecto.datalab.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.dto.VariableCreateRequest;
import com.proyecto.datalab.entity.Variable;
import com.proyecto.datalab.service.VariableService;

@RestController
@RequestMapping("api/variables")
public class VariableController {
    @Autowired
    private VariableService variableService;

    @GetMapping
    public Map<String, Object> listar() {
        Map<String, Object> response = new HashMap<>();
        response.put("data", variableService.listarVariables());
        return response;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Variable postMethodName(@RequestBody VariableCreateRequest entity) {
        return variableService.crearVariable(entity);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@org.springframework.web.bind.annotation.PathVariable String codigo) {
        variableService.eliminarVariable(codigo);
    }

}
