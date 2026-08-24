package com.softwells.fanops.controller.dto;

/**
 * Identidad de una peña, para pintar el login y el registro antes de que exista sesión.
 *
 * Es deliberadamente un DTO mínimo y no la PenaEntity: este endpoint es público, y la entidad
 * arrastra la cuenta bancaria, el importe de las cuotas y la colección de socios, que no puede
 * ver quien todavía no se ha identificado.
 *
 * @param nombre nombre de la peña, para la cabecera de la pantalla
 * @param slug   su dominio, para poder construir los enlaces entre pantallas
 * @param logo   logo en data URI o URL, o {@code null} si la peña no tiene
 * @param lema   lema de la peña, o {@code null}
 * @param color  color de acento con el que se tiñe la interfaz, o {@code null} para el de serie
 */
public record PenaPublicaDto(
    String nombre,
    String slug,
    String logo,
    String lema,
    String color) {
}
