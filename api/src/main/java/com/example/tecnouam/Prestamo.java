package com.example.tecnouam;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "prestamos")
public class Prestamo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;
    @Column(nullable = false)
    private String nombreSolicitante;
    @Column(nullable = false)
    private LocalDate fechaPrestamo;
    @Column(nullable = false)
    private LocalDate fechaPrevistaDevolucion;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPrestamo estado = EstadoPrestamo.ACTIVO;

    protected Prestamo() {}
    public Prestamo(Equipo equipo, String nombreSolicitante, LocalDate fechaPrestamo, LocalDate fechaPrevistaDevolucion) {
        this.equipo = equipo;
        this.nombreSolicitante = nombreSolicitante;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaPrevistaDevolucion = fechaPrevistaDevolucion;
    }
    public Long getId() { return id; }
    public Equipo getEquipo() { return equipo; }
    public String getNombreSolicitante() { return nombreSolicitante; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaPrevistaDevolucion() { return fechaPrevistaDevolucion; }
    public EstadoPrestamo getEstado() { return estado; }
    public void setEstado(EstadoPrestamo estado) { this.estado = estado; }
}
