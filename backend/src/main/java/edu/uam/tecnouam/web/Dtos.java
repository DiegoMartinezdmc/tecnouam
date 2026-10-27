package edu.uam.tecnouam.web;

import edu.uam.tecnouam.domain.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public final class Dtos {
  private Dtos() {}

  public record EquipoInput(
      @NotBlank @Size(max = 40) @Pattern(regexp = "[A-Za-z0-9_-]+") String codigoPatrimonial,
      @NotBlank @Size(max = 120) String nombre,
      @NotNull Equipo.Tipo tipo) {}

  public record PrestamoInput(
      @NotNull @Positive Long equipoId,
      @NotBlank @Size(max = 120) String solicitante,
      @NotNull LocalDate fechaPrestamo,
      @NotNull LocalDate fechaPrevistaDevolucion) {}

  public record Login(@NotBlank String username, @NotBlank String password) {}

  public record PrestamoView(
      Long id,
      Equipo equipo,
      String solicitante,
      LocalDate fechaPrestamo,
      LocalDate fechaPrevistaDevolucion,
      Prestamo.Estado estado) {
    public static PrestamoView of(Prestamo p) {
      return new PrestamoView(
          p.getId(),
          p.getEquipo(),
          p.getSolicitante(),
          p.getFechaPrestamo(),
          p.getFechaPrevistaDevolucion(),
          p.getEstado());
    }
  }
}
