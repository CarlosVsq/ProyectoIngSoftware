package com.proyecto.datalab.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.datalab.entidades.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long>{

}
