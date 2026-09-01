/** Hora que se usa cuando la peña no ha configurado ninguna. */
export const HORA_POR_DEFECTO = '20:00';

/**
 * Fecha resultante de restar días a la del evento y fijar una hora.
 *
 * Vive aparte del componente porque es la única cuenta de todo esto que puede salir mal sin que
 * se note: restar días atravesando un cambio de mes o de año, y el paso a horario de verano, que
 * es justo cuando "dos días antes a las ocho" deja de ser un simple `-48h`. Fijar la hora con
 * setHours después de mover el día es lo que lo mantiene correcto.
 *
 * @param fechaEvento fecha del evento
 * @param diasAntes   días que se restan
 * @param hora        'HH:mm'; si viene vacía se usa {@link HORA_POR_DEFECTO}
 */
export function fechaRelativaAlEvento(fechaEvento: Date, diasAntes: number,
                                      hora?: string | null): Date {
  const fecha = new Date(fechaEvento);
  const [horas, minutos] = (hora || HORA_POR_DEFECTO).split(':');

  fecha.setDate(fecha.getDate() - diasAntes);
  fecha.setHours(Number(horas), Number(minutos), 0, 0);

  return fecha;
}
