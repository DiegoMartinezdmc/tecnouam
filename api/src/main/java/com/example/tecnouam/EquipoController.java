package com.example.tecnouam;

import com.example.tecnouam.ApiDtos.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {
    private final InventarioService service;
    public EquipoController(InventarioService service) { this.service = service; }
    @GetMapping public List<EquipoOutput> listar() { return service.listarEquipos(); }
    @GetMapping("/{id}") public EquipoOutput consultar(@PathVariable Long id) { return service.consultarEquipo(id); }
    @PostMapping public ResponseEntity<EquipoOutput> crear(@Valid @RequestBody EquipoInput input) {
        EquipoOutput creado = service.crearEquipo(input);
        return ResponseEntity.created(URI.create("/api/equipos/" + creado.id())).body(creado);
    }
    @PutMapping("/{id}") public EquipoOutput actualizar(@PathVariable Long id, @Valid @RequestBody EquipoInput input) {
        return service.actualizarEquipo(id, input);
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminarEquipo(id);
        return ResponseEntity.noContent().build();
    }
}
