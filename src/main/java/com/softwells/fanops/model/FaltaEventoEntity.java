package com.softwells.fanops.model;

import com.softwells.fanops.enums.MotivoFalta;
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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Falta de un socio en un evento: dijo que iba y no fue, o anuló su plaza fuera de plazo.
 *
 * <p>Es una entidad aparte de la inscripción porque una cancelación tardía borra la inscripción y
 * la falta tiene que sobrevivirla. Las filas se conservan cuando la penalización ya se ha
 * cumplido, para que quede el historial; solo desaparecen si gestión retira la falta o si alguien
 * ocupa la plaza que dejó libre una cancelación tardía.
 */
@Entity
@Table(name = "evento_faltas")
@Getter
@Setter
public class FaltaEventoEntity {

  @Id
  @GeneratedValue
  private UUID uid;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "socio_uid", nullable = false)
  private SocioEntity socio;

  /** Evento en el que se produjo la falta. */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "evento_uid", nullable = false)
  private EventoEntity evento;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MotivoFalta motivo;

  @Column(nullable = false)
  private LocalDateTime fechaRegistro;

  /**
   * Inscripciones que todavía irán forzadas a lista de espera por esta falta. Se descuenta una
   * cada vez que penaliza. A cero la falta sigue en el historial pero ya no castiga.
   */
  @Column(nullable = false)
  private int penalizacionesRestantes;
}
