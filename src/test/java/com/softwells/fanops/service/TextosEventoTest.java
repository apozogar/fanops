package com.softwells.fanops.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwells.fanops.model.EventoEntity;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TextosEventoTest {

  private static final BigDecimal DIEZ = new BigDecimal("10.00");
  private static final BigDecimal QUINCE = new BigDecimal("15");

  @Test
  @DisplayName("Sin precio de no socio, el mensaje dice un solo precio")
  void unSoloPrecio() {
    assertThat(TextosEvento.precioPlaza(DIEZ, null)).isEqualTo("10 € la plaza");
    assertThat(TextosEvento.precioPlaza(DIEZ, DIEZ)).isEqualTo("10 € la plaza");
  }

  @Test
  @DisplayName("Con dos precios distintos, el mensaje dice los dos")
  void dosPrecios() {
    assertThat(TextosEvento.precioPlaza(DIEZ, QUINCE))
        .isEqualTo("10 € socios · 15 € no socios");
  }

  @Test
  @DisplayName("Sin ningún precio no hay texto")
  void sinPrecio() {
    assertThat(TextosEvento.precioPlaza(null, null)).isNull();
  }

  @Test
  @DisplayName("El no socio paga lo del socio si no se indicó otro precio")
  void importeSegunSocio() {
    EventoEntity evento = new EventoEntity();
    evento.setCostePlaza(DIEZ);
    assertThat(evento.costePlazaPara(false)).isEqualTo(DIEZ);
    evento.setCostePlazaNoSocio(QUINCE);
    assertThat(evento.costePlazaPara(false)).isEqualTo(QUINCE);
    assertThat(evento.costePlazaPara(true)).isEqualTo(DIEZ);
  }
}
