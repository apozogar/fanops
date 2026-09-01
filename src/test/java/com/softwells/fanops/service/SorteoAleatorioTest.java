package com.softwells.fanops.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * El sorteo se anima en el navegador y se puede volver a ver cuantas veces se quiera, así que lo
 * que aquí se comprueba no es un detalle técnico: si el mismo bombo diese dos resultados
 * distintos, la repetición mostraría un ganador que no es el que se llevó el carnet.
 */
class SorteoAleatorioTest {

  private static final String SEMILLA = "0f1e2d3c4b5a69788796a5b4c3d2e1f0";

  private static List<String> participantes(int cuantos) {
    return IntStream.rangeClosed(1, cuantos).mapToObj(i -> "socio-" + i).toList();
  }

  @Test
  @DisplayName("La misma semilla y los mismos participantes dan siempre el mismo orden")
  void mismaSemillaMismoResultado() {
    List<String> candidatos = participantes(30);

    List<String> primera = SorteoAleatorio.extraer(candidatos, socio -> 1, SEMILLA);
    List<String> segunda = SorteoAleatorio.extraer(candidatos, socio -> 1, SEMILLA);

    assertThat(primera).isEqualTo(segunda);
  }

  @Test
  @DisplayName("Cambiar la semilla cambia el orden")
  void otraSemillaOtroResultado() {
    List<String> candidatos = participantes(30);

    List<String> conUna = SorteoAleatorio.extraer(candidatos, socio -> 1, SEMILLA);
    List<String> conOtra = SorteoAleatorio.extraer(candidatos, socio -> 1,
        "ffffffffffffffffffffffffffffffff");

    assertThat(conUna).isNotEqualTo(conOtra);
  }

  @Test
  @DisplayName("El bombo se vacía entero y sin repetir a nadie")
  void extraeATodosUnaSolaVez() {
    List<String> candidatos = participantes(25);

    List<String> orden = SorteoAleatorio.extraer(candidatos, socio -> 3, SEMILLA);

    assertThat(orden).hasSize(25).containsExactlyInAnyOrderElementsOf(candidatos);
  }

  @Test
  @DisplayName("Con más papeletas se gana más a menudo, pero el resto sigue teniendo opciones")
  void masPapeletasMasOpciones() {
    // "afortunado" entra con 10 papeletas y los otros nueve con 1: en un reparto justo se lleva
    // el primer puesto en torno al 53% de las veces (10 de 19), nunca siempre ni nunca nunca.
    List<String> candidatos = participantes(10);

    long victorias = IntStream.range(0, 400)
        .filter(i -> {
          List<String> orden = SorteoAleatorio.extraer(candidatos,
              socio -> "socio-1".equals(socio) ? 10 : 1,
              SorteoAleatorio.hash("sorteo-" + i));
          return "socio-1".equals(orden.get(0));
        })
        .count();

    assertThat(victorias).isBetween(150L, 300L);
  }

  @Test
  @DisplayName("El hash de la semilla es estable, que es lo que permite comprometerla antes")
  void hashEstable() {
    assertThat(SorteoAleatorio.hash("prueba")).isEqualTo(SorteoAleatorio.hash("prueba"));
    assertThat(SorteoAleatorio.hash("prueba")).hasSize(64);
  }
}
