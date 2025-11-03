package com.proyecto.datalab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.datalab.entity.Respuesta;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Integer> {
}
