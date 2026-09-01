package com.softwells.fanops.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwells.fanops.controller.dto.ValoresEventoDTO;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.PenaRepository;
import com.softwells.fanops.repository.UsuarioRepository;
import com.softwells.fanops.repository.ValoresEventoPenaRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

/**
 * Valores por defecto de los eventos de una peña.
 *
 * Lo que aquí se fija es que sean de la peña y no globales, y que guardar dos veces actualice la
 * misma fila: son los dos fallos que no se verían hasta tener dos peñas en producción, cuando una
 * empezara a proponer los precios de la otra.
 */
@SpringBootTest
@Transactional
@WithMockUser(username = ValoresEventoServiceTest.EMAIL_ADMIN)
class ValoresEventoServiceTest {

  static final String EMAIL_ADMIN = "test.valores.evento@fanops.local";

  @Autowired
  private ValoresEventoService valoresEventoService;
  @Autowired
  private ValoresEventoPenaRepository valoresRepository;
  @Autowired
  private UsuarioRepository usuarioRepository;
  @Autowired
  private PenaRepository penaRepository;

  @Test
  @DisplayName("Una peña sin configurar no sugiere nada")
  void sinConfigurarNoSugiereNada() {
    cuentaEnPenaNueva();

    ValoresEventoDTO valores = valoresEventoService.obtenerDeMiPena();

    assertThat(valores.getPlazas()).isNull();
    assertThat(valores.getCostePlaza()).isNull();
    assertThat(valores.getCarnets()).isNull();
    assertThat(valores.getCosteCarnet()).isNull();
    assertThat(valores.getCosteTotalEstimado()).isNull();
  }

  @Test
  @DisplayName("Lo guardado es lo que se propone después")
  void guardaYDevuelveLoGuardado() {
    cuentaEnPenaNueva();

    valoresEventoService.guardarEnMiPena(ValoresEventoDTO.builder()
        .plazas(55)
        .costePlaza(new BigDecimal("15.00"))
        .carnets(3)
        .costeCarnet(new BigDecimal("25.00"))
        .costeTotalEstimado(new BigDecimal("800.00"))
        .build());

    ValoresEventoDTO valores = valoresEventoService.obtenerDeMiPena();

    assertThat(valores.getPlazas()).isEqualTo(55);
    assertThat(valores.getCostePlaza()).isEqualByComparingTo("15.00");
    assertThat(valores.getCarnets()).isEqualTo(3);
    assertThat(valores.getCosteCarnet()).isEqualByComparingTo("25.00");
    assertThat(valores.getCosteTotalEstimado()).isEqualByComparingTo("800.00");
  }

  @Test
  @DisplayName("Las fechas se guardan relativas al evento: días antes y hora")
  void guardaLasFechasRelativas() {
    cuentaEnPenaNueva();

    valoresEventoService.guardarEnMiPena(ValoresEventoDTO.builder()
        .diasAntesFinInscripcion(3)
        .horaFinInscripcion(LocalTime.of(20, 0))
        .diasAntesSorteo(2)
        .horaSorteo(LocalTime.of(18, 30))
        .build());

    ValoresEventoDTO valores = valoresEventoService.obtenerDeMiPena();

    assertThat(valores.getDiasAntesFinInscripcion()).isEqualTo(3);
    assertThat(valores.getHoraFinInscripcion()).isEqualTo(LocalTime.of(20, 0));
    assertThat(valores.getDiasAntesSorteo()).isEqualTo(2);
    assertThat(valores.getHoraSorteo()).isEqualTo(LocalTime.of(18, 30));
  }

  @Test
  @DisplayName("Guardar dos veces actualiza la fila de la peña, no crea otra")
  void guardarDosVecesNoDuplica() {
    PenaEntity pena = cuentaEnPenaNueva();

    valoresEventoService.guardarEnMiPena(ValoresEventoDTO.builder().plazas(55).build());
    valoresEventoService.guardarEnMiPena(ValoresEventoDTO.builder().plazas(40).build());

    assertThat(valoresEventoService.obtenerDeMiPena().getPlazas()).isEqualTo(40);
    assertThat(valoresRepository.findByPenaId(pena.getId()))
        .as("la fila es única por peña")
        .isPresent();
    assertThat(valoresRepository.findAll().stream()
        .filter(v -> v.getPena().getId().equals(pena.getId()))
        .count())
        .isEqualTo(1);
  }

  @Test
  @DisplayName("Vaciar un valor deja de sugerirlo")
  void vaciarUnValorDejaDeSugerirlo() {
    cuentaEnPenaNueva();
    valoresEventoService.guardarEnMiPena(ValoresEventoDTO.builder().plazas(55).carnets(3).build());

    valoresEventoService.guardarEnMiPena(ValoresEventoDTO.builder().carnets(3).build());

    assertThat(valoresEventoService.obtenerDeMiPena().getPlazas()).isNull();
    assertThat(valoresEventoService.obtenerDeMiPena().getCarnets()).isEqualTo(3);
  }

  /** Cuenta de admin colgada de una peña recién creada, para no pisar la de desarrollo. */
  private PenaEntity cuentaEnPenaNueva() {
    PenaEntity pena = new PenaEntity();
    pena.setNombre("Peña de valores por defecto");
    pena.setSlug("pena-valores-" + System.nanoTime());
    PenaEntity guardada = penaRepository.save(pena);

    UsuarioEntity admin = new UsuarioEntity();
    admin.setEmail(EMAIL_ADMIN);
    admin.setPassword("no-se-usa");
    admin.setActivo(true);
    admin.setPena(guardada);
    usuarioRepository.save(admin);

    return guardada;
  }
}
