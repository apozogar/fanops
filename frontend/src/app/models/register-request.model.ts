export interface RegisterRequest {
    nombre: string;
    email: string;
    password: string;

    /**
     * Dominio de la peña por la que se ha entrado (/mi-pena/auth/register), para que el backend
     * sepa a qué peña asociar la ficha. Va vacío cuando se entra por la raíz de la aplicación, y
     * entonces el backend usa la peña por defecto.
     */
    penaSlug?: string | null;
}
