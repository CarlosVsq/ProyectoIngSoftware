package com.proyecto.datalab.repository;

import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.enums.EstadoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.correo = :correo")
    Optional<Usuario> findByCorreo(@Param("correo") String correo);

    boolean existsByCorreo(String correo);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.estado = :estado")
    List<Usuario> findByEstado(@Param("estado") EstadoUsuario estado);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol r WHERE r.idRol = :idRol")
    List<Usuario> findByRolId(@Param("idRol") Integer idRol);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol")
    List<Usuario> findAllWithRoles();
}
