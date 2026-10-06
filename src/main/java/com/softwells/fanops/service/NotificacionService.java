package com.softwells.fanops.service;

import com.softwells.fanops.enums.EstadoInscripcion;
import com.softwells.fanops.enums.EstadoSolicitudCarnet;
import com.softwells.fanops.enums.MotivoFalta;
import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.model.EventoInscripcionEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.SolicitudCarnetEntity;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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

  /** ID de la cuenta de WhatsApp Business (WABA); solo hace falta para crear la plantilla. */
  @Value("${whatsapp.waba-id:}")
  private String whatsappWabaId;

  /**
   * Con plantilla (lo normal) el aviso llega siempre; con texto libre solo si el destinatario
   * escribió al número en las últimas 24 h, así que queda para pruebas.
   */
  @Value("${whatsapp.usar-plantilla:true}")
  private boolean whatsappUsarPlantilla;

  @Value("${whatsapp.plantilla.nombre:aviso_lista_espera}")
  private String whatsappPlantillaNombre;

  @Value("${whatsapp.plantilla.idioma:es}")
  private String whatsappPlantillaIdioma;

  @Value("${whatsapp.plantilla.crear-al-arrancar:true}")
  private boolean whatsappCrearPlantilla;

  /**
   * Texto fijo de la plantilla. Habla de una solicitud concreta del destinatario (su plaza en la
   * lista de espera) y no lleva nada promocional: es lo que Meta pide para clasificarla como
   * utilidad, que es más barata que marketing y no exige consentimiento de marketing. Solo se
   * usa para avisos de lista de espera; el resto de avisos van por email.
   */
  private static final String PLANTILLA_TEXTO = "Hola {{1}}, te escribimos por tu solicitud de "
      + "plaza o carnet en un evento de la peña. Novedad en tu inscripción: {{2}} Un saludo de "
      + "tu peña.";
  private static final int MAX_NOMBRE = 60;
  private static final int MAX_NOVEDAD = 500;
  private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  /**
   * El WhatsApp es a menudo el único aviso que lee el socio (la peña casi no usa el correo), así
   * que la novedad lleva todo lo necesario sin tener que abrir nada: qué evento, cuándo, dónde y
   * el enlace con el resto.
   */
  private String detalleEvento(EventoEntity evento) {
    String lugar = evento.getUbicacion() != null && !evento.getUbicacion().isBlank()
        ? ", " + evento.getUbicacion().trim() : "";
    return "'" + evento.getNombreEvento() + "' (" + evento.getFechaEvento().format(FECHA)
        + lugar + ")";
  }

  private String enlaceEvento(EventoEntity evento) {
    return " Más información: " + publicBaseUrl + "/inscripcion/" + evento.getUid();
  }

  /**
   * Crea en Meta la plantilla genérica si aún no existe, para no tener que darla de alta a
   * mano. Meta la revisa antes de dejar usarla (en las de categoría utilidad suele tardar
   * minutos); mientras tanto los envíos fallan y se registran en el log.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void asegurarPlantilla() {
    if (!whatsappEnabled || !whatsappUsarPlantilla || !whatsappCrearPlantilla
        || whatsappWabaId.isBlank() || whatsappAccessToken.isBlank()) {
      return;
    }
    try {
      String base = "https://graph.facebook.com/" + whatsappApiVersion + "/" + whatsappWabaId
          + "/message_templates";
      // Meta devuelve JSON con Content-Type text/javascript, que RestClient no convierte solo:
      // se lee como texto y se mira lo justo (si hay plantilla y en qué estado está).
      String existentes = restClientBuilder.build().get()
          .uri(base + "?name=" + whatsappPlantillaNombre)
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + whatsappAccessToken)
          .retrieve().body(String.class);
      if (existentes != null && !existentes.replaceAll("\\s", "").contains("\"data\":[]")) {
        Matcher estado = Pattern.compile("\"status\"\\s*:\\s*\"(\\w+)\"").matcher(existentes);
        log.info("Plantilla de WhatsApp '{}' ya existe: {}", whatsappPlantillaNombre,
            estado.find() ? estado.group(1) : "?");
        return;
      }
      Map<String, Object> body = Map.of(
          "name", whatsappPlantillaNombre,
          "language", whatsappPlantillaIdioma,
          "category", "UTILITY",
          "components", List.of(Map.of(
              "type", "BODY",
              "text", PLANTILLA_TEXTO,
              "example", Map.of("body_text", List.of(List.of(
                  "Alberto",
                  "Ha quedado una plaza libre para 'Betis - Sevilla' (20/10/2026, Estadio) y tu "
                      + "inscripción ha sido confirmada. Más información: "
                      + "https://fanops.es/inscripcion/abc"))))));
      restClientBuilder.build().post().uri(base)
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + whatsappAccessToken)
          .contentType(MediaType.APPLICATION_JSON)
          .body(body)
          .retrieve().toBodilessEntity();
      log.info("Plantilla de WhatsApp '{}' enviada a Meta para su aprobación",
          whatsappPlantillaNombre);
    } catch (Exception e) {
      log.error("No se pudo comprobar/crear la plantilla de WhatsApp '{}'",
          whatsappPlantillaNombre, e);
    }
  }

  public void enviarConfirmacionInscripcionPublica(EventoInscripcionEntity inscripcion,
      EventoEntity evento) {
    String asunto = "Inscripción a " + evento.getNombreEvento();
    String cuerpo = cuerpoInscripcion(evento, inscripcion.getEstado(), publicBaseUrl);
    enviar(inscripcion.getEmail(), inscripcion.getNombre(), asunto, cuerpo,
        inscripcion.getTelefono(), null);
  }

  /**
   * Aviso de plazas que acaban de quedar libres para las inscripciones promocionadas de la lista
   * de espera. Se agrupa por contacto (email + teléfono): en un multicarnet los hijos comparten
   * los del titular, y un aviso por ficha supondría un correo y, sobre todo, un WhatsApp de pago
   * por cada una.
   */
  public void enviarPromocionEspera(List<EventoInscripcionEntity> promocionadas,
      EventoEntity evento) {
    if (promocionadas == null || promocionadas.isEmpty()) {
      return;
    }
    String asunto = "¡Tienes plaza para " + evento.getNombreEvento() + "!";
    Map<Map.Entry<String, String>, List<EventoInscripcionEntity>> porContacto =
        promocionadas.stream().collect(Collectors.groupingBy(
            i -> Map.entry(i.getEmail() != null ? i.getEmail() : "",
                i.getTelefono() != null ? i.getTelefono() : ""),
            LinkedHashMap::new, Collectors.toList()));

    porContacto.forEach((contacto, grupo) -> {
      String nombres = grupo.stream().map(EventoInscripcionEntity::getNombre)
          .collect(Collectors.joining(", "));
      boolean varias = grupo.size() > 1;
      String novedad = "Ha quedado " + (varias ? "plaza libre para " + nombres + " en "
          : "una plaza libre para ") + detalleEvento(evento) + " y la inscripción "
          + (varias ? "de cada uno ha sido confirmada." : "ha sido confirmada.")
          + enlaceEvento(evento);
      String cuerpo = "Hola " + nombres + ",\n\n¡Enhorabuena! " + novedad
          + "\n\nNos vemos allí. ¡Vamos mi Betis!";
      enviar(contacto.getKey(), nombres, asunto, cuerpo, contacto.getValue(), novedad);
    });
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
        .append(publicBaseUrl).append("/inscripcion/").append(evento.getUid());

    String asunto = "Inscripción a " + evento.getNombreEvento();
    // Se avisa al contacto de cada ficha, pero sin repetir destinatario: en un multicarnet los
    // hijos suelen compartir el email y el teléfono del titular.
    inscripciones.stream()
        .map(i -> Map.entry(i.getEmail() != null ? i.getEmail() : "",
            i.getTelefono() != null ? i.getTelefono() : ""))
        .distinct()
        .forEach(contacto -> enviar(contacto.getKey(), inscripciones.get(0).getNombre(), asunto,
            cuerpo.toString(), contacto.getValue(), null));
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
        inscripcion.getTelefono(), null);
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
    enviar(socio.getEmail(), socio.getNombre(), asunto, cuerpo.toString(), socio.getTelefono(),
        null);
  }

  /**
   * Resultado del sorteo de carnets a todos los que se apuntaron. Se avisa también a quien no
   * ha ganado, y con su número de suplente: si un ganador renuncia el carnet corre esa lista, y
   * sin saber su puesto nadie entiende por qué le llega el carnet dos días después.
   */
  public void enviarResultadoSorteoCarnet(List<SolicitudCarnetEntity> extraidos,
      EventoEntity evento, int numeroCarnets) {
    for (SolicitudCarnetEntity solicitud : extraidos) {
      SocioEntity socio = solicitud.getSocio();
      boolean premiado = solicitud.getEstado() == EstadoSolicitudCarnet.GANADORA;
      int puestoSuplente = solicitud.getPosicionSorteo() - numeroCarnets;

      String asunto = (premiado ? "¡Te ha tocado carnet para " : "Sorteo de carnets de ")
          + evento.getNombreEvento() + (premiado ? "!" : "");
      StringBuilder cuerpo = new StringBuilder("Hola " + socio.getNombre() + ",\n\n");
      if (premiado) {
        cuerpo.append("¡Enhorabuena! En el sorteo de carnets de '")
            .append(evento.getNombreEvento()).append("' (").append(evento.getFechaEvento())
            .append(") te ha tocado uno de los ").append(numeroCarnets).append(" carnets.")
            .append("\n\nSi al final no vas a poder ir, avísanos cuanto antes para que el carnet")
            .append(" pase al siguiente de la lista.");
      } else {
        cuerpo.append("Esta vez no ha habido suerte en el sorteo de carnets de '")
            .append(evento.getNombreEvento()).append("' (").append(evento.getFechaEvento())
            .append("). Eres el suplente número ").append(puestoSuplente)
            .append(": si alguno de los ganadores renuncia, el carnet va bajando por ese orden.")
            .append("\n\nAdemás, cada sorteo que se te resiste te suma una papeleta para el")
            .append(" siguiente.");
      }
      cuerpo.append("\n\nPuedes ver el sorteo completo aquí: ")
          .append(publicBaseUrl).append("/inscripcion/").append(evento.getUid());

      enviar(socio.getEmail(), socio.getNombre(), asunto, cuerpo.toString(), socio.getTelefono(),
          null);
    }

    // WhatsApp: un solo mensaje por teléfono con el resultado de todas las fichas que lo
    // comparten (multicarnet), porque cada plantilla se cobra.
    extraidos.stream()
        .collect(Collectors.groupingBy(
            s -> s.getSocio().getTelefono() != null ? s.getSocio().getTelefono() : "",
            LinkedHashMap::new, Collectors.toList()))
        .forEach((telefono, grupo) -> {
          String nombres = grupo.stream().map(s -> s.getSocio().getNombre())
              .collect(Collectors.joining(", "));
          enviarWhatsApp(telefono, nombres,
              novedadSorteo(grupo, evento, numeroCarnets));
        });
  }

  private String novedadSorteo(List<SolicitudCarnetEntity> grupo, EventoEntity evento,
      int numeroCarnets) {
    String suplente = "si un ganador renuncia, el carnet pasa por ese orden";
    StringBuilder texto = new StringBuilder("Resultado del sorteo de carnets de ")
        .append(detalleEvento(evento)).append(": ");
    if (grupo.size() == 1) {
      SolicitudCarnetEntity s = grupo.get(0);
      texto.append(s.getEstado() == EstadoSolicitudCarnet.GANADORA
          ? "¡enhorabuena, te ha tocado carnet! Si no vas a poder ir, avísanos cuanto antes."
          : "esta vez no ha habido suerte, eres el suplente número "
              + (s.getPosicionSorteo() - numeroCarnets) + " (" + suplente + ").");
    } else {
      texto.append(grupo.stream()
          .map(s -> s.getSocio().getNombre() + ": "
              + (s.getEstado() == EstadoSolicitudCarnet.GANADORA ? "carnet conseguido"
                  : "suplente número " + (s.getPosicionSorteo() - numeroCarnets)))
          .collect(Collectors.joining("; ")))
          .append(". Los suplentes: ").append(suplente).append(".");
    }
    return texto.append(enlaceEvento(evento)).toString();
  }

  /** Aviso al suplente que hereda el carnet de un ganador que ha renunciado. */
  public void enviarCarnetPorRenuncia(SolicitudCarnetEntity solicitud, EventoEntity evento) {
    SocioEntity socio = solicitud.getSocio();
    String asunto = "¡Tienes carnet para " + evento.getNombreEvento() + "!";
    String cuerpo = "Hola " + socio.getNombre() + ",\n\n"
        + "Uno de los ganadores del sorteo ha devuelto su carnet para '"
        + evento.getNombreEvento() + "' (" + evento.getFechaEvento() + ") y, como eras el "
        + "primer suplente, pasa a ser tuyo.\n\n"
        + "Nos vemos allí. ¡Vamos mi Betis!";
    String novedad = "Uno de los ganadores del sorteo ha devuelto su carnet para "
        + detalleEvento(evento) + " y, como eras el primer suplente, pasa a ser tuyo."
        + enlaceEvento(evento);
    enviar(socio.getEmail(), socio.getNombre(), asunto, cuerpo, socio.getTelefono(), novedad);
  }

  private String cuerpoInscripcion(EventoEntity evento, EstadoInscripcion estado,
      String baseUrl) {
    String enlace = baseUrl + "/inscripcion/" + evento.getUid();
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

  /**
   * Siempre por email; por WhatsApp solo si hay {@code novedadWhatsApp} (una frase corta, que es
   * lo que entra en la plantilla). Cada plantilla de WhatsApp se cobra, así que se reserva para
   * lo que no puede esperar a que alguien mire el correo: una plaza o un carnet que acaba de
   * quedar libre y el resultado del sorteo de carnets. Confirmaciones, bajas y faltas van solo
   * por email.
   */
  private void enviar(String email, String nombre, String asunto, String cuerpo,
      String telefono, String novedadWhatsApp) {
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
    if (novedadWhatsApp != null) {
      enviarWhatsApp(telefono, nombre, novedadWhatsApp);
    }
  }

  /** Meta no admite saltos de línea, tabulaciones ni más de 4 espacios seguidos en una variable. */
  private static String parametro(String texto, int max) {
    String limpio = texto.replaceAll("\\s+", " ").trim();
    return limpio.length() <= max ? limpio : limpio.substring(0, max - 1) + "…";
  }

  private void enviarWhatsApp(String telefono, String nombre, String novedad) {
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
      Map<String, Object> body = whatsappUsarPlantilla
          ? Map.of(
              "messaging_product", "whatsapp",
              "to", numero,
              "type", "template",
              "template", Map.of(
                  "name", whatsappPlantillaNombre,
                  "language", Map.of("code", whatsappPlantillaIdioma),
                  "components", List.of(Map.of(
                      "type", "body",
                      "parameters", List.of(
                          Map.of("type", "text", "text", parametro(nombre, MAX_NOMBRE)),
                          Map.of("type", "text", "text", parametro(novedad, MAX_NOVEDAD)))))))
          : Map.of(
              "messaging_product", "whatsapp",
              "to", numero,
              "type", "text",
              "text", Map.of("body", "Hola " + nombre + ", " + novedad));
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
