package com.proyecto.datalab.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.datalab.entidades.Participante;
import com.proyecto.datalab.enums.EstadoFicha;
import com.proyecto.datalab.enums.GrupoParticipante;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Integer> {
    
    Optional<Participante> findByRut(String rut);
    List<Participante> findByGrupo(GrupoParticipante grupo);
    List<Participante> findByEstado(EstadoFicha estado);
    List<Participante> findByUsuarioReclutador_IdUsuario(Integer idUsuario);
}