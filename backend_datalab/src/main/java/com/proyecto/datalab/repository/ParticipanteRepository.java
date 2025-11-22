package com.proyecto.datalab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.enums.EstadoFicha;
import com.proyecto.datalab.enums.GrupoParticipante;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Integer> {
    
    List<Participante> findByGrupo(GrupoParticipante grupo);
    List<Participante> findByEstadoFicha(EstadoFicha estadoFicha);
    List<Participante> findByReclutador(Usuario reclutador);
}