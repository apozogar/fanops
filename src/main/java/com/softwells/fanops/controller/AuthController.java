package com.softwells.fanops.controller;

import com.softwells.fanops.controller.dto.ApiResponse;
import com.softwells.fanops.controller.dto.AuthRequest;
import com.softwells.fanops.controller.dto.AuthResponse;
import com.softwells.fanops.controller.dto.ConfirmarVinculacionRequest;
import com.softwells.fanops.controller.dto.ForgotPasswordRequest;
import com.softwells.fanops.controller.dto.RegisterRequest;
import com.softwells.fanops.controller.dto.RegisterResponse;
import com.softwells.fanops.controller.dto.ResetPasswordRequest;
import com.softwells.fanops.controller.dto.VinculacionInfoDto;
import com.softwells.fanops.exception.EmailAlreadyExistsException;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.UsuarioRepository;
import com.softwells.fanops.security.JwtService;
import com.softwells.fanops.service.SocioService;
import com.softwells.fanops.service.VinculacionSocioService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final SocioService socioService;
  private final VinculacionSocioService vinculacionSocioService;
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final JwtService jwtService;
  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;
  private final JavaMailSender mailSender;

  @Value("${mail.from.address}")
  private String fromAddress;

  @Value("${app.public-base-url:http://localhost:4200}")
  private String publicBaseUrl;

  /**
   * Base del frontend para los enlaces que viajan por correo: el origen de la petición si viene
   * (así el enlace apunta al mismo sitio desde el que se está usando la app) y, si no, la URL
   * pública configurada.
   */
  private String origenFrontend(HttpServletRequest servletRequest) {
    String origin = servletRequest.getHeader("Origin");
    return (origin != null && !origin.isBlank()) ? origin : publicBaseUrl;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody AuthRequest request) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
      final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
      final String jwt = jwtService.generateToken(userDetails);
      return ResponseEntity.ok(new AuthResponse(jwt));
    } catch (AuthenticationException e) {
      // Si las credenciales son incorrectas, devolvemos un 401 Unauthorized
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, "Email o contraseña incorrectos", null));
    }
  }

  /**
   * Registro público. Si el email ya figura en el listado de socios de una peña no se crea una
   * ficha nueva (eso duplicaría al socio y lo dejaría además en la peña por defecto): se envía un
   * correo con un enlace de un solo uso para confirmar que quien se registra controla ese buzón y
   * vincular así la cuenta con la ficha que ya existía.
   */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<RegisterResponse>> register(
      @RequestBody RegisterRequest registerRequest, HttpServletRequest servletRequest) {
    try {
      if (vinculacionSocioService.tieneSociosVinculables(registerRequest.getEmail())) {
        vinculacionSocioService.enviarInvitacion(registerRequest.getEmail(),
            registerRequest.getPassword(), origenFrontend(servletRequest));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(new ApiResponse<>(true,
                "Ese correo ya figura en el listado de socios de la peña. Te hemos enviado un "
                    + "email para confirmar que eres tú y vincular tu ficha a tu nueva cuenta.",
                new RegisterResponse(true, null)));
      }

      SocioEntity nuevoSocio = socioService.registrarSocio(registerRequest);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(new ApiResponse<>(true, "Usuario y socio registrados exitosamente",
              new RegisterResponse(false, nuevoSocio)));
    } catch (EmailAlreadyExistsException e) {
      // Captura el error si el email ya existe
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(false, e.getMessage(), null));
    }
  }

  /**
   * Datos de la invitación de vinculación asociada al token del enlace, para que la pantalla de
   * confirmación pueda mostrar con qué ficha se va a vincular la cuenta.
   */
  @GetMapping("/vinculacion")
  public ResponseEntity<ApiResponse<VinculacionInfoDto>> consultarVinculacion(
      @RequestParam String token) {
    VinculacionInfoDto info = vinculacionSocioService.consultar(token);
    return ResponseEntity.ok(new ApiResponse<>(true, "Invitación válida", info));
  }

  /**
   * Confirma la vinculación: crea la cuenta, le asocia las fichas de socio que ya existían y
   * devuelve el JWT para dejar la sesión iniciada.
   */
  @PostMapping("/vinculacion/confirmar")
  public ResponseEntity<AuthResponse> confirmarVinculacion(
      @RequestBody ConfirmarVinculacionRequest request) {
    UsuarioEntity usuario =
        vinculacionSocioService.confirmar(request.getToken(), request.getPassword());
    return ResponseEntity.ok(new AuthResponse(jwtService.generateToken(usuario)));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request,
      HttpServletRequest servletRequest) {
    usuarioRepository.findByEmailIgnoreCase(request.getEmail()).ifPresentOrElse(usuario -> {
      String token = jwtService.generateToken(usuario);

      // Construimos el enlace de reseteo dinámicamente a partir del origen de la petición.
      // El "/#/" es obligatorio porque el frontend enruta por hash (withHashLocation): sin él
      // el enlace acaba en el login y se pierde el token.
      String resetLink =
          origenFrontend(servletRequest) + "/#/auth/reset-password?token=" + token;

      try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(usuario.getEmail());
        message.setSubject("Solicitud de restablecimiento de contraseña");
        message.setText(
            "Para restablecer tu contraseña, haz clic en el siguiente enlace: " + resetLink);
        mailSender.send(message);
      } catch (Exception e) {
        log.error("Error al enviar el correo de restablecimiento de contraseña a {}",
            usuario.getEmail(), e);
      }
    },
        // Sin cuenta todavía: si el email figura en el listado de socios, lo que necesita no es
        // recuperar la contraseña sino vincular su ficha, así que se le envía esa invitación.
        () -> vinculacionSocioService.enviarInvitacion(request.getEmail(), null,
            origenFrontend(servletRequest)));

    // Siempre se devuelve OK para no revelar si un email existe en el sistema (prevención de enumeración de emails)
    return ResponseEntity.ok().build();
  }

  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
    try {
      String userEmail = jwtService.extractUsername(request.getToken());
      if (userEmail != null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
        if (jwtService.isTokenValid(request.getToken(), userDetails)) {
          UsuarioEntity usuario = usuarioRepository.findByEmailIgnoreCase(userEmail)
              .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
          usuario.setPassword(passwordEncoder.encode(request.getPassword()));
          usuarioRepository.save(usuario);
          return ResponseEntity.ok().build();
        }
      }
    } catch (Exception e) {
      log.error("Error al restablecer la contraseña", e);
    }
    return ResponseEntity.badRequest().build();
  }
}