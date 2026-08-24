package com.softwells.fanops.repository;

import com.softwells.fanops.enums.MotivoFalta;
import com.softwells.fanops.model.FaltaEventoEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface FaltaEventoRepository extends JpaRepository<FaltaEventoEntity, UUID> {

  List<FaltaEventoEntity> findBySocioUidOrderByFechaRegistroDesc(UUID socioUid);

  /** Faltas que todavía penalizan, la más antigua primero: es la que toca gastar. */
  List<FaltaEventoEntity> findBySocioUidAndPenalizacionesRestantesGreaterThanOrderByFechaRegistroAsc(
      UUID socioUid, int minimo);

  long countBySocioUid(UUID socioUid);

  /**
   * Faltas por socio de una peña, en una sola consulta: el listado de gestión muestra una columna
   * de faltas por fila y contarlas socio a socio sería una consulta por fila.
   */
  @Query("SELECT f.socio.uid AS socioUid, COUNT(f) AS total, "
      + "SUM(CASE WHEN f.penalizacionesRestantes > 0 THEN 1L ELSE 0L END) AS pendientes "
      + "FROM FaltaEventoEntity f WHERE f.socio.pena.id = :penaId GROUP BY f.socio.uid")
  List<ResumenFaltasSocio> resumenPorPena(@Param("penaId") Long penaId);

  /** Total de faltas de un socio y cuántas de ellas siguen penalizando. */
  interface ResumenFaltasSocio {

    UUID getSocioUid();

    long getTotal();

    long getPendientes();
  }

  Optional<FaltaEventoEntity> findByEventoUidAndSocioUid(UUID eventoUid, UUID socioUid);

  List<FaltaEventoEntity> findByEventoUid(UUID eventoUid);

  Optional<FaltaEventoEntity> findByEventoUidAndSocioUidAndMotivo(UUID eventoUid, UUID socioUid,
      MotivoFalta motivo);

  /** Cancelaciones tardías de un evento pendientes de que alguien ocupe el hueco. */
  List<FaltaEventoEntity> findByEventoUidAndMotivoOrderByFechaRegistroAsc(UUID eventoUid,
      MotivoFalta motivo);
}
