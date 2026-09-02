# Despliegue de FanOps en Fly.io (Madrid) + Neon (Frankfurt)

Sustituye al despliegue en el plan gratuito de Render. Los tres problemas que resuelve:

| Problema en Render | Causa | Cómo queda |
|---|---|---|
| La primera visita tarda ~1 minuto | El plan gratuito para el servicio a los 15 min de inactividad | Máquina siempre encendida (`min_machines_running = 1`) |
| Todas las páginas van lentas | La base de datos estaba en `us-west-2` (Oregón) y la app en Frankfurt: ~150 ms **por consulta** | App en Madrid y base de datos en Frankfurt: ~15 ms |
| La primera consulta tras un rato tarda ~1 s | Neon suspende el compute a los ~5 min de inactividad | El health check cada 30 s toca la base de datos y la mantiene despierta |
| El sorteo de carnets no se celebraba solo | Con el servicio dormido no hay `@Scheduled` que valga | La máquina no se duerme, `SorteoCarnetScheduler` se ejecuta siempre |

---

## 0. Antes de empezar

Necesitas:

- [`flyctl`](https://fly.io/docs/flyctl/install/) instalado y sesión iniciada (`fly auth login`).
- `pg_dump` / `pg_restore` de PostgreSQL **17 o superior** (Neon corre versiones recientes; un
  `pg_dump` más antiguo que el servidor se niega a volcar).
- Las credenciales actuales de Neon (las que hoy están en las variables de entorno de Render).

---

## 1. Mover la base de datos a Frankfurt

Neon **no permite cambiar la región de un proyecto existente**: hay que crear uno nuevo y volcar
los datos.

1. En la consola de Neon, crea un proyecto nuevo en la región **AWS Europe (Frankfurt)
   `eu-central-1`**, con una base de datos llamada `fanops`.
2. Copia la cadena de conexión del endpoint **directo**, el que **no** lleva `-pooler` en el host.
   Con una sola instancia de la aplicación y su propio pool de HikariCP, el pooler de Neon
   solo añade un salto y complica las sentencias preparadas.
3. Vuelca el proyecto viejo y restaura en el nuevo (usa también los endpoints directos: pgbouncer
   no lleva bien `pg_dump`):

```bash
pg_dump --no-owner --no-privileges --format=custom -f fanops.dump "postgresql://USUARIO:CLAVE@ep-VIEJO.c-2.us-west-2.aws.neon.tech/fanops?sslmode=require"
```

```bash
pg_restore --no-owner --no-privileges --clean --if-exists -d "postgresql://USUARIO:CLAVE@ep-NUEVO.eu-central-1.aws.neon.tech/fanops?sslmode=require" fanops.dump
```

4. Comprueba que están los datos antes de seguir:

```bash
psql "postgresql://USUARIO:CLAVE@ep-NUEVO.eu-central-1.aws.neon.tech/fanops?sslmode=require" -c "select count(*) from socios;"
```

> **La URL de JDBC no es la de `psql`.** Para `SPRING_DATASOURCE_URL` hay que usar el formato
> `jdbc:postgresql://HOST/fanops?sslmode=require`, **sin** usuario ni contraseña dentro (van en
> sus propias variables) y **sin** `channel_binding=require`: ese parámetro es de `libpq` y el
> driver JDBC no lo entiende.

---

## 2. Crear la aplicación en Fly

```bash
fly apps create fanops
```

Si la app ya existe de un intento anterior, sáltate este paso. La configuración está en
[`fly.toml`](fly.toml): región `mad`, 1 vCPU compartida, 1 GB de RAM, siempre encendida.

---

## 3. Configurar los secretos

En Fly **no existe interpolación de secretos dentro de `fly.toml`**: lo que se crea con
`fly secrets set` aparece en el contenedor como variable de entorno con ese mismo nombre, así que
los nombres tienen que coincidir con los que lee `application.yml`.

```bash
fly secrets set --app fanops SPRING_DATASOURCE_URL="jdbc:postgresql://ep-NUEVO.eu-central-1.aws.neon.tech/fanops?sslmode=require" SPRING_DATASOURCE_USERNAME="neondb_owner" SPRING_DATASOURCE_PASSWORD="LA_CLAVE_DE_NEON" APP_JWT_SECRET="$(openssl rand -base64 48)" RESEND_API_KEY="re_..." MAIL_FROM_ADDRESS="noreply@tudominio.com" APP_SUPERADMIN_EMAIL="tu@correo.com" APP_SUPERADMIN_PASSWORD="una-clave-larga-y-temporal"
```

Notas:

- **`APP_JWT_SECRET` nuevo invalida todas las sesiones abiertas**, que es justo lo que quieres al
  cambiar de plataforma.
- `APP_SUPERADMIN_PASSWORD` solo se usa si todavía no existe ningún superadmin; cámbiala desde la
  aplicación tras el primer login.
- El resto de valores (proveedor de correo, `PUBLIC_BASE_URL`, nivel de log, tamaño del pool) van
  en claro en `[env]` de `fly.toml`, porque no son secretos.
- Para volver a SMTP en lugar de la API de Resend: `fly secrets set APP_EMAIL_PROVEEDOR=smtp
  SMTP_HOST=... SMTP_PORT=587 SMTP_USERNAME=... SMTP_PASSWORD=...`. En Fly los puertos SMTP de
  salida **no** están bloqueados, a diferencia del plan gratuito de Render.

---

## 4. Desplegar

```bash
fly deploy --ha=false
```

`--ha=false` evita que Fly cree dos máquinas por defecto. Con una basta para esta carga, y dos
duplicarían el coste sin aportar nada mientras el estado siga viviendo entero en la base de datos.

Verificación:

```bash
fly status --app fanops && fly logs --app fanops
```

```bash
curl -s https://fanops.fly.dev/management/health
```

Debe responder `{"status":"UP"}`. Si da `DOWN`, `fly logs` dirá si es la conexión a la base de
datos (URL de JDBC mal formada, contraseña, o IP no permitida en Neon).

---

## 5. Dominio propio (opcional)

```bash
fly certs add fanops.tudominio.com --app fanops
```

`fly certs show` indica qué registros DNS hay que crear. Después hay que actualizar la URL pública,
porque de ella salen los enlaces de los correos y de WhatsApp: cambia `PUBLIC_BASE_URL` en
`fly.toml` y vuelve a desplegar.

---

## 6. Cuando el esquema esté estable

`SPRING_JPA_DDL_AUTO` está en `update` en `fly.toml` para que el primer arranque contra la base de
datos restaurada cuadre solo. Una vez comprobado que no hay diferencias, pásalo a `validate`:
`update` compara todo el metamodelo de Hibernate contra la base de datos **en cada arranque**, y
eso son decenas de idas y vueltas antes de aceptar la primera petición.

---

## 7. Operación

| Qué | Comando |
|---|---|
| Logs en vivo | `fly logs --app fanops` |
| Reiniciar | `fly apps restart fanops` |
| Consola dentro del contenedor | `fly ssh console --app fanops` |
| Subir a 2 GB de RAM | `fly scale memory 2048 --app fanops` |
| Ver secretos configurados (no sus valores) | `fly secrets list --app fanops` |
| Backups de la base de datos | Los gestiona Neon (point-in-time restore según el plan) |

---

## 8. Coste aproximado

- Máquina `shared-cpu-1x` con 1 GB encendida 24/7: ~6 $/mes.
- Neon: el plan gratuito sirve; si el consumo de horas de compute se queda corto por el health
  check cada 30 s, el plan de pago más bajo ronda los 5 $/mes.

---

## 9. Pendiente / mejoras opcionales

- **`spring.jpa.open-in-view`**: hoy queda en `true` (el default). Ponerlo a `false` libera antes
  la conexión del pool, pero los controllers serializan entidades JPA directamente, así que las
  colecciones perezosas se cargan durante la serialización, ya fuera de la transacción. Se puede
  probar con `fly secrets set SPRING_JPA_OPEN_IN_VIEW=false` y recorrer los endpoints antes de
  hacerlo permanente.
- **Cloudflare gratis por delante** del dominio: cachea en el edge los bundles de Angular, que
  llevan hash en el nombre y se pueden cachear indefinidamente.
- **Rotar credenciales**: en `application.yml` hay valores por defecto de desarrollo que son
  credenciales reales y están en el historial de git (la API key de Resend y el usuario de
  Mailtrap). Conviene revocarlas y dejar los defaults vacíos.
