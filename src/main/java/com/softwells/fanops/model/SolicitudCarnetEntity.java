package com.softwells.fanops.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.softwells.fanops.enums.EstadoSolicitudCarnet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Un socio apuntado al bombo de un evento. Es independiente de {@link EventoInscripcionEntity}:
 * la plaza de bus y el carnet son dos cosas distintas y uno no condiciona al otro.
 */
@Entity
@Table(name = "solicitudes_carnet",
    uniqueConstraints = @UniqueConstraint(name = "uk_solicitud_carnet_evento_socio",
        columnNames = {"evento_uid", "socio_uid"}))
@Getter
@Setter
public class SolicitudCarnetEntity {

  @Id
  @GeneratedValue
  private UUID uid;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "evento_uid", nullable = false)
  private EventoEntity evento;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "socio_uid")
  private SocioEntity socio;

  /** Datos de quien entra sin ser socio (socio == null); el resultado se le avisa por email. */
  private String nombreInvitado;
  private String emailInvitado;
  private String telefonoInvitado;

  @Column(nullable = false)
  private LocalDateTime fechaSolicitud;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoSolicitudCarnet estado = EstadoSolicitudCarnet.PENDIENTE;

  /** Orden de salida del bombo, 1..N. Null mientras el sorteo no se ha celebrado. */
  private Integer posicionSorteo;

  /**
   * Papeletas con las que entró al bombo. Se guarda con el resultado, y no se recalcula al
   * mostrarlo, porque depende del historial del socio y ese historial sigue creciendo: sin
   * congelarlo, un sorteo antiguo dejaría de cuadrar con sus propios números.
   */
  private Integer pesoSorteo;

  /**
   * Papeletas que la gestión ha añadido a mano, además de las que da el historial. Solo cuentan
   * si la peña del socio tiene activada la opción; null equivale a ninguna.
   */
  private Integer papeletasExtra;

  /** true si quien participa no es socio. */
  public boolean esInvitado() {
    return socio == null;
  }

  public String nombreParticipante() {
    return socio != null ? socio.getNombre() : nombreInvitado;
  }

  /** Email al que avisar: el de la ficha o, si no tiene, el de su cuenta. */
  public String emailParticipante() {
    if (socio == null) {
      return emailInvitado;
    }
    if (socio.getEmail() != null && !socio.getEmail().isBlank()) {
      return socio.getEmail();
    }
    return socio.getUsuario() != null ? socio.getUsuario().getEmail() : null;
  }

  public String telefonoParticipante() {
    return socio != null ? socio.getTelefono() : telefonoInvitado;
  }

  /** true si llegó a tener el carnet, aunque después renunciara. */
  public boolean fuePremiada() {
    return estado == EstadoSolicitudCarnet.GANADORA || estado == EstadoSolicitudCarnet.RENUNCIADA;
  }
}
