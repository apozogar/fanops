package com.softwells.fanops.service;

import com.softwells.fanops.controller.dto.EventoInscripcionDTO;
import com.softwells.fanops.model.PenaEntity;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

/**
 * Etiquetas Open Graph del enlace público de inscripción a un evento.
 *
 * <p>WhatsApp, Telegram y compañía no ejecutan JavaScript: para pintar la vista previa leen las
 * etiquetas {@code og:*} del HTML que devuelve el servidor. Como el frontend es una SPA, sin esto
 * todas las rutas llevan la misma cabecera genérica y el enlace se ve desnudo. Aquí se genera la
 * cabecera propia de cada evento: título, fecha y lugar, plazo y precio, y como imagen el cartel
 * del evento ({@link CartelEventoService}), que WhatsApp pinta a todo el ancho del mensaje.
 *
 * <p>No se incluyen datos que cambian a cada rato, como las plazas libres: las aplicaciones de
 * mensajería guardan la vista previa en caché y al cabo de unas horas estarían mintiendo.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrevisualizacionEnlaceService {

  private final EventoService eventoService;
  private final PenaService penaService;

  /**
   * Bloque de etiquetas para la cabecera del enlace de inscripción, o vacío si el evento no
   * existe (entonces se sirve la página genérica y el frontend ya enseña su aviso).
   *
   * @param base origen por el que ha llegado la petición ({@code https://fanops.es}). Las URLs
   *     de la vista previa salen de ahí y no de la configuración: así apuntan siempre al dominio
   *     desde el que se ha compartido el enlace, aunque falte {@code PUBLIC_BASE_URL}.
   */
  public Optional<String> etiquetasInscripcion(UUID eventoId, String base) {
    EventoInscripcionDTO evento;
    try {
      evento = eventoService.infoPublica(eventoId);
    } catch (EntityNotFoundException noExiste) {
      return Optional.empty();
    }
    Optional<PenaEntity> pena = penaService.penaPrincipal();

    String titulo = evento.getNombreEvento() + " · Inscripción";
    String descripcion = descripcion(evento);
    String origen = base.replaceFirst("/+$", "");
    String url = origen + "/inscripcion/" + eventoId;
    // La versión va en la ruta y no como parámetro: hay rastreadores que tratan peor las imágenes
    // con query string, y la vista grande de WhatsApp es caprichosa.
    String cartel = origen + "/api/eventos/" + eventoId + "/cartel/"
        + CartelEventoService.version(evento) + ".jpg";

    StringBuilder html = new StringBuilder();
    html.append(meta("description", descripcion));
    html.append(og("og:type", "website"));
    html.append(og("og:locale", "es_ES"));
    html.append(og("og:url", url));
    html.append(og("og:title", titulo));
    html.append(og("og:description", descripcion));
    pena.ifPresent(p -> html.append(og("og:site_name", p.getNombre())));
    html.append(og("og:image", cartel));
    html.append(og("og:image:secure_url", cartel));
    html.append(og("og:image:type", "image/jpeg"));
    html.append(og("og:image:width", String.valueOf(CartelEventoService.ANCHO)));
    html.append(og("og:image:height", String.valueOf(CartelEventoService.ALTO)));
    html.append(og("og:image:alt", "Cartel de " + evento.getNombreEvento()));
    html.append(meta("twitter:card", "summary_large_image"));
    html.append(meta("twitter:title", titulo));
    html.append(meta("twitter:description", descripcion));
    html.append(meta("twitter:image", cartel));
    return Optional.of(html.toString());
  }

  /** Título de la pestaña del navegador para el mismo enlace. */
  public Optional<String> tituloInscripcion(UUID eventoId) {
    try {
      EventoInscripcionDTO evento = eventoService.infoPublica(eventoId);
      String pena = penaService.penaPrincipal().map(PenaEntity::getNombre).orElse("FanOps");
      return Optional.of(evento.getNombreEvento() + " · Inscripción · " + pena);
    } catch (EntityNotFoundException noExiste) {
      return Optional.empty();
    }
  }

  /**
   * Primero fecha y lugar, y después lo que hace falta para decidirse: hasta cuándo hay plazo y
   * cuánto cuesta. Va en una sola línea: varias aplicaciones convierten los saltos de línea en un
   * espacio y dejarían las dos frases pegadas sin separador.
   */
  private String descripcion(EventoInscripcionDTO evento) {
    List<String> partes = new ArrayList<>();
    if (evento.getFechaEvento() != null) {
      // 🗓️ y no 📅: WhatsApp pinta el 📅 con un "17" o un "24" dentro, que al lado de la
      // fecha del partido despista.
      partes.add("🗓️ " + TextosEvento.fecha(evento.getFechaEvento()));
    }
    if (evento.getUbicacion() != null && !evento.getUbicacion().isBlank()) {
      partes.add("📍 " + evento.getUbicacion().trim());
    }
    if (evento.isInscripcionCerrada()) {
      partes.add("🔒 Inscripción cerrada");
    } else if (evento.getFechaLimiteInscripcion() != null) {
      partes.add("⏰ Plazo hasta el " + TextosEvento.plazo(evento.getFechaLimiteInscripcion(),
          evento.getFechaEvento()));
    } else {
      partes.add("✅ Inscripción abierta");
    }
    String precio = TextosEvento.precioPlaza(evento.getCostePlaza(), evento.getCostePlazaNoSocio());
    if (precio != null) {
      partes.add("💶 " + precio);
    }
    return String.join(" · ", partes);
  }

  private static String og(String propiedad, String valor) {
    return "<meta property=\"" + propiedad + "\" content=\""
        + HtmlUtils.htmlEscape(valor, "UTF-8") + "\" />\n";
  }

  private static String meta(String nombre, String valor) {
    return "<meta name=\"" + nombre + "\" content=\"" + HtmlUtils.htmlEscape(valor, "UTF-8")
        + "\" />\n";
  }
}
