/**
 * Nombres de los roles, tal y como llegan en las authorities del JWT.
 *
 * Centralizados aquí porque estaban repetidos como literales en guards, servicios y
 * componentes, donde una errata no la detecta ni el compilador ni ningún test.
 */
export const ROLE_USER = 'ROLE_USER';
export const ROLE_ADMIN = 'ROLE_ADMIN';
export const ROLE_SUPERADMIN = 'ROLE_SUPERADMIN';
