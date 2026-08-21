package com.softwells.fanops.repository;

import com.softwells.fanops.enums.EstadoInscripcion;
import com.softwells.fanops.model.EventoInscripcionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface EventoInscripcionRepository
    extends JpaRepository<EventoInscripcionEntity, UUID> {

  List<EventoInscripcionEntity> findByEventoUidOrderByFechaInscripcionAsc(UUID eventoUid);

  List<EventoInscripcionEntity> findByEventoUidAndEstadoOrderByFechaInscripcionAsc(
      UUID eventoUid, EstadoInscripcion estado);

  Optional<EventoInscripcionEntity> findByEventoUidAndSocioUid(UUID eventoUid, UUID socioUid);

  boolean existsByEventoUidAndSocioUid(UUID eventoUid, UUID socioUid);

  boolean existsByEventoUidAndEmailIgnoreCase(UUID eventoUid, String email);

  long countByEventoUidAndEstado(UUID eventoUid, EstadoInscripcion estado);
}
