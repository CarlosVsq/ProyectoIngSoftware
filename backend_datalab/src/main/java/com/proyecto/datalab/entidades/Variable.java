package com.proyecto.datalab.entidades;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "Variable")
public class Variable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variable")
    private Integer idVariable;

    @Lob
    @Column(name = "enunciado", nullable = false)
    private String enunciado;

    @Column(name = "codigo_variable", nullable = false, unique = true, length = 100)
    private String codigoVariable;

    @Column(name = "tipo_dato", nullable = false, length = 50)
    private String tipoDato;

    @Lob
    @Column(name = "opciones")
    private String opciones;

    @Column(name = "aplica_a", nullable = false, length = 50)
    private String aplicaA;

    @Column(name = "seccion", length = 100)
    private String seccion;

    @Column(name = "orden_enunciado")
    private Integer ordenEnunciado;

    @Column(name = "es_obligatoria", nullable = false)
    private boolean esObligatoria;

    @Lob
    @Column(name = "regla_validacion")
    private String reglaValidacion;

    @OneToMany(mappedBy = "variable", fetch = FetchType.LAZY)
    private List<Respuesta> respuestas;
}