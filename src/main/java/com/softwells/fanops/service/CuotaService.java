package com.softwells.fanops.service;

import com.softwells.fanops.controller.dto.RemesaResponse;
import com.softwells.fanops.enums.EstadoCuota;
import com.softwells.fanops.model.CuotaEntity;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.repository.CuotaRepository;
import com.softwells.fanops.repository.SocioRepository;
import io.micrometer.common.util.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuotaService {

  private final SocioRepository socioRepository;
  private final CuotaRepository cuotaRepository;
  private final SepaService sepaService;
  private final UsuarioService usuarioService;

  @Transactional
  public List<CuotaEntity> generarCuotas(String concepto) {
    PenaEntity pena = usuarioService.obtenerPenaDelUsuarioAutenticado();
    List<SocioEntity> sociosActivos = socioRepository.findByActivoAndPenaId(true, pena.getId());

    List<CuotaEntity> nuevasCuotas = new ArrayList<>();
    List<SocioEntity> sociosParaCobrar = new ArrayList<>();
    LocalDate hoy = LocalDate.now();

    for (SocioEntity socio : sociosActivos) {
      // 1. Omitir si el socio está exento de pago
      if (socio.isExentoPago()) {
        log.info("Omitiendo socio '{}' por estar exento de pago.", socio.getNombre());
        continue;
      }

      // 2. Omitir si no tiene número de cuenta
      if (StringUtils.isEmpty(socio.getNumeroCuenta())) {
        log.info("Omitiendo socio '{}' por no tener número de cuenta.", socio.getNombre());
        continue;
      }

      // 3. Omitir si ya tiene una cuota pendiente de pago
      if (cuotaRepository.existsBySocioAndEstado(socio, EstadoCuota.PENDIENTE)) {
        log.info("Omitiendo socio '{}' porque ya tiene una cuota pendiente.", socio.getNombre());
        continue;
      }

      // Lógica para determinar el importe de la cuota mensual
      int edad =
          (socio.getFechaNacimiento() != null) ? Period.between(socio.getFechaNacimiento(), hoy)
              .getYears() : 30;
      double importeMensual =
          (edad > pena.getEdadMayoria()) ? pena.getCuotaAdulto() : pena.getCuotaMenor();

      // 4. Calcular el importe semestral
      double importeSemestral = importeMensual * 6;

      CuotaEntity cuota = new CuotaEntity();
      cuota.setSocio(socio);
      cuota.setImporte(importeSemestral);
      cuota.setMes(hoy.getMonthValue());
      cuota.setAnio(hoy.getYear());
      cuota.setEstado(EstadoCuota.PENDIENTE); // Estado inicial
      cuota.setFechaEmision(hoy);
      cuota.setConcepto(concepto);

      nuevasCuotas.add(cuota);
      sociosParaCobrar.add(socio); // Añadir a la lista para generar el fichero SEPA
    }

    return cuotaRepository.saveAll(nuevasCuotas);
  }

  @Transactional
  public RemesaResponse generarCuotasYRemesaSemestral(String concepto) {

    List<CuotaEntity> nuevasCuotas = generarCuotas(concepto);

    if (nuevasCuotas.isEmpty()) {
      return new RemesaResponse("No se generaron nuevas cuotas para la remesa.", null);
    }

    // Ahora que las cuotas están generadas, creamos el fichero SEPA solo con los socios a cobrar
    String sepaXml = sepaService.generarFicheroSepa(nuevasCuotas, LocalDateTime.now());

    String mensaje =
        "Se generaron " + nuevasCuotas.size() + " cuotas semestrales y se creó la remesa SEPA.";
    return new RemesaResponse(mensaje, sepaXml);
  }

  @Transactional
  public String marcarPendientesComoPagadas() {
    Long penaId = usuarioService.obtenerPenaDelUsuarioAutenticado().getId();
    int actualizadas = cuotaRepository.actualizarPendientesAPagadas(penaId);
    return actualizadas + " cuotas pendientes han sido marcadas como PAGADAS.";
  }
}