package com.softwells.fanops.controller.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistorialAccesoDto {
  private String email;
  private boolean exito;
  private LocalDateTime fecha;
  private String ip;
  private String userAgent;
}
