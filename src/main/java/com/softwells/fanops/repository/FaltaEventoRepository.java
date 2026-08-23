package com.softwells.fanops.repository;

import com.softwells.fanops.enums.MotivoFalta;
import com.softwells.fanops.model.FaltaEventoEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface FaltaEventoRepository extends JpaRepository<FaltaEventoEntity, UUID> {

  List<FaltaEventoEntity> findBySocioUidOrderByFechaRegistroDesc(UUID socioUid);

  /** Faltas que todavía penalizan, la más antigua primero: es la que toca gastar. */
  List<FaltaEventoEntity> findBySocioUidAndPenalizacionesRestantesGreaterThanOrderByFechaRegistroAsc(
      UUID socioUid, int minimo);

  long countBySocioUid(UUID socioUid);

  Optional<FaltaEventoEntity> findByEventoUidAndSocioUid(UUID eventoUid, UUID socioUid);

  List<FaltaEventoEntity> findByEventoUid(UUID eventoUid);

  Optional<FaltaEventoEntity> findByEventoUidAndSocioUidAndMotivo(UUID eventoUid, UUID socioUid,
      MotivoFalta motivo);

  /** Cancelaciones tardías de un evento pendientes de que alguien ocupe el hueco. */
  List<FaltaEventoEntity> findByEventoUidAndMotivoOrderByFechaRegistroAsc(UUID eventoUid,
      MotivoFalta motivo);
}
