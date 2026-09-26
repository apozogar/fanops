package com.softwells.fanops.service;

import com.softwells.fanops.controller.dto.HistorialAccesoDto;
import com.softwells.fanops.model.HistorialAccesoEntity;
import com.softwells.fanops.repository.HistorialAccesoRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Historial de accesos a la aplicación, para que el superadmin pueda monitorizar la actividad:
 * quién ha entrado (o lo ha intentado) y cuándo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HistorialAccesoService {

  private final HistorialAccesoRepository repository;

  /**
   * Deja constancia de un intento de acceso, correcto o no. Deliberadamente silencioso: un fallo
   * al guardar el historial no debe impedir el login.
   */
  @Transactional
  public void registrar(String email, boolean exito, String ip, String userAgent) {
    try {
      HistorialAccesoEntity acceso = new HistorialAccesoEntity();
      acceso.setEmail(email);
      acceso.setExito(exito);
      acceso.setFecha(LocalDateTime.now());
      acceso.setIp(ip);
      acceso.setUserAgent(userAgent);
      repository.save(acceso);
    } catch (Exception e) {
      log.warn("No se pudo registrar el acceso de '{}' en el historial", email, e);
    }
  }

  @Transactional(readOnly = true)
  public List<HistorialAccesoDto> listarUltimos() {
    return repository.findTop200ByOrderByFechaDesc().stream()
        .map(a -> new HistorialAccesoDto(a.getEmail(), a.isExito(), a.getFecha(), a.getIp(),
            a.getUserAgent()))
        .toList();
  }
}
