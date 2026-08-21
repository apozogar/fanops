package com.softwells.fanops.exception;

import com.softwells.fanops.controller.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // Manejador para errores 404 Not Found
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ApiResponse<String>> handleEntityNotFoundException(
      EntityNotFoundException ex, WebRequest request) {
    log.warn("Recurso no encontrado: {}", ex.getMessage());
    ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  // Manejador para errores 403 Forbidden
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(AccessDeniedException ex,
      WebRequest request) {
    log.warn("Acceso denegado: {}", ex.getMessage());
    ApiResponse<String> response = new ApiResponse<>(false,
        "Acceso denegado. No tienes los permisos necesarios.", null);
    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
  }

  // Manejador para errores 400 Bad Request
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    ex.printStackTrace();
    log.warn("Argumento ilegal o petición incorrecta: {}", ex.getMessage());
    ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  /**
   * Estado no válido para atender la petición. El caso típico es un superadmin que todavía no
   * ha seleccionado peña: sin este manejador caía en el genérico y devolvía un 500 con "Ha
   * ocurrido un error inesperado", ocultando una causa que el usuario sí puede resolver.
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiResponse<String>> handleIllegalStateException(IllegalStateException ex,
      WebRequest request) {
    log.warn("Estado no válido para la petición: {}", ex.getMessage());
    ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  /**
   * El proveedor de correo no aceptó el envío. Se devuelve 503 y no 500 porque no es un fallo
   * de la aplicación sino de un servicio externo, y porque quien ha pedido el enlace (registro,
   * vinculación, recuperar contraseña) tiene que enterarse: antes esto se registraba en el log y
   * se le decía que mirase su correo.
   */
  @ExceptionHandler(EmailNoEnviadoException.class)
  public ResponseEntity<ApiResponse<String>> handleEmailNoEnviadoException(
      EmailNoEnviadoException ex, WebRequest request) {
    log.error("No se pudo enviar el correo", ex);
    ApiResponse<String> response = new ApiResponse<>(false,
        "No hemos podido enviar el correo. Vuelve a intentarlo en unos minutos.", null);
    return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
  }

  // Manejador genérico para errores 500 Internal Server Error
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex,
      WebRequest request) {
    // ¡Aquí es donde se registrará tu error 500 en la consola!
    log.error("Error inesperado en la aplicación: ", ex);

    ApiResponse<String> response = new ApiResponse<>(false,
        "Ha ocurrido un error inesperado en el servidor.", null);
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}