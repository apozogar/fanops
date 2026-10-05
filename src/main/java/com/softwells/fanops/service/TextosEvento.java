package com.softwells.fanops.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Cómo se escriben las fechas, el plazo y el precio de un evento cuando se comparte fuera de la
 * aplicación: en la vista previa del enlace y en el cartel. Viven juntos para que los dos digan
 * exactamente lo mismo.
 */
final class TextosEvento {

  static final Locale ES = Locale.forLanguageTag("es-ES");

  private static final DateTimeFormatter FECHA_EVENTO =
      DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ES);
  private static final DateTimeFormatter FECHA_LIMITE =
      DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'a las' HH:mm", ES);
  /** Plazo que cae en el mismo mes que el evento: el mes ya se ha dicho, sobra repetirlo. */
  private static final DateTimeFormatter FECHA_LIMITE_MISMO_MES =
      DateTimeFormatter.ofPattern("EEEE d 'a las' HH:mm", ES);

  private TextosEvento() {
  }

  /** "Martes 13 de octubre". */
  static String fecha(LocalDate fecha) {
    return capitalizar(fecha.format(FECHA_EVENTO));
  }

  /** "domingo 11 a las 19:00", con el mes solo si no es el del evento. */
  static String plazo(LocalDateTime limite, LocalDate fechaEvento) {
    boolean mismoMes = fechaEvento != null
        && YearMonth.from(limite).equals(YearMonth.from(fechaEvento));
    return limite.format(mismoMes ? FECHA_LIMITE_MISMO_MES : FECHA_LIMITE);
  }

  /** "10 €" o "12,50 €", con espacio de no separación para que el € no se quede solo. */
  static String euros(BigDecimal importe) {
    NumberFormat formato = NumberFormat.getCurrencyInstance(ES);
    if (importe.stripTrailingZeros().scale() <= 0) {
      formato.setMaximumFractionDigits(0);
    }
    return formato.format(importe);
  }

  private static String capitalizar(String texto) {
    return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
  }
}
