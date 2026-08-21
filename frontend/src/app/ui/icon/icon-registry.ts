import {
    LucideBan,
    LucideCalendar,
    LucideCalendarPlus,
    LucideCheck,
    LucideCircleAlert,
    LucideChevronDown,
    LucideCircleCheck,
    LucideClock,
    LucideCreditCard,
    LucideDollarSign,
    LucideDownload,
    LucideEllipsis,
    LucideEuro,
    LucideFileOutput,
    LucideFunnel,
    LucideHourglass,
    LucideIcon,
    LucideIdCard,
    LucideInfo,
    LucideLock,
    LucideLogOut,
    LucideMapPin,
    LucideMoon,
    LucidePencil,
    LucidePlus,
    LucideRefreshCw,
    LucideSearch,
    LucideShare2,
    LucideShield,
    LucideSun,
    LucideThumbsUp,
    LucideTrash2,
    LucideTriangleAlert,
    LucideUpload,
    LucideUser,
    LucideUserPlus,
    LucideUsers,
    LucideX
} from '@lucide/angular';

/**
 * Iconos disponibles en la aplicación.
 *
 * Se importan de uno en uno a propósito: el empaquetado incluye solo estos y no los ~1600 de
 * Lucide. Es además la única capa que conoce Lucide, así que cambiar de set de iconos, o
 * sustituir uno concreto por otro, se hace aquí sin tocar ninguna plantilla.
 *
 * Las claves son el nombre que se usa en `<fo-icon name="...">`. Al estar tipadas, una errata
 * la detecta el compilador; antes eran cadenas libres del tipo "pi pi-users".
 */
export const FO_ICONS = {
    anadir: LucidePlus,
    aviso: LucideTriangleAlert,
    banco: LucideCreditCard,
    bloqueado: LucideLock,
    buscar: LucideSearch,
    calendario: LucideCalendar,
    cerrar: LucideX,
    'cerrar-sesion': LucideLogOut,
    check: LucideCheck,
    'check-circulo': LucideCircleCheck,
    compartir: LucideShare2,
    confirmar: LucideThumbsUp,
    descargar: LucideDownload,
    dolar: LucideDollarSign,
    editar: LucidePencil,
    error: LucideCircleAlert,
    eliminar: LucideTrash2,
    espera: LucideHourglass,
    euro: LucideEuro,
    exportar: LucideFileOutput,
    filtrar: LucideFunnel,
    hora: LucideClock,
    info: LucideInfo,
    inscribirse: LucideCalendarPlus,
    'mas-opciones': LucideEllipsis,
    'no-permitido': LucideBan,
    refrescar: LucideRefreshCw,
    seleccionar: LucideChevronDown,
    socio: LucideIdCard,
    socios: LucideUsers,
    'super-admin': LucideShield,
    'tema-claro': LucideSun,
    'tema-oscuro': LucideMoon,
    ubicacion: LucideMapPin,
    usuario: LucideUser,
    'usuario-nuevo': LucideUserPlus,
    subir: LucideUpload
} satisfies Record<string, LucideIcon>;

/** Nombres válidos para `<fo-icon name="...">`. */
export type FoIconName = keyof typeof FO_ICONS;
