package com.example.tecnouam;

import jakarta.persistence.*;

@Entity
@Table(name = "equipos", uniqueConstraints = @UniqueConstraint(columnNames = "codigo_patrimonial"))
public class Equipo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "codigo_patrimonial", nullable = false, unique = true)
    private String codigoPatrimonial;
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false)
    private String tipo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEquipo estado = EstadoEquipo.DISPONIBLE;

    protected Equipo() {}
    public Equipo(String codigoPatrimonial, String nombre, String tipo) {
        this.codigoPatrimonial = codigoPatrimonial;
        this.nombre = nombre;
        this.tipo = tipo;
    }
    public Long getId() { return id; }
    public String getCodigoPatrimonial() { return codigoPatrimonial; }
    public void setCodigoPatrimonial(String codigoPatrimonial) { this.codigoPatrimonial = codigoPatrimonial; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public EstadoEquipo getEstado() { return estado; }
    public void setEstado(EstadoEquipo estado) { this.estado = estado; }
}
