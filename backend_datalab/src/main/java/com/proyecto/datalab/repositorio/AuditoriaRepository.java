package com.proyecto.datalab.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.datalab.entidades.Auditoria;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

}
