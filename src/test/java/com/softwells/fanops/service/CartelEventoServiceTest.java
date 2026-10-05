package com.softwells.fanops.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwells.fanops.model.EventoEntity;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * El cartel de la vista previa sale con el tamaño que WhatsApp pinta a todo el ancho. Además deja
 * una copia en {@code target/carteles} para poder mirarlo a ojo tras tocar el diseño.
 */
@SpringBootTest
@Transactional
class CartelEventoServiceTest {

  @Autowired
  private CartelEventoService cartelEventoService;
  @Autowired
  private EventoService eventoService;

  @Test
  @DisplayName("Genera un JPEG de 1200×630 con un evento completo")
  void cartelCompleto() throws Exception {
    EventoEntity evento = new EventoEntity();
    evento.setNombreEvento("Betis - Oporto");
    evento.setFechaEvento(LocalDate.now().plusDays(8));
    evento.setFechaLimiteInscripcion(LocalDateTime.now().plusDays(6).withHour(19).withMinute(0));
    evento.setUbicacion("Estadio de la Cartuja");
    evento.setCostePlaza(new BigDecimal("10.00"));
    evento.setNumeroPlazas(50);

    byte[] jpeg = generar(evento, "completo");

    // Por peso: WhatsApp reduce a miniatura o descarta las imágenes pesadas.
    assertThat(jpeg.length).isLessThan(200 * 1024);
    BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(jpeg));
    assertThat(imagen.getWidth()).isEqualTo(CartelEventoService.ANCHO);
    assertThat(imagen.getHeight()).isEqualTo(CartelEventoService.ALTO);
  }

  @Test
  @DisplayName("Un nombre de evento largo no se sale del cartel")
  void nombreLargo() throws Exception {
    EventoEntity evento = new EventoEntity();
    evento.setNombreEvento("Real Betis Balompié - Olympique de Lyon (Europa League, cuartos)");
    evento.setFechaEvento(LocalDate.now().plusDays(20));
    evento.setUbicacion("Estadio Benito Villamarín, Sevilla");
    evento.setCostePlaza(new BigDecimal("12.50"));

    byte[] png = generar(evento, "nombre-largo");

    assertThat(ImageIO.read(new ByteArrayInputStream(png)).getWidth())
        .isEqualTo(CartelEventoService.ANCHO);
  }

  @Test
  @DisplayName("Un evento que no existe no tiene cartel")
  void eventoInexistente() {
    assertThat(cartelEventoService.cartel(UUID.randomUUID())).isEmpty();
  }

  private byte[] generar(EventoEntity evento, String nombre) throws Exception {
    UUID uid = eventoService.save(evento).getUid();
    byte[] png = cartelEventoService.cartel(uid).orElseThrow();
    Path carpeta = Path.of("target", "carteles");
    Files.createDirectories(carpeta);
    Files.write(carpeta.resolve(nombre + ".jpg"), png);
    return png;
  }
}
