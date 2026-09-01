package com.softwells.fanops.enums;

/** Situación de un socio dentro del sorteo de carnets de un evento. */
public enum EstadoSolicitudCarnet {
  /** Apuntado al bombo, todavía sin sortear. */
  PENDIENTE,
  /** Le ha tocado carnet. */
  GANADORA,
  /** No le ha tocado; entraría por orden si un ganador renuncia. */
  SUPLENTE,
  /** Le tocó y devolvió el carnet, que pasó al siguiente suplente. */
  RENUNCIADA
}
