package com.softwells.fanops.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee la cabecera {@code X-Pena-Id} (enviada por el selector de peña del panel de superadmin) y
 * la deja disponible en {@link PenaContextHolder} durante la petición. La cabecera solo tiene
 * efecto real para un usuario con ROLE_SUPERADMIN; para cualquier otro usuario se ignora, ya que
 * su peña de trabajo es siempre la suya propia (ver UsuarioService.obtenerPenaDelUsuarioAutenticado).
 */
@Component
@Slf4j
public class PenaContextFilter extends OncePerRequestFilter {

  public static final String HEADER_NAME = "X-Pena-Id";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    try {
      String header = request.getHeader(HEADER_NAME);
      if (header != null && !header.isBlank()) {
        try {
          PenaContextHolder.set(Long.parseLong(header.trim()));
        } catch (NumberFormatException e) {
          log.warn("Cabecera {} con valor no numérico ignorada: '{}'", HEADER_NAME, header);
        }
      }
      filterChain.doFilter(request, response);
    } finally {
      PenaContextHolder.clear();
    }
  }
}
