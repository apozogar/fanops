package com.softwells.fanops.service;

import com.softwells.fanops.controller.dto.EventoInscripcionDTO;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.repository.PenaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

/**
 * Etiquetas Open Graph del enlace público de inscripción a un evento.
 *
 * <p>WhatsApp, Telegram y compañía no ejecutan JavaScript: para pintar la vista previa leen las
 * etiquetas {@code og:*} del HTML que devuelve el servidor. Como el frontend es una SPA, sin esto
 * todas las rutas llevan la misma cabecera genérica y el enlace se ve desnudo. Aquí se genera la
 * cabecera propia de cada evento: título, fecha y lugar, plazo y precio, y el escudo de la peña.
 *
 * <p>No se incluyen datos que cambian a cada rato, como las plazas libres: las aplicaciones de
 * mensajería guardan la vista previa en caché y al cabo de unas horas estarían mintiendo.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrevisualizacionEnlaceService {

  private static final Locale ES = Locale.forLanguageTag("es-ES");
  private static final DateTimeFormatter FECHA_EVENTO =
      DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ES);
  private static final DateTimeFormatter FECHA_LIMITE =
      DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'a las' HH:mm", ES);

  /** Formatos de imagen que las aplicaciones de mensajería pintan en la vista previa. */
  private static final List<String> TIPOS_IMAGEN_PREVIA =
      List.of("image/png", "image/jpeg", "image/webp", "image/gif");

  private final EventoService eventoService;
  private final PenaRepository penaRepository;

  @Value("${app.public-base-url:http://localhost:5300}")
  private String publicBaseUrl;

  /**
   * Bloque de etiquetas para la cabecera del enlace de inscripción, o vacío si el evento no
   * existe (entonces se sirve la página genérica y el frontend ya enseña su aviso).
   */
  public Optional<String> etiquetasInscripcion(UUID eventoId) {
    EventoInscripcionDTO evento;
    try {
      evento = eventoService.infoPublica(eventoId);
    } catch (EntityNotFoundException noExiste) {
      return Optional.empty();
    }
    Optional<PenaEntity> pena = penaPrincipal();

    String titulo = evento.getNombreEvento() + " · Inscripción";
    String descripcion = descripcion(evento);
    String url = base() + "/inscripcion/" + eventoId;

    StringBuilder html = new StringBuilder();
    html.append(meta("description", descripcion));
    html.append(og("og:type", "website"));
    html.append(og("og:locale", "es_ES"));
    html.append(og("og:url", url));
    html.append(og("og:title", titulo));
    html.append(og("og:description", descripcion));
    pena.ifPresent(p -> html.append(og("og:site_name", p.getNombre())));
    pena.flatMap(this::urlLogo).ifPresent(logo -> {
      html.append(og("og:image", logo));
      html.append(og("og:image:alt", "Escudo de " + pena.get().getNombre()));
    });
    // "summary" es la tarjeta con miniatura cuadrada a un lado, que es lo que encaja con un
    // escudo; la grande recortaría el logo para llenar un 1,91:1.
    html.append(meta("twitter:card", "summary"));
    html.append(meta("twitter:title", titulo));
    html.append(meta("twitter:description", descripcion));
    return Optional.of(html.toString());
  }

  /** Título de la pestaña del navegador para el mismo enlace. */
  public Optional<String> tituloInscripcion(UUID eventoId) {
    try {
      EventoInscripcionDTO evento = eventoService.infoPublica(eventoId);
      String pena = penaPrincipal().map(PenaEntity::getNombre).orElse("FanOps");
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
      partes.add("📅 " + capitalizar(evento.getFechaEvento().format(FECHA_EVENTO)));
    }
    if (evento.getUbicacion() != null && !evento.getUbicacion().isBlank()) {
      partes.add("📍 " + evento.getUbicacion().trim());
    }
    if (evento.isInscripcionCerrada()) {
      partes.add("Inscripción cerrada");
    } else if (evento.getFechaLimiteInscripcion() != null) {
      partes.add("Apúntate hasta el " + formatoLimite(evento.getFechaLimiteInscripcion()));
    } else {
      partes.add("Inscripción abierta");
    }
    if (evento.getCostePlaza() != null) {
      partes.add(euros(evento.getCostePlaza()) + " la plaza");
    }
    return String.join(" · ", partes);
  }

  /**
   * URL absoluta del escudo, si hay uno que se pueda enseñar. Un SVG no vale: ninguna aplicación
   * de mensajería lo pinta, así que es mejor no poner imagen que poner una rota.
   */
  private Optional<String> urlLogo(PenaEntity pena) {
    String logo = pena.getLogo();
    if (logo == null || logo.isBlank()) {
      return Optional.empty();
    }
    logo = logo.trim();
    if (logo.startsWith("data:")) {
      String tipo = logo.substring(5, Math.max(5, logo.indexOf(';'))).toLowerCase(Locale.ROOT);
      return TIPOS_IMAGEN_PREVIA.contains(tipo) && pena.getSlug() != null
          ? Optional.of(base() + "/api/pena/publica/" + pena.getSlug() + "/logo")
          : Optional.empty();
    }
    // Logos antiguos guardados como URL o como ruta a un asset del frontend.
    if (logo.toLowerCase(Locale.ROOT).endsWith(".svg")) {
      return Optional.empty();
    }
    if (logo.startsWith("http://") || logo.startsWith("https://")) {
      return Optional.of(logo);
    }
    return Optional.of(base() + "/" + logo.replaceFirst("^/+", ""));
  }

  /** La peña es única por ahora (ver AGENTS.md): la de menor id. */
  private Optional<PenaEntity> penaPrincipal() {
    return penaRepository.findAll(Sort.by("id")).stream().findFirst();
  }

  private String base() {
    return publicBaseUrl.replaceFirst("/+$", "");
  }

  private static String formatoLimite(LocalDateTime limite) {
    return limite.format(FECHA_LIMITE);
  }

  private static String euros(BigDecimal importe) {
    NumberFormat formato = NumberFormat.getCurrencyInstance(ES);
    if (importe.stripTrailingZeros().scale() <= 0) {
      formato.setMaximumFractionDigits(0);
    }
    return formato.format(importe);
  }

  private static String capitalizar(String texto) {
    return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
  }

  private static String og(String propiedad, String valor) {
    return "<meta property=\"" + propiedad + "\" content=\"" + HtmlUtils.htmlEscape(valor, "UTF-8")
        + "\" />\n";
  }

  private static String meta(String nombre, String valor) {
    return "<meta name=\"" + nombre + "\" content=\"" + HtmlUtils.htmlEscape(valor, "UTF-8") + "\" />\n";
  }
}
