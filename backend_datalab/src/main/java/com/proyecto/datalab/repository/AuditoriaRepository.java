package com.proyecto.datalab.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.datalab.entity.Auditoria;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {

        // 1. Estadísticas para los KPIs
        long countByFechaCambioAfter(LocalDateTime fecha); // Para "Hoy" o "Esta semana"

        long countByAccionAndFechaCambioAfter(String accion, LocalDateTime fecha); // Para "Accesos Hoy"

        @Query("SELECT COUNT(a) FROM Auditoria a WHERE a.accion IN ('LOGIN_FALLIDO', 'ERROR', 'ACCESO_DENEGADO') AND a.fechaCambio >= :fecha")
        long contarErroresDesde(@Param("fecha") LocalDateTime fecha);

        // 2. Buscador con filtros dinámicos (Usuario, Acción, Fechas)
        @Query("SELECT a FROM Auditoria a WHERE " +
                        "(:usuarioId IS NULL OR a.usuario.idUsuario = :usuarioId) AND " +
                        "(:accion IS NULL OR a.accion = :accion) AND " +
                        "(:fechaInicio IS NULL OR a.fechaCambio >= :fechaInicio) AND " +
                        "(:fechaFin IS NULL OR a.fechaCambio <= :fechaFin)")
        Page<Auditoria> buscarConFiltros(
                        @Param("usuarioId") Integer usuarioId,
                        @Param("accion") String accion,
                        @Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin,
                        Pageable pageable);

        boolean existsByParticipanteAndAccionAndFechaCambioAfter(com.proyecto.datalab.entity.Participante participante,
                        String accion, LocalDateTime fechaCambio);

        boolean existsByParticipanteAndAccion(com.proyecto.datalab.entity.Participante participante, String accion);
}