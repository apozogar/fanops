package com.softwells.fanops.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Celebra los sorteos de carnet cuando les llega la hora, sin que nadie tenga que pulsar nada.
 *
 * <p>No es la única vía: consultar el sorteo también lo celebra si ya estaba vencido. Hacen falta
 * las dos, porque en un despliegue que se duerme por inactividad puede no haber nadie ejecutando
 * el planificador a la hora exacta, y al revés, un sorteo sin espectadores tiene que celebrarse
 * igual para que salgan los avisos.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SorteoCarnetScheduler {

  private final SorteoCarnetService sorteoCarnetService;

  @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
  public void celebrarSorteosVencidos() {
    for (UUID eventoUid : sorteoCarnetService.eventosConSorteoVencido()) {
      try {
        sorteoCarnetService.celebrarSiVencido(eventoUid);
      } catch (Exception e) {
        // Un sorteo que falla no puede dejar sin celebrar a los demás.
        log.error("Error celebrando el sorteo de carnets del evento {}", eventoUid, e);
      }
    }
  }
}
