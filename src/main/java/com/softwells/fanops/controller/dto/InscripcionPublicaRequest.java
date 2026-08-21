package com.softwells.fanops.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InscripcionPublicaRequest {

  @NotBlank
  private String nombre;

  @NotBlank
  @Email
  private String email;

  private String telefono;
}
