package com.example.tecnouam;

import com.example.tecnouam.ApiDtos.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {
    private final InventarioService service;
    public PrestamoController(InventarioService service) { this.service = service; }
    @GetMapping public List<PrestamoOutput> listar() { return service.listarPrestamos(); }
    @PostMapping public ResponseEntity<PrestamoOutput> crear(@Valid @RequestBody PrestamoInput input) {
        PrestamoOutput creado = service.registrarPrestamo(input);
        return ResponseEntity.created(URI.create("/api/prestamos/" + creado.id())).body(creado);
    }
    @PutMapping("/{id}/devolver") public PrestamoOutput devolver(@PathVariable Long id) { return service.devolver(id); }
}
