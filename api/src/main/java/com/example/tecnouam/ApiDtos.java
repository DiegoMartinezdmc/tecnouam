package com.example.tecnouam;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class ApiDtos {
    private ApiDtos() {}
    public record EquipoInput(@NotBlank @Size(max = 255) String codigoPatrimonial,
                              @NotBlank @Size(max = 255) String nombre,
                              @NotBlank @Size(max = 255) String tipo) {}
    public record EquipoOutput(Long id, String codigoPatrimonial, String nombre, String tipo, EstadoEquipo estado) {
        public static EquipoOutput from(Equipo e) {
            return new EquipoOutput(e.getId(), e.getCodigoPatrimonial(), e.getNombre(), e.getTipo(), e.getEstado());
        }
    }
    public record PrestamoInput(@NotNull @Positive Long equipoId, @NotBlank @Size(max = 255) String nombreSolicitante,
                                @NotNull LocalDate fechaPrestamo, @NotNull LocalDate fechaPrevistaDevolucion) {}
    public record PrestamoOutput(Long id, Long equipoId, String nombreSolicitante, LocalDate fechaPrestamo,
                                 LocalDate fechaPrevistaDevolucion, EstadoPrestamo estado) {
        public static PrestamoOutput from(Prestamo p) {
            return new PrestamoOutput(p.getId(), p.getEquipo().getId(), p.getNombreSolicitante(),
                    p.getFechaPrestamo(), p.getFechaPrevistaDevolucion(), p.getEstado());
        }
    }
}
