package com.proyecto.datalab.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.proyecto.datalab.entity.Rol;
import com.proyecto.datalab.repository.RolRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(RolRepository rolRepository) {
        return args -> {
            crearRolSiNoExiste(rolRepository, Rol.INVESTIGADORA_PRINCIPAL, true, true, true, true);
            crearRolSiNoExiste(rolRepository, Rol.INVESTIGADOR_RECLUTA, true, true, true, false);
            crearRolSiNoExiste(rolRepository, Rol.INVESTIGADOR_SIN_RECLUTA, true, true, true, false); // Investigador
            crearRolSiNoExiste(rolRepository, Rol.MEDICO, true, true, false, false); // Coordinadora equivalent? Or just
                                                                                     // another role
            crearRolSiNoExiste(rolRepository, "Coordinadora", true, true, false, false);
            crearRolSiNoExiste(rolRepository, "Analista", true, false, false, false);
            crearRolSiNoExiste(rolRepository, Rol.ESTUDIANTE, true, true, false, false);
            crearRolSiNoExiste(rolRepository, Rol.ADMINISTRADOR, true, true, true, true);
        };
    }

    private void crearRolSiNoExiste(RolRepository repository, String nombre, boolean ver, boolean mod, boolean exp,
            boolean admin) {
        if (repository.findByNombreRol(nombre).isEmpty()) {
            Rol rol = new Rol();
            rol.setNombreRol(nombre);
            rol.setPermisoVerDatos(ver);
            rol.setPermisoModificar(mod);
            rol.setPermisoExportar(exp);
            rol.setPermisoAdministrar(admin);
            repository.save(rol);
            System.out.println("Rol creado: " + nombre);
        }
    }
}
