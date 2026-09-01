package com.softwells.fanops.repository;

import com.softwells.fanops.enums.EstadoSolicitudCarnet;
import com.softwells.fanops.model.SolicitudCarnetEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface SolicitudCarnetRepository extends JpaRepository<SolicitudCarnetEntity, UUID> {

  List<SolicitudCarnetEntity> findByEventoUidOrderByFechaSolicitudAsc(UUID eventoUid);

  List<SolicitudCarnetEntity> findByEventoUidAndEstadoOrderByPosicionSorteoAsc(UUID eventoUid,
      EstadoSolicitudCarnet estado);

  Optional<SolicitudCarnetEntity> findByEventoUidAndSocioUid(UUID eventoUid, UUID socioUid);

  boolean existsByEventoUidAndSocioUid(UUID eventoUid, UUID socioUid);

  /**
   * Participaciones del socio en sorteos ya celebrados, de la más antigua a la más reciente. Es
   * el historial con el que se calculan las papeletas: cuantas más veces se ha quedado sin
   * carnet desde la última que le tocó, más opciones tiene.
   */
  @Query("""
      select s from SolicitudCarnetEntity s
      join SorteoCarnetEntity so on so.evento = s.evento
      where s.socio.uid = :socioUid
        and so.estado = com.softwells.fanops.enums.EstadoSorteo.EJECUTADO
        and s.posicionSorteo is not null
      order by so.fechaEjecucion asc
      """)
  List<SolicitudCarnetEntity> historialSorteosDe(@Param("socioUid") UUID socioUid);
}
