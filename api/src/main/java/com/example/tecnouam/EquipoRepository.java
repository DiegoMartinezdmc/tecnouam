package com.example.tecnouam;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    boolean existsByCodigoPatrimonial(String codigoPatrimonial);
    boolean existsByCodigoPatrimonialAndIdNot(String codigoPatrimonial, Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Equipo e where e.id = :id")
    Optional<Equipo> findByIdForUpdate(Long id);
}
