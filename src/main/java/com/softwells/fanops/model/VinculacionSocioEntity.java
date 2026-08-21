package com.softwells.fanops.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Invitación pendiente para vincular una cuenta de usuario con fichas de socio que ya existen
 * en el listado de la peña (altas manuales del admin o importadas de Excel).
 *
 * <p>El correo no prueba por sí solo la identidad de quien se registra: si vinculásemos la ficha
 * en caliente, cualquiera que conociese un email del listado se apropiaría de esa ficha (con su
 * IBAN, sus cuotas y su carnet). Por eso el registro no vincula nada: crea una invitación con un
 * token aleatorio de un solo uso que se envía al correo de la ficha, y la vinculación solo ocurre
 * cuando alguien demuestra que controla ese buzón.
 */
@Entity
@Table(name = "vinculaciones_socio",
    indexes = @Index(name = "idx_vinculaciones_socio_email", columnList = "email"))
@Getter
@Setter
public class VinculacionSocioEntity {

  @Id
  @GeneratedValue
  private UUID uid;

  /**
   * SHA-256 (hex) del token que viaja en el enlace del correo. Se guarda el hash y no el token
   * en claro para que un volcado de la tabla no permita activar invitaciones ajenas.
   */
  @Column(nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(nullable = false)
  private String email;

  /**
   * Contraseña ya codificada que eligió la persona al registrarse. Es {@code null} cuando la
   * invitación no viene de un registro (p. ej. la que se envía desde "he olvidado mi
   * contraseña"): en ese caso la contraseña se pide al confirmar la vinculación.
   */
  private String password;

  @Column(nullable = false)
  private LocalDateTime fechaCreacion;

  @Column(nullable = false)
  private LocalDateTime fechaExpiracion;

  /** Fecha en la que se consumió la invitación; {@code null} mientras siga pendiente. */
  private LocalDateTime fechaUso;

  public boolean estaCaducada() {
    return fechaExpiracion.isBefore(LocalDateTime.now());
  }

  public boolean estaUsada() {
    return fechaUso != null;
  }
}
