package edu.uam.tecnouam.service;

import edu.uam.tecnouam.domain.*;
import edu.uam.tecnouam.repository.*;
import edu.uam.tecnouam.web.Dtos.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class InventarioService {
  @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
  private final EquipoRepository equipos;
  private final PrestamoRepository prestamos;

  public InventarioService(EquipoRepository equipos, PrestamoRepository prestamos) {
    this.equipos = equipos;
    this.prestamos = prestamos;
  }

  private ResponseStatusException error(HttpStatus status, String message) {
    return new ResponseStatusException(status, message);
  }

  @Transactional(readOnly = true)
  public List<Equipo> equipos() {
    return equipos.findAll(org.springframework.data.domain.Sort.by("id"));
  }

  @Transactional(readOnly = true)
  public Equipo equipo(Long id) {
    return equipos
        .findById(id)
        .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Equipo no encontrado"));
  }

  private Equipo bloquear(Long id) {
    return equipos
        .bloquear(id)
        .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Equipo no encontrado"));
  }

  public Equipo guardar(Long id, EquipoInput input) {
    Equipo e = id == null ? new Equipo() : bloquear(id);
    e.setCodigoPatrimonial(input.codigoPatrimonial().trim().toUpperCase(java.util.Locale.ROOT));
    e.setNombre(input.nombre().trim());
    e.setTipo(input.tipo());
    return equipos.saveAndFlush(e);
  }

  public void eliminar(Long id) {
    Equipo e = bloquear(id);
    if (prestamos.existsByEquipoIdAndEstado(id, Prestamo.Estado.ACTIVO))
      throw error(HttpStatus.CONFLICT, "El equipo tiene un préstamo activo");
    prestamos.deleteByEquipoId(id);
    prestamos.flush();
    equipos.delete(e);
    equipos.flush();
  }

  @Transactional(readOnly = true)
  public List<PrestamoView> prestamos() {
    return prestamos.findAllByOrderByIdDesc().stream().map(PrestamoView::of).toList();
  }

  public PrestamoView prestar(PrestamoInput input) {
    if (input.fechaPrevistaDevolucion().isBefore(input.fechaPrestamo()))
      throw error(
          HttpStatus.BAD_REQUEST, "La devolución prevista no puede ser anterior al préstamo");
    Equipo e = bloquear(input.equipoId());
    if (e.getEstado() == Equipo.Estado.PRESTADO
        || prestamos.existsByEquipoIdAndEstado(e.getId(), Prestamo.Estado.ACTIVO))
      throw error(HttpStatus.CONFLICT, "El equipo ya está prestado");
    Prestamo p = new Prestamo();
    p.setEquipo(e);
    p.setSolicitante(input.solicitante().trim());
    p.setFechaPrestamo(input.fechaPrestamo());
    p.setFechaPrevistaDevolucion(input.fechaPrevistaDevolucion());
    e.setEstado(Equipo.Estado.PRESTADO);
    return PrestamoView.of(prestamos.saveAndFlush(p));
  }

  public PrestamoView devolver(Long id) {
    Prestamo p =
        prestamos
            .findById(id)
            .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Préstamo no encontrado"));
    Equipo e = bloquear(p.getEquipo().getId()); // Mismo orden de bloqueo que el alta de préstamos.
    entityManager.refresh(p);
    if (p.getEstado() == Prestamo.Estado.DEVUELTO)
      throw error(HttpStatus.CONFLICT, "El préstamo ya fue devuelto");
    p.setEstado(Prestamo.Estado.DEVUELTO);
    e.setEstado(Equipo.Estado.DISPONIBLE);
    return PrestamoView.of(p);
  }
}
