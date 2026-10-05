package com.softwells.fanops.service;

import com.softwells.fanops.controller.dto.EventoInscripcionDTO;
import com.softwells.fanops.model.PenaEntity;
import jakarta.persistence.EntityNotFoundException;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cartel de un evento para la vista previa del enlace en WhatsApp: 1200×630, el formato que esas
 * aplicaciones pintan a todo el ancho del mensaje cuando eligen la vista grande.
 *
 * <p>Se dibuja al vuelo con los datos del evento y la identidad de la peña (color, escudo y
 * lema), así que no hay que diseñar nada a mano y cambiar el evento cambia el cartel. Igual que la
 * descripción de la vista previa, no lleva datos que cambian a cada rato, como las plazas libres:
 * WhatsApp guarda la imagen en caché.
 *
 * <p>No usa emojis: las fuentes del servidor no los traen y saldrían como cuadros. Los iconos se
 * dibujan con primitivas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartelEventoService {

  public static final int ANCHO = 1200;
  public static final int ALTO = 630;

  /** Versión del diseño del cartel; subirla invalida las vistas previas guardadas por WhatsApp. */
  private static final int FORMATO = 2;

  private static final Color COLOR_POR_DEFECTO = new Color(0x00742d);
  private static final Color TINTA_OSCURA = new Color(0x0f172a);

  private static final int MARGEN = 70;
  private static final int ALTO_PIE = 78;
  private static final int DIAMETRO_ESCUDO = 320;
  private static final int X_TEXTO = MARGEN + DIAMETRO_ESCUDO + 60;
  private static final int ANCHO_TEXTO = ANCHO - X_TEXTO - MARGEN;

  /**
   * Fuentes por orden de preferencia: Segoe UI en Windows (desarrollo) y DejaVu Sans en el
   * contenedor, que la instala el Dockerfile. Si no hay ninguna, la sans-serif lógica de Java.
   */
  private static final List<String> FUENTES_PREFERIDAS =
      List.of("Inter", "Segoe UI", "DejaVu Sans", "Liberation Sans", "Arial");

  private final EventoService eventoService;
  private final PenaService penaService;

  private volatile String familia;

  /**
   * JPEG del cartel, o vacío si el evento no existe. JPEG y no PNG por peso: un cartel con
   * degradado pesa en PNG más del doble, y WhatsApp es exigente con el tamaño de la imagen de la
   * vista previa (las pesadas las reduce a miniatura o las descarta).
   */
  public Optional<byte[]> cartel(UUID eventoId) {
    EventoInscripcionDTO evento;
    try {
      evento = eventoService.infoPublica(eventoId);
    } catch (EntityNotFoundException noExiste) {
      return Optional.empty();
    }
    PenaEntity pena = penaService.penaPrincipal().orElse(null);

    BufferedImage imagen = dibujar(evento, pena);
    try {
      return Optional.of(jpeg(imagen));
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo generar el cartel del evento", e);
    }
  }

  /**
   * Huella de lo que sale en el cartel y en la vista previa. Va en la URL de la imagen y en el
   * enlace que se comparte: WhatsApp guarda la vista previa por URL, así que si cambia la fecha o
   * el plazo del evento cambia la URL y no se queda con lo viejo. {@code FORMATO} se sube a mano
   * cuando cambia el diseño, por lo mismo.
   */
  public static String version(EventoInscripcionDTO evento) {
    return Integer.toHexString(Objects.hash(FORMATO, evento.getNombreEvento(),
        evento.getFechaEvento(), evento.getUbicacion(), evento.getFechaLimiteInscripcion(),
        evento.getCostePlaza(), evento.isInscripcionCerrada()));
  }

  private static byte[] jpeg(BufferedImage imagen) throws IOException {
    ImageWriter escritor = ImageIO.getImageWritersByFormatName("jpeg").next();
    ImageWriteParam parametros = escritor.getDefaultWriteParam();
    parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    parametros.setCompressionQuality(0.9f);
    try (ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageOutputStream flujo = ImageIO.createImageOutputStream(salida)) {
      escritor.setOutput(flujo);
      escritor.write(null, new IIOImage(imagen, null, null), parametros);
      flujo.flush();
      return salida.toByteArray();
    } finally {
      escritor.dispose();
    }
  }

  private BufferedImage dibujar(EventoInscripcionDTO evento, PenaEntity pena) {
    BufferedImage imagen = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = imagen.createGraphics();
    try {
      calidad(g);
      Color base = colorDe(pena);
      boolean fondoClaro = luminancia(base) > 0.6;
      Color tinta = fondoClaro ? TINTA_OSCURA : Color.WHITE;

      fondo(g, base);
      escudo(g, pena, base);
      contenido(g, evento, pena, base, tinta);
      pie(g, pena, tinta);
    } finally {
      g.dispose();
    }
    return imagen;
  }

  // ----------------------------------------------------------------
  // Partes del cartel
  // ----------------------------------------------------------------

  /** Degradado del color de la peña hacia oscuro, con dos círculos de adorno y la franja del pie. */
  private void fondo(Graphics2D g, Color base) {
    g.setPaint(new GradientPaint(0, 0, base, ANCHO, ALTO, mezclar(base, Color.BLACK, 0.45)));
    g.fillRect(0, 0, ANCHO, ALTO);

    g.setColor(new Color(255, 255, 255, 20));
    g.fill(new Ellipse2D.Double(ANCHO - 400, -240, 660, 660));
    g.setColor(new Color(255, 255, 255, 12));
    g.fill(new Ellipse2D.Double(ANCHO - 230, ALTO - 300, 460, 460));

    g.setColor(new Color(0, 0, 0, 70));
    g.fillRect(0, ALTO - ALTO_PIE, ANCHO, ALTO_PIE);
  }

  /** El escudo dentro de un círculo blanco, centrado en la zona por encima del pie. */
  private void escudo(Graphics2D g, PenaEntity pena, Color base) {
    int x = MARGEN;
    int y = (ALTO - ALTO_PIE - DIAMETRO_ESCUDO) / 2;

    g.setColor(new Color(0, 0, 0, 45));
    g.fill(new Ellipse2D.Double(x + 6, y + 10, DIAMETRO_ESCUDO, DIAMETRO_ESCUDO));
    g.setColor(Color.WHITE);
    g.fill(new Ellipse2D.Double(x, y, DIAMETRO_ESCUDO, DIAMETRO_ESCUDO));

    Optional<BufferedImage> logo = logoDe(pena);
    if (logo.isPresent()) {
      BufferedImage img = logo.get();
      int hueco = (int) (DIAMETRO_ESCUDO * 0.72);
      double escala = Math.min((double) hueco / img.getWidth(), (double) hueco / img.getHeight());
      int w = (int) Math.round(img.getWidth() * escala);
      int h = (int) Math.round(img.getHeight() * escala);
      // Recortado al círculo: un escudo cuadrado con fondo propio asomaría las esquinas.
      Shape antes = g.getClip();
      g.setClip(new Ellipse2D.Double(x + 4, y + 4, DIAMETRO_ESCUDO - 8, DIAMETRO_ESCUDO - 8));
      g.drawImage(img, x + (DIAMETRO_ESCUDO - w) / 2, y + (DIAMETRO_ESCUDO - h) / 2, w, h, null);
      g.setClip(antes);
      return;
    }

    // Sin escudo: las iniciales de la peña en su color.
    String iniciales = iniciales(pena != null ? pena.getNombre() : "FanOps");
    Font fuente = fuente(Font.BOLD, 120);
    g.setFont(fuente);
    g.setColor(base);
    FontMetrics fm = g.getFontMetrics();
    g.drawString(iniciales, x + (DIAMETRO_ESCUDO - fm.stringWidth(iniciales)) / 2,
        y + (DIAMETRO_ESCUDO - fm.getHeight()) / 2 + fm.getAscent());
  }

  /**
   * Nombre de la peña, el evento en grande, fecha y lugar con su icono, y en etiquetas el plazo y
   * el precio. El título se encoge hasta que el bloque entero cabe; el bloque se centra en vertical.
   */
  private void contenido(Graphics2D g, EventoInscripcionDTO evento, PenaEntity pena, Color base,
      Color tinta) {
    Color tintaSuave = conAlfa(tinta, 200);

    String nombrePena = pena != null ? pena.getNombre().toUpperCase(TextosEvento.ES) : null;
    Font fuentePena = fuente(Font.BOLD, 24).deriveFont(Map.of(TextAttribute.TRACKING, 0.08));
    Font fuenteFecha = fuente(Font.BOLD, 34);
    Font fuenteLugar = fuente(Font.PLAIN, 30);
    Font fuenteEtiqueta = fuente(Font.BOLD, 24);

    String fecha = evento.getFechaEvento() != null ? TextosEvento.fecha(evento.getFechaEvento())
        : null;
    String lugar = evento.getUbicacion() != null && !evento.getUbicacion().isBlank()
        ? evento.getUbicacion().trim() : null;
    List<Etiqueta> etiquetas = etiquetas(evento, base, tinta);

    // Título: el mayor tamaño con el que todo cabe, entre 80 y 46 puntos.
    int espacioDisponible = ALTO - ALTO_PIE - 2 * 40;
    Font fuenteTitulo = null;
    List<String> lineasTitulo = List.of();
    int altoBloque = 0;
    for (int tamano = 80; tamano >= 46; tamano -= 2) {
      fuenteTitulo = fuente(Font.BOLD, tamano);
      lineasTitulo = partir(g, evento.getNombreEvento(), fuenteTitulo, ANCHO_TEXTO, 3);
      altoBloque = altoBloque(g, nombrePena, fuentePena, lineasTitulo, fuenteTitulo, fecha,
          fuenteFecha, lugar, fuenteLugar, etiquetas, fuenteEtiqueta);
      if (altoBloque <= espacioDisponible && lineasTitulo.size() <= 2) {
        break;
      }
    }

    int y = 40 + (espacioDisponible - altoBloque) / 2;

    if (nombrePena != null) {
      g.setFont(fuentePena);
      g.setColor(tintaSuave);
      FontMetrics fm = g.getFontMetrics();
      g.drawString(recortar(g, nombrePena, ANCHO_TEXTO), X_TEXTO, y + fm.getAscent());
      y += fm.getHeight() + 14;
    }

    g.setFont(fuenteTitulo);
    g.setColor(tinta);
    FontMetrics fmTitulo = g.getFontMetrics();
    int altoLineaTitulo = (int) (fuenteTitulo.getSize() * 1.08);
    for (String linea : lineasTitulo) {
      g.drawString(linea, X_TEXTO, y + fmTitulo.getAscent());
      y += altoLineaTitulo;
    }
    y += 20;

    // Raya corta bajo el título.
    g.setColor(conAlfa(tinta, 110));
    g.fill(new RoundRectangle2D.Double(X_TEXTO, y, 90, 6, 6, 6));
    y += 6 + 26;

    if (fecha != null) {
      y = filaConIcono(g, Icono.CALENDARIO, fecha, fuenteFecha, tinta, base, y);
    }
    if (lugar != null) {
      y = filaConIcono(g, Icono.LUGAR, lugar, fuenteLugar, tintaSuave, base, y);
    }
    y += 12;

    g.setFont(fuenteEtiqueta);
    int x = X_TEXTO;
    FontMetrics fmEtiqueta = g.getFontMetrics();
    int altoEtiqueta = fmEtiqueta.getHeight() + 18;
    for (Etiqueta etiqueta : etiquetas) {
      int anchoEtiqueta = fmEtiqueta.stringWidth(etiqueta.texto()) + 36;
      if (x > X_TEXTO && x + anchoEtiqueta > X_TEXTO + ANCHO_TEXTO) {
        x = X_TEXTO;
        y += altoEtiqueta + 12;
      }
      g.setColor(etiqueta.fondo());
      g.fill(new RoundRectangle2D.Double(x, y, anchoEtiqueta, altoEtiqueta, altoEtiqueta,
          altoEtiqueta));
      g.setColor(etiqueta.tinta());
      g.drawString(etiqueta.texto(), x + 18,
          y + (altoEtiqueta - fmEtiqueta.getHeight()) / 2 + fmEtiqueta.getAscent());
      x += anchoEtiqueta + 12;
    }
  }

  /** Lema de la peña a la izquierda y la llamada a la acción a la derecha. */
  private void pie(Graphics2D g, PenaEntity pena, Color tinta) {
    int base = ALTO - ALTO_PIE;
    String llamada = "Toca el enlace para apuntarte  →";
    g.setFont(fuente(Font.BOLD, 26));
    FontMetrics fmLlamada = g.getFontMetrics();
    int anchoLlamada = fmLlamada.stringWidth(llamada);
    int yTexto = base + (ALTO_PIE - fmLlamada.getHeight()) / 2 + fmLlamada.getAscent();
    // El pie va siempre sobre una franja oscura: el texto en blanco se lee con cualquier color.
    g.setColor(Color.WHITE);
    g.drawString(llamada, ANCHO - MARGEN - anchoLlamada, yTexto);

    String lema = pena != null && pena.getLema() != null && !pena.getLema().isBlank()
        ? pena.getLema().trim() : null;
    if (lema != null) {
      g.setFont(fuente(Font.ITALIC, 26));
      g.setColor(new Color(255, 255, 255, 215));
      g.drawString(recortar(g, lema, ANCHO - 2 * MARGEN - anchoLlamada - 40), MARGEN, yTexto);
    }
  }

  // ----------------------------------------------------------------
  // Piezas
  // ----------------------------------------------------------------

  private record Etiqueta(String texto, Color fondo, Color tinta) {
  }

  /** Plazo (resaltada, en blanco) y precio (translúcida). */
  private List<Etiqueta> etiquetas(EventoInscripcionDTO evento, Color base, Color tinta) {
    Color tintaResaltada = luminancia(base) > 0.6 ? TINTA_OSCURA : mezclar(base, Color.BLACK, 0.3);
    List<Etiqueta> etiquetas = new ArrayList<>();
    if (evento.isInscripcionCerrada()) {
      etiquetas.add(new Etiqueta("Inscripción cerrada", new Color(0xfee2e2), new Color(0x991b1b)));
    } else if (evento.getFechaLimiteInscripcion() != null) {
      etiquetas.add(new Etiqueta("Plazo hasta el " + TextosEvento.plazo(
          evento.getFechaLimiteInscripcion(), evento.getFechaEvento()), Color.WHITE,
          tintaResaltada));
    } else {
      etiquetas.add(new Etiqueta("Inscripción abierta", Color.WHITE, tintaResaltada));
    }
    if (evento.getCostePlaza() != null) {
      etiquetas.add(new Etiqueta(TextosEvento.euros(evento.getCostePlaza()) + " la plaza",
          conAlfa(tinta, 45), tinta));
    }
    return etiquetas;
  }

  private enum Icono { CALENDARIO, LUGAR }

  /** Una línea de texto con su icono delante. Devuelve la y de la línea siguiente. */
  private int filaConIcono(Graphics2D g, Icono icono, String texto, Font fuente, Color tinta,
      Color base, int y) {
    g.setFont(fuente);
    FontMetrics fm = g.getFontMetrics();
    int lado = 30;
    int xTexto = X_TEXTO + lado + 18;
    int centroY = y + fm.getHeight() / 2;
    dibujarIcono(g, icono, X_TEXTO, centroY - lado / 2, lado, tinta, base);
    g.setColor(tinta);
    g.drawString(recortar(g, texto, ANCHO_TEXTO - lado - 18), xTexto, y + fm.getAscent());
    return y + fm.getHeight() + 8;
  }

  private void dibujarIcono(Graphics2D g, Icono icono, int x, int y, int lado, Color tinta,
      Color base) {
    g.setColor(tinta);
    if (icono == Icono.CALENDARIO) {
      g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g.draw(new RoundRectangle2D.Double(x + 1.5, y + 4, lado - 3, lado - 5.5, 7, 7));
      g.fill(new RoundRectangle2D.Double(x + 1.5, y + 4, lado - 3, 8, 7, 7));
      g.drawLine(x + 9, y, x + 9, y + 7);
      g.drawLine(x + lado - 9, y, x + lado - 9, y + 7);
      return;
    }
    // Chincheta de mapa: círculo con punta hacia abajo y un hueco en el centro.
    double cx = x + lado / 2.0;
    double r = lado * 0.36;
    double cy = y + r + 1;
    Path2D gota = new Path2D.Double();
    gota.moveTo(cx, y + lado);
    gota.curveTo(cx - r * 0.6, cy + r * 1.2, cx - r, cy + r * 0.6, cx - r, cy);
    gota.curveTo(cx - r, cy - r * 1.35, cx + r, cy - r * 1.35, cx + r, cy);
    gota.curveTo(cx + r, cy + r * 0.6, cx + r * 0.6, cy + r * 1.2, cx, y + lado);
    gota.closePath();
    g.fill(gota);
    g.setColor(mezclar(base, Color.BLACK, 0.2));
    g.fill(new Ellipse2D.Double(cx - r * 0.42, cy - r * 0.42, r * 0.84, r * 0.84));
  }

  /** Alto total del bloque de texto con un tamaño de título dado, para poder centrarlo. */
  private int altoBloque(Graphics2D g, String nombrePena, Font fuentePena, List<String> titulo,
      Font fuenteTitulo, String fecha, Font fuenteFecha, String lugar, Font fuenteLugar,
      List<Etiqueta> etiquetas, Font fuenteEtiqueta) {
    int alto = 0;
    if (nombrePena != null) {
      alto += g.getFontMetrics(fuentePena).getHeight() + 14;
    }
    alto += titulo.size() * (int) (fuenteTitulo.getSize() * 1.08) + 20 + 6 + 26;
    if (fecha != null) {
      alto += g.getFontMetrics(fuenteFecha).getHeight() + 8;
    }
    if (lugar != null) {
      alto += g.getFontMetrics(fuenteLugar).getHeight() + 8;
    }
    alto += 12;
    int filas = filasEtiquetas(g, etiquetas, fuenteEtiqueta);
    if (filas > 0) {
      int altoEtiqueta = g.getFontMetrics(fuenteEtiqueta).getHeight() + 18;
      alto += filas * altoEtiqueta + (filas - 1) * 12;
    }
    return alto;
  }

  /** Cuántas filas ocupan las etiquetas, con el mismo reparto que al dibujarlas. */
  private int filasEtiquetas(Graphics2D g, List<Etiqueta> etiquetas, Font fuente) {
    FontMetrics fm = g.getFontMetrics(fuente);
    int filas = etiquetas.isEmpty() ? 0 : 1;
    int x = X_TEXTO;
    for (Etiqueta etiqueta : etiquetas) {
      int ancho = fm.stringWidth(etiqueta.texto()) + 36;
      if (x > X_TEXTO && x + ancho > X_TEXTO + ANCHO_TEXTO) {
        filas++;
        x = X_TEXTO;
      }
      x += ancho + 12;
    }
    return filas;
  }

  // ----------------------------------------------------------------
  // Utilidades de dibujo
  // ----------------------------------------------------------------

  /** Parte un texto en líneas que caben en el ancho; la última se recorta con puntos suspensivos. */
  private List<String> partir(Graphics2D g, String texto, Font fuente, int ancho, int maxLineas) {
    FontMetrics fm = g.getFontMetrics(fuente);
    List<String> lineas = new ArrayList<>();
    StringBuilder actual = new StringBuilder();
    for (String palabra : texto.trim().split("\\s+")) {
      String prueba = actual.isEmpty() ? palabra : actual + " " + palabra;
      if (fm.stringWidth(prueba) <= ancho || actual.isEmpty()) {
        actual.setLength(0);
        actual.append(prueba);
      } else {
        lineas.add(actual.toString());
        actual.setLength(0);
        actual.append(palabra);
      }
    }
    if (!actual.isEmpty()) {
      lineas.add(actual.toString());
    }
    if (lineas.size() > maxLineas) {
      List<String> recortadas = new ArrayList<>(lineas.subList(0, maxLineas));
      String resto = String.join(" ", lineas.subList(maxLineas - 1, lineas.size()));
      g.setFont(fuente);
      recortadas.set(maxLineas - 1, recortar(g, resto, ancho));
      return recortadas;
    }
    return lineas;
  }

  /** El texto tal cual si cabe; si no, recortado con "…". Usa la fuente activa. */
  private static String recortar(Graphics2D g, String texto, int ancho) {
    FontMetrics fm = g.getFontMetrics();
    if (fm.stringWidth(texto) <= ancho) {
      return texto;
    }
    String recorte = texto;
    while (!recorte.isEmpty() && fm.stringWidth(recorte + "…") > ancho) {
      recorte = recorte.substring(0, recorte.length() - 1);
    }
    return recorte.stripTrailing() + "…";
  }

  private Font fuente(int estilo, int tamano) {
    String nombre = familia;
    if (nombre == null) {
      Set<String> disponibles = Set.of(
          GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
      nombre = FUENTES_PREFERIDAS.stream().filter(disponibles::contains).findFirst()
          .orElse(Font.SANS_SERIF);
      familia = nombre;
      log.info("Cartel de eventos: fuente {}", nombre);
    }
    return new Font(nombre, estilo, tamano);
  }

  private Optional<BufferedImage> logoDe(PenaEntity pena) {
    if (pena == null || pena.getSlug() == null) {
      return Optional.empty();
    }
    try {
      return penaService.logo(pena.getSlug()).map(logo -> {
        try {
          return ImageIO.read(new ByteArrayInputStream(logo.contenido()));
        } catch (IOException e) {
          return null;
        }
      });
    } catch (RuntimeException e) {
      // Un logo ilegible (SVG, WEBP, base64 roto) no debe tumbar el cartel: salen las iniciales.
      log.warn("No se pudo leer el logo de la peña {} para el cartel", pena.getSlug());
      return Optional.empty();
    }
  }

  private static Color colorDe(PenaEntity pena) {
    if (pena == null || pena.getColor() == null) {
      return COLOR_POR_DEFECTO;
    }
    try {
      return Color.decode(pena.getColor().trim());
    } catch (NumberFormatException e) {
      return COLOR_POR_DEFECTO;
    }
  }

  private static void calidad(Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    g.setComposite(AlphaComposite.SrcOver);
  }

  private static double luminancia(Color c) {
    return (0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue()) / 255.0;
  }

  private static Color mezclar(Color a, Color b, double proporcionB) {
    double p = Math.max(0, Math.min(1, proporcionB));
    return new Color(
        (int) Math.round(a.getRed() * (1 - p) + b.getRed() * p),
        (int) Math.round(a.getGreen() * (1 - p) + b.getGreen() * p),
        (int) Math.round(a.getBlue() * (1 - p) + b.getBlue() * p));
  }

  private static Color conAlfa(Color c, int alfa) {
    return new Color(c.getRed(), c.getGreen(), c.getBlue(), alfa);
  }

  private static String iniciales(String nombre) {
    StringBuilder iniciales = new StringBuilder();
    for (String palabra : nombre.trim().split("\\s+")) {
      if (!palabra.isEmpty() && Character.isLetter(palabra.charAt(0))) {
        iniciales.append(Character.toUpperCase(palabra.charAt(0)));
      }
      if (iniciales.length() == 2) {
        break;
      }
    }
    return iniciales.isEmpty() ? "F" : iniciales.toString();
  }
}
