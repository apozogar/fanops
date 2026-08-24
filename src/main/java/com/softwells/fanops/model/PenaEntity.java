package com.softwells.fanops.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pena")
public class PenaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String nombre;

  /**
   * Identificador de la peña en la URL: {@code https://fanops.example/mi-pena/auth/login}.
   *
   * Es lo que permite saber a qué peña se está accediendo antes de tener sesión, y por tanto lo
   * que decide a qué peña se asocia quien se registra. Sin él, el auto-registro público no tenía
   * forma de saberlo y todo el mundo acababa en la peña por defecto.
   *
   * Solo minúsculas, dígitos y guiones (ver PenaService.normalizarSlug), y único en toda la
   * aplicación, porque vive en el primer segmento de la ruta.
   */
  @Column(nullable = false, unique = true, length = 60)
  private String slug;

  private String iniciadorId;
  private String direccion1;
  private String direccion2;
  private String cuentaIban;
  private String cuentaBic;
  private Double cuotaAdulto;
  private Double cuotaMenor;
  private Integer edadMayoria;
  private Integer edadJubilacion;

  /**
   * Cuántas inscripciones posteriores van forzadas a lista de espera por cada falta. 0 desactiva
   * las penalizaciones sin dejar de registrar las faltas.
   */
  private Integer eventosPenalizacionPorFalta;
  @Column(columnDefinition = "TEXT")
  private String logo;
  private String lema;
  private String color;

  @OneToMany(mappedBy = "pena", cascade = CascadeType.ALL)
  private Set<SocioEntity> socios = new HashSet<>();
}
