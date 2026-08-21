package com.softwells.fanops.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
// El índice por email lo usa la búsqueda de fichas vinculables al registrarse (el email no es
// único: una misma persona puede tener varias fichas, p. ej. las de su familia).
@Table(name = "socios", indexes = @Index(name = "idx_socios_email", columnList = "email"))
@Getter
@Setter
public class SocioEntity {

  @EqualsAndHashCode.Include
  @ToString.Include
  @Id
  @GeneratedValue
  private UUID uid;

  @Column(unique = true, nullable = false)
  private Integer numeroSocio;

  @Column(nullable = false)
  private String nombre;

  private LocalDate fechaNacimiento;

  // Eliminamos la restricción 'unique' del DNI y email
  private String dni;
  private String direccion;
  private String poblacion;
  private String provincia;
  private String codigoPostal;
  private String telefono;
  private String email;

  @Column(nullable = false)
  private LocalDate fechaAlta;

  @Column(nullable = false)
  private boolean activo;

  @Column(nullable = false)
  private boolean abonadoBetis;

  @Column(nullable = false)
  private boolean accionistaBetis;

  @Column(nullable = false)
  private boolean exentoPago = false;

  private String numeroCuenta;

  @Column(length = 1000)
  private String observaciones;

  private String mandateId;

  private LocalDate mandateSignatureDate;

  @JsonIgnore
  @OneToMany(mappedBy = "socio", cascade = CascadeType.ALL)
  private Set<CuotaEntity> cuotas = new HashSet<>();

  @JsonBackReference
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_uid")
  private UsuarioEntity usuario;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pena_id")
  private PenaEntity pena;
}
