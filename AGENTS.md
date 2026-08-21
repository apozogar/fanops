# FanOps (pblb) — Guía del proyecto

Aplicación de gestión de peñas de fútbol: socios, cuotas (SEPA), eventos/partidos,
carnet de socio e inscripciones con lista de espera.

## Estructura

- **Backend**: Spring Boot 4.0.0-M3, Java 21, Maven (`mvnw`) en la raíz del repo. Código en `src/main/java/com/softwells/fanops`.
- **Frontend**: Angular 20 + PrimeNG en `frontend/` (monorepo con el backend; el build mueve el `dist/` a `src/main/resources/static/`).
- **BD**: PostgreSQL. Config en `src/main/resources/application.yml` (JPA `ddl-auto`).

## Comandos

Backend (desde la raíz):
- `.\mvnw.cmd compile` — compilar
- `.\mvnw.cmd test` — tests unitarios
- `.\mvnw.cmd spring-boot:run` — arranca la API en :8080

Frontend (desde `frontend/`):
- `npm start` — dev server en :4200
- `npm run build` — build de producción hacia `dist/`
- `npm run format` — formatea con Prettier
- `npx eslint "src/**/*.ts"` — lint

## Convenciones

- Commits en **español**, formato `tipo(alcance): descripción breve`.
- **No usar git worktrees**: trabajar siempre sobre el checkout principal del repositorio (el IDE y los arranques apuntan ahí).
- Mensajes de la app en español.
- Java: Lombok (`@RequiredArgsConstructor`, `@Data`/`@Getter`/`@Setter`), servicios `@Transactional`, seguridad por `@PreAuthorize` en los controllers y rutas públicas en `SecurityConfig`.
- Entidades: IDs `UUID` con `@GeneratedValue`, enums persistidos como `STRING`, `@JsonIgnore`/`@JsonBackReference` para evitar recursión JSON.
- Frontend: componentes standalone, rutas en `app.routes.ts` / `pages/*/pages.routes.ts`, consumo de API a través de servicios en `services/`, tipos en `interfaces/`.
- **Nunca commitear secretos**: las credenciales (JWT, BD, SMTP, WhatsApp) van por variables de entorno definidas en `application.yml` con defaults de desarrollo.

## Reglas de dominio

- **Socio prioritario** para eventos = ficha `activo` + cuota al día (`EstadoCuota.PAGADA` en los últimos 2 meses) o `exentoPago`. Ver `EventoService.esSocioAlDia`.
- **Inscripción a eventos**: el socio prioritario con hueco → `CONFIRMADA`; el resto (socios sin cuota al día y no socios del enlace público) → `EN_ESPERA`.
  - Cuando se anula una inscripción confirmada o el admin ejecuta `asignar-plazas`, se promocionan los de espera (prioridad: socios al día, luego por fecha de inscripción).
  - El plazo de inscripción por evento se guarda en `EventoEntity.fechaLimiteInscripcion`; fuera de plazo no se admiten inscripciones.
- La peña es **singleton** (ID 1), usado en cuotas, remesas SEPA y carnet.
- El flujo SEPA genera cuotas y remesas `pain.008`; los retornos se procesan desde `/api/cobros`.

## Despliegue (Fly.io)

- Configuración en `fly.toml` (con instrucciones comentadas).
- Postgres gestionado: `fly postgres create` + `fly postgres attach`.
- Secretos: `fly secrets set APP_JWT_SECRET=... SPRING_DATASOURCE_URL=... SMTP_* WHATSAPP_* ...`.
- En `Fly.io` los secretos se referencian en `fly.toml` con `{{ secrets.NOMBRE }}`.