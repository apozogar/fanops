package com.softwells.fanops.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "usuarios")
@Data
public class UsuarioEntity implements UserDetails {

  @Id
  @GeneratedValue
  private UUID uid;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private boolean activo = true;

  @JsonManagedReference
  @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<SocioEntity> socios = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "usuario_roles",
      joinColumns = @JoinColumn(name = "usuario_uid"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<RoleEntity> roles = new HashSet<>();

  /**
   * Peña a la que pertenece este usuario. Un usuario trabaja siempre sobre una única peña (no
   * tiene sentido operativo pertenecer a varias); los servicios que antes asumían la peña con
   * id=1 ahora resuelven la peña "de trabajo" a partir de este campo. Puede ser null para el
   * superadmin, que no está atado a ninguna peña en concreto.
   */
  // EAGER a propósito: se lee en el login (para el claim "clubId" del JWT, fuera de cualquier
  // transacción) y en casi cualquier operación de socios/cuotas/cobros, así que perezoso daría
  // LazyInitializationException en varios de esos sitios. Es una entidad pequeña, así que el
  // coste de traerla siempre es asumible.
  @JsonIgnore
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "pena_id")
  private PenaEntity pena;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream()
        .map(role -> new SimpleGrantedAuthority(role.getName()))
        .collect(Collectors.toList());
  }

  public void addRole(RoleEntity role) {
    this.roles.add(role);
  }

  public void removeRole(RoleEntity role) {
    this.roles.remove(role);
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return this.activo;
  }
}