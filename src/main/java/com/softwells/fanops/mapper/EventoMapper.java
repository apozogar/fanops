package com.softwells.fanops.mapper;

import com.softwells.fanops.controller.dto.EventoInscripcionDTO;
import com.softwells.fanops.controller.dto.InscripcionAdminDTO;
import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.model.EventoInscripcionEntity;
import com.softwells.fanops.model.SocioEntity;

public class EventoMapper {

  public static EventoInscripcionDTO toInscripcionDTO(EventoEntity evento) {
    return EventoInscripcionDTO.builder()
        .uid(evento.getUid())
        .nombreEvento(evento.getNombreEvento())
        .fechaEvento(evento.getFechaEvento())
        .fechaLimiteInscripcion(evento.getFechaLimiteInscripcion())
        .ubicacion(evento.getUbicacion())
        .inscripcionCerrada(evento.isInscripcionCerrada())
        .plazasOcupadas(evento.getNumInscritos())
        .plazasLibres(evento.getPlazasLibres())
        .enListaEspera(evento.getNumEnEspera())
        .isCurrentUserInscrito(evento.isCurrentUserInscrito())
        .estadoInscripcionActual(evento.getEstadoInscripcionActual())
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
        .build();
  }
}
