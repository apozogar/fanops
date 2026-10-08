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
  - En el **enlace público**, si el correo es el de una ficha de socio (o el de su cuenta) se
    inscribe a esa ficha exactamente como desde la app: el correo es lo que valida al socio. En un
    multicarnet, con varias fichas en el mismo correo, se elige por el nombre escrito; si no
    coincide con ninguna, entra como no socio (`EventoService.socioPorCorreo`).
  - **Vista previa del enlace público** (`/inscripcion/{id}`): WhatsApp y compañía no ejecutan
    JavaScript, así que `PrevisualizacionEnlaceController` sirve el `index.html` de la SPA con las
    etiquetas Open Graph del evento ya puestas (título, fecha, lugar, plazo y precio). La imagen
    es un **cartel de 1200×630** que se dibuja al vuelo con Java2D (`CartelEventoService`,
    `GET /api/eventos/{id}/cartel/{version}.jpg`, público) con el color, escudo y lema de la
    peña; el Dockerfile instala `font-dejavu` porque Alpine no trae fuentes. La URL del cartel y
    el enlace que se copia desde la tabla de eventos (`?v=`) llevan una versión calculada con los
    datos del evento, para que WhatsApp no reutilice una vista previa vieja o fallida. Si la
    imagen sale grande arriba o en miniatura lo decide WhatsApp en el dispositivo que envía. No lleva
    datos que cambian a cada rato,
    como las plazas libres, porque las apps guardan la vista previa en caché. Las URLs de la
    vista previa salen del dominio por el que llega la petición (Caddy pasa `X-Forwarded-Proto`),
    no de `PUBLIC_BASE_URL`: así funcionan aunque falte esa variable.
  - Desde el **listado de socios** (modal de eventos del socio) la gestión puede apuntar a un
    socio que no usa la app, al evento o al sorteo del carnet
    (`POST /api/eventos/{id}/socios/{socioUid}/inscribir?sorteoCarnet=`). Sigue exactamente las
    mismas reglas que si se apuntara él (plazo, hueco, penalizaciones) y solo alcanza a socios de
    la peña de quien gestiona (`EventoService.inscribirSocioDesdeGestion`).
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
- **Costes de un evento**: `costePlaza` (socios) y `costePlazaNoSocio` (si queda vacío, los no
  socios pagan lo mismo: `EventoEntity.costePlazaPara`) y `costeCarnet` son lo que paga cada persona (la plaza y,
  aparte, ir con carnet sorteado). La lista de inscritos de gestión enseña el `importe` de cada
  uno (según tenga ficha de socio o no) para validarlo en la puerta; el mensaje del enlace y el
  cartel dicen los dos precios si difieren (`TextosEvento.precioPlaza`) y se enseñan al socio; `costeTotalEstimado` / `costeTotalReal`
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
  - **Papeletas extra**: si la peña tiene activado `papeletasExtraSorteo` (lo activa el
    superadmin en la ficha de la peña), la gestión puede sumar papeletas a mano a un participante
    (`PUT /api/eventos/{id}/sorteo-carnet/papeletas-extra/{socioUid}`, entre 0 y 100) mientras el
    sorteo no se ha celebrado. Se suman a las del historial al fijar el peso, y se publican
    aparte (`papeletasExtra`) para que el reparto siga siendo comprobable. Si la peña desactiva
    la opción, las que hubiera dejan de contar.
  - La semilla se genera al **programar** el evento y no se regenera nunca (`SorteoAleatorio`):
    eso es lo que permite adelantarlo o reiniciarlo sin que cambie el resultado. Ni la semilla ni
    su SHA-256 se publican (la peña prefirió no enseñarlos): la API no los devuelve.
  - Al celebrarse se vacía el bombo entero y se guarda el orden completo (`posicionSorteo`): los
    `plazasCarnet` primeros son `GANADORA` y el resto `SUPLENTE`. Una renuncia pasa el carnet al
    primer suplente, nunca se vuelve a sortear. El front solo reproduce ese orden guardado.
  - Se celebra solo (`SorteoCarnetScheduler`, cada minuto) y también de forma perezosa al
    consultarlo, porque en un despliegue dormido puede no haber nadie a la hora exacta. Un admin
    puede adelantarlo con `POST /api/eventos/{id}/sorteo-carnet/celebrar`.
  - Un admin puede **reiniciar** un sorteo celebrado (`POST .../sorteo-carnet/reiniciar`): se
    deshace el resultado y el bombo vuelve a abrirse con la **misma semilla**, así que con los
    mismos participantes y papeletas sale el mismo reparto (no sirve para repetir hasta que salga
    otro). Quien renunció al carnet sale del bombo. Exige que la fecha de sorteo del evento sea
    futura; si no, el planificador lo volvería a celebrar al momento.
- La peña es **singleton** (ID 1), usado en cuotas, remesas SEPA y carnet.
- El flujo SEPA genera cuotas y remesas `pain.008`; los retornos se procesan desde `/api/cobros`.

## Despliegue (VPS en Hetzner, https://fanops.es)

Guía completa en **`DESPLIEGUE.md`**. Lo esencial:

- Al subir el código, el servidor lo despliega solo. El stack está en `deploy/`:
  `docker-compose.yml` (la aplicación, construida con el `Dockerfile` de la raíz, más Caddy como
  proxy inverso con HTTPS automático) y `Caddyfile`.
- La base de datos es un Postgres que vive en el propio servidor, fuera de Docker: la aplicación
  llega a él por `host.docker.internal`.
- Configuración por variables de entorno en `deploy/.env` (no se commitea; plantilla en
  `deploy/.env.example`), con los mismos nombres que lee `application.yml`
  (`SPRING_DATASOURCE_URL`, `APP_JWT_SECRET`, `RESEND_API_KEY`, `PUBLIC_BASE_URL`...).
- `PUBLIC_BASE_URL=https://fanops.es`: de ahí salen los enlaces de los correos (recuperar
  contraseña, vincular cuenta, inscripciones). Si falta, apuntan a `localhost:5300`.
- `SPRING_DATASOURCE_URL` es una URL **JDBC** (`jdbc:postgresql://host.docker.internal:5432/fanops`),
  no la cadena de `psql`: sin credenciales embebidas y sin `channel_binding`, que es un parámetro
  de `libpq` que el driver JDBC no entiende.
- El contenedor de la aplicación va limitado a 1,5 GB (`mem_limit`) para que la JVM no se quede
  con la RAM que necesita Postgres en la misma máquina.
- El `Dockerfile` desempaqueta el jar y entrena un archivo CDS durante el build para recortar el
  arranque. Si ese paso falla, el build continúa y la JVM arranca sin él.
