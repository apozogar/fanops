package com.softwells.fanops.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.softwells.fanops.enums.EstadoSorteo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Sorteo de los carnets de un evento. Hay como mucho uno por evento.
 *
 * <p>La semilla se genera al programar el sorteo y no se vuelve a tocar: es lo que hace que el
 * resultado sea reproducible y, sobre todo, comprobable. Hasta que el sorteo se celebra solo se
 * publica su hash ({@code hashSemilla}); al celebrarse se revela la semilla, de modo que
 * cualquiera puede verificar que no se cambió después de ver quién se había apuntado.
 */
@Entity
@Table(name = "sorteos_carnet")
@Getter
@Setter
public class SorteoCarnetEntity {

  @Id
  @GeneratedValue
  private UUID uid;

  @JsonIgnore
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "evento_uid", nullable = false, unique = true)
  private EventoEntity evento;

  /** Semilla en hexadecimal. No se expone hasta que el sorteo está EJECUTADO. */
  @Column(nullable = false, length = 64)
  private String semilla;

  /** SHA-256 de la semilla, publicado desde el primer momento. */
  @Column(nullable = false, length = 64)
  private String hashSemilla;

  /** Carnets que se reparten. Se congela al ejecutar, aunque luego cambie el evento. */
  @Column(nullable = false)
  private int numeroCarnets;

  @Column(nullable = false)
  private LocalDateTime fechaProgramada;

  private LocalDateTime fechaEjecucion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoSorteo estado = EstadoSorteo.PROGRAMADO;

  public boolean estaEjecutado() {
    return estado == EstadoSorteo.EJECUTADO;
  }
}
