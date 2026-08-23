package com.softwells.fanops.service;

import com.softwells.fanops.enums.EstadoInscripcion;
import com.softwells.fanops.enums.MotivoFalta;
import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.model.EventoInscripcionEntity;
import com.softwells.fanops.model.SocioEntity;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

  private final EmailSender emailSender;
  private final RestClient.Builder restClientBuilder;

  @Value("${app.public-base-url:http://localhost:5300}")
  private String publicBaseUrl;

  @Value("${whatsapp.enabled:false}")
  private boolean whatsappEnabled;

  @Value("${whatsapp.api-version:v21.0}")
  private String whatsappApiVersion;

  @Value("${whatsapp.phone-number-id:}")
  private String whatsappPhoneNumberId;

  @Value("${whatsapp.access-token:}")
  private String whatsappAccessToken;

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

  /**
   * Aviso único para una inscripción de uno o varios socios, detallando el estado de cada
   * persona. En un multicarnet evita mandar un correo por hijo al mismo titular y, sobre todo,
   * deja claro a quién se ha apuntado.
   */
  public void enviarResumenInscripcion(List<EventoInscripcionEntity> inscripciones,
      EventoEntity evento) {
    if (inscripciones == null || inscripciones.isEmpty()) {
      return;
    }

    StringBuilder cuerpo = new StringBuilder("Hola,\n\n")
        .append("Inscripción a '").append(evento.getNombreEvento()).append("' (")
        .append(evento.getFechaEvento()).append("):\n\n");
    boolean algunoEnEspera = false;
    for (EventoInscripcionEntity inscripcion : inscripciones) {
      boolean confirmada = inscripcion.getEstado() == EstadoInscripcion.CONFIRMADA;
      algunoEnEspera |= !confirmada;
      cuerpo.append("- ").append(inscripcion.getNombre()).append(": ")
          .append(confirmada ? "PLAZA CONFIRMADA" : "LISTA DE ESPERA").append("\n");
    }
    if (algunoEnEspera) {
      cuerpo.append("\nAvisaremos por email o WhatsApp en cuanto se libere una plaza.");
    }
    cuerpo.append("\n\nMás información: ")
        .append(publicBaseUrl).append("/#/inscripcion/").append(evento.getUid());

    String asunto = "Inscripción a " + evento.getNombreEvento();
    // Se avisa al contacto de cada ficha, pero sin repetir destinatario: en un multicarnet los
    // hijos suelen compartir el email y el teléfono del titular.
    inscripciones.stream()
        .map(i -> Map.entry(i.getEmail() != null ? i.getEmail() : "",
            i.getTelefono() != null ? i.getTelefono() : ""))
        .distinct()
        .forEach(contacto -> enviar(contacto.getKey(), inscripciones.get(0).getNombre(), asunto,
            cuerpo.toString(), contacto.getValue()));
  }

  /** Aviso a quien un administrador da de baja de un evento. */
  public void enviarBajaInscripcion(EventoInscripcionEntity inscripcion, EventoEntity evento) {
    String asunto = "Baja en " + evento.getNombreEvento();
    String cuerpo =
        "Hola " + inscripcion.getNombre() + ",\n\n"
            + "Tu inscripción a '" + evento.getNombreEvento() + "' ("
            + evento.getFechaEvento() + ") ha sido dada de baja por la organización.\n\n"
            + "Si crees que se trata de un error, ponte en contacto con nosotros.";
    enviar(inscripcion.getEmail(), inscripcion.getNombre(), asunto, cuerpo,
        inscripcion.getTelefono());
  }

  /**
   * Aviso de falta. Se explica el motivo y el efecto, porque la penalización se nota más tarde
   * (al apuntarse al siguiente evento) y sin este aviso parecería un fallo del sistema.
   */
  public void enviarAvisoFalta(SocioEntity socio, EventoEntity evento, MotivoFalta motivo,
      int penalizaciones) {
    String asunto = "Falta registrada en " + evento.getNombreEvento();
    StringBuilder cuerpo = new StringBuilder("Hola " + socio.getNombre() + ",\n\n");
    if (motivo == MotivoFalta.CANCELACION_TARDIA) {
      cuerpo.append("Has anulado tu plaza de '").append(evento.getNombreEvento())
          .append("' (").append(evento.getFechaEvento())
          .append(") con el plazo de inscripción ya cerrado, así que se te ha registrado una falta.")
          .append("\n\nSi alguien de la lista de espera ocupa tu plaza, la falta se retirará sola.");
    } else {
      cuerpo.append("Tenías plaza en '").append(evento.getNombreEvento())
          .append("' (").append(evento.getFechaEvento())
          .append(") y no se ha registrado tu asistencia, así que se te ha puesto una falta.");
    }
    if (penalizaciones > 0) {
      cuerpo.append("\n\nEfecto: ")
          .append(penalizaciones == 1
              ? "en tu próxima inscripción entrarás en lista de espera"
              : "en tus próximas " + penalizaciones + " inscripciones entrarás en lista de espera")
          .append(", aunque queden plazas libres.");
    }
    cuerpo.append("\n\nSi crees que se trata de un error, ponte en contacto con nosotros.");
    enviar(socio.getEmail(), socio.getNombre(), asunto, cuerpo.toString(), socio.getTelefono());
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
      // Aquí el fallo sí se registra y sigue, al contrario que en los correos de acceso: estos
      // avisos salen dentro de operaciones que ya han cambiado datos (confirmar una plaza,
      // promocionar la lista de espera) y no tendría sentido deshacer la plaza de un socio
      // porque el proveedor de correo esté caído. Además se intenta también por WhatsApp.
      try {
        emailSender.enviar(email, nombre, asunto, cuerpo);
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
