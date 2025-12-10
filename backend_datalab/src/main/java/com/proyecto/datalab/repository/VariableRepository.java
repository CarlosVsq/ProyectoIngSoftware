package com.proyecto.datalab.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.datalab.entity.Variable;

@Repository
public interface VariableRepository extends JpaRepository<Variable, Integer> {

    Optional<Variable> findByCodigoVariable(String codigoVariable);

    void deleteByCodigoVariable(String codigoVariable);
}
