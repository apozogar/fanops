package com.softwells.fanops.controller.dto;

import lombok.Data;

@Data
public class ConfirmarVinculacionRequest {

  private String token;

  /** Solo es necesaria si la invitación no venía de un registro (ver VinculacionInfoDto). */
  private String password;
}
