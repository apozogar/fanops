package com.softwells.fanops.service;

import com.softwells.fanops.exception.EmailNoEnviadoException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Envío por la API HTTP de Resend (POST https://api.resend.com/emails).
 *
 * Es otro transporte válido para el plan gratuito de Render: al ir por HTTPS (443) no le afecta
 * el bloqueo de los puertos SMTP. Se activa con {@code app.email.proveedor=resend} y necesita
 * {@code RESEND_API_KEY}. Para pruebas se puede usar como remitente {@code onboarding@resend.dev}
 * sin verificar ningún dominio; para producción real hay que verificar un dominio propio en
 * Resend y usar un remitente de ese dominio en {@code MAIL_FROM_ADDRESS}.
 */
@Service
@ConditionalOnProperty(name = "app.email.proveedor", havingValue = "resend")
@RequiredArgsConstructor
@Slf4j
public class ResendEmailSender implements EmailSender {

  private final RestClient.Builder restClientBuilder;

  @Value("${app.email.resend.url}")
  private String url;

  @Value("${app.email.resend.api-key:}")
  private String apiKey;

  @Value("${mail.from.address}")
  private String fromAddress;

  @Value("${app.email.remitente-nombre}")
  private String fromName;

  @Override
  public void enviar(String destinatario, String nombreDestinatario, String asunto,
      String cuerpo) {
    if (StringUtils.isBlank(apiKey)) {
      // Mejor fallar claro que dejar de enviar correos sin que nadie se entere.
      throw new EmailNoEnviadoException(
          "Falta la clave de API de Resend: configura RESEND_API_KEY para poder enviar correos.");
    }

    try {
      restClientBuilder.build()
          .post()
          .uri(url)
          .header("Authorization", "Bearer " + apiKey)
          .contentType(MediaType.APPLICATION_JSON)
          .body(cuerpoPeticion(destinatario, nombreDestinatario, asunto, cuerpo))
          .retrieve()
          .toBodilessEntity();
      log.info("Email enviado por la API de Resend a {} (asunto: {})", destinatario, asunto);
    } catch (Exception e) {
      throw new EmailNoEnviadoException(
          "No se pudo enviar el correo por la API de Resend a " + destinatario, e);
    }
  }

  private Map<String, Object> cuerpoPeticion(String destinatario, String nombreDestinatario,
      String asunto, String cuerpo) {
    String destinatarioCompleto =
        StringUtils.isNotBlank(nombreDestinatario) ? nombreDestinatario + " <" + destinatario + ">"
            : destinatario;

    Map<String, Object> peticion = new LinkedHashMap<>();
    peticion.put("from", fromName + " <" + fromAddress + ">");
    peticion.put("to", List.of(destinatarioCompleto));
    peticion.put("subject", asunto);
    peticion.put("text", cuerpo);
    return peticion;
  }
}
