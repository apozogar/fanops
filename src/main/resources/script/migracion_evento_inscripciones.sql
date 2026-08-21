-- Migración: evento_participaciones (ManyToMany antigua) -> evento_inscripciones (OneToMany nueva)
--
-- Contexto: antes los participantes se guardaban en la tabla join `evento_participaciones`
-- (evento_uid, socio_uid) sin estado ni fecha. Ahora cada inscripción vive en
-- `evento_inscripciones` con estado (CONFIRMADA/EN_ESPERA), fecha y copia de datos del socio.
--
-- Requisitos:
--   1. El backend debe haberse arrancado al menos una vez con ddl-auto=update para que
--      Hibernate cree la tabla `evento_inscripciones`.
--   2. La tabla `eventos` y `socios` deben contener los datos actuales.
--
-- El script es idempotente: si se ejecuta varias veces no duplica inscripciones.
-- Como antes no había aforo, todos los participantes históricos entran como CONFIRMADA.

INSERT INTO evento_inscripciones
    (uid, evento_uid, socio_uid, nombre, email, telefono, fecha_inscripcion, estado, socio_prioritario)
SELECT
    gen_random_uuid(),
    ep.evento_uid,
    ep.socio_uid,
    s.nombre,
    s.email,
    s.telefono,
    now(),
    'CONFIRMADA',
    (s.activo
      AND (s.exento_pago
           OR EXISTS (SELECT 1
                      FROM cuotas c
                      WHERE c.socio_uid = s.uid
                        AND c.estado = 'PAGADA'
                        AND c.fecha_emision >= CURRENT_DATE - INTERVAL '2 months')))
FROM evento_participaciones ep
JOIN socios s ON s.uid = ep.socio_uid
WHERE NOT EXISTS (SELECT 1
                  FROM evento_inscripciones ei
                  WHERE ei.evento_uid = ep.evento_uid
                    AND ei.socio_uid = ep.socio_uid);

-- Una vez confirmada la migración, se puede eliminar la tabla antigua:
-- DROP TABLE evento_participaciones;