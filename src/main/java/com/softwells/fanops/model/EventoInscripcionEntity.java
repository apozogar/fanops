package com.softwells.fanops.model;

import com.softwells.fanops.enums.EstadoInscripcion;
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

@Entity
@Table(name = "evento_inscripciones")
@Getter
@Setter
public class EventoInscripcionEntity {

  @Id
  @GeneratedValue
  private UUID uid;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "evento_uid", nullable = false)
  private EventoEntity evento;

  /** Socio inscrito. Es null cuando la inscripción viene del enlace público de no socios. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "socio_uid")
  private SocioEntity socio;

  private String nombre;

  private String email;

  private String telefono;

  @Column(nullable = false)
  private LocalDateTime fechaInscripcion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoInscripcion estado;

  /** true si el inscrito era socio prioritario (activo y con cuota al día) al inscribirse. */
  private boolean socioPrioritario;
}
