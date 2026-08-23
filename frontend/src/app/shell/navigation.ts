/**
 * Modelo de navegación de la aplicación.
 *
 * Es la única fuente de verdad: el sidebar de escritorio, la barra de pestañas de móvil y el
 * drawer se construyen todos a partir de aquí, así que añadir una sección se hace en un único
 * sitio y no puede quedar descuadrada entre plataformas.
 */

import { ROLE_ADMIN, ROLE_SUPERADMIN } from '@/core/auth/roles';
import { FoIconName } from '@/ui/icon/icon-registry';

export interface NavItem {
    id: string;
    /** Etiqueta completa, para el sidebar y el drawer. */
    label: string;
    /** Etiqueta corta para la barra de pestañas de móvil, donde el espacio es mínimo. */
    shortLabel: string;
    /** Nombre del icono en el registro de la aplicación. */
    icon: FoIconName;
    route: string;
}

export interface NavSection {
    label: string;
    items: NavItem[];
}

const AREA_PERSONAL: NavSection = {
    label: 'Área personal',
    items: [
        {
            id: 'carnet',
            label: 'Mi carnet',
            shortLabel: 'Carnet',
            icon: 'socio',
            route: '/carnet-socio'
        },
        {
            id: 'cuotas',
            label: 'Mis cuotas',
            shortLabel: 'Cuotas',
            icon: 'euro',
            route: '/cuotas-socio'
        },
        {
            id: 'inscripciones',
            label: 'Inscripción a eventos',
            shortLabel: 'Inscribirme',
            icon: 'inscribirse',
            route: '/inscripciones'
        }
    ]
};

const GESTION: NavSection = {
    label: 'Gestión',
    items: [
        {
            id: 'socios',
            label: 'Socios',
            shortLabel: 'Socios',
            icon: 'socios',
            route: '/socios'
        },
        {
            id: 'eventos',
            label: 'Eventos',
            shortLabel: 'Eventos',
            icon: 'calendario',
            route: '/eventos'
        }
    ]
};

const SUPERADMIN: NavSection = {
    label: 'Superadmin',
    items: [
        {
            id: 'penas',
            label: 'Gestión de peñas',
            shortLabel: 'Peñas',
            icon: 'super-admin',
            route: '/penas'
        }
    ]
};

/**
 * Secciones visibles para un usuario según sus authorities.
 *
 * El superadmin ve la gestión de peñas y también la gestión (socios y eventos) de la peña que
 * tenga seleccionada, porque para eso existe el selector de la cabecera. Lo que no ve es el
 * área personal: no tiene carnet ni ficha de socio propia, así que esas pantallas no le
 * aplican.
 *
 * La jerarquía de roles del backend (SUPERADMIN implies ADMIN) es la que le concede permiso
 * real sobre esos endpoints; aquí solo se decide qué se le enseña.
 */
export function buildNavigation(authorities: readonly string[]): NavSection[] {
    if (authorities.includes(ROLE_SUPERADMIN)) {
        return [SUPERADMIN, GESTION];
    }

    const sections = [AREA_PERSONAL];

    if (authorities.includes(ROLE_ADMIN)) {
        sections.push(GESTION);
    }

    return sections;
}

export function flattenNavigation(sections: readonly NavSection[]): NavItem[] {
    return sections.flatMap((section) => section.items);
}

/**
 * Máximo de pestañas que caben cómodamente en la barra inferior de móvil. Si hay más
 * destinos, la barra muestra los primeros y deja el resto en el drawer.
 */
export const MAX_TAB_ITEMS = 5;
