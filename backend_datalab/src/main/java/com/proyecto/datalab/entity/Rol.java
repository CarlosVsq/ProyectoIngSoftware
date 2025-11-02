package com.proyecto.datalab.entity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad Rol - HU-03: Accesos por rol
 * Representa los roles del sistema con sus permisos asociados
 */
@Entity
@Table(name = "Rol")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Rol implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol", nullable = false)
    private Integer idRol;

    @Column(name = "nombre_rol", nullable = false, unique = true, length = 100)
    private String nombreRol;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @OneToMany(mappedBy = "rol", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Usuario> usuarios;
    // Constantes de roles
    public static final String INVESTIGADORA_PRINCIPAL = "Investigadora Principal";
    public static final String MEDICO = "Medico";
    public static final String INVESTIGADOR_RECLUTA = "Investigador que recluta";
    public static final String INVESTIGADOR_SIN_RECLUTA = "Investigador sin reclutamiento";
    public static final String ESTUDIANTE = "Estudiante";
    public static final String ADMINISTRADOR = "Administrador";

    // Métodos de utilidad para permisos
    public boolean puedeCrudCrf() {
        return INVESTIGADORA_PRINCIPAL.equals(nombreRol) ||
               MEDICO.equals(nombreRol) ||
               INVESTIGADOR_RECLUTA.equals(nombreRol) ||
               ESTUDIANTE.equals(nombreRol);
    }

    public boolean puedeExportar() {
        return INVESTIGADORA_PRINCIPAL.equals(nombreRol) ||
               INVESTIGADOR_SIN_RECLUTA.equals(nombreRol) ||
               ADMINISTRADOR.equals(nombreRol);
    }

    public boolean puedeReclutar() {
        return INVESTIGADORA_PRINCIPAL.equals(nombreRol) ||
               MEDICO.equals(nombreRol) ||
               INVESTIGADOR_RECLUTA.equals(nombreRol);
    }

    public boolean puedeAdministrarUsuarios() {
        return INVESTIGADORA_PRINCIPAL.equals(nombreRol) ||
               ADMINISTRADOR.equals(nombreRol);
    }

    public boolean puedeVerAuditoria() {
        return INVESTIGADORA_PRINCIPAL.equals(nombreRol) ||
               ADMINISTRADOR.equals(nombreRol);
    }

    /**
     * Nombre del rol formateado para Spring Security
     * Ejemplo: "Investigadora Principal" → "INVESTIGADORA_PRINCIPAL"
     */
    public String getNombreRolParaSecurity() {
        if (nombreRol == null) return "";
        return nombreRol
            .toUpperCase()
            .replace(" ", "_")
            .replace("Á", "A")
            .replace("É", "E")
            .replace("Í", "I")
            .replace("Ó", "O")
            .replace("Ú", "U")
            .replace("Ñ", "N");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rol)) return false;
        Rol rol = (Rol) o;
        return idRol != null && idRol.equals(rol.idRol);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}