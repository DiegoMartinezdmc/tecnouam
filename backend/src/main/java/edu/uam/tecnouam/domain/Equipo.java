package edu.uam.tecnouam.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "equipos")
public class Equipo {
  public enum Tipo {
    PORTATIL,
    PROYECTOR,
    TABLETA
  }

  public enum Estado {
    DISPONIBLE,
    PRESTADO
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "codigo_patrimonial", nullable = false, unique = true, length = 40)
  private String codigoPatrimonial;

  @Column(nullable = false, length = 120)
  private String nombre;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Tipo tipo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Estado estado = Estado.DISPONIBLE;

  public Long getId() {
    return id;
  }

  public String getCodigoPatrimonial() {
    return codigoPatrimonial;
  }

  public void setCodigoPatrimonial(String codigoPatrimonial) {
    this.codigoPatrimonial = codigoPatrimonial;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Tipo getTipo() {
    return tipo;
  }

  public void setTipo(Tipo tipo) {
    this.tipo = tipo;
  }

  public Estado getEstado() {
    return estado;
  }

  public void setEstado(Estado estado) {
    this.estado = estado;
  }
}
