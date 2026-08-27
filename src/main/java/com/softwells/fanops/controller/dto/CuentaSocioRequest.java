package com.softwells.fanops.controller.dto;

import lombok.Data;

/**
 * Alta manual de la cuenta de acceso de un socio, hecha por un administrador desde el listado de
 * socios. Es la vía para los socios que no se van a registrar por su cuenta: el administrador les
 * elige la contraseña y se la comunica por el canal que use la peña.
 */
@Data
public class CuentaSocioRequest {

  /** Contraseña en claro que tendrá la cuenta. Mínimo 8 caracteres. */
  private String password;

  /** true para que la cuenta pueda además gestionar la peña (ROLE_ADMIN). */
  private boolean admin;
}
