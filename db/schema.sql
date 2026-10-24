-- P08. Persistencia - Esquema relacional para tcsw-ventas (PostgreSQL 16)
-- Se ejecuta automaticamente al crear el contenedor (docker-entrypoint-initdb.d).
-- Es idempotente: puede volver a ejecutarse sin error.
-- Las restricciones replican las invariantes del dominio para que la base
-- tambien rechace estados invalidos aunque alguien escriba sin pasar por Java.

CREATE TABLE IF NOT EXISTS producto (
    codigo      VARCHAR(50)    PRIMARY KEY,
    nombre      VARCHAR(150)   NOT NULL CHECK (length(trim(nombre)) > 0),
    precio      NUMERIC(12, 2) NOT NULL CHECK (precio > 0),
    existencia  INTEGER        NOT NULL CHECK (existencia >= 0)
);

CREATE TABLE IF NOT EXISTS venta (
    id          BIGSERIAL      PRIMARY KEY,
    fecha       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    descuento   NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (descuento >= 0),
    total       NUMERIC(12, 2) NOT NULL CHECK (total >= 0)
);

CREATE TABLE IF NOT EXISTS partida (
    id               BIGSERIAL      PRIMARY KEY,
    venta_id         BIGINT         NOT NULL REFERENCES venta (id) ON DELETE CASCADE,
    producto_codigo  VARCHAR(50)    NOT NULL REFERENCES producto (codigo),
    cantidad         INTEGER        NOT NULL CHECK (cantidad > 0),
    precio_unitario  NUMERIC(12, 2) NOT NULL CHECK (precio_unitario > 0)
);

CREATE INDEX IF NOT EXISTS idx_partida_venta ON partida (venta_id);
