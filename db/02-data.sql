-- Datos de prueba que se cargan para nutrir la tabla producto.
-- Se monta en docker-entrypoint-initdb.d como 02 para correr despues del esquema.
-- Es idempotente: las pruebas de integracion hacen TRUNCATE de producto, asi que
-- este archivo se vuelve a aplicar despues de `mvn verify` sin chocar con la PK.
-- Datos ficticios: ningun dato real de personas o proveedores.

INSERT INTO producto (codigo, nombre, precio, existencia)
VALUES ('P001', 'Jabón Roma', 25.50, 40)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO producto (codigo, nombre, precio, existencia)
VALUES ('P002', 'Leche Lala +Proteína', 42.80, 23)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO producto (codigo, nombre, precio, existencia)
VALUES ('P003', 'Knorr Suiza', 12.50, 300)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO producto (codigo, nombre, precio, existencia)
VALUES ('P004', 'Triki Trakes', 33.20, 100)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO producto (codigo, nombre, precio, existencia)
VALUES ('P005', 'Kola Loka', 50.20, 300)
ON CONFLICT (codigo) DO NOTHING;
