package edu.uam.tecnouam.repository;

import edu.uam.tecnouam.domain.Equipo;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Equipo e where e.id = :id")
  Optional<Equipo> bloquear(Long id);
}
