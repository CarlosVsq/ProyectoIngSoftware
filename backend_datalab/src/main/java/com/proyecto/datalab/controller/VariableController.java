package com.proyecto.datalab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.dto.VariableCreateRequest;
import com.proyecto.datalab.entity.Variable;
import com.proyecto.datalab.service.VariableService;


@RestController
@RequestMapping("api/preguntas")
public class VariableController {
    @Autowired
    private VariableService variableService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Variable postMethodName(@RequestBody VariableCreateRequest entity) {
        return variableService.crearVariable(entity);
    }
    
}
