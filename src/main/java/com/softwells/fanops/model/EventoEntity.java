package com.softwells.fanops.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Entity
@Table(name = "eventos")
@Data
public class EventoEntity {

  @Id
  @GeneratedValue
  private UUID uid;

  @Column(nullable = false)
  private String nombreEvento;

  @Column(nullable = false)
  private LocalDate fechaEvento;

  /** Fecha y hora límite para inscribirse. Si es null, el evento no tiene plazo de inscripción. */
  private LocalDateTime fechaLimiteInscripcion;

  private String ubicacion;

  @Column(length = 1000)
  private String descripcion;

  private Integer numeroPlazas;

  /**
   * Carnets que se sortean para este evento (null o 0 = el evento no sortea carnets). Es un
   * recurso aparte de {@code numeroPlazas}: el bus se reparte por orden y el carnet por sorteo.
   */
  private Integer plazasCarnet;

  /** Momento en que se celebra el sorteo de carnets. */
  private LocalDateTime fechaSorteoCarnet;

  /**
   * Lo que paga cada persona por la plaza del evento (el autobús, normalmente). Null significa
   * "sin indicar", que no es lo mismo que 0: un evento gratis se marca con 0 y se dice.
   */
  private BigDecimal costePlaza;

  /**
   * Lo que paga quien va con uno de los carnets sorteados. Es aparte del anterior porque suele
   * ser otra cifra: el carnet no lleva autobús, o la entrada cuesta distinto.
   */
  private BigDecimal costeCarnet;

  private BigDecimal costeTotalEstimado;

  private BigDecimal costeTotalReal;

  @JsonIgnore
  @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EventoInscripcionEntity> inscripciones = new ArrayList<>();

  @Transient // No se persiste en la base de datos
  @JsonProperty("isCurrentUserInscrito") // Forza este nombre en el JSON
  private boolean isCurrentUserInscrito;


  @Transient
  private boolean inscripcionCerrada;

  @Transient
  private int numInscritos;

  @Transient
  private int numEnEspera;

  /** Número de plazas libres (-1 significa ilimitado). */
  @Transient
  private int plazasLibres;

  /** true si el sorteo de carnets ya se ha celebrado. Solo se rellena en la vista de gestión. */
  @Transient
  private boolean sorteoCelebrado;

  /**
   * true si ya no se admiten inscripciones: se pasó el plazo o el evento fue antes de hoy. Vive
   * en la entidad porque lo consultan tanto las inscripciones como el sorteo de carnets, que
   * apunta al evento y necesita saber si eso todavía es posible.
   */
  public boolean plazoInscripcionCerrado() {
    if (fechaEvento != null && fechaEvento.isBefore(LocalDate.now())) {
      return true;
    }
    return fechaLimiteInscripcion != null
        && LocalDateTime.now().isAfter(fechaLimiteInscripcion);
  }

}
