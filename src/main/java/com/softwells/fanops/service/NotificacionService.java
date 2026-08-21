package com.softwells.fanops.service;

import com.softwells.fanops.enums.EstadoInscripcion;
import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.model.EventoInscripcionEntity;
import com.softwells.fanops.model.SocioEntity;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Envía confirmaciones y avisos de inscripción a eventos por email y, si está configurado,
 * por WhatsApp (Meta Business Cloud API). Si WhatsApp no está configurado o falla, se degrada
 * silenciosamente a email.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

  private final JavaMailSender mailSender;
  private final RestClient.Builder restClientBuilder;

  @Value("${mail.from.address}")
  private String fromAddress;

  @Value("${app.public-base-url:http://localhost:4200}")
  private String publicBaseUrl;

  @Value("${whatsapp.enabled:false}")
  private boolean whatsappEnabled;

  @Value("${whatsapp.api-version:v21.0}")
  private String whatsappApiVersion;

  @Value("${whatsapp.phone-number-id:}")
  private String whatsappPhoneNumberId;

  @Value("${whatsapp.access-token:}")
  private String whatsappAccessToken;

  public void enviarConfirmacionInscripcion(SocioEntity socio, EventoEntity evento,
      EstadoInscripcion estado) {
    String asunto = "Inscripción a " + evento.getNombreEvento();
    String cuerpo = cuerpoInscripcion(evento, estado, publicBaseUrl);
    enviar(socio.getEmail(), socio.getNombre(), asunto, cuerpo, socio.getTelefono());
  }

  public void enviarConfirmacionInscripcionPublica(EventoInscripcionEntity inscripcion,
      EventoEntity evento) {
    String asunto = "Inscripción a " + evento.getNombreEvento();
    String cuerpo = cuerpoInscripcion(evento, inscripcion.getEstado(), publicBaseUrl);
    enviar(inscripcion.getEmail(), inscripcion.getNombre(), asunto, cuerpo,
        inscripcion.getTelefono());
  }

  public void enviarPromocionEspera(EventoInscripcionEntity inscripcion, EventoEntity evento) {
    String asunto = "¡Tienes plaza para " + evento.getNombreEvento() + "!";
    String cuerpo =
        "Hola " + inscripcion.getNombre() + ",\n\n"
            + "¡Enhorabuena! Ha quedado una plaza libre para '" + evento.getNombreEvento()
            + "' (" + evento.getFechaEvento() + ") y tu inscripción ha sido confirmada.\n\n"
            + "Nos vemos allí. ¡Vamos mi Betis!";
    enviar(inscripcion.getEmail(), inscripcion.getNombre(), asunto, cuerpo,
        inscripcion.getTelefono());
  }

  private String cuerpoInscripcion(EventoEntity evento, EstadoInscripcion estado,
      String baseUrl) {
    String enlace = baseUrl + "/#/inscripcion/" + evento.getUid();
    if (estado == EstadoInscripcion.CONFIRMADA) {
      return "Hola,\n\nTu inscripción a '" + evento.getNombreEvento() + "' ("
          + evento.getFechaEvento() + ") ha sido CONFIRMADA.\n\n"
          + "Más información: " + enlace;
    }
    return "Hola,\n\nTu solicitud para '" + evento.getNombreEvento() + "' ("
        + evento.getFechaEvento() + ") ha quedado en LISTA DE ESPERA.\n\n"
        + "Te avisaremos por email o WhatsApp si se libera una plaza.\n"
        + "Más información: " + enlace;
  }

  private void enviar(String email, String nombre, String asunto, String cuerpo,
      String telefono) {
    if (email != null && !email.isBlank()) {
      try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject(asunto);
        message.setText(cuerpo);
        mailSender.send(message);
        log.info("Email enviado a {} (asunto: {})", email, asunto);
      } catch (Exception e) {
        log.error("Error enviando email a {} (asunto: {})", email, asunto, e);
      }
    }
    enviarWhatsApp(telefono, asunto + "\n\n" + cuerpo);
  }

  private void enviarWhatsApp(String telefono, String texto) {
    if (!whatsappEnabled || whatsappPhoneNumberId == null || whatsappPhoneNumberId.isBlank()
        || whatsappAccessToken == null || whatsappAccessToken.isBlank()) {
      return; // WhatsApp no configurado: degradación a email
    }
    String numero = normalizarTelefono(telefono);
    if (numero == null) {
      return;
    }
    try {
      String url = "https://graph.facebook.com/" + whatsappApiVersion + "/"
          + whatsappPhoneNumberId + "/messages";
      Map<String, Object> body = Map.of(
          "messaging_product", "whatsapp",
          "to", numero,
          "type", "text",
          "text", Map.of("body", texto)
      );
      restClientBuilder.build()
          .post()
          .uri(url)
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + whatsappAccessToken)
          .contentType(MediaType.APPLICATION_JSON)
          .body(body)
          .retrieve()
          .toBodilessEntity();
      log.info("WhatsApp enviado a {}", numero);
    } catch (Exception e) {
      log.error("Error enviando WhatsApp a {}", numero, e);
    }
  }

  /** Normaliza un teléfono al formato internacional sin '+', necesario para la API de Meta. */
  private String normalizarTelefono(String telefono) {
    if (telefono == null) {
      return null;
    }
    String soloDigitos = telefono.replaceAll("[^0-9]", "");
    if (soloDigitos.length() == 9 && (soloDigitos.startsWith("6") || soloDigitos.startsWith("7"))) {
      return "34" + soloDigitos; // España
    }
    if (soloDigitos.length() >= 11) {
      return soloDigitos;
    }
    return null;
  }
}
