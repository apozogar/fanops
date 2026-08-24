package com.softwells.fanops.service;

import com.softwells.fanops.exception.EmailNoEnviadoException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Envío por SMTP. Es el transporte por defecto y el que se usa en desarrollo (Mailtrap).
 *
 * No sirve en el plan gratuito de Render, que bloquea la salida a los puertos SMTP: allí hay que
 * poner {@code app.email.proveedor=brevo} (o {@code resend}) para salir por HTTPS. Ver
 * {@link BrevoEmailSender} y {@link ResendEmailSender}.
 */
@Service
@ConditionalOnProperty(name = "app.email.proveedor", havingValue = "smtp", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailSender implements EmailSender {

  private final JavaMailSender mailSender;

  @Value("${mail.from.address}")
  private String fromAddress;

  @Value("${mail.active}")
  private boolean active;

  @Override
  public void enviar(String destinatario, String nombreDestinatario, String asunto,
      String cuerpoTexto, String cuerpoHtml) {
    try {
      if (!active) {
        log.warn(
            "Envío por SMTP desactivado (mail.active=false): NO se ha enviado el correo a {} "
                + "(asunto: {})", destinatario, asunto);
        return;
      }

      if (StringUtils.isNotBlank(cuerpoHtml)) {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(cuerpoTexto, cuerpoHtml);
        mailSender.send(mensaje);
      } else {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(fromAddress);
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpoTexto);
        mailSender.send(mensaje);
      }
      log.info("Email enviado por SMTP a {} (asunto: {}):\n cuerpo {}", destinatario, asunto,
          cuerpoTexto);
    } catch (Exception e) {
      throw new EmailNoEnviadoException("No se pudo enviar el correo por SMTP a " + destinatario,
          e);
    }
  }
}
