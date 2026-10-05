package com.softwells.fanops.controller;

import com.softwells.fanops.service.CartelEventoService;
import com.softwells.fanops.service.PrevisualizacionEnlaceService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.HtmlUtils;

/**
 * Sirve el enlace público de inscripción con su vista previa para mensajería.
 *
 * <p>Es la misma página que devolvería {@link com.softwells.fanops.config.SpaWebConfig} (el
 * {@code index.html} del frontend, que arranca la SPA y pinta el formulario), pero con las
 * etiquetas Open Graph del evento ya puestas en la cabecera. Eso es lo que leen WhatsApp y
 * Telegram para enseñar título, fecha y escudo en lugar del enlace pelado.
 *
 * <p>Un controlador tiene prioridad sobre el manejador de estáticos, así que esta ruta concreta
 * se resuelve aquí y el resto de la navegación sigue yendo por SpaWebConfig.
 */
@Controller
@RequiredArgsConstructor
public class PrevisualizacionEnlaceController {

  private static final String INDEX = "static/index.html";

  private final PrevisualizacionEnlaceService previsualizacion;
  private final CartelEventoService cartelEvento;

  /**
   * Cartel del evento (PNG de 1200×630) para la vista previa del enlace. Público: lo descarga
   * WhatsApp sin sesión. Se dibuja en cada petición, que es barato, y se deja una hora en caché;
   * la URL ya lleva una versión que cambia cuando cambian los datos del evento.
   */
  @GetMapping(value = "/api/eventos/{id}/cartel.png", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> cartel(@PathVariable String id) {
    UUID eventoId = uuid(id);
    if (eventoId == null) {
      return ResponseEntity.notFound().build();
    }
    return cartelEvento.cartel(eventoId)
        .map(png -> ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
            .body(png))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping(value = "/inscripcion/{id}", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> inscripcion(@PathVariable String id) throws IOException {
    Optional<String> index = leerIndex();
    if (index.isEmpty()) {
      // En local sin el frontend compilado: lo sirve su propio servidor de desarrollo.
      return ResponseEntity.notFound().build();
    }

    String html = index.get();
    UUID eventoId = uuid(id);
    if (eventoId != null) {
      Optional<String> titulo = previsualizacion.tituloInscripcion(eventoId);
      if (titulo.isPresent()) {
        html = html.replaceFirst("(?is)<title>.*?</title>", Matcher.quoteReplacement(
            "<title>" + HtmlUtils.htmlEscape(titulo.get(), "UTF-8") + "</title>"));
      }
      // Origen tal como lo vio el cliente: detrás de Caddy, el esquema llega en
      // X-Forwarded-Proto y lo aplica server.forward-headers-strategy=framework.
      String origen = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
      Optional<String> etiquetas = previsualizacion.etiquetasInscripcion(eventoId, origen);
      if (etiquetas.isPresent()) {
        html = insertarEnCabecera(html, etiquetas.get());
      }
    }

    // Sin caché en el navegador: el index referencia los bundles por hash y, tras un despliegue,
    // una copia vieja cargaría scripts que ya no existen.
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
        .body(html);
  }

  /** Mete las etiquetas al final de la cabecera; sin {@code </head>}, tras el título. */
  private static String insertarEnCabecera(String html, String etiquetas) {
    if (html.matches("(?is).*</head>.*")) {
      return html.replaceFirst("(?i)</head>", Matcher.quoteReplacement(etiquetas + "</head>"));
    }
    if (html.matches("(?is).*</title>.*")) {
      return html.replaceFirst("(?i)</title>", Matcher.quoteReplacement("</title>\n" + etiquetas));
    }
    return etiquetas + html;
  }

  private static Optional<String> leerIndex() throws IOException {
    ClassPathResource recurso = new ClassPathResource(INDEX);
    if (!recurso.exists()) {
      return Optional.empty();
    }
    try (InputStream entrada = recurso.getInputStream()) {
      return Optional.of(new String(entrada.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  private static UUID uuid(String valor) {
    try {
      return UUID.fromString(valor);
    } catch (IllegalArgumentException noEsUuid) {
      return null;
    }
  }
}
