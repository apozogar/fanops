package com.softwells.fanops.service;

import com.softwells.fanops.controller.dto.VinculacionInfoDto;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.RoleEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.model.VinculacionSocioEntity;
import com.softwells.fanops.repository.RoleRepository;
import com.softwells.fanops.repository.SocioRepository;
import com.softwells.fanops.repository.UsuarioRepository;
import com.softwells.fanops.repository.VinculacionSocioRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vinculación de una cuenta nueva con fichas de socio que ya existen en el listado de la peña,
 * confirmada por un token enviado al correo de la ficha.
 *
 * <p>El correo no prueba por sí solo la identidad de quien se registra: si vinculásemos la ficha
 * en caliente, cualquiera que conociese un email del listado se apropiaría de esa ficha (con su
 * IBAN, sus cuotas y su carnet). Por eso el registro no vincula nada: crea una invitación con un
 * token aleatorio de un solo uso que se envía al correo de la ficha, y la vinculación solo ocurre
 * cuando alguien demuestra que controla ese buzón.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VinculacionSocioService {

  /** Bytes de entropía del token del enlace. */
  private static final int BYTES_TOKEN = 32;

  private static final SecureRandom ALEATORIO = new SecureRandom();

  private final VinculacionSocioRepository vinculacionRepository;
  private final SocioRepository socioRepository;
  private final UsuarioRepository usuarioRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JavaMailSender mailSender;

  @Value("${mail.from.address}")
  private String fromAddress;

  @Value("${app.public-base-url:http://localhost:4200}")
  private String publicBaseUrl;

  @Value("${app.vinculacion.expiracion-horas:48}")
  private long horasValidez;

  /**
   * true si ese email corresponde a fichas de socio del listado que todavía no tienen cuenta, es
   * decir, si el registro debe pedir confirmación por correo en lugar de crear una ficha nueva.
   *
   * <p>Si ya existe un usuario con ese email no hay nada que vincular: el registro devolverá el
   * 409 de siempre y quien no recuerde su contraseña tiene "he olvidado mi contraseña".
   */
  @Transactional(readOnly = true)
  public boolean tieneSociosVinculables(String email) {
    if (StringUtils.isBlank(email)) {
      return false;
    }
    if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
      return false;
    }
    return !socioRepository.findByEmailIgnoreCaseAndUsuarioIsNull(email).isEmpty();
  }

  /**
   * Crea la invitación y envía el enlace al correo indicado.
   *
   * @param passwordEnClaro contraseña elegida al registrarse, o {@code null} si la invitación no
   *                        viene de un registro (entonces se pedirá al confirmar el enlace)
   * @param baseUrl         origen del frontend para construir el enlace, o {@code null} para usar
   *                        {@code app.public-base-url}
   */
  public void enviarInvitacion(String email, String passwordEnClaro, String baseUrl) {
    if (StringUtils.isBlank(email)) {
      return;
    }

    List<SocioEntity> socios = socioRepository.findByEmailIgnoreCaseAndUsuarioIsNull(email);
    if (socios.isEmpty()) {
      // No es un error: se llama desde flujos que no deben revelar si el email existe.
      log.debug("No hay fichas de socio vinculables para el email indicado.");
      return;
    }

    // Solo puede haber una invitación viva por email: al pedir una nueva, las anteriores dejan
    // de valer (evita que un enlace antiguo, quizá reenviado a otra persona, siga sirviendo).
    vinculacionRepository.deleteAll(
        vinculacionRepository.findByEmailIgnoreCaseAndFechaUsoIsNull(email));

    String token = generarToken();
    VinculacionSocioEntity invitacion = new VinculacionSocioEntity();
    invitacion.setTokenHash(hash(token));
    invitacion.setEmail(email);
    invitacion.setPassword(
        StringUtils.isBlank(passwordEnClaro) ? null : passwordEncoder.encode(passwordEnClaro));
    invitacion.setFechaCreacion(LocalDateTime.now());
    invitacion.setFechaExpiracion(LocalDateTime.now().plusHours(horasValidez));
    vinculacionRepository.save(invitacion);

    enviarCorreo(email, token, socios, baseUrl);
  }

  /**
   * Datos de la invitación para la pantalla de confirmación. Devolverlos solo a quien presenta el
   * token es seguro (el token es el secreto) y permite que la pantalla diga con qué ficha se va a
   * vincular la cuenta antes de confirmar.
   */
  @Transactional(readOnly = true)
  public VinculacionInfoDto consultar(String token) {
    VinculacionSocioEntity invitacion = obtenerInvitacionValida(token);
    List<SocioEntity> socios = obtenerSociosAVincular(invitacion.getEmail());
    SocioEntity principal = socios.get(0);

    return new VinculacionInfoDto(
        invitacion.getEmail(),
        principal.getNombre(),
        principal.getNumeroSocio(),
        principal.getPena() != null ? principal.getPena().getNombre() : null,
        socios.size(),
        invitacion.getPassword() == null);
  }

  /**
   * Consume la invitación: crea el usuario y le vincula las fichas de socio que ya existían.
   *
   * @param passwordEnClaro contraseña a establecer; obligatoria solo si la invitación no la traía
   * @return el usuario ya creado y vinculado, listo para generar su JWT
   */
  public UsuarioEntity confirmar(String token, String passwordEnClaro) {
    VinculacionSocioEntity invitacion = obtenerInvitacionValida(token);
    String email = invitacion.getEmail();

    // Entre el envío del correo y la confirmación pueden haber creado la cuenta por otra vía.
    if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
      throw new IllegalArgumentException(
          "Ya existe una cuenta con ese email. Inicia sesión o usa la opción de recuperar "
              + "contraseña.");
    }

    String passwordCodificada = invitacion.getPassword();
    if (passwordCodificada == null) {
      if (StringUtils.isBlank(passwordEnClaro)) {
        throw new IllegalArgumentException("Debes establecer una contraseña para tu cuenta.");
      }
      passwordCodificada = passwordEncoder.encode(passwordEnClaro);
    }

    List<SocioEntity> socios = obtenerSociosAVincular(email);
    PenaEntity pena = socios.get(0).getPena();

    RoleEntity rolUsuario = roleRepository.findByName("ROLE_USER")
        .orElseGet(() -> roleRepository.save(new RoleEntity("ROLE_USER")));

    UsuarioEntity usuario = new UsuarioEntity();
    usuario.setEmail(email);
    usuario.setPassword(passwordCodificada);
    usuario.setActivo(true);
    usuario.setRoles(Set.of(rolUsuario));
    // La peña la manda la ficha que ya existía, no la peña por defecto del auto-registro.
    usuario.setPena(pena);
    UsuarioEntity guardado = usuarioRepository.save(usuario);

    socios.forEach(socio -> socio.setUsuario(guardado));
    socioRepository.saveAll(socios);

    invitacion.setFechaUso(LocalDateTime.now());
    vinculacionRepository.save(invitacion);

    log.info("Vinculadas {} fichas de socio de la peña '{}' a la nueva cuenta.", socios.size(),
        pena != null ? pena.getNombre() : "sin peña");
    return guardado;
  }

  private VinculacionSocioEntity obtenerInvitacionValida(String token) {
    if (StringUtils.isBlank(token)) {
      throw new IllegalArgumentException("El enlace no es válido.");
    }
    VinculacionSocioEntity invitacion = vinculacionRepository.findByTokenHash(hash(token))
        .orElseThrow(() -> new IllegalArgumentException("El enlace no es válido."));
    if (invitacion.estaUsada()) {
      throw new IllegalArgumentException(
          "Este enlace ya se ha usado. Inicia sesión con tu email y contraseña.");
    }
    if (invitacion.estaCaducada()) {
      throw new IllegalArgumentException(
          "El enlace ha caducado. Vuelve a registrarte para recibir uno nuevo.");
    }
    return invitacion;
  }

  /**
   * Fichas que se vinculan a la cuenta: las que llevan ese email y siguen sin usuario. Si hay
   * fichas en varias peñas (raro: implicaría que la misma persona está en dos listados) se
   * vinculan solo las de una, porque un usuario pertenece a una única peña.
   */
  private List<SocioEntity> obtenerSociosAVincular(String email) {
    List<SocioEntity> socios = socioRepository.findByEmailIgnoreCaseAndUsuarioIsNull(email);
    if (socios.isEmpty()) {
      throw new IllegalArgumentException(
          "Ya no hay fichas de socio pendientes de vincular para ese email.");
    }

    Long penaId = idDePena(socios.get(0));
    List<SocioEntity> mismaPena = socios.stream()
        .filter(socio -> Objects.equals(penaId, idDePena(socio)))
        .toList();
    if (mismaPena.size() != socios.size()) {
      log.warn("El email tiene fichas en varias peñas; se vinculan solo las {} de la peña {}.",
          mismaPena.size(), penaId);
    }
    return mismaPena;
  }

  private Long idDePena(SocioEntity socio) {
    return socio.getPena() != null ? socio.getPena().getId() : null;
  }

  private void enviarCorreo(String email, String token, List<SocioEntity> socios,
      String baseUrl) {
    String base = StringUtils.isNotBlank(baseUrl) ? baseUrl : publicBaseUrl;
    // El "/#/" es obligatorio: el frontend enruta por hash (withHashLocation), así que sin él
    // la ruta no la ve el router de Angular y el enlace acaba en el login, perdiendo el token.
    String enlace = base + "/#/auth/vincular-socio?token=" + token;
    SocioEntity principal = socios.get(0);
    String nombrePena = principal.getPena() != null ? principal.getPena().getNombre() : "la peña";

    StringBuilder cuerpo = new StringBuilder()
        .append("Hola ").append(principal.getNombre()).append(",\n\n")
        .append("Hemos recibido una solicitud de registro con este correo, y ya figura en el ")
        .append("listado de socios de ").append(nombrePena)
        .append(" (ficha nº ").append(principal.getNumeroSocio()).append(")");
    if (socios.size() > 1) {
      cuerpo.append(", junto con ").append(socios.size() - 1)
          .append(" ficha(s) más asociada(s) a este email");
    }
    cuerpo.append(".\n\n")
        .append("Para confirmar que eres tú y vincular tu ficha a tu nueva cuenta, abre este ")
        .append("enlace:\n").append(enlace).append("\n\n")
        .append("El enlace caduca en ").append(horasValidez)
        .append(" horas y solo puede usarse una vez.\n")
        .append("Si no has solicitado el registro, ignora este correo: no se hará ningún cambio ")
        .append("en tu ficha.\n");

    try {
      SimpleMailMessage mensaje = new SimpleMailMessage();
      mensaje.setFrom(fromAddress);
      mensaje.setTo(email);
      mensaje.setSubject("Vincula tu cuenta con tu ficha de socio");
      mensaje.setText(cuerpo.toString());
      mailSender.send(mensaje);
    } catch (Exception e) {
      log.error("Error al enviar el correo de vinculación de socio a {}", email, e);
    }
  }

  private String generarToken() {
    byte[] bytes = new byte[BYTES_TOKEN];
    ALEATORIO.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hash(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      // SHA-256 es obligatorio en cualquier JRE, así que esto no puede ocurrir.
      throw new IllegalStateException("No se pudo calcular el hash del token", e);
    }
  }
}
