package com.proyecto.datalab.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.entity.Rol;
import com.proyecto.datalab.repository.RolRepository;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    @Autowired
    private RolRepository rolRepository;

    @GetMapping
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rol> actualizarPermisos(@PathVariable Integer id, @RequestBody Rol rolDetails) {
        return rolRepository.findById(id).map(rol -> {
            rol.setPermisoVerDatos(rolDetails.isPermisoVerDatos());
            rol.setPermisoModificar(rolDetails.isPermisoModificar());
            rol.setPermisoExportar(rolDetails.isPermisoExportar());
            rol.setPermisoAdministrar(rolDetails.isPermisoAdministrar());
            return ResponseEntity.ok(rolRepository.save(rol));
        }).orElse(ResponseEntity.notFound().build());
    }
}
