# Despliegue de FanOps en el VPS (Hetzner)

FanOps corre en un VPS de Hetzner con dominio **https://fanops.es**. Al subir el código al
repositorio, el servidor lo despliega solo: no hay que lanzar nada a mano.

Todo lo necesario para levantarlo está en la carpeta [`deploy/`](deploy/):

| Fichero | Qué es |
|---|---|
| [`deploy/docker-compose.yml`](deploy/docker-compose.yml) | Dos contenedores: la aplicación (Spring Boot con el frontend dentro del jar, construida con el [`Dockerfile`](Dockerfile) de la raíz) y Caddy como proxy inverso con HTTPS. |
| [`deploy/Caddyfile`](deploy/Caddyfile) | Proxy de `fanops.es` hacia la aplicación. Caddy pide y renueva el certificado de Let's Encrypt solo. |
| [`deploy/.env.example`](deploy/.env.example) | Variables de entorno. En el servidor se copia a `deploy/.env` (que **no** se commitea) y se rellena. |

La base de datos **no** está en el compose: es un Postgres que ya vive en el propio servidor, fuera
de Docker. La aplicación llega a él por `host.docker.internal`.

---

## 1. Puesta en marcha desde cero

Requisitos en el servidor: Docker con el plugin de compose, el puerto 80 y el 443 abiertos en el
cortafuegos, y el DNS de `fanops.es` apuntando a la IP del VPS.

```bash
cd deploy
cp .env.example .env && nano .env      # rellenar credenciales
docker compose up -d --build
docker compose logs -f app
```

Comprobación:

```bash
curl -s https://fanops.es/management/health
```

Debe responder `{"status":"UP"}`. Si da `DOWN`, los logs de `app` dirán si es la conexión a la base
de datos.

---

## 2. Variables de entorno

Todas las lee `application.yml` con su propio nombre; en `deploy/.env.example` está la lista
comentada. Las que hay que revisar sí o sí:

- **`SPRING_DATASOURCE_URL`**: es una URL **JDBC** (`jdbc:postgresql://host.docker.internal:5432/fanops`),
  no la cadena de `psql`: sin usuario ni contraseña dentro (van en `SPRING_DATASOURCE_USERNAME` y
  `SPRING_DATASOURCE_PASSWORD`) y sin `channel_binding`, que es un parámetro de `libpq` que el
  driver JDBC no entiende.
- **`APP_JWT_SECRET`**: generar con `openssl rand -base64 48`. Cambiarlo cierra todas las sesiones.
- **`PUBLIC_BASE_URL=https://fanops.es`**: de aquí salen los enlaces de los correos y la vista
  previa del enlace de inscripción en WhatsApp (etiquetas Open Graph). Tiene que ser la URL HTTPS
  pública real.
- **Correo**: por defecto `APP_EMAIL_PROVEEDOR=resend` con `RESEND_API_KEY`. En el VPS los puertos
  SMTP de salida no están bloqueados, así que también vale `smtp` con las `SMTP_*`.
- **`APP_SUPERADMIN_EMAIL` / `APP_SUPERADMIN_PASSWORD`**: solo se usan para crear el primer
  superadmin si no existe ninguno. Cambiar la contraseña tras el primer acceso.

---

## 3. Memoria

El compose limita la aplicación a 1,5 GB (`mem_limit`). Sin el límite, la JVM vería toda la RAM del
servidor y reservaría un heap enorme (`MaxRAMPercentage=70` del `Dockerfile`), dejando sin memoria a
Postgres, que vive en la misma máquina.

El `Dockerfile` desempaqueta el jar y entrena un archivo CDS durante el build para recortar el
arranque. Si ese paso falla, el build continúa y la JVM arranca sin él.

---

## 4. Cuando el esquema esté estable

`SPRING_JPA_DDL_AUTO=update` sirve para que el primer arranque contra una base de datos vacía cree
el esquema. Una vez estable, pásalo a `validate`: `update` compara todo el metamodelo de Hibernate
contra la base de datos **en cada arranque**, y eso retrasa la primera petición.

---

## 5. Operación

Desde `deploy/` en el servidor:

| Qué | Comando |
|---|---|
| Logs en vivo | `docker compose logs -f app` |
| Logs de acceso del proxy | `docker compose exec caddy tail -f /data/access.log` |
| Reiniciar la aplicación | `docker compose restart app` |
| Reconstruir a mano | `docker compose up -d --build` |
| Consola dentro del contenedor | `docker compose exec app sh` |
| Estado y healthcheck | `docker compose ps` |

El healthcheck de Docker usa `/management/health/liveness`, que solo mira que el proceso responde:
con el de salud completo, un corte temporal de la base de datos haría que Docker reiniciara la
aplicación en bucle sin motivo.

Los certificados de Caddy viven en el volumen `caddy_data`. No hay que borrarlo: sin él, cada
reinicio pediría un certificado nuevo y se agotaría el límite de emisión de Let's Encrypt.

---

## 6. Pendiente / mejoras opcionales

- **Backups de la base de datos**: al ser un Postgres propio, no hay copias automáticas de un
  proveedor. Conviene un `pg_dump` periódico (cron) hacia fuera del servidor.
- **`spring.jpa.open-in-view`**: hoy queda en `true` (el default). Ponerlo a `false` libera antes
  la conexión del pool, pero los controllers serializan entidades JPA directamente, así que las
  colecciones perezosas se cargan durante la serialización, ya fuera de la transacción. Se puede
  probar con `SPRING_JPA_OPEN_IN_VIEW=false` en `.env` y recorrer los endpoints antes de hacerlo
  permanente.
- **Rotar credenciales**: en `application.yml` hay valores por defecto de desarrollo que son
  credenciales reales y están en el historial de git (la API key de Resend y el usuario de
  Mailtrap). Conviene revocarlas y dejar los defaults vacíos.
