package edu.uam.tecnouam.repository;

import edu.uam.tecnouam.domain.Prestamo;
import java.util.List;
import org.springframework.data.jpa.repository.*;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
  void deleteByEquipoId(Long id);

  boolean existsByEquipoIdAndEstado(Long id, Prestamo.Estado estado);

  @EntityGraph(attributePaths = "equipo")
  List<Prestamo> findAllByOrderByIdDesc();
}
