package com.softwells.fanops.mapper;

import com.softwells.fanops.controller.dto.EventoInscripcionDTO;
import com.softwells.fanops.controller.dto.InscripcionAdminDTO;
import com.softwells.fanops.controller.dto.SocioInscripcionDTO;
import com.softwells.fanops.controller.dto.SorteoResumenDTO;
import com.softwells.fanops.enums.AsistenciaEvento;
import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.model.EventoInscripcionEntity;
import com.softwells.fanops.model.SocioEntity;
import java.util.List;

public class EventoMapper {

  public static EventoInscripcionDTO toInscripcionDTO(EventoEntity evento) {
    return toInscripcionDTO(evento, List.of(), null);
  }

  public static EventoInscripcionDTO toInscripcionDTO(EventoEntity evento,
      List<SocioInscripcionDTO> misSocios) {
    return toInscripcionDTO(evento, misSocios, null);
  }

  /**
   * @param misSocios estado de cada ficha de socio del usuario autenticado frente al evento;
   *                  lista vacía en consultas anónimas o de administración
   */
  public static EventoInscripcionDTO toInscripcionDTO(EventoEntity evento,
      List<SocioInscripcionDTO> misSocios, SorteoResumenDTO sorteo) {
    return EventoInscripcionDTO.builder()
        .uid(evento.getUid())
        .nombreEvento(evento.getNombreEvento())
        .fechaEvento(evento.getFechaEvento())
        .fechaLimiteInscripcion(evento.getFechaLimiteInscripcion())
        .ubicacion(evento.getUbicacion())
        .costePlaza(evento.getCostePlaza())
        .costeCarnet(evento.getCosteCarnet())
        .inscripcionCerrada(evento.isInscripcionCerrada())
        .plazasOcupadas(evento.getNumInscritos())
        .plazasLibres(evento.getPlazasLibres())
        .enListaEspera(evento.getNumEnEspera())
        .isCurrentUserInscrito(evento.isCurrentUserInscrito())
        .misSocios(misSocios)
        .sorteo(sorteo)
        .build();
  }

  public static InscripcionAdminDTO toInscripcionAdminDTO(EventoInscripcionEntity inscripcion) {
    SocioEntity socio = inscripcion.getSocio();
    return InscripcionAdminDTO.builder()
        .uid(inscripcion.getUid())
        .estado(inscripcion.getEstado())
        .socioPrioritario(inscripcion.isSocioPrioritario())
        .fechaInscripcion(inscripcion.getFechaInscripcion())
        .socioUid(socio != null ? socio.getUid() : null)
        .numeroSocio(socio != null ? socio.getNumeroSocio() : null)
        .nombre(inscripcion.getNombre())
        .email(inscripcion.getEmail())
        .telefono(inscripcion.getTelefono())
        // Las inscripciones anteriores a la columna de asistencia pueden traerla a null.
        .asistencia(inscripcion.getAsistencia() != null
            ? inscripcion.getAsistencia()
            : AsistenciaEvento.PENDIENTE)
        .build();
  }
}
