package com.softwells.fanops.service;

import java.util.UUID;

/**
 * Se ha celebrado el sorteo de carnets de un evento con plazas reservadas. Lo escucha
 * {@link EventoService} para repartir entre la lista de espera las plazas que no han hecho falta.
 * Es un evento, y no una llamada directa, porque los dos servicios ya dependen en un sentido.
 */
record SorteoCelebradoEvent(UUID eventoUid) {
}
