package com.softwells.fanops.repository;

import com.softwells.fanops.model.PenaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenaRepository extends JpaRepository<PenaEntity, Long> {

  /** Peña por su identificador en la URL. Se usa sin sesión, desde el login y el registro. */
  Optional<PenaEntity> findBySlugIgnoreCase(String slug);

  boolean existsBySlugIgnoreCase(String slug);
}
