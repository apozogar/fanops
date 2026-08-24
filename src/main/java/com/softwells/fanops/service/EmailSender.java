package com.softwells.fanops.service;

/**
 * Envío de un correo, opcionalmente con una versión HTML además de la de texto plano.
 *
 * Existe como abstracción porque el transporte depende de dónde esté desplegada la aplicación:
 * en local se usa SMTP contra el sandbox de Mailtrap, pero el plan gratuito de Render bloquea
 * la salida a los puertos SMTP (25, 465 y 587), así que allí hay que enviar por la API HTTP del
 * proveedor, que va por el 443. Cambiar de uno a otro es la variable {@code app.email.proveedor}
 * y ningún cambio de código.
 *
 * <p>Las implementaciones lanzan {@link com.softwells.fanops.exception.EmailNoEnviadoException}
 * si el envío falla, en lugar de tragárselo: un enlace de vinculación o de recuperación de
 * contraseña que no sale es un error que quien lo ha pedido tiene que ver, no una línea de log.
 */
public interface EmailSender {

  /**
   * Envío solo de texto plano, para los correos que todavía no tienen versión HTML.
   *
   * @param destinatario       dirección de correo del destinatario
   * @param nombreDestinatario nombre para la cabecera To, o {@code null} si no se conoce
   * @param asunto             asunto del mensaje
   * @param cuerpoTexto        cuerpo en texto plano
   */
  default void enviar(String destinatario, String nombreDestinatario, String asunto,
      String cuerpoTexto) {
    enviar(destinatario, nombreDestinatario, asunto, cuerpoTexto, null);
  }

  /**
   * @param destinatario       dirección de correo del destinatario
   * @param nombreDestinatario nombre para la cabecera To, o {@code null} si no se conoce
   * @param asunto             asunto del mensaje
   * @param cuerpoTexto        cuerpo en texto plano (siempre se manda, como alternativa para
   *                           clientes de correo que no muestran HTML)
   * @param cuerpoHtml         cuerpo en HTML, o {@code null}/vacío para enviar solo texto plano
   */
  void enviar(String destinatario, String nombreDestinatario, String asunto, String cuerpoTexto,
      String cuerpoHtml);
}
