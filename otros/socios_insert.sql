-- Socios importados desde Excel (Socios 2026.xlsx)
-- Orden segun tab "SOCIOS JULIO 2026"
-- Ejecutar contra la BD de produccion (Neon) una sola vez.
-- Requiere que exista la pena con id=1.

BEGIN;

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('a49748f7-a0b1-46cc-970c-9af04b0bdb37', 1, 'MANUEL GARCIA RGUEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES4521008052640200045113', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('0beab31c-54ef-4c95-ae9f-387f87e8b1e8', 2, 'MANUEL JESUS RUIZ PAEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES4800495367602594188388', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('0e338756-afe2-482a-8cf9-47e4f73e643c', 3, 'MANUEL RUIZ DIAZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES4800495367602594188388', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('e824f409-0bc0-4491-8e6f-4f3d3f13e213', 4, 'RAFAEL MORALES CARMONA', '1960-05-03', '25308269.0', 'C/Ramón y Cajal 41', 'Gilena', 'Sevilla', '41565', '673660583', NULL, CURRENT_DATE, true, false, false, false, 'ES3200304169210000316271', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('cf93f54c-1f5f-41af-afeb-723fc3612b5e', 5, 'FRANCISCO JAVIER ACEIJAS DIAZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES1921008052610100075103', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('3512bf5b-9fc1-4995-9f93-e870735d5a83', 6, 'MANUEL RODRIGUEZ RUIZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES5800496287912110017763', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('cee057ae-d6fa-4656-b1c9-357204cc7a48', 7, 'JUAN ANTONIO PAEZ GUERRERO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES1831870402681384345516', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('160fa65f-c964-4995-b5a2-2f5e1923ea3b', 8, 'MANUEL MONTANO POZO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES8900492647722214050470', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('0876fbf9-9dcc-4b05-b225-2d1a759a62c4', 9, 'PEDRO ROMERO GRANADOS', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES0800043023840700312694', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('27a21227-d589-4d06-8261-fe27fce7eab8', 10, 'RAFAEL JOYA RODRIGUEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES7931870402692524723414', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('f8bb7033-ae7d-48ec-b102-1f1f651f8cca', 11, 'Ángela Chia Gutiérrez', '2014-06-29', '26792426 w', 'C/jardinez 14', 'Gilena', 'Sevilla', '41565', '665832479', 'Vanebiza@hotmail.com', CURRENT_DATE, true, false, false, false, 'ES4031870402611384324511', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('404d43af-fae1-4cb9-ad47-8dc9594fae4a', 12, 'Antonio jesus García Gutiérrez', '2011-12-05', '26791695 F', 'Calle de la luna 8', 'Gilena', 'Sevilla', '41565', '66582479', 'Vanebiza@hotmail.com', CURRENT_DATE, true, false, false, false, 'ES4031870402611384324511', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('273221d3-a210-4200-b3a9-297477b05c4c', 13, 'Antonio jose Pozo Gutiérrez', '2010-07-02', '23883135 G', 'Calle jardines 14', 'Gilena', 'Sevilla', '41565', '66582479', 'Vanebiza@hotmail.com', CURRENT_DATE, true, false, false, false, 'ES4031870402611384324511', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('fba023a3-79f3-44ba-9f12-63360e4d7267', 14, 'VANESA GUTIERREZ CHIA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES4031870402611384324511', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('5879379e-3c8c-4736-8b8c-251f1eee4dfd', 15, 'JOSE MANUEL DIAZ GUERRA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES0500753023750700219547', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('ceaa8cd3-5c0c-4526-9dda-c4c9e7196dea', 16, 'FRANCISCO BORREGO RAMOS', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES9700304169220000548143', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('d26ca9b0-7444-4679-a322-7f2db0358e1d', 17, 'JUAN BLANCO JIMENEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES9231870402651384309314', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('11125af3-52fa-4c1f-961f-69fa08c8417b', 18, 'Benjamín Borrego Romero', '2010-05-25', '26300970X', 'Silencio 1', 'Gilena', 'Sevilla', '41565', '722597443', 'villalerna@gmail.com', CURRENT_DATE, true, false, false, false, 'ES2131870402661384363311', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('ba9d3953-9679-4013-8cd5-bc382069d057', 19, 'FCO ROMERO REINA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES2131870402661384363311', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('ee062608-a2c2-43db-afea-4bd21ceb82b4', 20, 'JUAN CARLOS MONTANO RGUEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES3731870402631384336010', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('2a31e349-df9a-4a91-acc3-33f5e4777c2f', 21, 'FCO JURADO REINA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES1500043023840700089856', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('f3f62976-857d-42fa-8b4f-93bc5b632bab', 22, 'Ángela Maria Reina Ruiz', NULL, '26907409P', 'Calle Álamos 9 2A', 'Gilena', 'Sevilla', '41565', '687157428', NULL, CURRENT_DATE, true, true, true, false, 'ES0821008052652100200458', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('ca9ae237-d137-4d69-9aa9-1c430b1694ff', 23, 'FCO MANUEL REINA RGUEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES0821008052652100200458', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('135baaf9-f3ab-4358-beea-91fbacccb061', 24, 'JERONIMO MUNOZ RGUEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES7031870402631384355010', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('9998534f-e721-4243-a34a-06a6c0d071cc', 25, 'JOSE REINA REINA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES6500043023800700207492', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('069d1320-fd85-4ab1-92fa-c7c76069d160', 26, 'JOSE MORENO MONTANO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES3821008052620200043882', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('462d0db7-233f-455f-88ac-c6f805787dd0', 27, 'FCO DIAZ TRESGALLO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES8431870402601093131223', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('6c3cb8cc-1d2d-4fc7-9dfd-b8a384a438a0', 28, 'FRANCISCO DIAZ TRESGALLO MIR', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES8431870402601093131223', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('506e0d42-609b-41c5-80d1-f8a21aa94a1b', 29, 'FRANCISCO DIAZ LOPEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES1400043023890700228339', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('bcd06747-284c-4223-8d5f-f20ee2fd8036', 30, 'EMILIO GOMEZ RODRIGUEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES1331870402681384367916', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('70571d41-da23-4351-a760-403e916966f6', 31, 'EDUARDO SANCHEZ PONCE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES8921008052642100302160', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('46e4467c-9323-4e31-b55f-2334cac396bb', 32, 'FCO SANCHEZ RODRIGUEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES8921008052642100302160', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('42f31786-8836-4827-b1b1-ab1a9b4c9c54', 33, 'ANGEL DIAZ RODRIGUEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES2100043023800600000203', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('4159e0c7-ce20-4c52-a009-afa2f3af5ce7', 34, 'ANTONINO DIAZ GIL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES2100043023800600000203', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('ff9c6d79-bb97-4a1c-99d8-12e4f8f2680f', 35, 'INMA VICTORIA DIAZ RGUEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES2100043023800600000203', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('d7513b6d-bbbc-4b8a-ae36-dc6958acc9fa', 36, 'MARCO ANTONIO DIAZ RGUEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES7700043023810700161055', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('b51d7d7f-8542-4b6b-b1a6-4a3e170bbe24', 37, 'RAMON MORENO MONTANO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES3300753023710601197953', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('18c8c34a-c4f2-41a3-b3b6-b7d299c090af', 38, 'FCO RGUEZ JIMENEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES2731870402692314364718', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('e18c868a-a107-4717-80ff-80b18546ee46', 39, 'FCO REYES CORDON', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES1031870402691384345417', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('2dd82398-eb59-413e-96d5-b9dbafab02f5', 40, 'JUAN LEIVA RGUEZ', '1987-07-05', '47509309A', 'C/san Isidro 33', 'Gilena', 'Sevilla', '41565', '651373834', 'palimpa16@gmail.com', CURRENT_DATE, true, false, false, false, 'ES0502370311109171570912', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('67348c3b-670f-4408-a67c-ddc83debfc09', 41, 'JERONIMO ALVAREZ MUNOZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES2331870402631093146627', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('c7677a01-ef60-4402-a4dc-1f3f53157a56', 42, 'FCO JESUS RGUEZ GUTIERREZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES1600304169280000243272', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('9ec51f47-8388-4800-9a9f-b0f5091ec811', 43, 'JOSE JOAQUIN RUIZ CHIA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES8031870402601384308514', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('97e4d0e4-040a-45cf-b86f-3bd4543ae66e', 44, 'RAMON GUTIERREZ RGUEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES1600304169280386927273', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('70f17452-4b93-47bc-8689-bdebd55f6a66', 45, 'Rafael Reina Gomez', '1955-10-27', '75377153N', 'Silencio 15', 'Gilena', 'Sevilla', '41565', '642631955', 'matias_3_34@hotmail.com', CURRENT_DATE, true, true, false, false, 'ES1121008052630200039954', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('35472475-e8fa-4f06-83d3-7a963c8c96ba', 46, 'MATIAS REINA GORDILLO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES6931870402641384337810', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('e3e183d8-dcb9-4262-86ae-0438c6b26ce9', 47, 'SANTIAGO JIMENEZ FDEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES2331870402601384329619', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('ac4402d5-8b98-499a-84bc-8c61eb04f61b', 48, 'ANTONIO JURADO CHIA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES2700043023810700027319', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('170d6d1c-a65d-4c36-b516-77984678bdd9', 49, 'FRANCISCO JESUS HARO MAIRELES', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES7601827081410201523812', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('12a733c2-81d3-4610-bc45-103d458d6bd4', 50, 'ALEJANDRA BELLVER MURGA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES2100491652152710026146', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('0ff529dd-d5aa-40ec-becd-dcb2f4974f69', 51, 'CRISTINA BELLVER MURGA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES6114650130041724035366', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('b7aed0ca-a922-4f2b-a12d-0ce70fbe8893', 52, 'ANTONINO DIAZ RGUEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES2800043023810600097125', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('77f3cfdb-b1d0-41fe-9526-46ccabed6772', 53, 'ANTONIO ACEIJA JURADO', '1974-08-07', '77538488Z', NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES9531870402601384370712', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('6ad0d3db-70b3-40c2-8aed-2f7799b43179', 54, 'ANTONIO JESUS RODRIGUEZ DIAZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES5321004478570100208573', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('eb164535-9e23-4549-8409-fba7989d2224', 55, 'MARI CRUZ ACUNA JOYA', '1985-05-03', '47501050r', 'calle sevilla 36a', 'Gilena', 'Sevilla', '41565', '617280094', 'maricruzmartin16@gmail.com', CURRENT_DATE, true, false, false, false, 'ES0431870402612912425911', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('0e399186-82ba-4149-94f9-c6a17ab3e662', 56, 'Martín Romero Acuña', '2014-04-16', '47501050R', 'Cl Sevilla 36A', 'Gilena', 'Sevilla', '41565', '617280094', 'maricruzmartin16@gmail.com', CURRENT_DATE, true, false, false, false, 'ES0431870402612912425911', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('97ef9b9a-4645-4017-bd3d-b456a7ea9400', 57, 'PILAR BELLVER MURGA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES1021007503012300028989', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('470819dd-5cca-4c02-a35b-aea2b9aa2c7d', 58, 'DAVID CORONA ROBLES', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES9700651078790001019188', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('98230d1d-3421-40ae-9986-de4f91eb0e5e', 59, 'DIAZ HARO ANTONIO MANUEL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES2331870402621093132320', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('35792e32-5b16-4092-8ccb-6808aa5a3706', 60, 'SEBASTIAN RODRIGUEZ RODRIGUEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES0731870402691093133526', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('ed6bbad8-a595-4539-82c9-7e7e1b5b96c9', 61, 'LUNA MACIAS ANTONIO JOSE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES3200304169210387430273', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('6d179f83-093e-4443-aca2-93d065daa29b', 62, 'HARO RODRIGUEZ, ALFONSO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES8121008052642100256811', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('f877647a-bfcd-403f-8d1f-912ada59f357', 63, 'DIAZ GUERA, ENRIQUE JESUS', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES6721008052650100044749', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('18ef0e7f-4aa1-44ed-8859-0d2dc3b54a5a', 64, 'SANCHEZ BORREGO,MANUEL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES6931870402633117894117', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('06d8ae1e-01f3-4ecd-a754-5aad7d79ed83', 65, 'GONZALEZ RODRIGUEZ, JOSE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES1721060407850002282044', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('e6dfeeae-8ab8-4891-9182-6f19a759f5ad', 66, 'CANSINO RIVERO, MANUEL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES9321060407850002049040', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('1c330600-0da8-4e7f-8288-cc784c9ce05f', 67, 'GONZALEZ CALZADO, ANTONIO MANUEL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES5221060407870140389043', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('d7041296-02d2-4937-811b-322e4ae69000', 68, 'DIAZ RODRIGUEZ, CASTO JESUS', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES9521060407850003076040', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('76916bc0-c497-4284-bee7-753a84533ac3', 69, 'GUTIERREZ LUNA, VICTOR JOSE', '1975-03-03', '77538298P', 'C/María Zambrano 13', 'Gilena', 'Sevilla', '41565', '645924092', 'victorgilena@gmail.com', CURRENT_DATE, true, false, false, false, 'ES9321060407850003310040', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('73a3464a-b0e7-4026-910e-140121819254', 70, 'MONTANO POZO, RAFAEL', '1964-03-02', '75412407F', 'San Francisco de Boja $', 'Gilena', 'Sevilla', '41565', '600794622', 'rafamontanopozo1964@gmail.com', CURRENT_DATE, true, false, false, false, 'ES6721060407850002129048', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('3e85baeb-40e6-4795-8064-4dc9afb91499', 71, 'LOPEZ PARDO, INMACULADA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES9221060407850002305041', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('f1e8f1de-7137-41e5-b33f-8223a0c154a8', 72, 'GUTIERREZ GORDILLO, ANTONIO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES1521060407820253392044', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('6093d0fe-cdd7-44f5-83bf-b8d444573731', 73, 'GOMEZ CARO, JOSE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES7321060407850002313046', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('b7de8be5-0c26-4d24-86f5-dfe27e1e14c2', 74, 'GOMEZ GOMEZ, MARIA JOSE', NULL, NULL, NULL, 'F', NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES7321060407850002313046', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('4e66fdeb-b34e-466f-98c2-e88cdabea8c1', 75, 'MONTANO DIAZ, JOSE MIGUEL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES5721060407850000632033', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('d5c0a437-b5d3-4082-bcc3-d5682e3d9e7d', 76, 'MONTANO RODRIGUEZ, JOSE MANUEL', '1956-06-29', '75377228H', 'Calle nueva 19', 'Gilena', 'Sevilla', '41565', '686481886', 'jm.montarodri@gmail.com', CURRENT_DATE, true, false, false, false, 'ES5721060407850000632033', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('18030425-0199-4869-be12-0dccde3f4b3f', 77, 'POZO RODRIGUEZ, JOSE', '1944-11-01', '75276054K', 'C/del paso 15', 'Gilena', 'Sevilla', '41565', '617701964', 'jpozorod@gmail.com', CURRENT_DATE, true, false, false, false, 'ES0721060407850001221044', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('20c33183-8187-4035-b270-0c06f8879e68', 78, 'DIAZ GONZALEZ, MANUEL JERONIMO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES4421060407840003244041', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('6bc61039-6632-4119-a251-cc406f18b485', 79, 'ACEIJAS DIAZ, JESUS', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES8621060407830161414056', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('c00b8163-0fcf-4f9d-a69b-348359e96d37', 80, 'DIAZ RODRIGUEZ, PEDRO MANUEL', '1070-08-24', '52563965H', 'Calle nueva 24', 'Gilena', 'Sevilla', '41565', '626994443', 'pdiaz906@gmail.com', CURRENT_DATE, true, true, false, false, 'ES2800494575192190004973', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('0e193a8f-3bac-4ff5-91b6-28aa86262c72', 81, 'MORILLAS RODRIGUEZ, MIGUEL ANGEL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES7321060407810386378049', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('f00322ff-c114-488c-a68b-2257725671d8', 82, 'ROMERO GUERRA, MARIA VICTORIA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES7521060407840417205048', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('8471645e-38c3-4e85-9a76-f1053d2407d7', 83, 'GOMEZ GUERRERO, MANUEL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES7021060407830394114046', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('bc46d707-def9-4cc9-89d2-86e494cbf1ff', 84, 'ACUNA CARMONA, PEDRO', '1956-05-26', '28453999d', 'c/fatima', 'Gilena', 'Sevilla', '41565', '645307508', NULL, CURRENT_DATE, true, false, false, false, 'ES3421060407850002207048', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('59b92d78-c25b-496e-906b-e4d2d62f798d', 85, 'GARCIA AMADOR, MANUEL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES5521060407850002353043', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('32759afb-35c6-439a-b048-e2257a93eed8', 86, 'GUTIERREZ LINARES, FRANCISCO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES7921008052642100210578', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('683d078f-494d-4668-b950-7896d4274ca3', 87, 'ALVAREZ RODRIGUEZ, SALVADOR', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES2621060407870433946043', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('86a3f1e5-993b-4bf9-afc3-2a8bd1faf911', 88, 'CARVAJAL PEREZ, MANUEL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES9221060407890145910047', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('8cbdd872-f80e-49c8-a46c-c36f33f705e9', 89, 'RUIZ POZO, JUAN', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES2921060407850002071047', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('9e2f5063-37ea-46d6-a874-0fe6caa7002b', 90, 'HARO FERNANDEZ, MIGUEL RAMON', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES0521060407850002674043', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('2880b5da-90fa-46e4-a89f-ecb37136b261', 91, 'JURADO LUNA, FRANCISCO JOSE', '1965-05-02', '75431131D', 'calle ramon y cajal 3 primero', 'Gilena', 'Sevilla', '41565', '651834978', 'franjuradoluna@hotmail.com', CURRENT_DATE, true, false, false, false, 'ES6921060407850003059048', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('cb5b7a66-5ff5-40aa-88c8-b0f66b5d1f0a', 92, 'MILLA RODRIGUEZ, MANUEL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES9521060407850002746046', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('02531bf2-8889-492a-9870-fe082ef08131', 93, 'LOBO ESCOBAR, MARCOS ANTONIO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES5921060407870528734043', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('ce549fbe-0f82-42cb-85fe-70a7894ccc46', 94, 'LOPEZ PARDO, ANDRES MARIA', '1960-02-06', '25564924x', 'C/1 Octubre 3', 'Gilena', 'Sevilla', '41565', '670943652', 'cosasdenervios@gmail.com', CURRENT_DATE, true, true, true, false, 'ES1021060407850002173045', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('f782d2c6-ab9f-4f76-8415-a50283fbc6c6', 95, 'JURADO LUNA, JUAN MANUEL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES4421060407850003087046', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('07a0f89a-f145-485c-94c5-c90846f917c3', 96, 'POZO GORDILLO, FRANCISCO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES8321060407850002682048', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('b840a3f3-3272-4012-80be-7232b328da5b', 97, 'DIAZ MARTIN, MANUEL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES5421060407850002053040', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('2b4d3b5a-4468-4537-86a1-f23198681c00', 98, 'JURADO GOMEZ, ANTONIO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES1121060407850002195046', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('ce25d078-d50a-44f7-b943-66af344fab44', 99, 'POZO REINA, PEDRO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES8921060407850002950048', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('7d261e4d-fd2a-4d9b-88df-a4bd16a6b76d', 100, 'POZO PRADAS, JOSE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES0621060407850002974046', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('64208fbe-1c0b-433b-af84-b3fa800da244', 101, 'DIAZ CHIA, JUAN DE DIOS', '1968-11-15', '75429389S', 'Eusebio Dieguez 21', NULL, NULL, NULL, '699985729', 'juanandrea237@gmail.com', CURRENT_DATE, true, false, false, false, 'ES7921060407810247354049', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('9e8aa9e4-b00b-4985-9c33-ac667bbb95b2', 102, 'DIAZ CARVAJAL, JOSE DAVID', '1990-10-04', '47537845L', 'c/Triana 9', 'Gilena', 'Sevilla', '41565', '625582738', 'jdadiazcar_17@hotmail.com', CURRENT_DATE, true, true, false, false, 'ES8221060407850003171043', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('d3955e83-bf27-4419-9867-55fd3e106f04', 103, 'DIAZ GALVEZ, JOSE ANTONIO', '1962-01-29', '75412711A', 'calle Luis Cernuda 43', 'Gilena', 'Sevilla', '41565', '638223165', NULL, CURRENT_DATE, true, false, false, false, 'ES8221060407850003171043', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('1322dc04-6dd0-4baa-9def-ac2681f7dc94', 104, 'ROMERO GUERRA, LIDIA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES7421060407880182540045', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('44fbecee-da4a-424d-ba76-94731a50ecb2', 105, 'MANUEL CARVAJAL CASTILLO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES9600753023780700044908', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('0296abb6-09a0-4b13-bc73-26b57c9b8a3e', 106, 'Jose Rodriguez Corrales', NULL, '77538316A', 'C/nueva 112', 'Gilena', 'Sevilla', '41565', '670356021', NULL, CURRENT_DATE, true, false, false, false, 'ES6021008052682100021671', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('3e09ecce-9042-4428-a22a-e88aa7e2b8b2', 107, 'Jerónimo Muñoz Arias', NULL, '25313018T', 'C/Almería 6', 'Gilena', 'Sevilla', '41565', '615872808', NULL, CURRENT_DATE, true, false, false, false, 'ES9421008052642200062302', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('182b0d12-9864-49ba-8da9-27ed147a5015', 108, 'Miguel Antonio García Carajello', '1966-10-17', '75425130B', NULL, 'Gilena', 'Sevilla', '41565', '626987996', 'miguelgarciapozo.MGC@gmail.com', CURRENT_DATE, true, false, false, false, 'ES2531870402693443106418', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('30340167-ad70-4598-ba6f-814cfd02ce37', 109, 'Alejandro Segura Pozas', '2017-12-19', '14638027E', 'Huelva, 18 primero', 'Gilena', 'Sevilla', '41565', '617544949', 'Pbeticaluisbellver@gmail.com', CURRENT_DATE, true, false, false, false, 'ES9331870402695249871814', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('e6ba1b27-025a-4332-85bb-282c1804650b', 110, 'Antonio Jesús Segura Fernández', '1986-07-22', '14638027E', 'Huelva, 18 primero', 'Gilena', 'Sevilla', '41565', '617544949', 'Pbeticaluisbellver@gmail.com', CURRENT_DATE, true, false, false, false, 'ES9331870402695249871814', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('f8724381-d9b8-4e7a-abad-38549d1f277b', 111, 'Jesús Segura Pozas', '2015-12-27', '14638027e', 'Huelva, 18 primero', 'Gilena', 'Sevilla', '41565', '+34617544949', 'Pbeticaluisbellver@gmail.com', CURRENT_DATE, true, false, false, false, 'ES9331870402695249871814', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('51790f18-d9fe-4dfb-9483-350dc3454c9b', 112, 'Triana Martin Pozas', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES6931870402694546160914', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('04f7f230-ae78-4561-a1e0-917cd34d7a50', 113, 'Adriana Nogales Rodríguez', '2023-03-30', '47509320Z', 'C/ Donantes de Órganos, 24', 'Gilena', 'Sevilla', '41565', '617500811', 'noritdewit@gmail.com', CURRENT_DATE, true, true, false, false, 'ES8821008052670200041282', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('67069b1b-4473-4c11-9b33-05c4ef67ba03', 114, 'Honorato Nogales Vallejo', '1987-10-07', '47509320Z', 'C/ Donantes de Órganos, 24', 'Gilena', 'Sevilla', '41565', '617500811', 'noritdewit@gmail.com', CURRENT_DATE, true, true, false, false, 'ES8821008052670200041282', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('72c9733e-5f86-4776-8d53-c8b0ab4af933', 115, 'Alberto García Gutiérrez', '2015-04-26', '47508203J', 'C/ De la Luna,8', 'Gilena', 'Sevilla', '41565', '665832480', 'Noebiza83@gmail.com', CURRENT_DATE, true, false, false, false, 'ES5421004478580200057590', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('7e23f4fb-efa4-42e1-b4be-5f00d658f7c1', 116, 'Noelia Gutiérrez Chía', '1983-12-15', '47507203j', 'C/ De la Luna, 8', 'Gilena', 'Sevilla', '41565', '665832480', 'noebiza83@gmail.com', CURRENT_DATE, true, false, false, false, 'ES5421004478580200057590', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('9e8b40e1-b9ed-407a-9990-2cb302295296', 117, 'Alexis Díaz Chía', '2022-10-14', '47537845L', 'La Cruz 37', 'Gilena', 'Sevilla', '41565', '625582738', 'jdadiazcar_17@hotmail.com', CURRENT_DATE, true, true, false, false, 'ES0800496287912190045894', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('169486cd-5e47-4e00-b0be-d73663eea12a', 118, 'Dylan Díaz Chia', '2021-03-17', '47537845L', 'Calle la Cruz 37', 'Gilena', 'Sevilla', '41565', '625582738', 'jdadiazcar_17@hotmail.com', CURRENT_DATE, true, true, false, false, 'ES0800496287912190045894', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('04fecec5-ec59-4f19-bb25-b3f993ed1a32', 119, 'Ana María Reina Reina', '2011-11-20', '47500521R', 'Calle Jesús Nazareno 11', 'Gilena', 'Sevilla', '41565', '600303888', NULL, CURRENT_DATE, true, false, false, false, 'ES7531870402624658588514', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('58b13c8e-4bce-42e4-885f-73ca606766fd', 120, 'Antonio Manuel Reina Castillo', '1982-03-10', '47500521R', 'Calle Jesus Nazareno 11', 'Gilena', 'Sevilla', '41565', '600303888', 'jreinacastillo@gmail.com', CURRENT_DATE, true, false, false, false, 'ES7531870402624658588514', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('1407e88c-771c-4259-88ed-d7d46f40205d', 121, 'Ángel Díaz Montaño', '2010-04-29', '29548240W', 'Paseo de Andalucía 2 - 1⁰', 'Gilena', 'Sevilla', '41565', '604541027', 'angeldiazmnn@gmail.com', CURRENT_DATE, true, true, false, false, 'ES9821001874600200053719', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('bcf21682-f6a3-4bdf-91fa-744a2e58b1a4', 122, 'Antonio Damian Leiva Reina', '1982-09-08', '47501059X', 'C/ Eusebio Dieguez 8', 'Gilena', 'Sevilla', '41565', '637155303', NULL, CURRENT_DATE, true, false, false, false, 'ES4402370311119159905911', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('785cf560-aaf1-48c4-aedc-52ea29af83d8', 123, 'Antonio Leiva Caballero', '2014-06-05', '26300967F', 'C/ Eusebio Dieguez 8', 'Gilena', 'Sevilla', '41565', '637155303', NULL, CURRENT_DATE, true, false, false, false, 'ES4402370311119159905911', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('b03b2b78-14fd-4f89-90f2-c485633eaf4e', 124, 'Daniel Rodriguez', '2011-03-18', '26907983F', 'Ramon y cajal 4', 'Gilena', 'Sevilla', '41565', '623300937', 'DanielRodríguezcarvajal@gmail.com', CURRENT_DATE, true, false, false, false, 'ES4631870402622797425010', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('87c6887c-047d-49a0-a899-c788038008a3', 125, 'Fabián Fernández Naranjo', '2021-10-29', '47538186s', 'Del Rosario 9B', 'Gilena', 'Sevilla', '41565', '663662284', 'miguelantoniofernandezdiaz@gmail.com', CURRENT_DATE, true, false, false, false, 'ES1521002604140110409942', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('64612f60-7b0f-4da6-8a6c-6c327c4be929', 126, 'Miguel Antonio Fernández Díaz', '1989-04-07', '47538186s', 'Del Rosario 9B', 'Gilena', 'Sevilla', '41565', '663662284', 'miguelantoniofernandezdiaz@gmail.com', CURRENT_DATE, true, false, false, false, 'ES1521002604140110409942', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('12f27549-a42c-488d-9fd4-6f43d43ea986', 127, 'Felipe Antonio Gómez Rodríguez', '1976-09-29', '28929745T', 'Eusebio Dieguez 37', 'Gilena', 'Sevilla', '41565', '653797004', 'antoniofe89@gmail.com', CURRENT_DATE, true, false, false, false, 'ES7821008052652100321515', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('3163ccee-74e2-4b47-811c-8a20583787c7', 128, 'Felipe Gómez Rodríguez', '2011-09-28', '14618696J', 'Eusebio Dieguez 37', 'Gilena', 'Sevilla', '41565', '653034393', 'inrodriv@gmail.com', CURRENT_DATE, true, false, false, false, 'ES7821008052652100321515', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('41e78de8-b82f-4a4f-9a49-3a2161fc1050', 129, 'Inmaculada Rodríguez Rivera', '1979-04-08', '14618606 J', 'Eusebio Dieguez 37', 'Gilena', 'Sevilla', '41565', '653034393', 'inrodriv@gmail.com', CURRENT_DATE, true, false, false, false, 'ES7821008052652100321515', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('75c78268-b819-47fe-b30b-5ad97f65f7a5', 130, 'Francisco Javier Rodríguez González', '2017-10-11', '47545348R', 'Calle Carretera de Estepa,3', 'Gilena', 'Sevilla', '41565', '635463405', 'lalyglezchia@gmail.com', CURRENT_DATE, true, false, false, false, 'ES0421008052640100068242', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('9be2b905-d027-4336-a9c0-eeb13b17c0de', 131, 'Laly González Chia', '1988-03-10', '47545348R', 'Calle Carretera de Estepa 3', 'Gilena', 'Sevilla', '41565', '635463405', 'lalyglezchia@gmail.com', CURRENT_DATE, true, false, false, false, 'ES0421008052640100068242', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('f3cfcb34-cae2-44a0-9aef-06b27e2d65fb', 132, 'Gonzalo Pozo pariente', '2021-10-25', '23882678F', 'C/Nueva,76', 'Gilena', 'Sevilla', '41565', '693068232', 'inmaculadapozogalvez@gmail.com', CURRENT_DATE, true, false, false, false, 'ES6431870402672809058718', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('4262ee57-e429-4d7a-8699-13367325e4bf', 133, 'Inmaculada Pozo Gálvez', '2001-09-20', '23882678F', 'C/Nueva, 76', 'Gilena', 'Sevilla', '41565', '693068232', 'inmaculadapozogalvez@gmail.com', CURRENT_DATE, true, true, false, false, 'ES6431870402672809058718', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('7ddd9627-6efe-433e-a392-eb4d88e05ea5', 134, 'Julieta Rodríguez Pozo', '2020-10-20', '23882678F', 'C/ Nueva,76', 'Gilena', 'Sevilla', '41565', '693068232', 'inmaculadapozogalvez@gmail.com', CURRENT_DATE, true, false, false, false, 'ES6431870402672809058718', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('a1f1b2fa-82c8-4a5a-8b46-a743bbb80d51', 135, 'Marta Pozo Pariente', '2018-03-22', '23882678F', 'C/Nueva,76', 'Gilena', 'Sevilla', '41565', '693068232', 'inmaculadapozogalvez@gmail.com', CURRENT_DATE, true, false, false, false, 'ES6431870402672809058718', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('f65112ca-5761-488c-a810-3311806d4c44', 136, 'Hugo Gómez Escudero', '2017-02-19', '47537084V', 'Calle Los Villares', 'Gilena', 'Sevilla', '41565', '653044749', NULL, CURRENT_DATE, true, false, false, false, 'ES9421008052630100064537', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('26c03ad0-16ad-418d-b736-33656b2e3e33', 137, 'ANTONIO MANUEL JOYA CARRASQUILLA', '1982-09-03', '74909676X', 'CL 9 OCTUBRE 8', 'GILENA', 'SEVILLA', '41565', '676069467', 'Carrasquilla99@hotmail.com', CURRENT_DATE, true, true, false, false, 'ES9131870402673329855112', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('600eb778-fff1-4be2-9cac-d36189802186', 138, 'ISABEL JOYA MONTAÑO', '2021-12-07', '74909676X', 'Cl 9 de octubre 8', 'Gilena', 'Sevilla', '41565', '619308422', 'Mada_11_5@hotmail.com', CURRENT_DATE, true, false, false, false, 'ES9131870402673329855112', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('a695c47f-4287-41cc-9af0-ed7a0365c433', 139, 'Izan Franco Ponce', NULL, '04732968M', 'C/ Paloma n 21', 'Gilena', 'Sevilla', '41565', '615060627', NULL, CURRENT_DATE, true, false, false, false, 'ES8521008052672100190050', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('5cdddeb1-23e8-477d-910c-d2f6f707b006', 140, 'José Antonio Ponce Ruiz', '1957-12-12', '75377238m', 'Calle Fátima n 13', 'Gilena', 'Sevilla', '41565', '630789876', 'joseantonioponceruiz@gmail.com', CURRENT_DATE, true, false, false, false, 'ES8521008052672100190050', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('d6dd6bcd-3a87-41a0-8773-65de7f1a263f', 141, 'Javier Acuña Pozo', '2011-05-26', '26907411x 47501691k', 'Maria Auxiliadora 15A', 'Gilena', 'Sevilla', '41565', '693793235', 'Ajam9779@gmail.com', CURRENT_DATE, true, false, false, false, 'ES0421008052652100305193', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('26a55a2e-6064-4d90-84f4-87f5e58070c5', 142, 'CAYETANO JOYA GÓMEZ', '2008-01-16', '24481714P', 'Calle García Lorca,  1', 'Gilena', 'Sevilla', '41565', '681659462', NULL, CURRENT_DATE, true, false, false, false, 'ES2421008052602100299646', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('b862fd2f-f38f-4eba-99bf-1674dad2c824', 143, 'CLAUDIA JOYA GÓMEZ', '2006-01-29', '24481713F', 'Calle García Lorca,  1', 'Gilena', 'Sevilla', '41565', '635294192', 'claudiajoyagom@gmail.com', CURRENT_DATE, true, false, false, false, 'ES2421008052602100299646', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('6a56ea23-1851-4adf-a443-b4f2c73447a4', 144, 'JOSÉ MARÍA JOYA GÓMEZ', '2018-12-14', '17510198E', 'Calle García Lorca,  1', 'Gilena', 'Sevilla', '41565', '687189715', 'macajomacla4@gmail.com', CURRENT_DATE, true, false, false, false, 'ES2421008052602100299646', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('0ef074e9-287f-43a1-a392-bf6e53f66dcd', 145, 'Antonio Jose Rodriguez Gálvez', '1983-01-31', '47506593R', 'C/Las Palmeras 7', 'Gilena', 'Sevilla', '41565', '677341145', 'Antoniojoserodriguezgalvez@gmail.com', CURRENT_DATE, true, false, false, false, 'ES3400753023710700320489', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('2b7d0e67-576b-4ebf-a2f3-69bd105996b7', 146, 'Leire Rodríguez Carvajal', '2010-08-07', '2448109N', 'C/ Las Palmeras 7', 'Gilena', 'Sevila', '41565', '+34604318092', 'leirerodriguezcarvajal@gmail.com', CURRENT_DATE, true, false, false, false, NULL, 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('92fcacb1-cbea-46d2-ab1f-94acd0496aae', 147, 'Pablo Rodríguez Carvajal', '2014-01-10', '47500487j', 'C/Las Palmeras 7', 'GILENA', 'Sevilla', '41565', '+34722862058', 'nataliacarvalmont@gmail.com', CURRENT_DATE, true, false, false, false, 'ES3400753023710700320489', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('856f32c4-3d74-4578-8aad-5bf4ebe3f38a', 148, 'JESÚS REINA CASTILLO', '1988-03-31', '47500522W', 'CALLE PARÍS 14 1-A', 'MONTEQUINTO', 'SEVILLA', '41089', '628621221', 'jreinacastillo@gmail.com', CURRENT_DATE, true, true, false, false, 'ES2721008052662100268840', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('bbdf93fe-c0e9-44bc-992b-9d05aa5fb5c5', 149, 'Martina Reina Reina', '2018-10-01', '47500522W', 'Calle Jesús Nazareno 11', 'Gilena', 'Sevilla', '41565', '628621221', 'jreinacastillo@gmail.com 47500522W', CURRENT_DATE, true, false, false, false, 'ES2721008052662100268840', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('9f5d42e2-82be-46f0-9628-2aa930acbe60', 150, 'Daniel Reina Gordillo', '1997-04-15', '48190040a', 'Calle cervantes 3 bajo', 'Gilena', 'Sevilla', '41565', '678398766', 'gilenadrg@gmail.com', CURRENT_DATE, true, true, false, false, 'ES5600492647772814055765', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('8953939f-2ff2-46c4-a939-22ecc76ee4fa', 151, 'Noelia Diaz Reina', '2025-03-10', '05333860E', 'Calle cervantes 3 bajo', 'Gilena', 'Sevilla', '41565', '678464774', 'Noedr98@gmail.com', CURRENT_DATE, true, true, false, false, 'ES5600492647772814055765', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('840c2c0e-402f-40be-9140-5c976d0c78fd', 152, 'Patricia Rodriguez Aguilar', '2020-01-31', '17510798R', 'C/La Paloma 8', 'Gilena', 'Sevilla', '41565', '603637279', 'rafarodriguezgiraldez@hotmail.com', CURRENT_DATE, true, false, false, false, 'ES3100492647782714052561', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('b821fa10-3014-405e-97be-4d9558ac15a9', 153, 'Rafa Rodriguez Giraldez', '1993-07-21', '25349013T', 'C/ La Paloma 8', 'Gilena', 'Sevilla', '41565', '603637279', 'rafarodriguezgiraldez@hotmail.com', CURRENT_DATE, true, false, false, false, 'ES3100492647782714052561', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('e2ee637b-a164-4147-8345-0529eaa06eae', 154, 'Pedro Gonzalez Jurado', '2010-05-29', '17490972R', 'C/ Carmen 42 PBJ', 'Gilena', 'Sevilla', '41565', '671214652', NULL, CURRENT_DATE, true, false, false, false, 'ES5221008052602100303410', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('25c3d091-607a-4e36-8cc2-3f8edd1ae918', 155, 'Saul Jurado Romero', '2010-03-14', '24481089G', 'C/Lepanto 27', 'Gilena', 'Sevilla', '41565', 'n/a', NULL, CURRENT_DATE, true, false, false, false, 'ES4721008052682100030441', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('b4f61075-c6bc-4f81-914d-484ba90ede13', 156, 'Alberto Pozo García', '1988-11-02', '47510338C', 'Nueva 32, piso 2', 'Gilena', 'Sevilla', '41565', '653251123', 'albertopozogarcia@gmail.com', CURRENT_DATE, true, false, false, false, 'ES9615632626323267226517', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('e5e5d4c5-55a2-484c-b846-032bbb87ded7', 157, 'Alberto Reina Alvarez', '1995-10-17', '47559473G', 'Ramón y Cajal', 'Gilena', 'Sevilla', '41565', '691924959', 'albertoreinaalvared@gmail.com', CURRENT_DATE, true, true, false, false, 'ES6021008052622100114503', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('6a34a6ec-7314-406f-81ac-d328a925d511', 158, 'Alejandro Vallejo González', '2005-08-25', '24484113S', 'Calle Santa Cruz, 2', 'Gilena', 'Sevilla', '41565', '678907372', 'alejandrovallejogilena@gmail.com', CURRENT_DATE, true, true, false, false, 'ES4621008052630100089845', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('21d293f1-0e70-4542-823a-75d27f74e6aa', 159, 'Almudena Guzmán Gómez', '2009-02-26', '26611877C', 'Jorge Guillén, 40', 'Gilena', 'Sevilla', '41565', '693555309', 'a69331375@gmail.com', CURRENT_DATE, true, true, false, false, 'ES1900753023720700062787', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('998c20e8-64f3-40f3-8a0f-bb7bb2c79a90', 160, 'Antonio Luis Gómez Caballero', '1956-01-14', '75377229L', 'Jorge Guillén, 40', 'Gilena', 'Sevilla', '41565', '615546042', 'a69331375@gmail.com', CURRENT_DATE, true, true, false, false, 'ES1900753023720700062787', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('f204d1f1-d010-4508-b0c7-26b8bdc1f933', 161, 'Angela leiva cansino', '1986-05-03', '47501978D', 'Calle toledo 31', 'Gilena', 'Sevilla', '41565', '665943412', 'Angelaleiva86@gmail.com', CURRENT_DATE, true, true, false, false, 'ES6131870402674464517517', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('1e2a5748-cc81-4ca8-9d4f-b347ff989b2e', 162, 'Antonio Manuel Ángel Sánchez', '1982-09-09', '14623322Z', 'Calle Animas n°6', 'Gilena', 'Sevilla', '41565', '626351456', 'Pyter@hotmail.es', CURRENT_DATE, true, false, false, false, 'ES1321004478530100280203', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('c8c6dde9-019f-409d-8b83-f56b78c5c3c0', 163, 'ANTONIO MANUEL MONTAÑO RODRÍGUEZ', NULL, '28563199M', 'C. Paz 17.', 'GILENA', 'SEVILLA', '41565', '680349608', 'manuelmontanor.58@gmail.com', CURRENT_DATE, true, false, false, false, 'ES4700495422822116117362', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('af8c37a7-9687-4808-be95-33bae86d9a48', 164, 'Antonio Rodríguez Caballero', '1982-09-28', '28816386p', 'Calle Nueva 124', 'Gilena', 'Sevilla', '41565', '678738235', 'Caballero198224@gmail.com', CURRENT_DATE, true, true, false, false, 'ES4202370311119164827921', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('07f73996-fdba-424f-8210-ba3a1acfd994', 165, 'Antonio Ruiz Ponce', '1972-03-04', '77538442Z', 'Agua 4', 'Gilena', 'Sevilla', '41565', '606208506', 'mariajoaquinaruiz69@gmail.com', CURRENT_DATE, true, true, false, false, 'ES8200492647712514053894', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('aa5163e9-9845-41e8-b1e0-6f504ab86095', 166, 'Antonito Díaz Rodríguez', '1962-08-04', '75407186F', 'c/Nueva 86c', 'Gilena', 'Sevilla', '41565', '626994442', 'antoninogilena@gmail.com', CURRENT_DATE, true, false, false, false, 'ES4621008052672100203265', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('68fb5dfd-787a-4d67-85b4-b86f8468c4b2', 167, 'Asunción Luna Ruiz', '1992-03-13', '47555024V', 'C/Osuna n°17', 'Gilena', 'Sevilla', '41565', '663192637', 'susilunaruiz@gmail.com', CURRENT_DATE, true, true, false, false, 'ES7921008052652100065417', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('c8828df3-d126-4752-9d13-7352a3357006', 168, 'Azahara Díaz Rodriguez', '1992-03-22', '15457615M', 'Calle del Rosario', 'Gilena', 'Sevilla', '41565', '662170004', 'Azahara92dr@gmail.com', CURRENT_DATE, true, false, false, false, 'ES3200492647772814054548', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('68fd0f05-e37f-4eff-9628-9af3da3b24f4', 169, 'Azahara Gutiérrez Torres', '1985-08-03', '44595044E', 'Urb. Colina Soleada, calle Beethoven 33', 'Benajarafe (Vélez Málaga)', 'Málaga', '29790', '600286773', 'byza3_2@hotmail.con', CURRENT_DATE, true, false, false, false, 'ES2030580762242820006504', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('c086eeee-c1e8-4224-94aa-e934e37f43e0', 170, 'Cindia Jurado Aranda', '1993-09-15', '47546120Z', 'C/ Ramón y Cajal 3', 'Gilena', 'Sevilla', '41565', '697926111', 'cindiajuradoaranda@gmail.com', CURRENT_DATE, true, false, false, false, 'ES2721002904010239632828', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('419f69fd-d9d5-4532-8a99-fe495037e44f', 171, 'Daniel Montaño Vargas', '1994-08-20', '47511500D', 'Calle nueva 35 tercero', 'Gilena', 'Sevilla', '41565', '664329984', 'danimonguemar@gmail.com', CURRENT_DATE, true, true, false, false, 'ES1300492647712314056559', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('5ed8eec7-99cc-422f-a448-2000f0debc42', 172, 'David Pozo García', '1986-12-15', '47510339k', 'Calle Del Paso 15', 'Gilena', 'Sevilla', '41565', '685568237', 'davidpozogarcia@gmail.com', CURRENT_DATE, true, false, false, false, 'ES2600304169260000347271', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('f6d818d5-7ade-4bcc-9f86-e1bc890c0484', 173, 'Dolores Chía Díaz', '1972-10-28', '28744410E', 'Luis Cernuda 57', 'Gilena', 'Sevilla', '41565', '661965537', 'doloreschia72@gmail.com', CURRENT_DATE, true, false, false, false, 'ES1621008052692100220585', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('5d5596c6-7d3b-4703-8adb-57834a7e4324', 174, 'Miriam Gomez Chía', '1996-07-15', '48190032H', 'Luis Cernuda 57', 'Gilena', 'Sevilla', '41565', '634275299', 'silexplorer@gmail.com', CURRENT_DATE, true, false, false, false, 'ES1621008052692100220585', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('841435ac-ace2-4d1f-a499-8bc35ae0d1fd', 175, 'Ramón Gomez Rodriguez', '1970-05-24', '75431050C', 'Luis Cernuda 57', 'Gilena', 'Sevilla', '41565', '661965536', 'silexplorer@gmail.com', CURRENT_DATE, true, false, false, false, 'ES1621008052692100220585', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('e47746df-1150-460e-96b1-005ddbc6e158', 176, 'Elena García Álvarez', '2004-08-18', '23880417T', 'Calle nueva,44', 'Gilena', 'Sevilla', '41565', '693026443', 'nueva44.ega@gmail.com', CURRENT_DATE, true, true, false, false, 'ES1321008052670100090049', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('35429453-3c0c-4e5a-bebf-374c7863af1b', 177, 'Emiliano Hurtado Santos', '1954-02-11', '27907476N', 'Plaza de la Ermita n3', 'Gilena', 'Sevilla', '41565', '667727291', NULL, CURRENT_DATE, true, false, false, false, 'ES9031870205204728955610', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('64456b03-aa86-44c8-a682-e67722885820', 178, 'Ezequiel Muñoz Sojo', '2000-04-15', '51184974S', 'C/de los sifones', 'Gilena', 'Sevilla', '41565', '653909626', 'ezequielmunoz598@gmail.com', CURRENT_DATE, true, false, false, false, NULL, 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('9c842343-b347-4cd9-8051-3eb508a943de', 179, 'Fco Javier Rivero Fernández', '1988-02-09', '14638029R', 'Avd Príncipe 1A', 'Gilena', 'Sevilla', '41565', '655259203', 'javier.rivero.fdez@gmail.com', CURRENT_DATE, true, true, true, false, 'ES2821002904020224940171', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('a86d3277-6ec4-4b24-9994-7ef580fa8f92', 180, 'Felipe Romero Reina', NULL, '28340223Z', 'C/huelva', 'Gilena', 'Sevilla', '41565', '607834801', NULL, CURRENT_DATE, true, true, false, false, 'ES0721008052612100166334', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('fa35276e-ddbd-4b83-b87f-b32cd021a4fc', 181, 'Francisco Calzado Garcia', '1957-11-18', '75377236A', 'Calle osuna 41', 'Gilena', 'Sevilla', '41565', '622599063', NULL, CURRENT_DATE, true, false, false, false, 'ES8531870402671384358618', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('22e87c02-c713-432a-9df9-5cc91abea56d', 182, 'Francisco Herrera García', '1991-09-10', '24180470H', 'Osuna, 40', 'Gilena', 'Sevilla', '41565', '693065464', 'marinahrezz@gmail.com', CURRENT_DATE, true, false, false, false, 'ES5221008052680100071159', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('95f76c70-a3a5-46b4-9aea-ab2f88588009', 183, 'Francisco Herrera Ramírez', '1991-03-13', '43137010G', 'Osuna, 40', 'Gilena', 'Sevilla', '41565', '693065464', 'marinahrezz@gmail.com', CURRENT_DATE, true, true, false, false, 'ES5221008052680100071159', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('b7e6da7b-40d3-4685-9e01-29a356e585dd', 184, 'Marina Herrera Ramírez', '2001-10-05', '25609969K', 'Osuna, 40', 'Gilena', 'Sevilla', '41565', '693065464', 'marinahrezz@gmail.com', CURRENT_DATE, true, true, false, false, 'ES5221008052680100071159', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('e0cf8113-a10b-4725-806e-93a996a02bbe', 185, 'Francisco Javier Moreno Rodríguez', '1995-08-31', '48190033L', 'Calle Paz 8', 'Gilena', 'Sevilla', '41565', '+34681623268', 'franciscojmoreno22@gmail.com', CURRENT_DATE, true, true, false, false, 'ES7731870402683159148216', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('acc2b7cc-0b20-4342-9294-6e4a9d2d45bc', 186, 'Francisco Jose Romero Carmona', '1985-04-11', '14635241L', 'Nueva, 136', 'Gilena', 'Sevilla', '41565', '615112974', 'franciscoromerocarmona63@gmail.com', CURRENT_DATE, true, true, false, false, 'ES6821008052612300034221', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('81a03a8d-970d-4a67-8538-2699b7c1b918', 187, 'Saray Morillas Gomez', '1985-09-23', '47510097D', 'Nueva, 136', 'Gilena', 'Sevilla', '41565', '615112959', 'saraymorillasgomez@gmail.com', CURRENT_DATE, true, false, false, false, 'ES6821008052612300034221', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('5419dd32-53d4-4713-85a0-329978e64061', 188, 'Francisco Romero García', NULL, '27283890w', 'Eusebio Dieguez N,4', 'Gilena', 'Sevilla', '41565', '615112937', 'ro.maku@hotmail.com', CURRENT_DATE, true, false, false, false, 'ES7100492647722214058942', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('ea72211e-f730-4694-80b4-a8e6c2c13ef1', 189, 'Iluminada Carmona Sánchez', '1972-02-02', '52564323P', 'Doctor Fleming, 8', 'Gilena', 'Sevilla', '41555', '633284629', 'inmaculadapozogalvez@gmail.com', CURRENT_DATE, true, false, false, false, 'ES0931870402603207657010', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('c201c2b7-208c-4497-b974-b2f752007e75', 190, 'Isaac Lucena Jurado', '2002-08-01', '23883408R', 'Calle Ramón y Cajal N3', 'Gilena', 'Sevilla', '41565', '633244149', 'lucenajuradoisaac@gmail.com', CURRENT_DATE, true, true, false, false, 'ES2100492647732114064918', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('4db8c66a-6e1c-444f-8a12-31337d153c83', 191, 'Isabel Rocío Cejudo Ruiz', '2008-04-20', '23881854B', 'C/Álamos 16', 'Gilena', 'Sevilla', '41565', '625298120', 'Isabelcejudoruiz@gmail.com', CURRENT_DATE, true, false, false, false, 'ES4602370311109163553124', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('e9bca496-8657-4b66-bba8-febf2bbaf118', 192, 'Ismael cansino pozo', '1982-11-10', '47500528P', 'Luis cernuda 1E', 'Gilena', 'Sevilla', '41565', '600217455', 'icansinopozo@gmail.com', CURRENT_DATE, true, false, false, false, 'ES6902370311109165147390', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('402d3fb6-cc58-48ab-a02c-772ae05b163a', 193, 'Javier Lozano Rodríguez', '2009-07-23', '26906933S', 'Calle Nueva 45', 'Gilena', 'Sevilla', '41565', '613504549', 'javierlozanorodriguez6@gmail.com', CURRENT_DATE, true, false, false, false, 'ES7721008052650100037197', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('cfc92f7a-70b7-4259-aa7f-07a90dc4b563', 194, 'Javier Rodríguez Gordillo', '1954-05-22', '75362366Z', 'Del Paso, 18', 'Gilena', 'Sevilla', '41565', '+34629533222', 'javierrodriguez@andaluciacentro.com', CURRENT_DATE, true, true, true, false, 'ES4301827081450201553310', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('d503ccec-2d05-4caa-931c-117d0257bd79', 195, 'Jesús González Jurado', '1989-02-02', '14639409R', 'Garcia Lorca 11', 'Gilena', 'Sevilla', '41565', '600827993', 'gilena89@hotmail.com', CURRENT_DATE, true, false, false, false, 'ES0521008052662100122475', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('b66cee0c-acc6-45f4-9e8d-0194ef880ee8', 196, 'Jesus María Rodriguez Díaz', '1994-06-26', '15456176S', 'Calle Huelva 7, bajo C', 'Gilena', 'Sevilla', '41564', '647134483', 'jesusmariard94@gmail.com', CURRENT_DATE, true, false, false, false, 'ES9721008052690100023837', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('040c8c13-5d18-4e86-8d29-f20a45ab73bb', 197, 'Jesús Montaño Gómez', '2007-07-22', '23883388G', 'Calle la Cruz 57A', 'Gilena', 'Sevilla', '41565', '644978640', 'jesusmontanogomez@gmail.com', CURRENT_DATE, true, true, false, false, NULL, 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('6bf52105-33ba-48cc-828b-b2e34485dc31', 198, 'Jesus Paez Fernandez', '1988-04-06', '47513114J', 'Don Bosco 4', 'Estepa', 'Sevilla', '41560', '600000683', 'jesuspaezfernandez88@gmail.com', CURRENT_DATE, true, true, false, false, 'ES6531870402664666807211', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('316fc944-62e1-4515-9506-a569b94b2535', 199, 'Jesús Pozo García', '1977-06-03', '77538438x', 'Calle Nueva 32-1°', 'Gilena', 'Sevilla', '41565', '617092285', 'JESUSPOZOGARCIA@YAHOO.ES', CURRENT_DATE, true, false, false, false, 'ES5700304169210387124273', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('d084e2c1-3cee-4782-8fb5-4a4bae81d521', 200, 'Jesus Segura Sánchez', '1989-12-24', '47546106T', 'Calle Almeria n8', 'Gilena', 'Sevilla', '41565', '651165959', 'segurasanchez99@gmail.com', CURRENT_DATE, true, true, false, false, 'ES4121008052620200040045', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('e546b454-e79e-4cce-a25a-ea0cdb8a72b6', 201, 'José Antonio Baena Marín', '1986-07-21', '47507082F', 'Calle carretera de estepa 5', 'Gilena', 'Sevilla', '41565', '685393271', 'Elsianodelora@hotmail.com', CURRENT_DATE, true, false, false, false, 'ES3100730100510608653320', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('c1d7b2e8-09fd-4dd2-a02b-36d838da2e36', 202, 'José Antonio Páez Ponce', '1985-04-15', '14635240H', 'C/ carretera Estepa 9D', 'Gilena', 'Sevilla', '41565', '666399489', 'Josearapid@gmail.com', CURRENT_DATE, true, true, false, false, 'ES5321004478510100281326', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('8fad7037-3fd8-4975-9b8f-4207f3660055', 203, 'JOSE CARLOS MONTAÑO POZO', '1989-10-30', '28640793C', 'CALLE CARMEN,45', 'GILENA', 'SEVILLA', '41565', '687657238', 'josekamontano@gmail.com', CURRENT_DATE, true, false, false, false, 'ES4121002470510100598543', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('8f63c865-4358-4c17-8047-1159b4c843a3', 204, 'Jose gomez linares', '1985-01-09', '47508617R', 'Calle toledo 31', 'Gilena', 'Sevilla', '41565', '616832756', 'Pepeyangela2012@gmail.com', CURRENT_DATE, true, false, false, false, 'ES2631870402624412894216', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('81e21f9d-7473-4d39-a069-8e267337b246', 205, 'Jose Manuel de la Cruz Paez', '1983-07-16', '47510536B', 'Calle Alamos 23', 'Gilena', 'Sevilla', '41565', '603541915', 'josemanueldelacruz1438@gmail.com', CURRENT_DATE, true, false, false, false, 'ES2000492647742014062834', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('4568f9cc-7a40-4304-8411-6898bbdec0dc', 206, 'José María Gálvez Calzado', '2004-04-17', '23883389M', 'C/. Nueva 69 P1', 'Gilena', 'Sevilla', '41565', '623479749', 'galvezcalzadojosemaria2004@gmail.com', CURRENT_DATE, true, false, false, false, 'ES1031870402652803387519', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('b4c9da8e-7195-4b8a-8514-be4f651ca4f6', 207, 'José María Torres Cansino', '1993-03-17', '25352188R', 'Calle María Auxiliadora, 17', 'Gilena', 'Sevilla', '41565', '691417883', 'jmtorcan@gmail.com', CURRENT_DATE, true, true, false, false, 'ES4021008052642100084307', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('078fa6aa-ff13-452c-bceb-25665bdc3c55', 208, 'José Nogales Gómez', '1963-05-18', '75412260K', 'San Francisco de Borja, 2', 'Gilena', 'Sevilla', '41565', '685865402', 'pizzamovilgilena@gmail.com', CURRENT_DATE, true, false, false, false, 'ES1921008052612200065448', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('7f028c91-a415-431e-b40e-6898cd9076ec', 209, 'José Páez Ponce', '1966-12-02', '75431209H', 'Calle Toledo, 45', 'Gilena', 'Sevilla', '41565', '635529900', 'josepaezponce@gmail.com', CURRENT_DATE, true, false, false, false, 'ES8521008052652100198490', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('3c44aaa7-4200-425c-8674-ee97c6ac7aa5', 210, 'Josué Gutiérrez Amador', '1993-08-22', '17474292L', 'La Cruz 23', 'Gilena', 'Sevilla', '41565', '656957574', 'josuegutierrezamador@gmail.com', CURRENT_DATE, true, true, false, false, 'ES2900496287912190044740', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('730c7933-7924-45cb-b80a-92c7301493dc', 211, 'Juan Carlos Rodríguez Álvarez', '1997-09-08', '48192695-J', 'Calle Lepanto, n° 36', 'Gilena', 'Sevilla', '41565', '636853563', 'jjaaccrraa@gmail.com', CURRENT_DATE, true, true, false, false, 'ES3021008052610100105751', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('b19cbc40-1a27-4cde-8f2a-dae559375d11', 212, 'JUAN DAMIAN MORA HARO', '1986-02-14', '47509457J', 'C/ DOCTOR FLEMING 12', 'GILENA', 'SEVILLA', '41565', '619986565', 'juan_mh14@hotmail.com', CURRENT_DATE, true, false, false, false, 'ES3221008052692100144637', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('1fa1f3a4-dc08-45cf-9da1-76225cc6cf3c', 213, 'Juan de Dios Díaz Reina', '2008-10-31', '26302547T', 'Eusebio Dieguez 21', 'Gilena', 'Sevilla', '41565', '681659318', 'Juandediosd132@gmail.com', CURRENT_DATE, true, true, false, false, 'ES6421008052692100052590', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('dbc60fd4-3df8-4f03-9bb1-db9d18840402', 214, 'Juan de Dios Pérez Joya', '1991-03-20', '47537548K', 'C/ Vicente Aleixandre 1', 'Gilena', 'Sevilla', '41565', '687371721', 'jddperezj1@gmail.com', CURRENT_DATE, true, false, false, false, 'ES3300494306642190052503', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('66b5bdec-fc58-4d30-8687-88f25803d85f', 215, 'Juan González Pozo', '1997-04-10', '25612449v', 'C/ Pedro Garfias N-26', 'Gilena', 'Sevilla', '41565', '674846121', 'juangonzalezpozo97@gmail.com', CURRENT_DATE, true, true, false, false, 'ES3500753023720702188254', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('865066e9-dfa1-4ef4-aa08-a8269e632c45', 216, 'Juan Jose Pruna Moreno', '1988-03-17', '47335040m', 'c/Nueva 25, 2º', 'Gilensa', 'Sevilla', '41565', '675586037', 'juaprumor@gmail.com', CURRENT_DATE, true, true, false, false, 'ES3921008353122100448280', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('4fd31535-c04e-4661-89bb-87d0291d3608', 217, 'Juan Paez Guerrero', '1955-08-31', '075377334-d', 'Calle Sevilla numero 29', 'Gilena', 'Sevilla', '41565', '667385555', NULL, CURRENT_DATE, true, false, false, false, 'ES1500304169250390861273', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('2de5da17-14ab-4e26-864e-d04d69985011', 218, 'Juan Paez Martín', '1985-12-23', '47510883-j', 'Calle Sevilla numero 29', 'Gilena', 'Sevilla', '41565', '693031082', NULL, CURRENT_DATE, true, false, false, false, 'ES1500304169250390861273', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('2a857709-777a-408c-b196-e9797a94dd39', 219, 'Juan Vicente Joya aguilar', '1992-05-24', '47545366L', 'Plaza del sol 8', 'Gilena', 'Sevilla', '41565', '634729672', 'Juanvigilena@gmail.com', CURRENT_DATE, true, true, false, false, 'ES6921002904080210860753', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('79f152b8-0f65-493c-8247-4260d5e01823', 220, 'Laura Espada Marin', '1995-09-22', '79043353N', 'calle nueva 32, 2 planta', 'Gilena', 'Sevilla', '41565', '620576000', 'lauraespadamarin@gmail.com', CURRENT_DATE, true, false, false, false, 'ES6815632626303263734634', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('44972f19-00b2-4967-aaea-27f837075283', 221, 'Luis Manuel Páez Martín', '1992-01-17', '47545881M', 'Sevilla 29', 'Gilena', 'Sevilla', '41565', '670306828', '11paez2bac12@gmail.com', CURRENT_DATE, true, false, false, false, 'ES0400492647762914053177', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('dc0fd36a-ff3f-4977-bb8c-e0794906533f', 222, 'Manuel Diaz Borrego', '1963-12-04', '75429387J', 'C/ Jesús Nazareno 14', 'Gilena', 'Sevilla', '41565', '654350249', 'gruasdiazborrego@hotmail.es', CURRENT_DATE, true, false, false, false, 'ES5031870402672993477310', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('15caaf1e-b9c9-4f11-812c-e819bd39d9ec', 223, 'Manuel Gordillo Humanes', '1969-09-20', '75431329T', 'Jesús Nazareno n°6', 'Gilena', 'Sevilla', '41565', '625442966', 'humanesgordillomanuel@gmail.com', CURRENT_DATE, true, false, false, false, 'ES6400753023700700147241', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('1a0391c5-bd6a-4373-9baf-fc7f6aea630b', 224, 'Manuel Jesus Gordillo Maireles', '1996-08-15', '47559454P', 'Jesús Nazareno 6', 'Gilena', 'Sevilla', '41565', '727707634', 'manu.tkd96@gmail.com', CURRENT_DATE, true, true, false, false, 'ES7600753023710702187856', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('a45d922f-97e4-4b08-9098-0242a4143cb0', 225, 'Manuel Jesús Rodríguez Páez', NULL, '25619656W', 'C Málaga, 9', 'Gilena', 'Sevilla', '41565', '611474301', 'manueljesusrodriguezpaez@gmail.com', CURRENT_DATE, true, false, false, false, 'ES5621008052610100071624', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('e2780b97-a066-4861-99cc-01b0bc2fe177', 226, 'Manuel Jiménez Saavedra', '2000-08-19', '17485487J', 'Cervantes, 21', 'Estepa', 'Sevilla', '41560', '697863549', 'manujimesav@gmail.com', CURRENT_DATE, true, true, false, false, 'ES7531870401164835043615', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('df084ee8-cb5b-4812-9390-6289a7360b52', 227, 'Manuel Joya Reina', '1982-07-30', '74915347-T', 'Calle Málaga, 30', 'Gilena', 'Sevilla', '41565', '635384417', 'manueljoyareina@hotmail.es', CURRENT_DATE, true, true, false, false, 'ES5202370311109167566488', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('f7b7f713-dc03-421f-afe0-c9a985d89178', 228, 'Manuel Pozo GUERRA', '1976-11-26', '77538454 -A', 'Nueva 40', 'Gilena', 'Sevilla', '45565', '655311935', 'manuelhuracan76@gmail.com', CURRENT_DATE, true, false, false, false, 'ES8600753023700700351321', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('dab22055-1f9a-4c0b-b81e-fe287da7d120', 229, 'MANUEL RUIZ MORENO', '1963-09-20', '34010117V', 'CALLE ALAMOS,25', 'GILENA', 'SEVILLA', '41565', '616251992', 'PBETICALUISBELLVER@GMAIL.COM', CURRENT_DATE, true, false, false, false, 'ES9231870402601093145629', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('41cb3881-da91-4738-910e-bf508ea1424d', 230, 'Marcos corona blanco', '2008-05-05', '23883402H', 'Naranjos 19', 'Gilena', 'Sevilla', '41465', '625293737', 'marcoscoronablanco@gmail.com', CURRENT_DATE, true, true, false, false, 'ES9321008052610100052043', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('2941b385-b3a1-4253-8263-26c4d3100496', 231, 'Marcos Segura Sánchez', '1997-01-14', '47546107R', 'C/Almería 8', 'Gilena', 'Sevilla', '41565', '646289525', NULL, CURRENT_DATE, true, false, false, false, 'ES3721008052630200055685', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('d462c5c0-9e94-4b77-92f5-fb2baba25372', 232, 'María Francisca Ruiz Ponce', '1969-12-09', '75431147W', 'Calle agua 4', 'Gilena', 'Sevilla', '41565', '606208506', 'mariajoaquinaruiz69@gmail.com', CURRENT_DATE, true, false, false, false, 'ES4300304169260387024273', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('ca3c77f3-4d5b-4977-9175-cc95e4980f20', 233, 'María Jesús Haro Guillen', '1991-06-03', '15402678S', 'Calle Lepanto n12 41565 Gilena', 'Gilena', 'Sevilla', '41565', '677281589', 'Maria_lusy@hotmail.com', CURRENT_DATE, true, true, false, false, 'ES8701827081450201570269', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('24fb518a-f9a6-4dc0-89dd-5769338c431d', 234, 'María Joaquina Ruiz Ponce', '1969-01-12', '75431169R', 'Calle Álamos 9 2 A', 'Gilena', 'Sevilla', '41565', '629352672', 'mariajoaquinaruiz69@gmail.com', CURRENT_DATE, true, false, false, false, NULL, 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('fcaf6d12-f95f-4f3c-bce6-3f3bea52abd6', 235, 'Mariano Rodriguez Caballero', '1982-09-28', '28816387D', 'Calle Nueva 126', 'Gilena', 'Sevilla', '41565', '670262668', 'marianocaballero25@gmail.com', CURRENT_DATE, true, true, false, false, 'ES5102370311109165545705', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('90fe1d70-8cb5-4d43-ad3b-359da0d3042c', 236, 'Marina González Haro', '2001-07-22', '17474832F', 'C/ San Benito, 4', 'Gilena', 'Sevilla', '41565', '627226517', 'marinaa170@gmail.com', CURRENT_DATE, true, false, false, false, 'ES2300753023770702187061', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('1eea333d-ffc1-40db-9d13-8caddccd261b', 237, 'Marina pozo caballero', NULL, '29517038B', 'Calle álamos 47', 'Gilena', 'Sevilla', '41565', '697547937', 'marinapozo9@gmail.com', CURRENT_DATE, true, false, false, false, NULL, 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('6750f4fb-717b-45c2-bb91-da45bb25b601', 238, 'Moisés Carmona sanchez', NULL, '77541519d', 'Doctor Fleming 8', 'Gilena', 'Sevilla', '41565', '717108375', 'moisescarmonasanchez@gmail.com', CURRENT_DATE, true, true, false, false, 'ES3200753023700702185884', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('5e993ad8-c9d4-4786-a02b-f7fe418a3e9f', 239, 'PEDRO DÍAZ CHÍA', '1963-10-19', '75412243G', 'EUSEBIO DIEGUEZ,19', 'GILENA', 'SEVILLA', '41565', '654569907', 'pbeticaluisbellver@gmail.com', CURRENT_DATE, true, false, false, false, 'ES2721008052652100294691', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('eb5d37ba-a9e1-4341-8843-7d8e9b94067a', 240, 'Pedro Ignacio Gutiérrez Gordillo', '1971-07-03', '25329218P', 'C/nueva 78', 'Gilena', 'Sevilla', '41565', '651823296', 'cruzcampobetis48@gmail.com', CURRENT_DATE, true, false, false, false, NULL, 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('c0a38ce4-040d-4d77-a323-84293c405c9a', 241, 'Rafael Montaño Garcia', '1999-11-12', '47545858M', 'C/ San Francisco de Borja 4', 'Gilena', 'Sevilla', '41565', '672727789', 'rafamontano11@gmail.com', CURRENT_DATE, true, false, false, false, 'ES4321008052600100072974', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('4e089a84-af71-4e90-80be-a83f953c5129', 242, 'Raúl Díaz Rodriguez', '1973-07-09', '52564227G', 'Calle carmen', 'Gilena', 'Sevilla', '41565', '+34673551242', 'haikodiaz@hotmail.com', CURRENT_DATE, true, false, false, false, 'ES6067170002938196611233', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('04bf45d7-2d61-4929-840b-a0ff31ab7dab', 243, 'Rosalía Pozo Gálvez', '2001-09-20', '23882677Y', 'Nueva, 76', 'Gilena', 'Sevilla', '41565', '693068231', 'rosaliapozogalvez@gmail.com', CURRENT_DATE, true, false, false, false, 'ES3531870402602809048719', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('fd8a4978-55b3-438d-9c47-66ec0e2540a4', 244, 'Santiago Rodriguez Rivera', '1970-01-04', '75431163H', 'San Juan de la Palma 50', 'Gilena', 'Sevilla', '41565', '653034393', NULL, CURRENT_DATE, true, false, false, false, 'ES1500492647742014052545', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('4abcc4a9-6707-4fb8-b62c-cc8bb50d3f98', 245, 'Sergio Morcillo Bafaluy', '1976-11-25', '53077347V', 'SANT CARLES 33-35 ESC DERECHA PISO 2 PUERTA 1', 'Santa Coloma de Gramenet', 'Barcelona', '08921', '620724671', 'smorcillo@gmail.com', CURRENT_DATE, true, false, false, false, 'ES8901821858220202768463', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('3adca2ea-8c65-4c81-a1b3-e4e176f6181d', 246, 'Silvestre nogales gomez', NULL, '25308358 D', 'C/ toledo n/ 21.', 'Gilena', 'Sevilla', '41565', '651385276', 'Silves3-69@hotmail.com', CURRENT_DATE, true, false, false, false, NULL, 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('2cdc23bc-0a5c-42b5-a8cd-e56e04913f9e', 247, 'Susana Bazán Arjona', '1978-08-23', '47500517C', 'Calle Lepanto', 'Gilena', 'Sevilla', '41565', '664595130', NULL, CURRENT_DATE, true, false, false, false, 'ES4700491690182410062674', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('46b75c1e-11ae-496a-bb44-86d6824d775c', 248, 'Tomas Rodríguez Díaz', '2002-08-23', '17489502A', 'Calle hermanos Machado 11', 'Gilena', 'Sevilla', '41565', '693059394', 'Tomasrodriguezdiaz2002@gmail.com', CURRENT_DATE, true, true, false, false, NULL, 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('08a7701c-3487-4e14-98e9-036edc520536', 249, 'Tomás Rodriguez Joya', '1961-07-01', '75412269F', 'Hermanos Machado 11', 'Gilena', 'Sevilla', '41565', '655770701', 'tomasrodriguezdiaz2002@gmail.com', CURRENT_DATE, true, false, false, false, 'ES2831870402621384398010', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('24bb8ec5-bbe6-4f3f-ac4f-8d7984bf598d', 250, 'Victoriano Montaño Diaz', NULL, '47539123-D', 'Calle Cádiz 5', 'Gilena', 'Sevilla', '41565', '659744600', 'victorianomd@gmail.com', CURRENT_DATE, true, true, false, false, 'ES4300492647732114061030', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('5a7f105d-1938-4b8a-a625-35e1b1847852', 251, 'Vitoriano Montaño Gomez', '1967-11-27', '75429363N', 'C/ Lo Coroneles 28, 1B', 'Seseña', 'Toledo', '45223', '633944905', 'vitobasida@gmail.com', CURRENT_DATE, true, false, false, false, 'ES4200491189542210090092', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('f7934913-e6a6-44d4-aa11-ae31805afc70', 252, 'Lidia Borrego Luna', '1999-09-01', '49385811G', 'Calle Lope de Vega 25', 'Gilena', 'Sevilla', '41565', '622955605', NULL, CURRENT_DATE, true, false, false, false, 'ES2002370311109155700084', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('67a75b2c-ca48-4ea0-8719-5cd67aadb646', 253, 'Manuel Borrego Gallardo', '1966-05-25', '75429373E', 'Calle Lope de Vega 25', 'Gilena', 'Sevilla', '41565', '605912763', NULL, CURRENT_DATE, true, false, false, false, 'ES2002370311109155700084', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('cf2062dc-229d-4f5c-adef-18888e71e1e9', 254, 'Francisco Pozo Moreno', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES3521002735141300294854', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('7d527cf8-fde1-420d-ba57-13e48becc827', 255, 'Benito Rodriguez Marting', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES0721008052652100184665', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('b7a26781-383b-4000-8e12-b5c9fead81dd', 256, 'RIVERO MORENO FRANCISCO JAVIER', '1960-04-20', '28532550S', 'Avd Principe 1', 'Gilena', 'Sevilla', '41565', '653887359', 'fcojavierriveromoreno@gmail.com', CURRENT_DATE, true, false, false, false, 'ES0721002904010226896991', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('af67f3f6-f8a7-47f1-a2c5-879bf6030c09', 257, 'Angela González jurado', '2005-02-03', '23883407T', 'Calle carmen 42', 'Gilena', 'Sevilla', '41565', '671388138', 'Angelagonzalezjurado06@gmail.com', CURRENT_DATE, true, false, false, false, 'ES2200496287922090044413', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('5fe3d3e6-e629-412c-81de-bb4cb46a5127', 258, 'Lorenzo Díaz García', '1993-11-18', '25352140E', 'C/Luis Cernuda, 37', 'Gilena', 'Sevilla', '41565', '670315491', 'lorenzogilena@gmail.com', CURRENT_DATE, true, true, false, false, 'ES4221005716510201169949', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('d68ed4fb-3a3b-4c02-9e74-73708510d185', 259, 'Francisco García González', '1962-09-09', '75412422-E', 'C/ Huelva,3 ,2B', 'Gilena', 'Sevilla', '41565', '697643595', 'frantagardino@gmail.com', CURRENT_DATE, true, true, false, false, 'ES6100492647712114052171', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('dc848d19-9f2d-4296-b731-e0f7ac071d2f', 260, 'gonzalo.tc2014@gmail.com', NULL, 'Gonzalo Tejada Carmona', '2014-01-23 00:00:00', 'C/ Eusebio Dieguez, 47', 'Gilena', 'Sevilla', '41565', NULL, CURRENT_DATE, true, false, false, false, 'ES9721008052652100119219', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('dc73ec9e-ba6b-4af2-86c2-b6f4984d7277', 261, 'inmagilena96@gmail.com', NULL, 'Inmaculada Borrego Pérez', '1996-12-08 00:00:00', 'Calle La Paloma,5', 'Gilena', 'Sevilla', '41565', NULL, CURRENT_DATE, true, false, true, false, 'ES5021008052680100070613', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('80645e4c-b209-40c3-bb7b-1eb02553a4ff', 262, 'natalygilena@gmail.com', NULL, 'Natalia Montaño Diaz', '1997-05-08 00:00:00', 'Calle Cádiz, 5', 'Gilena', 'Sevilla', '41565', NULL, CURRENT_DATE, true, false, false, false, 'ES6921008052610100116204', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('28d3598a-62ed-4ff9-a1f4-a22617cd9b64', 263, 'JESUS DAVID ANGULO RUIZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES4131870402611384346415', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('2b4fe9a9-01f5-4f63-97ae-d3b54036d580', 264, 'JOSE ANTONIO MARTIN GONZALEZ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES6931870402694546160914', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('e9912b7c-29ab-415e-988d-f885f164684b', 265, 'JOSE MANUEL POZO MAIRELES', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES6131870402601384326219', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('029750be-d74a-4e75-8db4-b734a3dc6725', 266, 'JUAN GARCIA MORENO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES7231870402661384339113', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('46e093b0-0656-4d6e-9608-4e5923bfd21a', 267, 'Marco Antonio Díaz Rodríguez', '1967-01-13', '34023912N', 'Virgen de la Sierra, 65', 'Gilena', 'Sevilla', '41565', '629394218', 'marcoantonio@msa-s@l.com', CURRENT_DATE, true, false, false, false, 'ES8600753023710700161055', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('9c63512a-ebfd-497c-bcc3-f1cd7d45f2e7', 268, 'Marian Dudiuc', '1980-05-31', 'X7532771H', 'c/lepanto 4', 'Gilena', 'Sevilla', '41565', '744669724', 'dudicmarian2@gmail.com', CURRENT_DATE, true, false, false, false, 'ES3700492647782714052065', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('f5ae3a24-8619-44da-abb8-d50c59b9336a', 269, 'POZO GALVEZ ANGELA', '1987-09-21', '47501972A', 'c/córdoba 9', 'Gilena', 'Sevilla', '41565', '680473751', 'angelapozogalvez@gmail.com', CURRENT_DATE, true, false, false, false, NULL, 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('d8cd9138-3c69-4259-bd6b-f86d969e03d9', 270, 'POZO GALVEZ RAFAEL', '1983-09-10', '47501971W', 'c/nueva 122', 'Gilena', 'Sevilla', '41565', '616304886', 'telegilena@telegilena.com', CURRENT_DATE, true, true, false, false, NULL, 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('7ef257e6-aa40-44fd-8f9b-7fa0ad9c2779', 271, 'POZO GORDILLO RAFAEL', '1958-01-03', '75377220X', 'C/nueva 76', 'Gilena', 'Sevilla', '41565', '693068232', 'inmaculadapozogalvez@gmail.com', CURRENT_DATE, true, false, false, false, NULL, 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('1b8c9d04-2c77-4bd9-8f9f-5ab7176f1eee', 272, 'PRADAS RODRIGUEZ, JUAN ANTONIO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES5621060407850002472044', 1);

INSERT INTO socios (uid, numero_socio, nombre, fecha_nacimiento, dni, direccion, poblacion, provincia, codigo_postal, telefono, email, fecha_alta, activo, abonado_betis, accionista_betis, exento_pago, numero_cuenta, pena_id)
VALUES ('9d711115-9f15-4fa2-a026-92c2c2fb4f6a', 273, 'RODRIGUEZ RODRIGUEZ, MANUEL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_DATE, true, false, false, false, 'ES3421060407850002026046', 1);

COMMIT;