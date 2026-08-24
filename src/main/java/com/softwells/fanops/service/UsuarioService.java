package com.softwells.fanops.service;

import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.UsuarioRepository;
import com.softwells.fanops.security.PenaContextHolder;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

  private static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";

  private final UsuarioRepository usuarioRepository;
  private final PenaService penaService;

  /**
   * Deja constancia de que la cuenta acaba de entrar en la aplicación. Se llama tras un login
   * correcto y tras confirmar una vinculación, que también deja la sesión iniciada. Es
   * deliberadamente silencioso: que no se pueda sellar el acceso no debe impedir entrar.
   */
  @Transactional
  public void registrarAcceso(String email) {
    usuarioRepository.registrarAcceso(email, LocalDateTime.now());
  }

  public UsuarioEntity obtenerUsuarioAutenticado() {
    String userEmail = Objects.requireNonNull(
        SecurityContextHolder.getContext().getAuthentication()).getName();
    return usuarioRepository.findByEmailIgnoreCase(userEmail)
        .orElseThrow(() -> new EntityNotFoundException(
            "No se encontró un usuario con el email: " + userEmail));
  }

  /**
   * Peña "de trabajo" para la petición actual: la peña sobre la que operan las funciones de
   * socios/cuotas/cobros/SEPA. Sustituye al antiguo id=1 hardcodeado.
   *
   * <p>Para un usuario normal es siempre su propia peña ({@code UsuarioEntity.pena}), ya que no
   * tiene sentido que pertenezca a varias. El superadmin no pertenece a ninguna peña en
   * concreto, así que su peña de trabajo es la que haya elegido en el selector del panel
   * (enviada como cabecera {@code X-Pena-Id} y capturada en {@link PenaContextHolder}).
   */
  public PenaEntity obtenerPenaDelUsuarioAutenticado() {
    UsuarioEntity usuario = obtenerUsuarioAutenticado();
    boolean esSuperAdmin = usuario.getAuthorities().stream()
        .anyMatch(a -> ROLE_SUPERADMIN.equals(a.getAuthority()));

    if (esSuperAdmin) {
      Long penaSeleccionadaId = PenaContextHolder.get();
      if (penaSeleccionadaId == null) {
        throw new IllegalStateException(
            "Selecciona una peña en el panel de superadmin para ver o gestionar sus datos.");
      }
      return penaService.findById(penaSeleccionadaId);
    }

    PenaEntity pena = usuario.getPena();
    if (pena == null) {
      throw new EntityNotFoundException(
          "El usuario '" + usuario.getEmail() + "' no tiene una peña asignada.");
    }
    return pena;
  }
}