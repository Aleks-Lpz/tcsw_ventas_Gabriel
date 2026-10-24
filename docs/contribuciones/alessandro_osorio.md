# Contribución individual P08 — Alessandro Osorio

> Bitácora de P08. Mis contribuciones de P04, P05 y P06 están en `docs/alessandro_osorio.md`.

## 1. Datos

- **Integrante:** Alessandro Osorio
- **Usuario de GitHub:** Alessito54
- **Actividad:** P08. Persistencia (M10)
- **Issue:** #ISSUE — Persistir ventas con JDBC y transacciones en PostgreSQL
- **Rama:** `feat/p08-jdbc-venta-transaccional`
- **Pull request:** #PR

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
| `75e1d0a` | `feat(db): agregar PostgreSQL 16 en Docker Compose y schema.sql` |
| `81cb0f4` | `feat(jdbc): implementar JdbcVentaRepository transaccional con PreparedStatement` |
| `b59af62` | `test(jdbc): agregar pruebas unitarias y de integracion del adaptador de ventas` |
| (este commit) | `docs: documentar persistencia de ventas P08 y bitacora individual` |

## 5. Pruebas

Ejecuté todas las pruebas del proyecto contra PostgreSQL 16.15: **73 de 73 en verde**, incluidas mis 13 pruebas nuevas, sin romper las existentes.

## 6. Estado

| Elemento | Estado |
|---|---|
| Esquema, adaptador y pruebas | VERIFICADO |
| `mvn clean verify` y `docker compose up` | NO_VERIFICADO en mi entorno (sin Maven Central ni Docker); las mismas pruebas pasaron con JUnit Console y PostgreSQL 16 nativo |
| Reporte Sonar | PENDIENTE al integrar en `main` |

## 7. Trazabilidad

| Issue | Implementación | Commits | PR | Pruebas |
|---|---|---|---|---|
| #ISSUE | `db/schema.sql`, `docker-compose.yml`, `adapter/out/jdbc/*` | `75e1d0a`, `81cb0f4`, `b59af62` | #PR | 13/13 PASS |
