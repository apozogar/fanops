package com.softwells.fanops.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.repository.PenaRepository;
import com.softwells.fanops.service.EventoService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vista previa del enlace de inscripción en WhatsApp y compañía: sin sesión, la página tiene que
 * llevar ya en el HTML las etiquetas Open Graph del evento, porque esas aplicaciones no ejecutan
 * el JavaScript del frontend.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PrevisualizacionEnlaceTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private EventoService eventoService;
  @Autowired
  private PenaRepository penaRepository;

  @Test
  @DisplayName("El enlace de un evento lleva título, fecha, plazo y precio en las etiquetas OG")
  void enlaceConEtiquetas() throws Exception {
    EventoEntity evento = new EventoEntity();
    evento.setNombreEvento("Betis - Osasuna");
    evento.setFechaEvento(LocalDate.of(2030, 10, 9));
    evento.setFechaLimiteInscripcion(LocalDateTime.of(2030, 10, 7, 20, 0));
    evento.setUbicacion("Benito Villamarín");
    evento.setCostePlaza(new BigDecimal("15.00"));
    evento.setNumeroPlazas(50);
    UUID uid = eventoService.save(evento).getUid();

    mockMvc.perform(get("/inscripcion/" + uid).header("Host", "fanops.es")
            .header("X-Forwarded-Proto", "https"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(
            "<meta property=\"og:title\" content=\"Betis - Osasuna · Inscripción\"")))
        .andExpect(content().string(containsString("Miércoles 9 de octubre")))
        .andExpect(content().string(containsString("Benito Villamarín")))
        .andExpect(content().string(containsString("⏰ Plazo hasta el lunes 7 a las 20:00")))
        .andExpect(content().string(containsString("💶 15 € la plaza")))
        .andExpect(content().string(not(containsString("📅"))))
        // Las URLs salen del dominio por el que ha llegado la petición, no de la configuración.
        .andExpect(content().string(containsString(
            "<meta property=\"og:url\" content=\"https://fanops.es/inscripcion/" + uid + "\"")))
        .andExpect(content().string(containsString("<title>Betis - Osasuna · Inscripción")));
  }

  @Test
  @DisplayName("La vista previa apunta al cartel JPEG con la versión en la ruta, y se sirve")
  void cartelVersionado() throws Exception {
    EventoEntity evento = new EventoEntity();
    evento.setNombreEvento("Betis - Oporto");
    evento.setFechaEvento(LocalDate.now().plusDays(8));
    UUID uid = eventoService.save(evento).getUid();

    String html = mockMvc.perform(get("/inscripcion/" + uid).header("Host", "fanops.es")
            .header("X-Forwarded-Proto", "https"))
        .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    Matcher imagen = Pattern.compile("og:image\" content=\"https://fanops\\.es(/api/eventos/"
        + uid + "/cartel/[0-9a-f]+\\.jpg)\"").matcher(html);
    assertThat(imagen.find()).as(html).isTrue();

    mockMvc.perform(get(imagen.group(1)))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "image/jpeg"));
  }

  @Test
  @DisplayName("Un evento que no existe sirve la página normal, sin etiquetas")
  void eventoInexistente() throws Exception {
    mockMvc.perform(get("/inscripcion/" + UUID.randomUUID()))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("og:title"))));
    mockMvc.perform(get("/inscripcion/no-es-un-uuid"))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("og:title"))));
  }

  @Test
  @DisplayName("El logo de la peña se sirve como imagen pública")
  void logoComoImagen() throws Exception {
    byte[] png = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};
    PenaEntity pena = new PenaEntity();
    String slug = "pena-logo-" + UUID.randomUUID().toString().substring(0, 8);
    pena.setNombre("Peña con logo");
    pena.setSlug(slug);
    pena.setLogo("data:image/png;base64," + Base64.getEncoder().encodeToString(png));
    penaRepository.save(pena);

    mockMvc.perform(get("/api/pena/publica/" + slug + "/logo"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "image/png"))
        .andExpect(content().bytes(png));
  }
}
