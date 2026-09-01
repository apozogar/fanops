package com.softwells.fanops.controller.dto;

import java.util.List;

/**
 * Resultado de entrar en el bombo del carnet, que apunta también al evento.
 *
 * <p>Van juntos porque son una sola acción del socio pero dos respuestas distintas: el sorteo es
 * lo que se pinta, y la inscripción es lo que hay que contarle (puede haber entrado en lista de
 * espera si el evento estaba completo).
 *
 * @param sorteo        estado del bombo tras entrar
 * @param inscripciones plaza conseguida por cada ficha que no estuviera ya apuntada al evento
 */
public record ApuntarSorteoResultado(SorteoCarnetDTO sorteo,
                                     List<SocioInscripcionDTO> inscripciones) {
}
