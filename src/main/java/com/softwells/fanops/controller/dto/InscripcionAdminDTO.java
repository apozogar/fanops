package com.softwells.fanops.controller.dto;

import com.softwells.fanops.enums.EstadoInscripcion;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InscripcionAdminDTO {

  private UUID uid;
  private EstadoInscripcion estado;
  private boolean socioPrioritario;
  private LocalDateTime fechaInscripcion;

  private UUID socioUid;
  private Integer numeroSocio;
  private String nombre;
  private String email;
  private String telefono;
}
