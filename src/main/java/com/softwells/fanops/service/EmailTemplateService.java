package com.softwells.fanops.service;

import com.softwells.fanops.model.PenaEntity;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Genera el HTML de los correos con la identidad visual de la peña (nombre y color), para que
 * quien lo recibe vea claramente de qué peña viene, no de "FanOps" a secas.
 *
 * <p>Es solo maquetación con estilos inline y tablas, porque es lo único que se renderiza de
 * forma fiable en la mayoría de clientes de correo (Gmail, Outlook...). Todas las
 * implementaciones de {@link EmailSender} mandan además el texto plano equivalente como
 * alternativa para clientes que no muestran HTML.
 */
@Service
public class EmailTemplateService {

  /** Color de acento cuando la peña no tiene uno configurado. */
  private static final String COLOR_POR_DEFECTO = "#2f6f4f";

  /**
   * @param pena       peña cuya identidad se muestra en la cabecera, o {@code null} si el correo
   *                   no está asociado a ninguna (se usa entonces la marca genérica de FanOps)
   * @param titulo     título destacado dentro de la tarjeta del correo
   * @param parrafos   párrafos del cuerpo, en texto plano (se escapan al insertarlos en el HTML)
   * @param textoBoton texto del botón de llamada a la acción, o {@code null} para no mostrarlo
   * @param urlBoton   enlace del botón, o {@code null} para no mostrarlo
   */
  public String renderizar(PenaEntity pena, String titulo, List<String> parrafos,
      String textoBoton, String urlBoton) {
    String color =
        pena != null && StringUtils.isNotBlank(pena.getColor()) ? pena.getColor()
            : COLOR_POR_DEFECTO;
    String nombrePena =
        pena != null && StringUtils.isNotBlank(pena.getNombre()) ? pena.getNombre() : "FanOps";

    StringBuilder html = new StringBuilder();
    html.append("<!doctype html><html lang=\"es\"><body style=\"margin:0;padding:0;")
        .append("background-color:#f2f2f5;font-family:Arial,Helvetica,sans-serif;\">");
    html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"")
        .append(" style=\"background-color:#f2f2f5;padding:32px 16px;\"><tr><td align=\"center\">");
    html.append("<table role=\"presentation\" width=\"480\" cellpadding=\"0\" cellspacing=\"0\"")
        .append(" style=\"max-width:480px;width:100%;background-color:#ffffff;")
        .append("border-radius:12px;overflow:hidden;\">");

    // Barra de acento con el color de la peña.
    html.append("<tr><td style=\"background-color:").append(escapeAttr(color))
        .append(";height:6px;line-height:6px;font-size:0;\">&nbsp;</td></tr>");

    // Cabecera: nombre de la peña.
    html.append("<tr><td style=\"padding:32px 32px 8px 32px;text-align:center;\">");
    html.append("<div style=\"font-size:13px;font-weight:bold;letter-spacing:0.5px;")
        .append("color:").append(escapeAttr(color)).append(";text-transform:uppercase;\">")
        .append(escapeHtml(nombrePena)).append("</div>");
    html.append("</td></tr>");

    // Título y párrafos del cuerpo.
    html.append("<tr><td style=\"padding:8px 32px 0 32px;\">")
        .append("<h1 style=\"font-size:20px;color:#1a1a1a;margin:0 0 16px 0;\">")
        .append(escapeHtml(titulo)).append("</h1></td></tr>");
    html.append("<tr><td style=\"padding:0 32px;color:#3c3c43;font-size:15px;line-height:1.5;\">");
    for (String parrafo : parrafos) {
      html.append("<p style=\"margin:0 0 16px 0;\">").append(escapeHtml(parrafo)).append("</p>");
    }
    html.append("</td></tr>");

    // Botón de llamada a la acción.
    if (StringUtils.isNotBlank(urlBoton) && StringUtils.isNotBlank(textoBoton)) {
      html.append("<tr><td style=\"padding:8px 32px 32px 32px;text-align:center;\">")
          .append("<a href=\"").append(escapeAttr(urlBoton)).append("\" style=\"")
          .append("display:inline-block;background-color:").append(escapeAttr(color))
          .append(";color:#ffffff;text-decoration:none;font-weight:bold;font-size:15px;")
          .append("padding:12px 28px;border-radius:8px;\">").append(escapeHtml(textoBoton))
          .append("</a></td></tr>");
    } else {
      html.append("<tr><td style=\"height:16px;\">&nbsp;</td></tr>");
    }

    // Pie.
    html.append("<tr><td style=\"padding:16px 32px 24px 32px;border-top:1px solid #eeeeee;")
        .append("text-align:center;color:#9a9aa2;font-size:12px;\">")
        .append("Este correo lo envía ").append(escapeHtml(nombrePena))
        .append(" a través de FanOps.</td></tr>");

    html.append("</table></td></tr></table></body></html>");
    return html.toString();
  }

  private String escapeHtml(String texto) {
    return texto == null ? ""
        : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  private String escapeAttr(String texto) {
    return texto == null ? "" : texto.replace("\"", "&quot;");
  }
}
