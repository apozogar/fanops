package com.softwells.fanops.exception;

/** El correo no se pudo entregar al proveedor (SMTP caído, API rechazando, credenciales mal). */
public class EmailNoEnviadoException extends RuntimeException {

  public EmailNoEnviadoException(String message, Throwable cause) {
    super(message, cause);
  }

  public EmailNoEnviadoException(String message) {
    super(message);
  }
}
