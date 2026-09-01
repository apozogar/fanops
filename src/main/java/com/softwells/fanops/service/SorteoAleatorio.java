package com.softwells.fanops.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Azar reproducible para los sorteos: a partir de una semilla y de la lista de participantes
 * siempre sale el mismo resultado.
 *
 * <p>El generador es un SHA-256 en modo contador y no {@link java.util.Random} a propósito.
 * El objetivo es que un socio desconfiado pueda rehacer el sorteo por su cuenta con la semilla
 * publicada, y para eso el algoritmo tiene que poder escribirse en cualquier lenguaje en diez
 * líneas:
 *
 * <ol>
 *   <li>{@code bytes = SHA-256(semilla + ":" + contador)}, con el contador empezando en 0 y
 *       subiendo de uno en uno a cada número que se pide.</li>
 *   <li>Los 8 primeros bytes se leen como entero big-endian sin signo y se llevan a [0,1)
 *       quedándose con sus 53 bits altos.</li>
 *   <li>Para cada extracción se suma el peso de los que quedan, se multiplica ese total por el
 *       número obtenido y se recorre la lista acumulando pesos hasta pasarse: ese es el
 *       extraído, que sale de la urna antes de la siguiente vuelta.</li>
 * </ol>
 *
 * <p>El bombo se vacía entero, no solo los premios: así queda un orden completo de suplentes y
 * una renuncia no obliga a repetir el sorteo.
 */
public final class SorteoAleatorio {

  private SorteoAleatorio() {
  }

  /** Semilla nueva de 128 bits en hexadecimal. */
  public static String nuevaSemilla() {
    byte[] bytes = new byte[16];
    new SecureRandom().nextBytes(bytes);
    return HexFormat.of().formatHex(bytes);
  }

  /** SHA-256 en hexadecimal, usado para comprometer la semilla antes de revelarla. */
  public static String hash(String texto) {
    return HexFormat.of().formatHex(digest(texto));
  }

  /**
   * Vacía el bombo: devuelve los candidatos en su orden de extracción, dando a cada uno tantas
   * opciones como papeletas tenga.
   *
   * @param candidatos en un orden estable y conocido de antemano; el resultado depende de él
   * @param peso       papeletas de cada candidato, mínimo 1
   * @param semilla    semilla en hexadecimal
   */
  public static <T> List<T> extraer(List<T> candidatos, ToIntFunction<T> peso, String semilla) {
    List<T> urna = new ArrayList<>(candidatos);
    List<T> extraidos = new ArrayList<>(urna.size());
    long contador = 0;

    while (!urna.isEmpty()) {
      long total = 0;
      for (T candidato : urna) {
        total += Math.max(1, peso.applyAsInt(candidato));
      }

      double objetivo = siguienteAleatorio(semilla, contador++) * total;
      // Si el redondeo del double deja el objetivo justo en el total, se queda el último: sin
      // este valor por defecto habría una probabilidad ínfima de no elegir a nadie.
      int elegido = urna.size() - 1;
      long acumulado = 0;
      for (int i = 0; i < urna.size(); i++) {
        acumulado += Math.max(1, peso.applyAsInt(urna.get(i)));
        if (objetivo < acumulado) {
          elegido = i;
          break;
        }
      }
      extraidos.add(urna.remove(elegido));
    }
    return extraidos;
  }

  /** Número en [0,1) a partir de la semilla y del número de extracción. */
  private static double siguienteAleatorio(String semilla, long contador) {
    byte[] bytes = digest(semilla + ":" + contador);
    long valor = 0;
    for (int i = 0; i < 8; i++) {
      valor = (valor << 8) | (bytes[i] & 0xFFL);
    }
    return (valor >>> 11) * 0x1.0p-53;
  }

  private static byte[] digest(String texto) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
    }
  }
}
