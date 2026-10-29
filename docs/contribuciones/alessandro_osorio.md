# Contribución individual P08 — Alessandro Osorio

> Bitácora de P08. Mis contribuciones de P04, P05 y P06 están en `docs/alessandro_osorio.md`.

## 1. Datos

- **Integrante:** Alessandro Osorio
- **Usuario de GitHub:** Alessito54
- **Actividad:** P08. Persistencia (M10)
- **Issue:** sin issue previo; el alcance se acordó en el PR
- **Rama:** `feat/p08-jdbc-venta-transaccional`
- **Pull request:** [#29](https://github.com/Aleks-Lpz/tcsw_ventas_Gabriel/pull/29) (merge `967da6f`)

## 2. Mi parte

En el equipo me encargué de **persistir las ventas**. Elegí esta parte porque en P05 yo implementé el puerto `VentaRepository` y su adaptador en memoria; en P08 lo continué con un adaptador real sobre PostgreSQL 16 sin tocar el puerto ni el dominio.

Lo que hice:

1. Levanté PostgreSQL 16 con `docker-compose.yml` y escribí `db/schema.sql` con las tablas `producto`, `venta` y `partida`, con llaves foráneas y `CHECK` que replican las reglas del dominio.
2. Implementé `JdbcVentaRepository`, que guarda la venta y todas sus partidas en una sola transacción usando `PreparedStatement`. Si algo falla, hago `rollback` y no queda nada a medias.
3. Separé la configuración en `ConfiguracionJdbc`, que lee las credenciales de variables de entorno, para no subir ninguna contraseña al repositorio.
4. Escribí 6 pruebas unitarias y 7 de integración, con casos positivos, negativos y de límite.
5. Documenté decisiones, reproducción y evidencia en `docs/P08-Persistencia-Venta.md`.

Dejé `JdbcProductoRepository` para otro integrante; mi esquema ya incluye la tabla `producto` que ese adaptador necesita.

## 3. Decisiones que tomé

- **Una transacción por venta.** Una venta sin sus partidas es un estado inválido. Por eso desactivo `autoCommit` y confirmo solo cuando todo se insertó.
- **Guardar `precio_unitario` en la partida.** Si el precio del producto cambia mañana, la venta de hoy debe conservar su precio original.
- **`NUMERIC(12,2)` y `BigDecimal`.** No quise guardar dinero con errores de `double`.
- **Validar antes de conectar.** Rechazo `null` y ventas sin partidas sin abrir conexión; la base de datos queda como segunda barrera.
- **No cambiar el puerto.** `VentaRepository` sigue igual, así que `RegistrarVentaService` puede usar el adaptador en memoria o el JDBC sin cambios.

## 4. Commits

| Hash | Mensaje |
|---|---|
Hashes tal como quedaron en `main` tras el merge del PR #29:

| Hash | Mensaje |
|---|---|
| `8a7aa6f` | `feat(db): agregar PostgreSQL 16 en Docker Compose y schema.sql` |
| `a55be97` | `feat(jdbc): implementar JdbcVentaRepository transaccional con PreparedStatement` |
| `4e2e6db` | `test(jdbc): agregar pruebas unitarias y de integracion del adaptador de ventas` |
| `095e792` | `docs: documentar persistencia de ventas P08 y bitacora individual` |

## 5. Pruebas

Ejecuté todas las pruebas del proyecto contra PostgreSQL 16.15: **73 de 73 en verde**, incluidas mis 13 pruebas nuevas, sin romper las existentes.

## 6. Estado

| Elemento | Estado |
|---|---|
| Esquema, adaptador y pruebas | VERIFICADO |
| `mvn clean verify` y `docker compose up` | **VERIFICADO en C02** con Docker y PostgreSQL 16 en contenedor: 77/77 pruebas. Ver [`docs/C02-Verificacion.md`](../C02-Verificacion.md). En P08 estaba NO_VERIFICADO por falta de Docker en mi entorno |
| Reporte Sonar | PENDIENTE al integrar en `main` |

## 7. Trazabilidad

| Issue | Implementación | Commits | PR | Pruebas |
|---|---|---|---|---|
| (sin issue) | `db/schema.sql`, `docker-compose.yml`, `adapter/out/jdbc/*` | `8a7aa6f`, `a55be97`, `4e2e6db`, `095e792` | #29 | 13/13 PASS |
| #35 | `docker-compose.yml` (semilla), `db/02-data.sql` (idempotente), `scripts/verify-module.sh`, `docs/C02-Verificacion.md`, limpieza de `*.class` y `.javamicro` | ver PR | #36 | 77/77 PASS |

## 8. C02 — Verificación reproducible (issue #35)

En C02 tomé la parte de reproducibilidad, que es la que sostiene el saber heurístico de R03.

Encontré que `db/02-data.sql` nunca se ejecutaba —`docker-compose.yml` solo montaba
`schema.sql`—, así que al reconstruir la base desde cero el catálogo quedaba vacío y el
recorrido guardar-consultar no se podía demostrar. Además, las pruebas de integración hacen
`TRUNCATE` de `producto`, de modo que la semilla también desaparecía después de `mvn verify`
y no se podía reaplicar porque usaba `INSERT` sin `ON CONFLICT`.

Lo resuelto: monté la semilla como `02-data.sql`, la hice idempotente y escribí
`scripts/verify-module.sh M11`, que reconstruye el esquema, corre las 77 pruebas, reaplica la
semilla en el orden correcto, confirma una venta, comprueba que un error controlado revierte la
transacción completa y demuestra que los datos sobreviven a `docker compose restart`. También
eliminé `Producto.class` y `Partida.javamicro` del árbol de fuentes.

Decisión que vale la pena explicar: el script **falla a propósito** si la base acepta una
partida con un `producto_codigo` inexistente. Preferí una comprobación que pueda fallar a una
que siempre pase; una verificación que no puede fallar no es evidencia.

Estado: VERIFICADO de punta a punta en mi entorno (log en `.verify/verify-M11.log`).
PENDIENTE y fuera de mi alcance: el adaptador Hibernate (#32, #34) y el tag del corte.
