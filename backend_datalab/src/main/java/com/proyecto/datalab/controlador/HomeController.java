package com.proyecto.datalab.controlador;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/") // <-- Esto le dice a Spring que responda en la ruta raíz
    public String home() {
        return "¡Bienvenido! Has iniciado sesión.";
    }
}