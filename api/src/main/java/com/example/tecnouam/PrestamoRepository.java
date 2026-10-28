package com.example.tecnouam;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    boolean existsByEquipoIdAndEstado(Long equipoId, EstadoPrestamo estado);
    void deleteByEquipoId(Long equipoId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Prestamo p where p.id = :id")
    Optional<Prestamo> findByIdForUpdate(Long id);
}
