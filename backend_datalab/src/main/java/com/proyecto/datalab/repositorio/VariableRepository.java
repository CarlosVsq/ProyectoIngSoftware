package com.proyecto.datalab.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.datalab.entidades.Variable;

@Repository
public interface VariableRepository extends JpaRepository<Variable, Long> {

}
