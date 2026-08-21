package com.softwells.fanops.repository;

import com.softwells.fanops.enums.EstadoCuota;
import com.softwells.fanops.model.CuotaEntity;
import com.softwells.fanops.model.SocioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CuotaRepository extends JpaRepository<CuotaEntity, UUID> {

  List<CuotaEntity> findBySocioUid(UUID uid);

  boolean existsBySocioAndEstado(SocioEntity socio, EstadoCuota estado);

  boolean existsBySocioAndEstadoAndFechaEmisionGreaterThanEqual(SocioEntity socio,
      EstadoCuota estado, LocalDate fecha);

  @Modifying
  @Query("UPDATE CuotaEntity c SET c.estado = 'PAGADA' WHERE c.estado = 'PENDIENTE'")
  int actualizarPendientesAPagadas();

  @Query("SELECT COUNT(DISTINCT c.socio.id) FROM CuotaEntity c WHERE c.estado IN :estados")
  int countDistinctSociosByEstadoIn(@Param("estados") List<EstadoCuota> estados);

  List<CuotaEntity> findByEstado(EstadoCuota estado);
}
