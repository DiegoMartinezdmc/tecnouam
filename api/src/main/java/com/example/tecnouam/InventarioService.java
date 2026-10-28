package com.example.tecnouam;

import com.example.tecnouam.ApiDtos.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventarioService {
    private final EquipoRepository equipos;
    private final PrestamoRepository prestamos;

    public InventarioService(EquipoRepository equipos, PrestamoRepository prestamos) {
        this.equipos = equipos;
        this.prestamos = prestamos;
    }

    @Transactional(readOnly = true)
    public List<EquipoOutput> listarEquipos() {
        return equipos.findAll().stream().map(EquipoOutput::from).toList();
    }

    @Transactional(readOnly = true)
    public EquipoOutput consultarEquipo(Long id) {
        return EquipoOutput.from(equipos.findById(id).orElseThrow(() -> noExiste("Equipo")));
    }

    @Transactional
    public EquipoOutput crearEquipo(EquipoInput input) {
        String codigo = input.codigoPatrimonial().trim();
        if (equipos.existsByCodigoPatrimonial(codigo)) throw conflicto("El código patrimonial ya existe");
        Equipo equipo = new Equipo(codigo, input.nombre().trim(), input.tipo().trim());
        return EquipoOutput.from(equipos.saveAndFlush(equipo));
    }

    @Transactional
    public EquipoOutput actualizarEquipo(Long id, EquipoInput input) {
        Equipo equipo = equipos.findByIdForUpdate(id).orElseThrow(() -> noExiste("Equipo"));
        String codigo = input.codigoPatrimonial().trim();
        if (equipos.existsByCodigoPatrimonialAndIdNot(codigo, id)) throw conflicto("El código patrimonial ya existe");
        equipo.setCodigoPatrimonial(codigo);
        equipo.setNombre(input.nombre().trim());
        equipo.setTipo(input.tipo().trim());
        return EquipoOutput.from(equipos.saveAndFlush(equipo));
    }

    @Transactional
    public void eliminarEquipo(Long id) {
        Equipo equipo = equipos.findByIdForUpdate(id).orElseThrow(() -> noExiste("Equipo"));
        if (prestamos.existsByEquipoIdAndEstado(id, EstadoPrestamo.ACTIVO))
            throw conflicto("No se puede eliminar un equipo con préstamos activos");
        prestamos.deleteByEquipoId(id);
        prestamos.flush();
        equipos.delete(equipo);
    }

    @Transactional(readOnly = true)
    public List<PrestamoOutput> listarPrestamos() {
        return prestamos.findAll().stream().map(PrestamoOutput::from).toList();
    }

    @Transactional
    public PrestamoOutput registrarPrestamo(PrestamoInput input) {
        if (input.fechaPrevistaDevolucion().isBefore(input.fechaPrestamo()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fecha prevista no puede ser anterior a la fecha del préstamo");
        Equipo equipo = equipos.findByIdForUpdate(input.equipoId()).orElseThrow(() -> noExiste("Equipo"));
        if (equipo.getEstado() == EstadoEquipo.PRESTADO ||
                prestamos.existsByEquipoIdAndEstado(equipo.getId(), EstadoPrestamo.ACTIVO))
            throw conflicto("El equipo ya está prestado");
        Prestamo prestamo = new Prestamo(equipo, input.nombreSolicitante().trim(),
                input.fechaPrestamo(), input.fechaPrevistaDevolucion());
        equipo.setEstado(EstadoEquipo.PRESTADO);
        prestamos.saveAndFlush(prestamo);
        return PrestamoOutput.from(prestamo);
    }

    @Transactional
    public PrestamoOutput devolver(Long id) {
        Prestamo prestamo = prestamos.findByIdForUpdate(id).orElseThrow(() -> noExiste("Préstamo"));
        Equipo equipo = equipos.findByIdForUpdate(prestamo.getEquipo().getId()).orElseThrow(() -> noExiste("Equipo"));
        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) throw conflicto("El préstamo ya fue devuelto");
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        equipo.setEstado(EstadoEquipo.DISPONIBLE);
        return PrestamoOutput.from(prestamos.saveAndFlush(prestamo));
    }

    private ApiException noExiste(String recurso) { return new ApiException(HttpStatus.NOT_FOUND, recurso + " inexistente"); }
    private ApiException conflicto(String mensaje) { return new ApiException(HttpStatus.CONFLICT, mensaje); }
}
