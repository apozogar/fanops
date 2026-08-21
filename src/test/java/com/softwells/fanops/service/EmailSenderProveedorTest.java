package com.softwells.fanops.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Con {@code app.email.proveedor=brevo}, que es la configuración que corre en Render, el
 * contexto tiene que arrancar con el sender de la API HTTP y sin el de SMTP.
 *
 * Sin esta comprobación, una errata en la condición de los beans no se vería hasta el despliegue:
 * o no habría ningún EmailSender y la aplicación no arrancaría, o seguiría enviando por SMTP
 * contra unos puertos bloqueados. El resto de los tests ya ejercitan el valor por defecto (smtp).
 */
@SpringBootTest(properties = "app.email.proveedor=brevo")
class EmailSenderProveedorTest {

  @Autowired
  private EmailSender emailSender;

  @Test
  @DisplayName("El proveedor 'brevo' activa el envío por API HTTP")
  void brevoActivaElSenderDeLaApi() {
    assertThat(emailSender).isInstanceOf(BrevoEmailSender.class);
  }
}
