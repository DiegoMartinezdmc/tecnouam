package edu.uam.tecnouam.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "prestamos")
public class Prestamo {
  public enum Estado {
    ACTIVO,
    DEVUELTO
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "equipo_id")
  private Equipo equipo;

  @Column(nullable = false, length = 120)
  private String solicitante;

  @Column(name = "fecha_prestamo", nullable = false)
  private LocalDate fechaPrestamo;

  @Column(name = "fecha_prevista_devolucion", nullable = false)
  private LocalDate fechaPrevistaDevolucion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Estado estado = Estado.ACTIVO;

  public Long getId() {
    return id;
  }

  public Equipo getEquipo() {
    return equipo;
  }

  public void setEquipo(Equipo equipo) {
    this.equipo = equipo;
  }

  public String getSolicitante() {
    return solicitante;
  }

  public void setSolicitante(String solicitante) {
    this.solicitante = solicitante;
  }

  public LocalDate getFechaPrestamo() {
    return fechaPrestamo;
  }

  public void setFechaPrestamo(LocalDate fechaPrestamo) {
    this.fechaPrestamo = fechaPrestamo;
  }

  public LocalDate getFechaPrevistaDevolucion() {
    return fechaPrevistaDevolucion;
  }

  public void setFechaPrevistaDevolucion(LocalDate fechaPrevistaDevolucion) {
    this.fechaPrevistaDevolucion = fechaPrevistaDevolucion;
  }

  public Estado getEstado() {
    return estado;
  }

  public void setEstado(Estado estado) {
    this.estado = estado;
  }
}
