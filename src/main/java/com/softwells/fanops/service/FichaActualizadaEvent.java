package com.softwells.fanops.service;

import java.util.UUID;

/**
 * Se ha creado o modificado la ficha de un socio. Lo escucha {@link EventoService} para que las
 * inscripciones de eventos próximos reflejen la ficha: es un evento, y no una llamada directa,
 * para que el servicio de socios no tenga que conocer el de eventos.
 */
record FichaActualizadaEvent(UUID socioUid) {
}
