package com.softwells.fanops.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.service.EmailTemplateService.DatosEvento;
import com.softwells.fanops.service.EmailTemplateService.Estado;
import com.softwells.fanops.service.EmailTemplateService.Tono;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailTemplateServiceTest {

  private final EmailTemplateService plantillas = new EmailTemplateService();

  @Test
  @DisplayName("El correo de un evento lleva la peña, la tarjeta del evento, los estados y el botón")
  void correoDeEvento() {
    PenaEntity pena = new PenaEntity();
    pena.setNombre("Peña Bética");
    pena.setColor("#0a7d3c");

    String html = plantillas.renderizarEvento(pena, "¡Plaza confirmada!",
        List.of("Hola Ana,"),
        new DatosEvento("Betis - Oporto", "Miércoles 14 de octubre", "La Cartuja",
            "10 € socios · 12 € no socios"),
        List.of(new Estado("Ana: plaza confirmada", Tono.OK),
            new Estado("Luis: lista de espera", Tono.AVISO)),
        "Ver el evento", "https://fanops.es/inscripcion/abc");

    assertThat(html)
        .contains("Peña Bética", "#0a7d3c", "¡Plaza confirmada!", "Hola Ana,")
        .contains("Betis - Oporto", "Miércoles 14 de octubre", "La Cartuja", "socios")
        .contains("Ana: plaza confirmada", "Luis: lista de espera")
        .contains("href=\"https://fanops.es/inscripcion/abc\"", "Ver el evento");
  }

  @Test
  @DisplayName("Lo que escribe el usuario se escapa en el HTML")
  void escapaElTexto() {
    String html = plantillas.renderizarEvento(null, "Hola", List.of("<script>x</script>"),
        new DatosEvento("A & B", null, null, null), null, null, null);

    assertThat(html).doesNotContain("<script>").contains("&lt;script&gt;", "A &amp; B");
  }
}
