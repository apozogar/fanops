package com.softwells.fanops.controller.dto;

import com.softwells.fanops.model.SocioEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resultado del registro público. Hay dos desenlaces posibles:
 *
 * <ul>
 *   <li>el email no estaba en el listado de socios: se crea la cuenta y su ficha al momento
 *       ({@code requiereVerificacion = false}, {@code socio} con la ficha creada);</li>
 *   <li>el email ya figuraba en el listado: no se crea nada todavía y se envía un correo para
 *       confirmar la vinculación con la ficha existente ({@code requiereVerificacion = true}).</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {

  private boolean requiereVerificacion;

  private SocioEntity socio;
}
