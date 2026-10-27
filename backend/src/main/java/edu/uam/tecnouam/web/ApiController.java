package edu.uam.tecnouam.web;

import edu.uam.tecnouam.domain.Equipo;
import edu.uam.tecnouam.service.InventarioService;
import edu.uam.tecnouam.web.Dtos.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {
  private final InventarioService service;

  public ApiController(InventarioService service) {
    this.service = service;
  }

  @GetMapping("/equipos")
  public List<Equipo> equipos() {
    return service.equipos();
  }

  @GetMapping("/equipos/{id}")
  public Equipo equipo(@PathVariable Long id) {
    return service.equipo(id);
  }

  @PostMapping("/equipos")
  public ResponseEntity<Equipo> crear(@Valid @RequestBody EquipoInput input) {
    Equipo e = service.guardar(null, input);
    return ResponseEntity.created(URI.create("/api/equipos/" + e.getId())).body(e);
  }

  @PutMapping("/equipos/{id}")
  public Equipo editar(@PathVariable Long id, @Valid @RequestBody EquipoInput input) {
    return service.guardar(id, input);
  }

  @DeleteMapping("/equipos/{id}")
  public ResponseEntity<Void> eliminar(@PathVariable Long id) {
    service.eliminar(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/prestamos")
  public List<PrestamoView> prestamos() {
    return service.prestamos();
  }

  @PostMapping("/prestamos")
  public ResponseEntity<PrestamoView> prestar(@Valid @RequestBody PrestamoInput input) {
    PrestamoView p = service.prestar(input);
    return ResponseEntity.created(URI.create("/api/prestamos/" + p.id())).body(p);
  }

  @PutMapping("/prestamos/{id}/devolver")
  public PrestamoView devolver(@PathVariable Long id) {
    return service.devolver(id);
  }
}
