package com.softwells.fanops.repository;

import com.softwells.fanops.model.HistorialAccesoEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialAccesoRepository extends JpaRepository<HistorialAccesoEntity, UUID> {
  /** Los últimos accesos primero: es lo único que le interesa mirar al superadmin. */
  List<HistorialAccesoEntity> findTop200ByOrderByFechaDesc();
}
