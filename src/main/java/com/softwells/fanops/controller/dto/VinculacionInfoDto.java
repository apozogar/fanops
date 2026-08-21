package com.softwells.fanops.controller.dto;

/**
 * Datos que muestra la pantalla de confirmación de vinculación antes de crear la cuenta, para que
 * la persona vea con qué ficha de socio se va a vincular.
 *
 * @param email            correo de la invitación
 * @param nombreSocio      nombre de la ficha principal
 * @param numeroSocio      número de la ficha principal
 * @param nombrePena       peña a la que pertenece la ficha
 * @param fichas           número de fichas que se vincularán (una persona puede tener varias)
 * @param requierePassword true si la invitación no traía contraseña y hay que pedirla
 */
public record VinculacionInfoDto(
    String email,
    String nombreSocio,
    Integer numeroSocio,
    String nombrePena,
    int fichas,
    boolean requierePassword) {

}
