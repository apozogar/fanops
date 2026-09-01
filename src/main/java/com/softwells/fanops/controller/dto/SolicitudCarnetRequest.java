package com.softwells.fanops.controller.dto;

import java.util.List;
import java.util.UUID;
import lombok.Data;

/** Petición para meter una o varias fichas del usuario en el bombo de un evento. */
@Data
public class SolicitudCarnetRequest {

  /**
   * Fichas a apuntar. Todas deben pertenecer al usuario autenticado. Si se omite y el usuario
   * tiene una única ficha, se apunta esa.
   */
  private List<UUID> socioUids;

  /**
   * Se traslada tal cual a la inscripción al evento que arrastra el sorteo: si es true y no caben
   * todos, el grupo entero va a la lista de espera en vez de partirse. No afecta al bombo, donde
   * cada ficha entra por su cuenta.
   */
  private boolean soloSiEntranTodos;
}
