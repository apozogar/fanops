package com.softwells.fanops.service;

import com.softwells.fanops.controller.dto.PenaRequestDTO;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.repository.PenaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.text.Normalizer;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PenaService {

  /** Tipos de imagen admitidos para el logo de la peña. */
  private static final Set<String> TIPOS_IMAGEN_PERMITIDOS = Set.of(
      "image/png", "image/jpeg", "image/webp", "image/gif", "image/svg+xml");

  /**
   * Tamaño máximo de la imagen del logo. Se guarda en base64 en la propia BD y viaja en cada
   * respuesta de la API (login, carnet, cabecera), así que conviene mantenerlo pequeño.
   */
  private static final int TAMANO_MAXIMO_LOGO_BYTES = 1024 * 1024; // 1 MB

  /** Logo subido por el usuario: data URI en base64, p. ej. "data:image/png;base64,iVBOR..." */
  private static final Pattern PATRON_DATA_URI =
      Pattern.compile("^data:([a-z0-9.+/-]+);base64,([A-Za-z0-9+/=\\s]+)$",
          Pattern.CASE_INSENSITIVE);

  private final PenaRepository repository;

  public PenaEntity findById(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Pena no encontrada con ID: " + id));
  }

  /**
   * Peña por su identificador en la URL. Se consulta sin sesión (login y registro), así que quien
   * la use debe exponer solo los datos de identidad de la peña, nunca su ficha completa.
   */
  public PenaEntity findBySlug(String slug) {
    String normalizado = normalizarSlug(slug);
    return repository.findBySlugIgnoreCase(normalizado)
        .orElseThrow(() -> new EntityNotFoundException("No hay ninguna peña con el dominio: " + slug));
  }

  /**
   * Logo de la peña listo para servirse como imagen HTTP real.
   *
   * Existe porque el logo se guarda como data URI en base64 (ver {@link #validarLogo}), y eso
   * funciona bien en la propia app (login, carnet), pero los clientes de correo (Gmail el primero)
   * bloquean las imágenes {@code data:} embebidas en el HTML del correo: solo cargan imágenes que
   * sean una URL de verdad. Por eso los correos referencian este endpoint en vez del data URI.
   *
   * @return los bytes y el tipo MIME si el logo es un data URI, o la URL si es una de las
   *     antiguas (ver {@link #validarLogo}), para que el controlador redirija a ella
   */
  @Transactional(readOnly = true)
  public LogoPena obtenerLogo(String slug) {
    PenaEntity pena = findBySlug(slug);
    String logo = pena.getLogo();
    if (logo == null || logo.isBlank()) {
      throw new EntityNotFoundException("La peña '" + slug + "' no tiene logo.");
    }

    String valor = logo.trim();
    if (!valor.startsWith("data:")) {
      return LogoPena.deUrl(valor);
    }

    var matcher = PATRON_DATA_URI.matcher(valor);
    if (!matcher.matches()) {
      throw new EntityNotFoundException("El logo de la peña '" + slug + "' no es válido.");
    }
    byte[] bytes = Base64.getMimeDecoder().decode(matcher.group(2));
    return LogoPena.deBytes(bytes, matcher.group(1).toLowerCase());
  }

  /** Resultado de {@link #obtenerLogo}: o bien los bytes de la imagen, o una URL a la que redirigir. */
  public record LogoPena(byte[] bytes, String contentType, String url) {

    public static LogoPena deBytes(byte[] bytes, String contentType) {
      return new LogoPena(bytes, contentType, null);
    }

    public static LogoPena deUrl(String url) {
      return new LogoPena(null, null, url);
    }

    public boolean esUrlExterna() {
      return url != null;
    }
  }

  public List<PenaEntity> findAll() {
    return repository.findAll();
  }

  /**
   * Peña por defecto usada al dar de alta usuarios que todavía no tienen una peña asignada
   * explícitamente (p.ej. auto-registro público, import de Excel). Devuelve la primera peña
   * creada; hoy solo puede haber una en producción, así que preserva el comportamiento previo
   * al CRUD de peñas.
   */
  public PenaEntity getDefaultPena() {
    return repository.findAll().stream()
        .min(Comparator.comparing(PenaEntity::getId))
        .orElseThrow(() -> new IllegalStateException(
            "No existe ninguna peña dada de alta todavía. Un superadmin debe crear al menos una "
                + "peña antes de poder registrar usuarios."));
  }

  public PenaEntity create(PenaRequestDTO dto) {
    PenaEntity pena = new PenaEntity();
    applyChanges(pena, dto);
    return repository.save(pena);
  }

  public PenaEntity update(Long id, PenaRequestDTO dto) {
    PenaEntity pena = findById(id);
    applyChanges(pena, dto);
    return repository.save(pena);
  }

  public void delete(Long id) {
    PenaEntity pena = findById(id);
    if (!pena.getSocios().isEmpty()) {
      throw new IllegalArgumentException(
          "No se puede eliminar la peña '" + pena.getNombre()
              + "' porque tiene socios asociados. Reasigna o elimina primero sus socios.");
    }
    repository.delete(pena);
  }

  /**
   * Valida el logo recibido. La imagen se sube desde el navegador ya codificada en base64
   * (data URI) y se guarda tal cual en la BD, de forma que no dependa de ninguna URL externa que
   * pueda cambiar o dejar de estar disponible. Se siguen admitiendo URLs para no romper las peñas
   * dadas de alta antes de este cambio.
   *
   * @return el logo normalizado, o {@code null} si no hay logo
   */
  private String validarLogo(String logo) {
    if (logo == null || logo.isBlank()) {
      return null;
    }

    String valor = logo.trim();
    if (!valor.startsWith("data:")) {
      // Compatibilidad: logos antiguos apuntando a una URL o a un asset del frontend.
      return valor;
    }

    var matcher = PATRON_DATA_URI.matcher(valor);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          "El logo no es una imagen válida. Vuelve a seleccionar el fichero.");
    }

    String tipo = matcher.group(1).toLowerCase();
    if (!TIPOS_IMAGEN_PERMITIDOS.contains(tipo)) {
      throw new IllegalArgumentException(
          "Formato de imagen no admitido. Usa PNG, JPG, WEBP, GIF o SVG.");
    }

    byte[] imagen;
    try {
      imagen = Base64.getMimeDecoder().decode(matcher.group(2));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "El logo no es una imagen válida. Vuelve a seleccionar el fichero.");
    }
    if (imagen.length == 0) {
      throw new IllegalArgumentException("El logo está vacío. Vuelve a seleccionar el fichero.");
    }
    if (imagen.length > TAMANO_MAXIMO_LOGO_BYTES) {
      throw new IllegalArgumentException(
          "La imagen del logo no puede superar 1 MB. Reduce su tamaño e inténtalo de nuevo.");
    }

    return valor;
  }

  /**
   * Palabras que no pueden ser el dominio de una peña porque colisionarían con una ruta del
   * frontend o con un prefijo del servidor: la URL {@code /socios} tiene que seguir siendo la
   * pantalla de gestión de socios, no la peña llamada "Socios".
   *
   * Debe mantenerse alineada con las rutas de primer nivel de app.routes.ts y con los prefijos
   * excluidos en SpaForwardingController.
   */
  private static final Set<String> SLUGS_RESERVADOS = Set.of(
      "api", "auth", "assets", "media", "management", "swagger-ui", "swagger-resources",
      "configuration", "webjars", "index", "socios", "eventos", "penas", "cuotas", "informes",
      "carnet-socio", "cuotas-socio", "inscripciones", "inscripcion", "notfound", "admin",
      "public", "publica", "static");

  /** Longitud máxima del slug. Debe coincidir con la de la columna en PenaEntity. */
  private static final int LONGITUD_MAXIMA_SLUG = 60;

  /**
   * Normaliza un texto a un slug apto para la URL: sin acentos, en minúsculas y con todo lo que no
   * sea letra o dígito convertido en un guion.
   *
   * La descomposición Unicode antes de quitar las marcas diacríticas es lo que hace que "Peña" dé
   * "pena" y no "pea": la eñe se separa en "n" + tilde combinante, y solo se descarta la tilde.
   */
  public static String normalizarSlug(String texto) {
    if (texto == null) {
      return "";
    }

    String sinAcentos = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", "");

    String slug = sinAcentos.toLowerCase(Locale.ROOT)
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("^-+|-+$", "");

    return slug.length() > LONGITUD_MAXIMA_SLUG ? slug.substring(0, LONGITUD_MAXIMA_SLUG)
        .replaceAll("-+$", "") : slug;
  }

  /**
   * Decide el slug definitivo de una peña.
   *
   * Si el superadmin lo ha escrito, se respeta (normalizado) y se valida: un slug que ya usa otra
   * peña, o que colisiona con una ruta de la aplicación, es un error que hay que corregir, no algo
   * que convenga arreglar por detrás. Si lo deja vacío se deriva del nombre y ahí sí se desempata
   * automáticamente con un sufijo, porque el usuario no ha elegido nada.
   *
   * @param idActual id de la peña que se está editando, o {@code null} en un alta. Sirve para no
   *                 considerar colisión el slug que la peña ya tenía.
   */
  private String resolverSlug(String slugSolicitado, String nombre, Long idActual) {
    String propuesto = normalizarSlug(slugSolicitado);

    if (!propuesto.isEmpty()) {
      validarSlugDisponible(propuesto, idActual);
      return propuesto;
    }

    String base = normalizarSlug(nombre);
    if (base.isEmpty()) {
      base = "pena";
    }

    String candidato = base;
    int sufijo = 2;
    while (estaOcupado(candidato, idActual)) {
      candidato = base + "-" + sufijo++;
    }
    return candidato;
  }

  private void validarSlugDisponible(String slug, Long idActual) {
    if (SLUGS_RESERVADOS.contains(slug)) {
      throw new IllegalArgumentException("El dominio '" + slug
          + "' está reservado por la aplicación. Elige otro.");
    }
    if (repository.findBySlugIgnoreCase(slug)
        .filter(otra -> !otra.getId().equals(idActual))
        .isPresent()) {
      throw new IllegalArgumentException("El dominio '" + slug + "' ya lo usa otra peña.");
    }
  }

  private boolean estaOcupado(String slug, Long idActual) {
    if (SLUGS_RESERVADOS.contains(slug)) {
      return true;
    }
    return repository.findBySlugIgnoreCase(slug)
        .filter(otra -> !otra.getId().equals(idActual))
        .isPresent();
  }

  private void applyChanges(PenaEntity pena, PenaRequestDTO dto) {
    pena.setNombre(dto.getNombre());
    pena.setSlug(resolverSlug(dto.getSlug(), dto.getNombre(), pena.getId()));
    pena.setIniciadorId(dto.getIniciadorId());
    pena.setDireccion1(dto.getDireccion1());
    pena.setDireccion2(dto.getDireccion2());
    pena.setCuentaIban(dto.getCuentaIban());
    pena.setCuentaBic(dto.getCuentaBic());
    pena.setCuotaAdulto(dto.getCuotaAdulto());
    pena.setCuotaMenor(dto.getCuotaMenor());
    pena.setEdadMayoria(dto.getEdadMayoria());
    pena.setEdadJubilacion(dto.getEdadJubilacion());
    pena.setEventosPenalizacionPorFalta(dto.getEventosPenalizacionPorFalta());
    pena.setLogo(validarLogo(dto.getLogo()));
    pena.setLema(dto.getLema());
    pena.setColor(dto.getColor());
  }
}
