package com.softwells.fanops.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Un intento de inicio de sesión, correcto o no. Es lo que permite al superadmin ver quién ha
 * entrado en la aplicación (y quién lo ha intentado sin conseguirlo), a diferencia de
 * {@link UsuarioEntity#getUltimoAcceso()}, que solo guarda el último acceso correcto y se
 * sobrescribe cada vez.
 *
 * <p>Se guarda el email escrito en el intento y no una referencia a {@link UsuarioEntity}: así
 * el historial también recoge los intentos con un email que no existe, y sobrevive si la cuenta
 * se llega a borrar.
 */
@Entity
@Table(name = "historial_accesos")
@Getter
@Setter
public class HistorialAccesoEntity {

  @Id
  @GeneratedValue
  private UUID uid;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private boolean exito;

  @Column(nullable = false)
  private LocalDateTime fecha;

  private String ip;

  private String userAgent;
}
