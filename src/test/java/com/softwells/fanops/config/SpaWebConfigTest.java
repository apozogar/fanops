package com.softwells.fanops.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Fija el reparto de rutas entre la API y el frontend.
 *
 * Es el invariante del que dependen tanto {@link SpaWebConfig} como SecurityConfig desde que el
 * frontend dejó de enrutar por hash: <strong>todo lo que cuelga de {@code /api} es API y todo lo
 * demás es navegación del frontend</strong>. Sin test, romperlo no da ningún error visible: la
 * aplicación arranca igual y el síntoma aparece en el navegador como un 404 al recargar una
 * pantalla, o como HTML devuelto donde el cliente esperaba JSON.
 *
 * <p>{@code useDefaultFilters = false} deja fuera todos los controladores de la aplicación: aquí
 * solo se ejercita el manejador de recursos, y escanearlos obligaría a simular todos sus
 * servicios sin aportar nada a lo que se quiere comprobar.
 *
 * <p>El index.html que resuelve el reenvío es el de src/test/resources/static: el de verdad lo
 * genera el build del frontend y no está en el árbol de fuentes.
 */
@WebMvcTest(useDefaultFilters = false)
@Import(SpaWebConfig.class)
class SpaWebConfigTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("La raíz sirve el frontend")
  void raizSirveElFrontend() throws Exception {
    // Solo se comprueba el estado: la raíz la resuelve la página de bienvenida de Spring Boot
    // mediante un forward a index.html, y MockMvc no sigue los forwards, así que aquí el cuerpo
    // llega vacío aunque un contenedor real sirva el HTML.
    mockMvc.perform(get("/")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("Una ruta del frontend sirve index.html en lugar de dar 404")
  void rutaDelFrontendSirveIndex() throws Exception {
    // Sin esto, recargar la página en /auth/login daría 404: es lo que rompía al quitar el hash.
    mockMvc.perform(get("/auth/login"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("FanOps")));

    mockMvc.perform(get("/socios")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("El dominio de una peña sirve el frontend, con y sin ruta detrás")
  void dominioDePenaSirveElFrontend() throws Exception {
    mockMvc.perform(get("/mi-pena")).andExpect(status().isOk());
    mockMvc.perform(get("/mi-pena/auth/login")).andExpect(status().isOk());
    mockMvc.perform(get("/mi-pena/auth/register")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("Una ruta inexistente de la API da 404 y no el HTML del frontend")
  void rutaInexistenteDeApiNoDevuelveElFrontend() throws Exception {
    // Si devolviera el HTML con un 200, un error de la API se manifestaría en el cliente como
    // una respuesta JSON ilegible y sería muy difícil de diagnosticar.
    mockMvc.perform(get("/api/esto-no-existe")).andExpect(status().isNotFound());
    mockMvc.perform(get("/management/esto-no-existe")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Un fichero que no existe da 404 y no el HTML del frontend")
  void ficheroInexistenteNoDevuelveElFrontend() throws Exception {
    // Un bundle con hash que ya no existe debe fallar como tal, no devolver HTML donde el
    // navegador espera un script.
    mockMvc.perform(get("/chunk-QUE-NO-EXISTE.js")).andExpect(status().isNotFound());
    mockMvc.perform(get("/assets/no-existe.png")).andExpect(status().isNotFound());
  }
}
