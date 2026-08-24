package com.softwells.fanops.controller.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String nombre;
    private String email;
    private String password;

    /**
     * Dominio de la peña desde la que se está registrando, tal como venía en la URL
     * (https://fanops.example/{penaSlug}/auth/register).
     *
     * Puede llegar vacío cuando alguien entra por la raíz de la aplicación, y entonces se cae
     * a la peña por defecto. Antes no existía este dato y todo el mundo acababa en esa peña por
     * defecto, fuese la suya o no.
     */
    private String penaSlug;

}