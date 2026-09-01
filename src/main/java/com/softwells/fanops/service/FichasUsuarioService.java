package com.softwells.fanops.service;

import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Fichas de socio del usuario autenticado. Vive aparte porque la usan tanto las inscripciones a
 * eventos como el sorteo de carnets, y en las dos hace falta lo mismo: saber qué fichas gestiona
 * la cuenta y comprobar que no está operando sobre una ajena.
 */
@Service
@RequiredArgsConstructor
public class FichasUsuarioService {

  private final UsuarioRepository usuarioRepository;

  /** Fichas de socio del usuario autenticado. */
  public List<SocioEntity> misFichas() {
    String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
    UsuarioEntity usuario = usuarioRepository.findByEmailIgnoreCase(userEmail)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    return ordenar(usuario);
  }

  /**
   * Igual que {@link #misFichas()}, pero con la lista vacía si no hay nadie autenticado. Lo usan
   * las vistas que también se construyen fuera de una petición de usuario, como el sorteo cuando
   * lo celebra el planificador.
   */
  public List<SocioEntity> misFichasOVacio() {
    Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
    if (autenticacion == null || !autenticacion.isAuthenticated()
        || "anonymousUser".equals(autenticacion.getPrincipal())) {
      return List.of();
    }
    return usuarioRepository.findByEmailIgnoreCase(autenticacion.getName())
        .map(this::ordenar)
        .orElseGet(List::of);
  }

  /**
   * Resuelve y valida las fichas de una petición. Todas deben pertenecer al usuario autenticado,
   * que es lo que impide operar sobre fichas ajenas pasando uids a mano. Si no se indica ninguna
   * y el usuario tiene una sola, se asume esa.
   *
   * @param accion verbo para el mensaje de error cuando hay varias fichas y no se concreta
   */
  public List<SocioEntity> resolver(List<UUID> solicitados, String accion) {
    List<SocioEntity> misFichas = misFichas();
    if (misFichas.isEmpty()) {
      throw new IllegalStateException("El usuario no tiene ninguna ficha de socio asociada.");
    }

    if (solicitados == null || solicitados.isEmpty()) {
      if (misFichas.size() > 1) {
        throw new IllegalArgumentException(
            "Indica a quién quieres " + accion + ": tu cuenta tiene varias fichas de socio.");
      }
      return List.of(misFichas.get(0));
    }

    Map<UUID, SocioEntity> porUid = new LinkedHashMap<>();
    misFichas.forEach(socio -> porUid.put(socio.getUid(), socio));

    List<SocioEntity> resueltos = new ArrayList<>();
    for (UUID socioUid : solicitados.stream().distinct().collect(Collectors.toList())) {
      SocioEntity socio = porUid.get(socioUid);
      if (socio == null) {
        throw new IllegalArgumentException("Esa ficha de socio no pertenece a tu cuenta.");
      }
      resueltos.add(socio);
    }
    return resueltos;
  }

  /**
   * Orden estable de las fichas. El orden importa: {@code socios} es un Set, así que sin
   * ordenar el "primer socio" varía entre llamadas.
   */
  private List<SocioEntity> ordenar(UsuarioEntity usuario) {
    return usuario.getSocios().stream()
        .sorted(Comparator
            .comparing(SocioEntity::getNumeroSocio, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(SocioEntity::getNombre, Comparator.nullsLast(Comparator.naturalOrder())))
        .collect(Collectors.toList());
  }
}
