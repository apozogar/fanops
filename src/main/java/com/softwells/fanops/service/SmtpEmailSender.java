package com.softwells.fanops.service;

import com.softwells.fanops.exception.EmailNoEnviadoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envío por SMTP. Es el transporte por defecto y el que se usa en desarrollo (Mailtrap).
 *
 * No sirve en el plan gratuito de Render, que bloquea la salida a los puertos SMTP: allí hay que
 * poner {@code app.email.proveedor=brevo} para salir por HTTPS. Ver {@link BrevoEmailSender}.
 */
@Service
@ConditionalOnProperty(name = "app.email.proveedor", havingValue = "smtp", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailSender implements EmailSender {

  private final JavaMailSender mailSender;

  @Value("${mail.from.address}")
  private String fromAddress;

  @Override
  public void enviar(String destinatario, String nombreDestinatario, String asunto,
      String cuerpo) {
    try {
      SimpleMailMessage mensaje = new SimpleMailMessage();
      mensaje.setFrom(fromAddress);
      mensaje.setTo(destinatario);
      mensaje.setSubject(asunto);
      mensaje.setText(cuerpo);
      mailSender.send(mensaje);
      log.info("Email enviado por SMTP a {} (asunto: {}):\n cuerpo {}", destinatario, asunto, cuerpo);
    } catch (Exception e) {
      throw new EmailNoEnviadoException(
          "No se pudo enviar el correo por SMTP a " + destinatario, e);
    }
  }
}
