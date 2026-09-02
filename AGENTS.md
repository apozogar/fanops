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
- No usar worktrees, trabajar en la rama actual.

## Reglas de dominio

- **Socio prioritario** para eventos = ficha `activo` + cuota al día (`EstadoCuota.PAGADA` en los últimos 2 meses) o `exentoPago`. Ver `EventoService.esSocioAlDia`.
- **Inscripción a eventos**: el socio prioritario con hueco → `CONFIRMADA`; el resto (socios sin cuota al día y no socios del enlace público) → `EN_ESPERA`.
  - Cuando se anula una inscripción confirmada o el admin ejecuta `asignar-plazas`, se promocionan los de espera (prioridad: socios al día, luego por fecha de inscripción).
  - El plazo de inscripción por evento se guarda en `EventoEntity.fechaLimiteInscripcion`; fuera de plazo no se admiten inscripciones.
- **Cuenta de acceso de un socio**: el camino normal es que la persona se registre y confirme el enlace de vinculación enviado a su correo (`VinculacionSocioService`). Desde el listado de socios, un admin puede además crearla a mano con una contraseña (`POST /api/socios/{id}/cuenta`), para socios que no van a registrarse; ahí los roles solo se fijan al crear la cuenta, nunca al cambiar una contraseña.
- **Valores por defecto de los eventos**: tabla `pena_valores_evento` (una fila por peña,
  `ValoresEventoPenaEntity`) con plazas, coste por plaza, carnets, coste con carnet y coste
  estimado. Solo se usan para **proponer** los campos al crear un evento desde gestión; cambiarlos
  no toca ningún evento existente y cada campo puede quedar a null para no sugerir nada. Los
  gestiona el admin de su peña en `/api/pena/valores-evento`, no el superadmin.
  - Las fechas se guardan **relativas** a la del evento (`diasAntesFinInscripcion` /
    `horaFinInscripcion`, `diasAntesSorteo` / `horaSorteo`): lo que se repite de un partido a otro
    no es una fecha concreta sino "dos días antes, a las ocho". El formulario las calcula al
    elegir la fecha del evento (`fechaRelativaAlEvento`), solo en eventos nuevos y solo si el
    campo sigue vacío.
- **Costes de un evento**: `costePlaza` y `costeCarnet` son lo que paga cada persona (la plaza y,
  aparte, ir con carnet sorteado) y se enseñan al socio; `costeTotalEstimado` / `costeTotalReal`
  son los totales del evento y solo los ve la gestión. Null significa "sin indicar", que no es lo
  mismo que 0.
- **Sorteo de carnets**: recurso aparte de las plazas de bus, con su propia inscripción
  (`SolicitudCarnetEntity`) y su propio reparto. Se configura por evento con `plazasCarnet` y
  `fechaSorteoCarnet`.
  - **Se apunta a una cosa o a la otra**: la tarjeta del evento ofrece "Solo al evento" o "Al
    sorteo del carnet", nunca las dos altas por separado, porque entrar en el bombo ya apunta al
    evento (`EventoService.apuntarAlSorteoCarnet`). La inscripción que arrastra sigue las reglas
    normales (penalizaciones y `soloSiEntranTodos` incluidos), así que con el evento completo la
    plaza queda en espera aunque el carnet le acabe tocando. A quien ya estaba inscrito no se le
    toca la plaza: solo entra en el bombo. Salir del bombo **no** da de baja del evento, porque
    cancelar una plaza puede costar una falta.
  - Por eso el bombo solo admite entradas si el plazo del evento sigue abierto
    (`admiteSolicitudes`), aunque su propia fecha no haya llegado.
  - El reparto es **ponderado**: cada socio entra con 1 papeleta más otra por cada sorteo en el
    que participó y se quedó sin carnet desde la última vez que le tocó. Ganar (o ganar y
    renunciar) devuelve el contador a 1.
  - La semilla se genera al **programar** el evento y no se regenera nunca; hasta que el sorteo se
    celebra solo se publica su SHA-256. Eso es lo que permite adelantarlo sin que cambie el
    resultado y lo que hace el sorteo comprobable desde fuera (`SorteoAleatorio`).
  - Al celebrarse se vacía el bombo entero y se guarda el orden completo (`posicionSorteo`): los
    `plazasCarnet` primeros son `GANADORA` y el resto `SUPLENTE`. Una renuncia pasa el carnet al
    primer suplente, nunca se vuelve a sortear. El front solo reproduce ese orden guardado.
  - Se celebra solo (`SorteoCarnetScheduler`, cada minuto) y también de forma perezosa al
    consultarlo, porque en un despliegue dormido puede no haber nadie a la hora exacta. Un admin
    puede adelantarlo con `POST /api/eventos/{id}/sorteo-carnet/celebrar`.
- La peña es **singleton** (ID 1), usado en cuotas, remesas SEPA y carnet.
- El flujo SEPA genera cuotas y remesas `pain.008`; los retornos se procesan desde `/api/cobros`.

## Despliegue (Fly.io Madrid + Neon Frankfurt)

Guía completa en **`DESPLIEGUE.md`**. Lo esencial:

- Configuración en `fly.toml`: región `mad`, `min_machines_running = 1` y
  `auto_stop_machines = 'off'`. La máquina **no se duerme**: es lo que quita el arranque en frío y
  lo que hace que `SorteoCarnetScheduler` se ejecute de verdad cada minuto.
- Base de datos en Neon, región `eu-central-1`, **endpoint directo** (el que no lleva `-pooler`):
  con una sola instancia y su pool de HikariCP, el pooler solo añade un salto.
- App y base de datos tienen que estar en la misma zona del mundo. Tenerlas separadas era lo que
  hacía lento el despliegue anterior: ~150 ms por consulta, y una petición con diez consultas se
  iba a segundo y medio de puro cable.
- Secretos: `fly secrets set NOMBRE=valor`. **No existe interpolación de secretos en `fly.toml`**;
  `{{ secrets.X }}` no se sustituye por nada. Los secretos se inyectan como variables de entorno
  con su propio nombre, así que hay que nombrarlos igual que las variables que lee
  `application.yml` (`SPRING_DATASOURCE_URL`, `APP_JWT_SECRET`, `RESEND_API_KEY`...). En `[env]`
  van solo los valores no sensibles.
- `SPRING_DATASOURCE_URL` es una URL **JDBC** (`jdbc:postgresql://host/fanops?sslmode=require`),
  no la cadena de `psql`: sin credenciales embebidas y sin `channel_binding`, que es un parámetro
  de `libpq` que el driver JDBC no entiende.
- El `Dockerfile` desempaqueta el jar y entrena un archivo CDS durante el build para recortar el
  arranque. Si ese paso falla, el build continúa y la JVM arranca sin él.
- `render.yaml` se conserva solo como referencia del despliegue anterior.
