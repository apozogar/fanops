package com.softwells.fanops.repository;

import com.softwells.fanops.enums.EstadoSorteo;
import com.softwells.fanops.model.SorteoCarnetEntity;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface SorteoCarnetRepository extends JpaRepository<SorteoCarnetEntity, UUID> {

  Optional<SorteoCarnetEntity> findByEventoUid(UUID eventoUid);

  /**
   * Igual que {@link #findByEventoUid}, pero bloqueando la fila. Es lo que impide que el sorteo
   * se celebre dos veces cuando el planificador y un admin que lo adelanta coinciden en el
   * mismo instante: el segundo espera y se encuentra el sorteo ya EJECUTADO.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from SorteoCarnetEntity s where s.evento.uid = :eventoUid")
  Optional<SorteoCarnetEntity> findByEventoUidParaEjecutar(@Param("eventoUid") UUID eventoUid);

  List<SorteoCarnetEntity> findByEstadoAndFechaProgramadaLessThanEqual(EstadoSorteo estado,
      LocalDateTime limite);
}
