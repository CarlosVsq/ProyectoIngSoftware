package com.proyecto.datalab.entidades;

import java.time.LocalDate;
import java.util.List;

import com.proyecto.datalab.enums.EstadoFicha;
import com.proyecto.datalab.enums.GrupoParticipante;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Participante")
@Data
@NoArgsConstructor
@Getter
@Setter
public class Participante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_participante")
    private Integer idParticipante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reclutador", nullable = false)
    private Usuario reclutador;

    @Column(name = "codigo_participante", nullable = false, unique = true, length = 50)
    private String codigoParticipante;

    @Column(name = "nombre_completo", length = 255)
    private String nombreCompleto;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "grupo", nullable = false)
    private GrupoParticipante grupo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_ficha", nullable = false)
    private EstadoFicha estadoFicha;

    @Column(name = "fecha_inclusion", nullable = false)
    private LocalDate fechaInclusion;

    @Lob
    @Column(name = "observacion")
    private String observacion;

    @OneToMany(
        mappedBy = "participante", 
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<Respuesta> respuestas;

    @OneToMany(
        mappedBy = "participante", 
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<Auditoria> auditorias;
}
