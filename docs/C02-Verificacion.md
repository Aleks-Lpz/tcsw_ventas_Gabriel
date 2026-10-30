# C02 — Verificación reproducible del módulo M11

> Alcance de este documento: la **reproducibilidad** del corte (issue #35). No cubre
> el adaptador Hibernate, que es trabajo pendiente de los issues #32 y #34.

## 1. Qué estaba roto

| # | Hallazgo | Efecto |
|---|---|---|
| 1 | `docker-compose.yml` solo montaba `db/schema.sql`; `db/02-data.sql` nunca llegaba a `docker-entrypoint-initdb.d` | Al reconstruir la base desde cero el catálogo quedaba **vacío** y el cliente Swing no podía mostrar productos. El paso "ejecuta flujo guardar-consultar" era imposible |
| 2 | Las pruebas de integración hacen `TRUNCATE partida, venta, producto RESTART IDENTITY CASCADE` en `@BeforeEach` | Después de `mvn verify` el catálogo quedaba vacío otra vez, incluso con la semilla montada |
| 3 | `db/02-data.sql` usaba `INSERT` sin `ON CONFLICT` | No se podía reaplicar la semilla: fallaba con llave duplicada |
| 4 | No existía el script de verificación del módulo | La evidencia no era reconstruible con un comando declarado |
| 5 | `Producto.class` y `Partida.javamicro` versionados dentro de `src/main/java` | Deuda técnica y ruido para el análisis estático |

## 2. Qué se hizo

1. La semilla se monta como `02-data.sql`, después del esquema.
2. `db/02-data.sql` es idempotente (`ON CONFLICT (codigo) DO NOTHING`), así que se puede reaplicar.
3. `scripts/verify-module.sh M11` ejecuta el recorrido completo del procedimiento de C02 y
   **reaplica la semilla después de las pruebas**, que es el orden correcto dado el `TRUNCATE`.
4. Se eliminaron los dos archivos basura y `.gitignore` bloquea `*.class`.

## 3. Ejecución verificada

Entorno: Debian/Linux, OpenJDK 25.0.5 (compilación con `--release 11`), Maven 3.8.7,
Docker 29.9.0, PostgreSQL 16 en contenedor. Puerto local 55432 porque 5432 estaba ocupado
por un PostgreSQL nativo.

```
$ cp .env.example .env     # + POSTGRES_PORT/DB_URL al 55432
$ ./scripts/verify-module.sh M11
```

```
==> 0/5 Requisitos y configuracion
    [VERIFICADO] java, mvn, docker y .env presentes (modulo M11)

==> 1/5 Reconstruir el esquema desde cero
    [VERIFICADO] contenedor tcsw-ventas-postgres healthy sobre un volumen nuevo
    [VERIFICADO] db/schema.sql creo producto, venta y partida
    [VERIFICADO] db/02-data.sql cargo 5 productos de prueba

==> 2/5 Pruebas unitarias y de integracion (mvn clean verify)
    [VERIFICADO] mvn clean verify termino sin errores
    [INFO] Tests run: 66, Failures: 0, Errors: 0, Skipped: 0      <- unitarias
    [INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0      <- integracion (*IT)

==> 3/5 Reaplicar la semilla (las pruebas *IT hacen TRUNCATE de producto)
    [VERIFICADO] catalogo restaurado con 6 productos (semilla idempotente)

==> 4/5 Flujo guardar-consultar y consistencia transaccional
    antes: venta=0 partida=0 producto=6
    [VERIFICADO] venta con 2 partidas confirmada (commit)
    [VERIFICADO] error controlado: rollback total, sin venta huerfana (1 ventas)

==> 5/5 Persistencia tras reiniciar el motor
    despues: venta=1 partida=2 producto=6
    [VERIFICADO] los datos sobrevivieron al reinicio sin cambios
```

**Antes / después del reinicio** (paso 5, el corazón del entregable):

| Tabla | Antes de `docker compose restart` | Después |
|---|---|---|
| `producto` | 6 | 6 |
| `venta` | 1 | 1 |
| `partida` | 2 | 2 |

Los 6 productos son los 5 de la semilla más uno que deja `JdbcProductoRepositoryIT`
tras su `TRUNCATE`; el script no asume un número fijo, solo que antes y después coincidan
y que haya al menos una venta guardada.

## 4. Error controlado (consistencia transaccional)

El paso 4 no solo guarda: intenta una venta cuya partida referencia `producto_codigo = 'NO-EXISTE'`.
La llave foránea la rechaza, la transacción completa se revierte y el conteo de `venta` no cambia.
Si la base *aceptara* esa inserción, el script falla a propósito: eso significaría que el esquema
ya no protege la invariante del dominio.

Es el mismo contrato que `JdbcVentaRepository` cumple en Java (`autoCommit = false`, `rollback`
en el `catch`), comprobado aquí a nivel de base para que la evidencia no dependa solo de las pruebas.

## 5. Estado declarado

| Elemento | Estado |
|---|---|
| Semilla cargada al reconstruir desde cero | VERIFICADO |
| `./scripts/verify-module.sh M11` de punta a punta | VERIFICADO |
| 77 pruebas (66 unitarias + 11 integración) | VERIFICADO |
| Rollback ante error controlado | VERIFICADO |
| Persistencia tras reinicio | VERIFICADO |
| Adaptador Hibernate y sus entities | PENDIENTE (issues #32 y #34, otro integrante) |
| Índice completo de evidencias de equipo y tag del corte | PENDIENTE (equipo) |
| Reporte Sonar del corte | PENDIENTE |

## 6. Relación con R03

| Criterio de R03 | Evidencia de este issue |
|---|---|
| Swing y MVC | No lo toca; sí restaura el catálogo sin el cual la vista aparecía vacía |
| Hibernate y PostgreSQL | Esquema y semilla reconstruibles desde cero (base para el adaptador Hibernate) |
| Consistencia transaccional | Paso 4: commit completo y rollback ante error controlado |
| Pruebas | Paso 2: 77 casos ejecutados, con log conservado en `.verify/verify-M11.log` |
| Colaboración | Issue #35, rama `fix/issue-35-reproducibilidad-c02`, este documento y la bitácora individual |
