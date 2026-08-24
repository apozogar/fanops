package com.softwells.fanops.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.PenaRepository;
import com.softwells.fanops.repository.SocioRepository;
import com.softwells.fanops.repository.UsuarioRepository;
import com.softwells.fanops.repository.VinculacionSocioRepository;
import com.softwells.fanops.service.EmailSender;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recorre el flujo completo de vinculación contra la base de datos y los endpoints reales:
 * registro con un email que ya está en el listado de socios, enlace recibido por correo y
 * confirmación.
 *
 * El test unitario del servicio cubre las reglas del token, pero no que el registro deje de
 * duplicar la ficha, que la invitación llegue con un enlace usable ni que los endpoints
 * devuelvan lo que espera el frontend. Eso es lo que se fija aquí.
 *
 * Va en una transacción que se deshace al terminar, así que no deja rastro en la base de datos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VinculacionSocioFlowTest {

  private static final String EMAIL = "test.vinculacion.flujo@fanops.local";

  /** Recupera el token del enlace del correo, que es la única vía de obtenerlo (se guarda hasheado). */
  private static final Pattern PATRON_TOKEN = Pattern.compile("token=([A-Za-z0-9_-]+)");

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private SocioRepository socioRepository;
  @Autowired
  private UsuarioRepository usuarioRepository;
  @Autowired
  private VinculacionSocioRepository vinculacionRepository;
  @Autowired
  private PenaRepository penaRepository;

  /** Se sustituye el envío real: el correo es de donde se saca el token del enlace. */
  @MockitoBean
  private EmailSender emailSender;

  @Test
  @DisplayName("Registrarse con un email del listado no duplica la ficha y la vincula por token")
  void flujoCompletoDeVinculacion() throws Exception {
    PenaEntity pena = penaDePruebas();
    int numero = socioRepository.findMaxNumeroSocio().orElse(0) + 1;
    SocioEntity titular = crearSocioSinCuenta("Titular De Prueba", numero, pena);
    SocioEntity familiar = crearSocioSinCuenta("Familiar De Prueba", numero + 1, pena);

    // 1. El registro no crea cuenta ni ficha: envía la invitación y lo dice al frontend.
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"nombre":"Titular De Prueba","email":"%s","password":"ClaveDePrueba123"}
                """.formatted(EMAIL)))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.data.requiereVerificacion").value(true))
        .andExpect(jsonPath("$.data.socio").doesNotExist());

    assertThat(socioRepository.findByEmailIgnoreCaseAndUsuarioIsNull(EMAIL))
        .as("el registro no debe crear una ficha nueva para un email que ya está en el listado")
        .hasSize(2);
    assertThat(usuarioRepository.findByEmailIgnoreCase(EMAIL))
        .as("la cuenta no existe hasta que se confirma el enlace")
        .isEmpty();
    assertThat(vinculacionRepository.findByEmailIgnoreCaseAndFechaUsoIsNull(EMAIL)).hasSize(1);

    // 2. El enlace del correo lleva un token usable.
    String token = tokenDelCorreoEnviado();

    mockMvc.perform(get("/api/auth/vinculacion").param("token", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.email").value(EMAIL))
        .andExpect(jsonPath("$.data.numeroSocio").value(numero))
        .andExpect(jsonPath("$.data.nombrePena").value(pena.getNombre()))
        // Las dos fichas del mismo email (el caso de una familia) van a la misma cuenta.
        .andExpect(jsonPath("$.data.fichas").value(2))
        // La contraseña ya se eligió al registrarse, así que no se vuelve a pedir.
        .andExpect(jsonPath("$.data.requierePassword").value(false));

    // 3. Confirmar crea la cuenta, la deja con sesión iniciada y vincula las fichas existentes.
    mockMvc.perform(post("/api/auth/vinculacion/confirmar")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty());

    Optional<UsuarioEntity> cuenta = usuarioRepository.findByEmailIgnoreCase(EMAIL);
    assertThat(cuenta).isPresent();
    assertThat(cuenta.get().getPena())
        .as("la peña la manda la ficha que ya existía, no la peña por defecto")
        .isEqualTo(pena);
    assertThat(titular.getUsuario()).isEqualTo(cuenta.get());
    assertThat(familiar.getUsuario()).isEqualTo(cuenta.get());
    assertThat(socioRepository.findByUsuarioEmail(EMAIL)).hasSize(2);

    // 4. El enlace es de un solo uso.
    mockMvc.perform(post("/api/auth/vinculacion/confirmar")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("usado")));
  }

  @Test
  @DisplayName("Un email que no está en el listado sigue creando cuenta y ficha al momento")
  void registroNormalSigueFuncionando() throws Exception {
    penaDePruebas();

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"nombre":"Socio Nuevo","email":"test.registro.nuevo@fanops.local","password":"ClaveDePrueba123"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.requiereVerificacion").value(false))
        .andExpect(jsonPath("$.data.socio.numeroSocio").isNumber());
  }

  private String tokenDelCorreoEnviado() {
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(emailSender, atLeastOnce())
        .enviar(eq(EMAIL), any(), eq("Vincula tu cuenta con tu ficha de socio"),
            captor.capture());

    String cuerpo = captor.getValue();
    assertThat(cuerpo).isNotNull();
    // El enlace va sin "#": el frontend enruta por ruta y el servidor la reenvía a index.html
    // (ver SpaWebConfig). Si el prefijo no fuera el correcto, la pantalla de vinculación no
    // se abriría y se perdería el token.
    assertThat(cuerpo).contains("/auth/vincular-socio?token=");

    Matcher matcher = PATRON_TOKEN.matcher(cuerpo);
    assertThat(matcher.find()).as("el correo debe llevar el enlace con el token").isTrue();
    return matcher.group(1);
  }

  private SocioEntity crearSocioSinCuenta(String nombre, int numeroSocio, PenaEntity pena) {
    SocioEntity socio = new SocioEntity();
    socio.setNombre(nombre);
    socio.setNumeroSocio(numeroSocio);
    socio.setEmail(EMAIL);
    socio.setFechaAlta(LocalDate.now());
    socio.setActivo(true);
    socio.setPena(pena);
    return socioRepository.save(socio);
  }

  /** Reutiliza una peña existente o crea una, para no depender del contenido de la base. */
  private PenaEntity penaDePruebas() {
    List<PenaEntity> penas = penaRepository.findAll();
    if (!penas.isEmpty()) {
      return penas.get(0);
    }
    PenaEntity pena = new PenaEntity();
    pena.setNombre("Peña de pruebas");
    // El dominio es obligatorio y único desde que cada peña se identifica por la URL.
    pena.setSlug("pena-de-pruebas");
    return penaRepository.save(pena);
  }
}
