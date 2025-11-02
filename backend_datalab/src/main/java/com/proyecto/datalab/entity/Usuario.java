package com.proyecto.datalab.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import com.proyecto.datalab.enums.EstadoUsuario;

/**
 * Entidad Usuario - Implementa UserDetails para Spring Security
 * HU-40: Mantener sesión iniciada
 * HU-03: Accesos por rol
 * HU-27: Crear usuario
 * HU-28: Bloquear/activar usuario
 */

@Entity
@Table(name = "Usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "contrasenia")
public class Usuario implements Serializable, UserDetails {

    private static final long serialVersionUID = 1L; // Agregado para la interfaz Serializable

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false, foreignKey = @ForeignKey(name = "FK_USUARIO_ROL"))
    private Rol rol;

    @Column(name = "nombre_completo", nullable = false, length = 255)
    private String nombreCompleto;

    @Column(name = "correo", nullable = false, unique = true, length = 255)
    private String correo;

    @Column(name = "contrasenia", nullable = false, length = 255)
    private String contrasenia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, columnDefinition = "ENUM('ACTIVO','INACTIVO') DEFAULT 'ACTIVO'")
    @Builder.Default
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // IMPLEMENTACIÓN DE UserDetails (Spring Security)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (rol == null) {
            return List.of();
        }
        
        // Obtener nombre del rol formateado para Spring Security
        String roleName = rol.getNombreRolParaSecurity();
        
        // Devolver con prefijo ROLE_
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }
    @OneToMany(mappedBy = "reclutador", fetch = FetchType.LAZY)
    private List<Participante> participantesReclutados;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    private List<Auditoria> auditorias;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoUsuario.ACTIVO;
        }
    }

    //Devuelve contraseña ya hasheada
    @Override
    public String getPassword() {
        return this.contrasenia;
    }

    @Override
    public String getUsername() {
        return this.correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return EstadoUsuario.ACTIVO.equals(this.estado);
    }

    //Las credenciales no expiran nunca
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    //Usuario habilitado si está Activo
    @Override
    public boolean isEnabled() {
        return EstadoUsuario.ACTIVO.equals(this.estado);
    }

    // MÉTODOS DE UTILIDAD
    // ====================================================================

    //Verifica si el usuario tiene un rol específico
    public boolean tieneRol(String nombreRol) {
        return rol != null && rol.getNombreRol().equals(nombreRol);
    }

    //Verifica si es Investigadora Principal
    public boolean esInvestigadoraPrincipal() {
        return tieneRol(Rol.INVESTIGADORA_PRINCIPAL);
    }

    //Verifica si es Administrador
    public boolean esAdministrador() {
        return tieneRol(Rol.ADMINISTRADOR);
    }

    //Verifica si puede crear/editar CRF
    public boolean puedeCrudCrf() {
        return rol != null && rol.puedeCrudCrf();
    }

    //Verifica si puede exportar datos
    public boolean puedeExportar() {
        return rol != null && rol.puedeExportar();
    }

    //Verifica si puede reclutar participantes
    public boolean puedeReclutar() {
        return rol != null && rol.puedeReclutar();
    }

    //Verifica si puede ver auditoría
    public boolean puedeAdministrarUsuarios() {
        return rol != null && rol.puedeAdministrarUsuarios();
    }

    //Verifica si puede ver auditoría
    public boolean puedeVerAuditoria() {
        return rol != null && rol.puedeVerAuditoria();
    }

    //Activa el usuario (HU-28)
    public void activar() {
        this.estado = EstadoUsuario.ACTIVO;
    }

    //Desactiva/bloquea el usuario (HU-28)
    public void desactivar() {
        this.estado = EstadoUsuario.INACTIVO;
    }

    //Verifica si el usuario está activo
    public boolean estaActivo() {
        return EstadoUsuario.ACTIVO.equals(this.estado);
    }

    // EQUALS Y HASHCODE (basados en ID)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return idUsuario != null && idUsuario.equals(usuario.idUsuario);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}