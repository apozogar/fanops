package com.softwells.fanops.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwells.fanops.enums.EstadoInscripcion;
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

  private BigDecimal costeTotalEstimado;

  private BigDecimal costeTotalReal;

  @JsonIgnore
  @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EventoInscripcionEntity> inscripciones = new ArrayList<>();

  @Transient // No se persiste en la base de datos
  @JsonProperty("isCurrentUserInscrito") // Forza este nombre en el JSON
  private boolean isCurrentUserInscrito;

  @Transient
  private EstadoInscripcion estadoInscripcionActual;

  @Transient
  private boolean inscripcionCerrada;

  @Transient
  private int numInscritos;

  @Transient
  private int numEnEspera;

  /** Número de plazas libres (-1 significa ilimitado). */
  @Transient
  private int plazasLibres;

}
