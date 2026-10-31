#!/usr/bin/env bash
# Verificacion reproducible de un modulo de la EE (C02 / M11).
#
# Uso: ./scripts/verify-module.sh M11
#
# Reconstruye la base desde cero, ejecuta las pruebas unitarias y de integracion,
# y demuestra que los datos sobreviven a un reinicio del motor. Conserva el primer
# error significativo: si un paso falla, el script se detiene ahi y deja el log
# completo en .verify/verify-M11.log para que el fallo quede registrado tal cual.
# El log vive fuera de target/ porque `mvn clean` borra ese directorio.
#
# No contiene credenciales: todo sale de .env (ver .env.example).

set -euo pipefail

MODULO="${1:-}"
RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$RAIZ"

LOG=".verify/verify-${MODULO:-sin-modulo}.log"

paso()  { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
ok()    { printf '    [VERIFICADO] %s\n' "$*"; }
fallo() { printf '\n\033[31m[FALLO] %s\033[0m\n' "$*" >&2; exit 1; }

if [[ "$MODULO" != "M11" ]]; then
    fallo "Modulo no soportado: '${MODULO}'. Uso: ./scripts/verify-module.sh M11"
fi

# ---------------------------------------------------------------- 0. Requisitos
paso "0/5 Requisitos y configuracion"
for binario in java mvn docker; do
    command -v "$binario" >/dev/null 2>&1 || fallo "Falta '$binario' en el PATH."
done
docker compose version >/dev/null 2>&1 || fallo "Falta el plugin 'docker compose'."
[[ -f .env ]] || fallo "No existe .env. Copialo con: cp .env.example .env y cambia el password."

set -a
# shellcheck disable=SC1091
source .env
set +a

for variable in POSTGRES_DB POSTGRES_USER DB_URL DB_USER DB_PASSWORD; do
    [[ -n "${!variable:-}" ]] || fallo "La variable $variable no esta definida en .env."
done
mkdir -p .verify
: >"$LOG"
ok "java, mvn, docker y .env presentes (modulo $MODULO)"

psql_en_contenedor() {
    docker compose exec -T postgres \
        psql -v ON_ERROR_STOP=1 -At -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "$1"
}

# --------------------------------------------- 1. Reconstruir esquema desde cero
paso "1/5 Reconstruir el esquema desde cero"
docker compose down -v >>"$LOG" 2>&1 || true
if ! docker compose up -d >>"$LOG" 2>&1; then
    if grep -q 'address already in use' "$LOG"; then
        fallo "El puerto ${POSTGRES_PORT:-5432} ya esta ocupado (otro PostgreSQL local). Cambia POSTGRES_PORT y DB_URL en .env."
    fi
    fallo "No se pudo levantar PostgreSQL. Revisa $LOG"
fi

printf '    esperando estado healthy'
for _ in $(seq 1 30); do
    estado="$(docker inspect -f '{{.State.Health.Status}}' tcsw-ventas-postgres 2>/dev/null || echo arrancando)"
    [[ "$estado" == "healthy" ]] && break
    printf '.'
    sleep 2
done
printf '\n'
[[ "${estado:-}" == "healthy" ]] || fallo "PostgreSQL no llego a 'healthy'. Revisa $LOG"
ok "contenedor tcsw-ventas-postgres healthy sobre un volumen nuevo"

tablas="$(psql_en_contenedor "SELECT count(*) FROM information_schema.tables
          WHERE table_schema = 'public' AND table_name IN ('producto','venta','partida');")"
[[ "$tablas" == "3" ]] || fallo "Se esperaban 3 tablas del esquema y hay $tablas. Revisa db/schema.sql"
ok "db/schema.sql creo producto, venta y partida"

semilla="$(psql_en_contenedor 'SELECT count(*) FROM producto;')"
[[ "$semilla" -gt 0 ]] || fallo "La semilla db/02-data.sql no se cargo (producto vacio). Revisa los volumes de docker-compose.yml"
ok "db/02-data.sql cargo $semilla productos de prueba"

# ------------------------------------------------------------------ 2. Pruebas
paso "2/5 Pruebas unitarias y de integracion (mvn clean verify)"
if mvn -B clean verify >>"$LOG" 2>&1; then
    resumen="$(grep -E 'Tests run:.*(Failures|Errors)' "$LOG" | tail -2 | sed 's/^/    /')"
    ok "mvn clean verify termino sin errores"
    [[ -n "$resumen" ]] && printf '%s\n' "$resumen"
else
    printf '\n    Primer error significativo:\n'
    grep -m 5 -E '\[ERROR\]' "$LOG" | sed 's/^/    /' || true
    fallo "mvn clean verify fallo. Log completo en $LOG"
fi

# ------------------------------------- 3. Reaplicar la semilla que borran los IT
paso "3/5 Reaplicar la semilla (las pruebas *IT hacen TRUNCATE de producto)"
docker compose exec -T postgres \
    psql -v ON_ERROR_STOP=1 -q -U "$POSTGRES_USER" -d "$POSTGRES_DB" < db/02-data.sql \
    || fallo "No se pudo reaplicar db/02-data.sql"
semilla="$(psql_en_contenedor 'SELECT count(*) FROM producto;')"
[[ "$semilla" -gt 0 ]] || fallo "El catalogo sigue vacio tras reaplicar la semilla."
ok "catalogo restaurado con $semilla productos (semilla idempotente)"

# ------------------------------------------ 4. Flujo guardar-consultar sobre JDBC
paso "4/5 Flujo guardar-consultar y consistencia transaccional"
antes_venta="$(psql_en_contenedor 'SELECT count(*) FROM venta;')"
antes_partida="$(psql_en_contenedor 'SELECT count(*) FROM partida;')"
printf '    antes: venta=%s partida=%s producto=%s\n' "$antes_venta" "$antes_partida" "$semilla"

# Guardar: una venta con dos partidas, en una sola transaccion.
psql_en_contenedor "BEGIN;
    INSERT INTO venta (descuento, total) VALUES (0, 93.80);
    INSERT INTO partida (venta_id, producto_codigo, cantidad, precio_unitario)
        VALUES ((SELECT max(id) FROM venta), 'P001', 2, 25.50);
    INSERT INTO partida (venta_id, producto_codigo, cantidad, precio_unitario)
        VALUES ((SELECT max(id) FROM venta), 'P002', 1, 42.80);
    COMMIT;" >/dev/null
ok "venta con 2 partidas confirmada (commit)"

# Error controlado: la transaccion completa debe revertirse y no dejar rastro.
venta_antes_rollback="$(psql_en_contenedor 'SELECT count(*) FROM venta;')"
if psql_en_contenedor "BEGIN;
        INSERT INTO venta (descuento, total) VALUES (0, 10.00);
        INSERT INTO partida (venta_id, producto_codigo, cantidad, precio_unitario)
            VALUES ((SELECT max(id) FROM venta), 'NO-EXISTE', 1, 10.00);
        COMMIT;" >/dev/null 2>&1; then
    fallo "La FK de partida no rechazo un producto inexistente: el esquema no protege la invariante."
fi
venta_tras_rollback="$(psql_en_contenedor 'SELECT count(*) FROM venta;')"
[[ "$venta_antes_rollback" == "$venta_tras_rollback" ]] \
    || fallo "Error controlado dejo estado inconsistente: venta paso de $venta_antes_rollback a $venta_tras_rollback"
ok "error controlado: rollback total, sin venta huerfana ($venta_tras_rollback ventas)"

# ---------------------------------------------- 5. Persistencia tras el reinicio
paso "5/5 Persistencia tras reiniciar el motor"
esperado_venta="$(psql_en_contenedor 'SELECT count(*) FROM venta;')"
esperado_partida="$(psql_en_contenedor 'SELECT count(*) FROM partida;')"
esperado_producto="$(psql_en_contenedor 'SELECT count(*) FROM producto;')"

docker compose restart postgres >>"$LOG" 2>&1 || fallo "No se pudo reiniciar el contenedor."
printf '    esperando estado healthy'
for _ in $(seq 1 30); do
    estado="$(docker inspect -f '{{.State.Health.Status}}' tcsw-ventas-postgres 2>/dev/null || echo arrancando)"
    [[ "$estado" == "healthy" ]] && break
    printf '.'
    sleep 2
done
printf '\n'
[[ "${estado:-}" == "healthy" ]] || fallo "PostgreSQL no volvio a 'healthy' tras el reinicio."

despues_venta="$(psql_en_contenedor 'SELECT count(*) FROM venta;')"
despues_partida="$(psql_en_contenedor 'SELECT count(*) FROM partida;')"
despues_producto="$(psql_en_contenedor 'SELECT count(*) FROM producto;')"
printf '    despues: venta=%s partida=%s producto=%s\n' "$despues_venta" "$despues_partida" "$despues_producto"

[[ "$esperado_venta" == "$despues_venta" && "$esperado_partida" == "$despues_partida" \
   && "$esperado_producto" == "$despues_producto" ]] \
    || fallo "Los datos no sobrevivieron al reinicio (antes: $esperado_venta/$esperado_partida/$esperado_producto)."
[[ "$despues_venta" -gt 0 ]] || fallo "No hay ventas guardadas: la demostracion de persistencia no prueba nada."
ok "los datos sobrevivieron al reinicio sin cambios"

paso "Resultado"
printf '    VERIFICADO: esquema desde cero, semilla, pruebas, transacciones y persistencia.\n'
printf '    Log completo: %s\n' "$LOG"
printf '    La base queda levantada; para limpiar: docker compose down -v\n'
