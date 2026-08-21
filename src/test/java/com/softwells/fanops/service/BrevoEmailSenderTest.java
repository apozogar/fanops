package com.softwells.fanops.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.softwells.fanops.exception.EmailNoEnviadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Comprueba la petición que se manda a Brevo sin salir a internet.
 *
 * Es la única forma de verificar el contrato antes de desplegar: si la cabecera de la clave, el
 * remitente o el cuerpo no son los que espera la API, el correo no sale y el fallo aparecería en
 * producción, justo en los enlaces de acceso (registro, vinculación y recuperar contraseña).
 */
class BrevoEmailSenderTest {

  private static final String URL = "https://api.brevo.com/v3/smtp/email";

  private MockRestServiceServer servidor;
  private BrevoEmailSender sender;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    servidor = MockRestServiceServer.bindTo(builder).build();
    sender = new BrevoEmailSender(builder);
    ReflectionTestUtils.setField(sender, "url", URL);
    ReflectionTestUtils.setField(sender, "apiKey", "clave-de-prueba");
    ReflectionTestUtils.setField(sender, "fromAddress", "noreply@mi-pena.test");
    ReflectionTestUtils.setField(sender, "fromName", "FanOps");
  }

  @Test
  @DisplayName("Manda el correo con la cabecera api-key y el cuerpo que espera Brevo")
  void enviaConElContratoDeBrevo() {
    servidor.expect(requestTo(URL))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("api-key", "clave-de-prueba"))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.sender.email").value("noreply@mi-pena.test"))
        .andExpect(jsonPath("$.sender.name").value("FanOps"))
        .andExpect(jsonPath("$.to[0].email").value("socio@ejemplo.test"))
        .andExpect(jsonPath("$.to[0].name").value("Un Socio"))
        .andExpect(jsonPath("$.subject").value("Vincula tu cuenta"))
        .andExpect(jsonPath("$.textContent").value("Abre este enlace: https://ejemplo.test"))
        .andRespond(withStatus(HttpStatus.CREATED)
            .body("{\"messageId\":\"<abc@brevo>\"}")
            .contentType(MediaType.APPLICATION_JSON));

    sender.enviar("socio@ejemplo.test", "Un Socio", "Vincula tu cuenta",
        "Abre este enlace: https://ejemplo.test");

    servidor.verify();
  }

  @Test
  @DisplayName("Sin nombre de destinatario no manda el campo 'name'")
  void omiteElNombreCuandoNoSeConoce() {
    servidor.expect(requestTo(URL))
        .andExpect(jsonPath("$.to[0].email").value("socio@ejemplo.test"))
        .andExpect(jsonPath("$.to[0].name").doesNotExist())
        .andRespond(withSuccess());

    assertThatCode(() -> sender.enviar("socio@ejemplo.test", null, "Asunto", "Cuerpo"))
        .doesNotThrowAnyException();
    servidor.verify();
  }

  @Test
  @DisplayName("Si Brevo rechaza el envío, el error se propaga en lugar de perderse")
  void propagaElErrorDeLaApi() {
    servidor.expect(requestTo(URL))
        .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
            .body("{\"message\":\"Key not found\"}")
            .contentType(MediaType.APPLICATION_JSON));

    assertThatThrownBy(() -> sender.enviar("socio@ejemplo.test", "Un Socio", "Asunto", "Cuerpo"))
        .isInstanceOf(EmailNoEnviadoException.class)
        .hasMessageContaining("socio@ejemplo.test");
  }

  @Test
  @DisplayName("Sin clave de API falla claro y no intenta el envío")
  void exigeLaClaveDeApi() {
    ReflectionTestUtils.setField(sender, "apiKey", "");

    assertThatThrownBy(() -> sender.enviar("socio@ejemplo.test", "Un Socio", "Asunto", "Cuerpo"))
        .isInstanceOf(EmailNoEnviadoException.class)
        .hasMessageContaining("BREVO_API_KEY");

    servidor.verify(); // no se esperaba ninguna petición
  }
}
