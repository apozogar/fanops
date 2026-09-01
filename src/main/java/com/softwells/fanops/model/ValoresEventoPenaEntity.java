package com.softwells.fanops.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Valores con los que se rellena un evento nuevo en una peña.
 *
 * <p>Casi todos los eventos de una peña se parecen: el mismo autobús, el mismo precio y los
 * mismos carnets a sortear. Guardarlos aquí evita teclearlos cada vez y, sobre todo, evita las
 * erratas de teclearlos cada vez.
 *
 * <p>Son solo sugerencias: se copian al formulario al crear el evento y a partir de ahí el
 * evento vive por su cuenta. Cambiar estos valores no toca ningún evento ya creado.
 *
 * <p>Cada campo es nullable porque "sin valor por defecto" es una opción legítima: el campo del
 * formulario se queda vacío y lo rellena quien crea el evento.
 */
@Entity
@Table(name = "pena_valores_evento")
@Getter
@Setter
public class ValoresEventoPenaEntity {

  @Id
  @GeneratedValue
  private UUID uid;

  @JsonIgnore
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pena_id", nullable = false, unique = true)
  private PenaEntity pena;

  /** Plazas del evento (el autobús, normalmente). */
  private Integer plazas;

  /** Lo que paga cada socio por su plaza. */
  private BigDecimal costePlaza;

  /** Carnets que se sortean. */
  private Integer carnets;

  /** Lo que paga quien se lleva un carnet. */
  private BigDecimal costeCarnet;

  /** Coste total estimado del evento, para las cuentas de la peña. */
  private BigDecimal costeTotalEstimado;

  /*
   * Las fechas se guardan relativas a la del evento, no absolutas: lo que se repite de un partido
   * a otro no es "el 8 de septiembre" sino "dos días antes, a las ocho". Se calculan en el
   * formulario en cuanto se elige la fecha del evento, que es cuando ya hay de qué restar.
   */

  /** Días antes del evento en que cierra la inscripción. */
  private Integer diasAntesFinInscripcion;

  /** Hora a la que cierra la inscripción ese día. */
  private LocalTime horaFinInscripcion;

  /** Días antes del evento en que se celebra el sorteo de carnets. */
  private Integer diasAntesSorteo;

  /** Hora a la que se celebra el sorteo ese día. */
  private LocalTime horaSorteo;
}
