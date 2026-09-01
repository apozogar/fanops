package com.softwells.fanops.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.PenaRepository;
import com.softwells.fanops.repository.SocioRepository;
import com.softwells.fanops.repository.UsuarioRepository;
import com.softwells.fanops.service.EventoService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * El listado de eventos del socio tiene que traer el resumen del sorteo de carnets: es lo que hace
 * que la tarjeta ofrezca elegir entre apuntarse solo al evento o entrar en el sorteo. Sin este
 * test, un evento sin carnets y un fallo al serializar el resumen se ven exactamente igual desde
 * la pantalla: un único botón de "Inscribir".
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventoSorteoListadoTest {

  static final String EMAIL_USUARIO = "test.listado.sorteo@fanops.local";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private EventoService eventoService;
  @Autowired
  private SocioRepository socioRepository;
  @Autowired
  private UsuarioRepository usuarioRepository;
  @Autowired
  private PenaRepository penaRepository;

  @Test
  @DisplayName("Un evento con carnets llega al listado con el sorteo abierto a solicitudes")
  void elListadoTraeElResumenDelSorteo() throws Exception {
    cuentaConFicha();
    EventoEntity evento = eventoConCarnets(3, LocalDateTime.now().plusDays(3));

    JsonNode fila = filaDelListado(evento);

    assertThat(fila.hasNonNull("sorteo"))
        .as("sin el resumen la tarjeta no puede ofrecer la opción del sorteo")
        .isTrue();
    assertThat(fila.get("sorteo").get("plazasCarnet").asInt()).isEqualTo(3);
    assertThat(fila.get("sorteo").get("admiteSolicitudes").asBoolean())
        .as("el sorteo es futuro y el evento no tiene plazo cerrado: se puede entrar")
        .isTrue();
  }

  @Test
  @DisplayName("Un evento sin carnets llega sin sorteo, y la tarjeta solo ofrece inscribirse")
  void elListadoNoInventaSorteos() throws Exception {
    cuentaConFicha();
    EventoEntity evento = eventoConCarnets(0, null);

    assertThat(filaDelListado(evento).hasNonNull("sorteo")).isFalse();
  }

  @Test
  @DisplayName("Con el plazo de inscripción cerrado ya no se admiten entradas al bombo")
  void plazoCerradoCierraElBombo() throws Exception {
    cuentaConFicha();
    EventoEntity evento = eventoConCarnets(3, LocalDateTime.now().plusDays(3));
    evento.setFechaLimiteInscripcion(LocalDateTime.now().minusMinutes(1));
    eventoService.save(evento);

    JsonNode sorteo = filaDelListado(evento).get("sorteo");

    assertThat(sorteo.get("admiteSolicitudes").asBoolean())
        .as("entrar al bombo apunta al evento, así que no puede saltarse su plazo")
        .isFalse();
  }

  // ----------------------------------------------------------------
  // Utilidades del test
  // ----------------------------------------------------------------

  private JsonNode filaDelListado(EventoEntity evento) throws Exception {
    String cuerpo = mockMvc.perform(get("/api/eventos")
            .with(user(EMAIL_USUARIO).authorities(() -> "ROLE_USER")))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    JsonNode datos = new ObjectMapper().readTree(cuerpo).get("data");
    return StreamSupport.stream(datos.spliterator(), false)
        .filter(fila -> evento.getUid().toString().equals(fila.get("uid").asText()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("El evento de prueba no aparece en el listado"));
  }

  private EventoEntity eventoConCarnets(int carnets, LocalDateTime fechaSorteo) {
    EventoEntity evento = new EventoEntity();
    evento.setNombreEvento("Partido con carnets");
    evento.setFechaEvento(LocalDate.now().plusDays(10));
    evento.setNumeroPlazas(50);
    evento.setPlazasCarnet(carnets);
    evento.setFechaSorteoCarnet(fechaSorteo);
    return eventoService.save(evento);
  }

  private void cuentaConFicha() {
    PenaEntity pena = penaRepository.findAll().stream().findFirst().orElseGet(() -> {
      PenaEntity nueva = new PenaEntity();
      nueva.setNombre("Peña de pruebas");
      nueva.setSlug("pena-de-pruebas");
      return penaRepository.save(nueva);
    });

    UsuarioEntity usuario = new UsuarioEntity();
    usuario.setEmail(EMAIL_USUARIO);
    usuario.setPassword("no-se-usa");
    usuario.setActivo(true);
    usuario.setPena(pena);
    UsuarioEntity guardado = usuarioRepository.save(usuario);

    SocioEntity socio = new SocioEntity();
    socio.setNumeroSocio(socioRepository.findMaxNumeroSocio().orElse(0) + 1);
    socio.setNombre("Socio Del Listado");
    socio.setFechaAlta(LocalDate.now());
    socio.setActivo(true);
    socio.setPena(pena);
    socio.setUsuario(guardado);
    guardado.getSocios().add(socioRepository.save(socio));
    usuarioRepository.save(guardado);
  }
}
