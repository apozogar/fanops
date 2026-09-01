import { Translation } from 'primeng/api';

/**
 * Textos de PrimeNG en español.
 *
 * Sin esto los componentes se quedan con los valores por defecto de la librería, que están en
 * inglés: el calendario salía con "September" y "Su Mo Tu…", y los diálogos de confirmación con
 * "Yes / No". Aquí solo se traduce lo que la aplicación usa de verdad (datepicker, confirmación,
 * tablas y contraseñas); el resto se queda con el original, que no llega a verse.
 *
 * `firstDayOfWeek: 1` importa tanto como la traducción: en España la semana empieza en lunes y
 * el calendario venía empezando en domingo.
 */
export const TRADUCCION_PRIMENG: Translation = {
  accept: 'Sí',
  reject: 'No',
  cancel: 'Cancelar',
  choose: 'Elegir',
  upload: 'Subir',
  clear: 'Limpiar',
  apply: 'Aplicar',
  completed: 'Completado',
  pending: 'Pendiente',

  dayNames: ['domingo', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado'],
  dayNamesShort: ['dom', 'lun', 'mar', 'mié', 'jue', 'vie', 'sáb'],
  dayNamesMin: ['D', 'L', 'M', 'X', 'J', 'V', 'S'],
  monthNames: ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto',
    'septiembre', 'octubre', 'noviembre', 'diciembre'],
  monthNamesShort: ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov',
    'dic'],
  dateFormat: 'dd/mm/yy',
  firstDayOfWeek: 1,
  today: 'Hoy',
  weekHeader: 'Sm',

  chooseYear: 'Elegir año',
  chooseMonth: 'Elegir mes',
  chooseDate: 'Elegir fecha',
  prevDecade: 'Década anterior',
  nextDecade: 'Década siguiente',
  prevYear: 'Año anterior',
  nextYear: 'Año siguiente',
  prevMonth: 'Mes anterior',
  nextMonth: 'Mes siguiente',
  prevHour: 'Hora anterior',
  nextHour: 'Hora siguiente',
  prevMinute: 'Minuto anterior',
  nextMinute: 'Minuto siguiente',
  prevSecond: 'Segundo anterior',
  nextSecond: 'Segundo siguiente',
  am: 'a. m.',
  pm: 'p. m.',

  weak: 'Débil',
  medium: 'Media',
  strong: 'Fuerte',
  passwordPrompt: 'Escribe una contraseña',

  emptyMessage: 'No se han encontrado resultados',
  emptySearchMessage: 'No se han encontrado resultados',
  emptyFilterMessage: 'No se han encontrado resultados',
  emptySelectionMessage: 'No hay ningún elemento seleccionado',
  selectionMessage: '{0} elementos seleccionados',
  searchMessage: 'Hay resultados disponibles',
  fileChosenMessage: 'Ficheros',
  noFileChosenMessage: 'Ningún fichero seleccionado',

  startsWith: 'Empieza por',
  contains: 'Contiene',
  notContains: 'No contiene',
  endsWith: 'Acaba en',
  equals: 'Es igual a',
  notEquals: 'No es igual a',
  noFilter: 'Sin filtro',
  lt: 'Menor que',
  lte: 'Menor o igual que',
  gt: 'Mayor que',
  gte: 'Mayor o igual que',
  is: 'Es',
  isNot: 'No es',
  before: 'Antes de',
  after: 'Después de',
  dateIs: 'La fecha es',
  dateIsNot: 'La fecha no es',
  dateBefore: 'La fecha es anterior a',
  dateAfter: 'La fecha es posterior a',
  matchAll: 'Cumplir todas',
  matchAny: 'Cumplir alguna',
  addRule: 'Añadir regla',
  removeRule: 'Quitar regla'
};
