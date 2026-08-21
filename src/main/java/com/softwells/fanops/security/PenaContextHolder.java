package com.softwells.fanops.security;

/**
 * Guarda, por hilo de petición, la peña que el superadmin ha elegido trabajar (enviada por el
 * frontend en la cabecera {@code X-Pena-Id}, ver {@link PenaContextFilter}).
 *
 * <p>Para un usuario normal (admin/socio) esto no se usa: su peña sale directamente de
 * {@code UsuarioEntity.pena}. Solo el superadmin, que no pertenece a ninguna peña en concreto,
 * necesita indicar sobre cuál quiere ver/operar en cada petición.
 */
public final class PenaContextHolder {

  private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

  private PenaContextHolder() {
  }

  public static void set(Long penaId) {
    CURRENT.set(penaId);
  }

  public static Long get() {
    return CURRENT.get();
  }

  public static void clear() {
    CURRENT.remove();
  }
}
