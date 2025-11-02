package com.proyecto.datalab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.datalab.entity.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long>{

}
