package com.proyecto.datalab.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.datalab.entity.Respuesta;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Integer> {

    Optional<Respuesta> findByParticipante_IdParticipanteAndVariable_IdVariable(Integer idParticipante, Integer idVariable);

    java.util.List<Respuesta> findByParticipante_IdParticipante(Integer idParticipante);
}
